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

package com.quartercode.disconnected.server.world.comp.program.general;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileUtils;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.comp.os.CommonFiles;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.server.world.comp.program.CommonLocation;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProgramEvent;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.shared.world.event.FilePlaceholder;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * The file list program is used to list all files which are children of a given directory ({@link #PATH}).
 * 
 * @see ProgramExecutor
 */
@CommonLocation (dir = CommonFiles.SYS_BIN_DIR, file = "filelist.exe")
public class FileListProgram extends ProgramExecutor {

    // ----- Properties -----

    /**
     * The path of the directory whose child files should be listed.
     */
    public static final PropertyDefinition<String> PATH;

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

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("listFiles", FileListProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileListProgram holder = (FileListProgram) invocation.getHolder();

                Validate.notNull(holder.get(PATH).get(), "PATH cannot be null");

                Process<?> process = holder.getParent();
                Bridge bridge = holder.getWorld().getBridge();
                OperatingSystem os = process.get(Process.GET_OPERATING_SYSTEM).invoke();
                String computerId = os.getParent().getId();
                int pid = process.get(Process.PID).get();

                String path = holder.get(PATH).get();
                FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();

                if (path.equals(File.SEPARATOR)) {
                    List<FilePlaceholder> files = new ArrayList<>();

                    for (KnownFileSystem fileSystem : fsModule.get(FileSystemModule.GET_MOUNTED).invoke()) {
                        files.add(new FilePlaceholder(fileSystem));
                    }

                    bridge.send(new SuccessEvent(computerId, pid, files));
                } else {
                    File<?> rawDir = null;
                    try {
                        rawDir = fsModule.get(FileSystemModule.GET_FILE).invoke(path);
                    } catch (UnknownMountpointException e) {
                        bridge.send(new UnknownMountpointEvent(computerId, pid, e.getMountpoint()));
                        stop(process);
                        return invocation.next(arguments);
                    }

                    if (! (rawDir instanceof ParentFile)) {
                        bridge.send(new InvalidPathEvent(computerId, pid, path));
                    } else {
                        ParentFile<?> dir = (ParentFile<?>) rawDir;

                        User sessionUser = process.get(Process.GET_USER).invoke();
                        if (!FileUtils.hasRight(sessionUser, dir, FileRight.READ)) {
                            bridge.send(new MissingRightsEvent(computerId, pid));
                        } else {
                            List<FilePlaceholder> files = new ArrayList<>();

                            String pathMountpoint = FileUtils.getComponents(path)[0];
                            for (File<?> file : dir.get(ParentFile.CHILDREN).get()) {
                                files.add(new FilePlaceholder(pathMountpoint, file));
                            }

                            bridge.send(new SuccessEvent(computerId, pid, files));
                        }
                    }
                }

                stop(process);
                return invocation.next(arguments);
            }

            private void stop(Process<?> process) {

                process.get(Process.STOP).invoke(true);
            }

        });

    }

    /**
     * File list program events are events that are fired by the {@link FileListProgram}.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileListProgram.</b>FileListProgramEvent )
     * </pre>
     * 
     * @see FileListProgram
     */
    public static class FileListProgramEvent extends ProgramEvent {

        private static final long serialVersionUID = 2098360282411098945L;

        /**
         * Creates a new file list program event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public FileListProgramEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The success event is fired by the {@link FileListProgram} when the file list process was successful.
     * It also carries some {@link FilePlaceholder}s which represent the listed files.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileListProgram.</b>SuccessEvent )
     * </pre>
     * 
     * @see FileListProgram
     */
    public static class SuccessEvent extends FileListProgramEvent {

        private static final long           serialVersionUID = -4145116285039354316L;

        private final List<FilePlaceholder> files;

        /**
         * Creates a new list create program success event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param files The placeholder objects that represent the requested files which are children of the input directory.
         */
        public SuccessEvent(String computerId, int pid, List<FilePlaceholder> files) {

            super(computerId, pid);
            this.files = files;
        }

        /**
         * Returns the placeholder objects that represent the requested files which are children of the input directory.
         * 
         * @return Placeholder objects for the requested files.
         */
        public List<FilePlaceholder> getFiles() {

            return files;
        }

    }

    /**
     * The error event is fired by the {@link FileListProgram} when something doesn't go to plan.
     * It is the superclass of all error events that can be fired by the file list program.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileListProgram.</b>ErrorEvent )
     * </pre>
     * 
     * @see FileListProgram
     */
    public static class ErrorEvent extends FileListProgramEvent {

        private static final long serialVersionUID = 153189680811572592L;

        /**
         * Creates a new file list program error event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public ErrorEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The unknown mountpoint event is fired by the {@link FileListProgram} when the provided file path describes the mountpoint of an unknown or unmounted file system.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileListProgram.</b>UnknownMountpointEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileListProgram
     */
    public static class UnknownMountpointEvent extends ErrorEvent {

        private static final long serialVersionUID = -4668086945570925486L;

        private final String      mountpoint;

        /**
         * Creates a new file list program unknown mountpoint event.
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
         * This mountpoint was derived from the provided {@link FileListProgram#PATH} string.
         * 
         * @return The unknown mountpoint.
         */
        public String getMountpoint() {

            return mountpoint;
        }

    }

    /**
     * The invalid path event is fired by the {@link FileListProgram} when the provided file path is not valid for listing its children.
     * The reason for the path's invalidity could be a file along the path which is not a directory.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileListProgram.</b>InvalidPathEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileListProgram
     */
    public static class InvalidPathEvent extends ErrorEvent {

        private static final long serialVersionUID = 8973847394902989176L;

        private final String      path;

        /**
         * Creates a new file list program invalid path event.
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
     * The missing rights event is fired by the {@link FileListProgram} when the session that runs the program has not enough rights for the file listing.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileListProgram.</b>MissingRightsEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileListProgram
     */
    public static class MissingRightsEvent extends ErrorEvent {

        private static final long serialVersionUID = -5289989493420422302L;

        /**
         * Creates a new file list program missing rights event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public MissingRightsEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

}
