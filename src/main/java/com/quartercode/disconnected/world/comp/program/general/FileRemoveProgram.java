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

import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.file.FileUtils;
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
 * The file remove program is used to remove a file from its file system.
 * 
 * @see ProgramExecutor
 * @see File
 */
@CommonLocation (dir = CommonFiles.SYS_BIN_DIR, file = "filedelete.exe")
public class FileRemoveProgram extends ProgramExecutor {

    // ----- Properties -----

    /**
     * The path of the {@link File} which should be removed.
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

        RUN.addExecutor("removeFile", FileRemoveProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileRemoveProgram holder = (FileRemoveProgram) invocation.getHolder();

                Validate.notNull(holder.get(PATH).get(), "PATH cannot be null");

                Process<?> process = holder.getParent();
                Bridge bridge = holder.getWorld().getBridge();
                OperatingSystem os = process.get(Process.GET_OPERATING_SYSTEM).invoke();
                String computerId = os.getParent().getId();
                int pid = process.get(Process.PID).get();

                String path = holder.get(PATH).get();
                FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();
                File<?> removeFile = null;
                try {
                    removeFile = fsModule.get(FileSystemModule.GET_FILE).invoke(path);
                } catch (UnknownMountpointException e) {
                    bridge.send(new UnknownMountpointEvent(computerId, pid, e.getMountpoint()));
                    stop(process);
                    return invocation.next(arguments);
                }

                if (removeFile == null) {
                    bridge.send(new InvalidPathEvent(computerId, pid, path));
                } else {
                    FileRemoveAction removeAction = removeFile.get(File.CREATE_REMOVE).invoke();

                    User sessionUser = process.get(Process.GET_USER).invoke();
                    if (removeAction.get(FileAddAction.IS_EXECUTABLE_BY).invoke(sessionUser)) {
                        removeAction.get(FileRemoveAction.EXECUTE).invoke();
                        bridge.send(new SuccessEvent(computerId, pid));
                    } else {
                        bridge.send(new MissingRightsEvent(computerId, pid));
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
     * Creates a new file remove program executor.
     */
    public FileRemoveProgram() {

    }

    /**
     * File remove program events are events that are fired by the {@link FileRemoveProgram}.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileRemoveProgram.</b>FileRemoveProgramEvent )
     * </pre>
     * 
     * @see FileRemoveProgram
     */
    public static class FileRemoveProgramEvent extends ProgramEvent {

        private static final long serialVersionUID = 4117274991893251301L;

        /**
         * Creates a new file remove program event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public FileRemoveProgramEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The success event is fired by the {@link FileRemoveProgram} when the file removal process was successful.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileRemoveProgram.</b>SuccessEvent )
     * </pre>
     * 
     * @see FileRemoveProgram
     */
    public static class SuccessEvent extends FileRemoveProgramEvent {

        private static final long serialVersionUID = 5899197829471023231L;

        /**
         * Creates a new file remove program success event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public SuccessEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The error event is fired by the {@link FileRemoveProgram} when something doesn't go to plan.
     * It is the superclass of all error events that can be fired by the file remove program.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileRemoveProgram.</b>ErrorEvent )
     * </pre>
     * 
     * @see FileRemoveProgram
     */
    public static class ErrorEvent extends FileRemoveProgramEvent {

        private static final long serialVersionUID = 1508598335152399248L;

        /**
         * Creates a new file remove program error event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public ErrorEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The unknown mountpoint event is fired by the {@link FileRemoveProgram} when the provided file path describes the mountpoint of an unknown or unmounted file system.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileRemoveProgram.</b>UnknownMountpointEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileRemoveProgram
     */
    public static class UnknownMountpointEvent extends ErrorEvent {

        private static final long serialVersionUID = -5271654433309884884L;

        private final String      mountpoint;

        /**
         * Creates a new file remove program unknown mountpoint event.
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
         * This mountpoint was derived from the provided {@link FileRemoveProgram#PATH} string.
         * 
         * @return The unknown mountpoint.
         */
        public String getMountpoint() {

            return mountpoint;
        }

    }

    /**
     * The invalid path event is fired by the {@link FileRemoveProgram} when the provided file path does not point to a valid file.
     * The reason for the path's invalidity could be a file along the path which is not a directory.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileRemoveProgram.</b>InvalidPathEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileRemoveProgram
     */
    public static class InvalidPathEvent extends ErrorEvent {

        private static final long serialVersionUID = 345535509295725575L;

        private final String      path;

        /**
         * Creates a new file remove program invalid path event.
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
     * The missing rights event is fired by the {@link FileRemoveProgram} when the session that runs the program has not enough rights for the file removal.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>FileRemoveProgram.</b>MissingRightsEvent )
     * </pre>
     * 
     * @see ErrorEvent
     * @see FileRemoveProgram
     */
    public static class MissingRightsEvent extends ErrorEvent {

        private static final long serialVersionUID = -2568133126052294005L;

        /**
         * Creates a new file remove program missing rights event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public MissingRightsEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

}
