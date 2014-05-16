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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.disconnected.bridge.Bridge;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.file.ParentFile;
import com.quartercode.disconnected.world.comp.file.RootFile;
import com.quartercode.disconnected.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.Group;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.CommonLocation;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.program.general.FileListProgram.SuccessEvent.FilePlaceholder;
import com.quartercode.disconnected.world.event.ProgramEvent;

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

        PATH = ObjectProperty.createDefinition("path");
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
                        String name = fileSystem.get(KnownFileSystem.MOUNTPOINT).get();

                        FileSystem actualFs = fileSystem.get(KnownFileSystem.FILE_SYSTEM).get();
                        long size = actualFs.get(File.GET_SIZE).invoke();

                        RootFile root = actualFs.get(FileSystem.ROOT).get();
                        String rights = root.get(File.RIGHTS).get().get(FileRights.TO_STRING).invoke();

                        User ownerObject = root.get(File.OWNER).get();
                        String owner = ownerObject == null ? null : ownerObject.get(User.NAME).get();

                        Group groupObject = root.get(File.GROUP).get();
                        String group = groupObject == null ? null : groupObject.get(Group.NAME).get();

                        files.add(new FilePlaceholder(name, RootFile.class, size, rights, owner, group));
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
                            for (File<?> file : dir.get(ParentFile.CHILDREN).get()) {
                                String name = file.get(File.NAME).get();

                                // This unchecked cast does always work since Class<? extends File> is just casted to Class<? extends File<?>>
                                @SuppressWarnings ("unchecked")
                                Class<? extends File<?>> type = (Class<? extends File<?>>) file.getClass();

                                long size = file.get(File.GET_SIZE).invoke();
                                String rights = file.get(File.RIGHTS).get().get(FileRights.TO_STRING).invoke();

                                User ownerObject = file.get(File.OWNER).get();
                                String owner = ownerObject == null ? null : ownerObject.get(User.NAME).get();

                                Group groupObject = file.get(File.GROUP).get();
                                String group = groupObject == null ? null : groupObject.get(Group.NAME).get();

                                files.add(new FilePlaceholder(name, type, size, rights, owner, group));
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
     * Creates a new file list program executor.
     */
    public FileListProgram() {

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

        private static final long serialVersionUID = -9112210048232206110L;

        public FileListProgramEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The success event is fired by the {@link FileListProgram} when the file list process was successful.
     * It also carries the {@link #FILES} property which contains the names of all listed files.<br>
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
    @Data
    @EqualsAndHashCode (callSuper = true)
    public static class SuccessEvent extends FileListProgramEvent {

        private static final long           serialVersionUID = 5150968724724858614L;

        /**
         * Placeholder objects that represent requested files which are children of the input directory.
         */
        private final List<FilePlaceholder> files;

        public SuccessEvent(String computerId, int pid, List<FilePlaceholder> files) {

            super(computerId, pid);
            this.files = files;
        }

        /**
         * A file placeholder represents a {@link File} object by storing commonly used data about it.
         * File systems are represented by their {@link RootFile}s.
         */
        @Data
        public static class FilePlaceholder implements Serializable {

            private static final long              serialVersionUID = 2757049420246691150L;

            /**
             * The name of the represented file.
             */
            private final String                   name;

            /**
             * The type (class object) of the represented file.
             */
            private final Class<? extends File<?>> type;

            /**
             * The total size of the represented file.
             * If the placeholder represents a file system, the size of the file system is used.
             */
            private final long                     size;

            /**
             * The {@link FileRights}s string that stores the file rights of the represented file.
             * 
             * @see FileRights#TO_STRING
             */
            private final String                   rights;

            /**
             * The name of the user that owns the represented file.
             */
            private final String                   owner;

            /**
             * The name of the group that is assigned to the represented file.
             */
            private final String                   group;

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

        private static final long serialVersionUID = 347053041373954853L;

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
    @Data
    @EqualsAndHashCode (callSuper = true)
    public static class UnknownMountpointEvent extends ErrorEvent {

        private static final long serialVersionUID = -1037703413535146645L;

        /**
         * The mountpoint that describes a file system which is not known or not mounted.
         * This mountpoint was derived from the provided {@link FileListProgram#PATH} string.
         */
        private final String      mountpoint;

        public UnknownMountpointEvent(String computerId, int pid, String mountpoint) {

            super(computerId, pid);
            this.mountpoint = mountpoint;
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
    @Data
    @EqualsAndHashCode (callSuper = true)
    public static class InvalidPathEvent extends ErrorEvent {

        private static final long serialVersionUID = -3573024132427884551L;

        /**
         * The global file path that is not valid.
         * The reason for its invalidity could be a file along the path is not a directory.
         */
        private final String      path;

        public InvalidPathEvent(String computerId, int pid, String path) {

            super(computerId, pid);
            this.path = path;
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

        private static final long serialVersionUID = 8170183641838774836L;

        public MissingRightsEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

}
