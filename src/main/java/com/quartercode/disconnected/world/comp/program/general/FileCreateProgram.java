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

import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAction;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.event.Event;

/**
 * The file create program is used to create a new {@link File} under a given global {@link #PATH}.
 * The program takes that global path ({@code /&lt;mountpoint&gt;/&lt;local path&gt;}) and the class object of the {@link #FILE_TYPE}.
 * 
 * @see ProgramExecutor
 * @see File
 */
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

        PATH = ObjectProperty.createDefinition("path");
        FILE_TYPE = ObjectProperty.<Class<? extends File<?>>> createDefinition("fileType", ContentFile.class, true);

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("createFile", FileCreateProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileCreateProgram holder = (FileCreateProgram) invocation.getHolder();
                Class<? extends File<?>> fileType = holder.get(FILE_TYPE).get();

                File<?> addFile = null;
                try {
                    addFile = fileType.newInstance();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot create instance of file type '" + fileType.getName() + "'");
                }

                User sessionUser = holder.getParent().get(Process.GET_USER).invoke();
                addFile.get(File.OWNER).set(sessionUser);
                addFile.get(File.GROUP).set(sessionUser.get(User.GET_PRIMARY_GROUP).invoke());

                Process<?> process = holder.getParent();
                FileSystemModule fsModule = process.get(Process.GET_OPERATING_SYSTEM).invoke().get(OperatingSystem.FS_MODULE).get();
                String path = holder.get(PATH).get();
                // Add a file seperator at the beginning of the path if it is not already there
                path = path.startsWith(File.SEPARATOR) ? path : File.SEPARATOR + path;
                FileAction addAction = null;
                try {
                    addAction = fsModule.get(FileSystemModule.CREATE_ADD_FILE).invoke(addFile, path);
                } catch (IllegalStateException e) {
                    UnknownMountpointEvent unknownMountpointEvent = new UnknownMountpointEvent();
                    unknownMountpointEvent.get(UnknownMountpointEvent.MOUNTPOINT).set(FileUtils.getComponents(path)[0]);
                    unknownMountpointEvent.get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                }

                if (addAction != null) {
                    User user = process.get(Process.GET_USER).invoke();
                    if (addAction.get(FileAction.IS_EXECUTABLE_BY).invoke(user)) {
                        try {
                            addAction.get(FileAddAction.EXECUTE).invoke();
                            SuccessEvent successEvent = new SuccessEvent();
                            successEvent.get(SuccessEvent.FILE).set(addFile);
                            successEvent.get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                        } catch (IllegalArgumentException e) {
                            InvalidPathEvent invalidPathEvent = new InvalidPathEvent();
                            invalidPathEvent.get(InvalidPathEvent.PATH).set(path);
                            invalidPathEvent.get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                        } catch (OutOfSpaceException e) {
                            OutOfSpaceEvent outOfSpaceEvent = new OutOfSpaceEvent();
                            outOfSpaceEvent.get(OutOfSpaceEvent.FILE_SYSTEM).set(e.getFileSystem());
                            outOfSpaceEvent.get(OutOfSpaceEvent.REQUIRED_SPACE).set(e.getSize());
                            outOfSpaceEvent.get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                        }
                    } else {
                        new MissingRightsEvent().get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                    }
                }

                process.get(Process.STOP).invoke(true);
                return invocation.next(arguments);
            }
        });

    }

    /**
     * Creates a new file create program executor.
     */
    public FileCreateProgram() {

    }

    /**
     * The success event is fired by the {@link FileCreateProgram} when the file creation process was successful.
     * It also carries the {@link #FILE} that was created by the file create program.<br>
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
    public static class SuccessEvent extends Event {

        // ----- Properties -----

        /**
         * The {@link File} object that was created by the {@link FileCreateProgram} which fired the event.
         */
        public static final PropertyDefinition<File<?>> FILE;

        static {

            FILE = ReferenceProperty.createDefinition("file");

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
    public static class ErrorEvent extends Event {

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

        // ----- Properties -----

        /**
         * The mountpoint that describes a file system which is not known or not mounted.
         * This mountpoint was derived from the provided {@link FileCreateProgram#PATH} string.
         */
        public static final PropertyDefinition<String> MOUNTPOINT;

        static {

            MOUNTPOINT = ObjectProperty.createDefinition("mountpoint");

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

    }

    /**
     * The invalid path event is fired by the {@link FileCreateProgram} when the provided file path is not valid for adding a file under it.
     * The reason for its invalidity could be a file along the path is not a parent file.<br>
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

        // ----- Properties -----

        /**
         * The global file path that is not valid.
         * The reason for its invalidity could be a file along the path is not a parent file.
         */
        public static final PropertyDefinition<String> PATH;

        static {

            PATH = ObjectProperty.createDefinition("path");

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

        // ----- Properties -----

        /**
         * The {@link FileSystem} where the file should have been added to.
         */
        public static final PropertyDefinition<FileSystem> FILE_SYSTEM;

        /**
         * The amount of bytes that should have been added to the {@link #FILE_SYSTEM}.
         */
        public static final PropertyDefinition<Long>       REQUIRED_SPACE;

        static {

            FILE_SYSTEM = ReferenceProperty.createDefinition("fileSystem");
            REQUIRED_SPACE = ObjectProperty.createDefinition("requiredSpace");

        }

    }

}
