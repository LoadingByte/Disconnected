/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
 *
 * Disconnected is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Disconnected is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
 */

package com.quartercode.disconnected.world.comp.program.general;

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.CommonLocation;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProgramEvent;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * The file create program is used to create a new {@link File} under a given global {@link #PATH}.
 * The program takes that global path ({@code /&lt;mountpoint&gt;/&lt;local path&gt;}) and the class object of the {@link #FILE_TYPE}.
 * 
 * @see ProgramExecutor
 * @see File
 */
@CommonLocation (dir = CommonFiles.SYS_BIN_DIR, file = "filecreate.exe")
public class FileCreateProgram extends ProgramExecutor {

    // ----- Properties -----

    /**
     * The global path under which the new {@link File} should be located.
     * The path must be a global one that uses the format {@code /&lt;mountpoint&gt;/&lt;local path&gt;}.
     */
    public static final PropertyDefinition<String>                   PATH;

    /**
     * The class object of the {@link File} class that must be instantiated in order to create the file to add.
     */
    public static final PropertyDefinition<Class<? extends File<?>>> FILE_TYPE;

    static {

        PATH = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "path", "storage", new StandardStorage<>());
        PATH.addSetterExecutor("normalize", FileAddAction.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                String normalizedPath = FileUtils.normalizePath((String) arguments[0]);
                if (!normalizedPath.isEmpty()) {
                    normalizedPath = normalizedPath.substring(1);
                }
                return invocation.next(normalizedPath);
            }

        });

        FILE_TYPE = create(new TypeLiteral<PropertyDefinition<Class<? extends File<?>>>>() {}, "name", "fileType", "storage", new StandardStorage<>(), "initialValue", new ConstantValueFactory<>(ContentFile.class));

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("createFile", FileCreateProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileCreateProgram holder = (FileCreateProgram) invocation.getHolder();

                Validate.notNull(holder.get(PATH).get(), "PATH cannot be null");
                Validate.notNull(holder.get(FILE_TYPE).get(), "FILE_TYPE cannot be null");

                Process<?> process = holder.getParent();
                Bridge bridge = holder.getWorld().getBridge();
                OperatingSystem os = process.get(Process.GET_OPERATING_SYSTEM).invoke();
                String computerId = os.getParent().getId();
                int pid = process.get(Process.PID).get();

                Class<? extends File<?>> fileType = holder.get(FILE_TYPE).get();
                File<?> addFile = null;
                try {
                    addFile = fileType.newInstance();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot create instance of file type '" + fileType.getName() + "'", e);
                }

                User sessionUser = process.get(Process.GET_USER).invoke();
                addFile.get(File.OWNER).set(sessionUser);
                addFile.get(File.GROUP).set(sessionUser.get(User.GET_PRIMARY_GROUP).invoke());

                FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();
                String path = holder.get(PATH).get();
                // Make sure that there is a file seperator at the beginning of the path
                path = path.startsWith(File.SEPARATOR) ? path : File.SEPARATOR + path;
                FileAddAction addAction = null;
                try {
                    addAction = fsModule.get(FileSystemModule.CREATE_ADD_FILE).invoke(addFile, path);
                } catch (UnknownMountpointException e) {
                    bridge.send(new UnknownMountpointEvent(computerId, pid, e.getMountpoint()));
                }

                if (addAction != null) {
                    if (addAction.get(FileAddAction.IS_EXECUTABLE_BY).invoke(sessionUser)) {
                        try {
                            addAction.get(FileAddAction.EXECUTE).invoke();
                            bridge.send(new SuccessEvent(computerId, pid));
                        } catch (InvalidPathException e) {
                            bridge.send(new InvalidPathEvent(computerId, pid, path));
                        } catch (OccupiedPathException e) {
                            bridge.send(new OccupiedPathEvent(computerId, pid, path));
                        } catch (OutOfSpaceException e) {
                            bridge.send(new OutOfSpaceEvent(computerId, pid, FileUtils.getComponents(path)[0], e.getSize()));
                        }
                    } else {
                        bridge.send(new MissingRightsEvent(computerId, pid));
                    }
                }

                process.get(Process.STOP).invoke(true);
                return invocation.next(arguments);
            }
        });

    }

    /**
     * File create program events are events that are fired by the {@link FileCreateProgram}.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileCreateProgram.</b>FileCreateProgramEvent )
     * </pre>
     * 
     * @see FileCreateProgram
     */
    public static class FileCreateProgramEvent extends ProgramEvent {

        private static final long serialVersionUID = -2640623294328041021L;

        /**
         * Creates a new file create program event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public FileCreateProgramEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The success event is fired by the {@link FileCreateProgram} when the file creation process was successful.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileCreateProgram.</b>SuccessEvent )
     * </pre>
     * 
     * @see FileCreateProgram
     */
    public static class SuccessEvent extends FileCreateProgramEvent {

        private static final long serialVersionUID = 2276947021146657523L;

        /**
         * Creates a new file create program success event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public SuccessEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The error event is fired by the {@link FileCreateProgram} when something doesn't go to plan.
     * It is the superclass of all error events that can be fired by the file create program.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileCreateProgram.</b>ErrorEvent )
     * </pre>
     * 
     * @see FileCreateProgram
     */
    public static class ErrorEvent extends FileCreateProgramEvent {

        private static final long serialVersionUID = -5715015104171607677L;

        /**
         * Creates a new file create program error event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public ErrorEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The unknown mountpoint event is fired by the {@link FileCreateProgram} when the provided file path describes the mountpoint of an unknown or unmounted file system.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileCreateProgram.</b>UnknownMountpointEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileCreateProgram
     */
    public static class UnknownMountpointEvent extends ErrorEvent {

        private static final long serialVersionUID = -1663068599050779385L;

        private final String      mountpoint;

        /**
         * Creates a new file create program unknown mountpoint event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param mountpoint The mountpoint which describes a file system that is not known or not mounted.
         */
        public UnknownMountpointEvent(String computerId, int pid, String mountpoint) {

            super(computerId, pid);

            this.mountpoint = mountpoint;
        }

        /**
         * Returns the mountpoint which describes a file system that is not known or not mounted.
         * This mountpoint was derived from the provided {@link FileCreateProgram#PATH} string.
         * 
         * @return The unknown mountpoint.
         */
        public String getMountpoint() {

            return mountpoint;
        }

    }

    /**
     * The invalid path event is fired by the {@link FileCreateProgram} when the provided file path is not valid for adding a file under it.
     * The reason for its invalidity could be a file along the path which is not a parent file.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileCreateProgram.</b>InvalidPathEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileCreateProgram
     */
    public static class InvalidPathEvent extends ErrorEvent {

        private static final long serialVersionUID = 6347414737051975493L;

        private final String      path;

        /**
         * Creates a new file create program invalid path event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param path The global file path which is not valid.
         */
        public InvalidPathEvent(String computerId, int pid, String path) {

            super(computerId, pid);

            this.path = path;
        }

        /**
         * Returns the global file path which is not valid.
         * The reason for its invalidity could be a file along the path which is not a directory.
         * 
         * @return The invalid path.
         */
        public String getPath() {

            return path;
        }

    }

    /**
     * The occupied path event is fired by the {@link FileCreateProgram} when the provided file path is already occupied by another {@link File}.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileCreateProgram.</b>OccupiedPathEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileCreateProgram
     */
    public static class OccupiedPathEvent extends ErrorEvent {

        private static final long serialVersionUID = 2560542890478154606L;

        private final String      path;

        /**
         * Creates a new file create program occupied path event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param path The global file path which is already occupied by another file.
         */
        public OccupiedPathEvent(String computerId, int pid, String path) {

            super(computerId, pid);

            this.path = path;
        }

        /**
         * Returns the global file path which is already occupied by another file.
         * 
         * @return The occupied path.
         */
        public String getPath() {

            return path;
        }

    }

    /**
     * The out of space event is fired by the {@link FileCreateProgram} when there's not enough space on the {@link FileSystem} to create the new file.
     * The reason for the event must not be the actual new file.
     * If there are directories missing along the way and there's not enough space for those, the event is fired as well.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileCreateProgram.</b>OutOfSpaceEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileCreateProgram
     */
    public static class OutOfSpaceEvent extends ErrorEvent {

        private static final long serialVersionUID = -678289769911469929L;

        private final String      fileSystemMountpoint;
        private final long        requiredSpace;

        /**
         * Creates a new file create program out of space event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param fileSystemMountpoint The mountpoint of the {@link FileSystem} where the new file should have been added to.
         * @param requiredSpace The amount of bytes which should have been added to the file system.
         */
        public OutOfSpaceEvent(String computerId, int pid, String fileSystemMountpoint, long requiredSpace) {

            super(computerId, pid);

            this.fileSystemMountpoint = fileSystemMountpoint;
            this.requiredSpace = requiredSpace;
        }

        /**
         * Returns the mountpoint of the {@link FileSystem} where the new file should have been added to.
         * 
         * @return The mountpoint of the target file system.
         */
        public String getFileSystemMountpoint() {

            return fileSystemMountpoint;
        }

        /**
         * Returns the amount of bytes which should have been added to the file system.
         * 
         * @return The amount of new bytes.
         */
        public long getRequiredSpace() {

            return requiredSpace;
        }

    }

    /**
     * The missing rights event is fired by the {@link FileCreateProgram} when the session that runs the program has not enough rights for the file creation.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileCreateProgram.</b>MissingRightsEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileCreateProgram
     */
    public static class MissingRightsEvent extends ErrorEvent {

        private static final long serialVersionUID = -1722850688027611488L;

        /**
         * Creates a new file create program missing rights event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public MissingRightsEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

}
