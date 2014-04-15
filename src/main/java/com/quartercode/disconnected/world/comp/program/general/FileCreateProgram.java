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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAction;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.event.Event;

public class FileCreateProgram extends ProgramExecutor {

    private static final Logger                                      LOGGER = LoggerFactory.getLogger(FileCreateProgram.class);

    // ----- Properties -----

    public static final PropertyDefinition<String>                   PATH;

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
                    LOGGER.error("Cannot create instance of file type '{}'", fileType.getName(), e);
                    // Stop execution of initialization function
                    return null;
                }

                User sessionUser = holder.getParent().get(Process.GET_USER).invoke();
                addFile.get(File.OWNER).set(sessionUser);
                addFile.get(File.GROUP).set(sessionUser.get(User.GET_PRIMARY_GROUP).invoke());

                Process<?> process = holder.getParent();
                FileSystemModule fsModule = process.get(Process.GET_OPERATING_SYSTEM).invoke().get(OperatingSystem.FS_MODULE).get();
                String path = holder.get(PATH).get();
                FileAction addAction = fsModule.get(FileSystemModule.CREATE_ADD_FILE).invoke(addFile, path);

                User user = process.get(Process.GET_USER).invoke();
                if (addAction.get(FileAction.IS_EXECUTABLE_BY).invoke(user)) {
                    addAction.get(FileAction.EXECUTE).invoke();
                    SuccessEvent successEvent = new SuccessEvent();
                    successEvent.get(SuccessEvent.FILE).set(addFile);
                    successEvent.get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                } else {
                    new MissingRightEvent().get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
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

    public static class SuccessEvent extends Event {

        // ----- Properties -----

        public static final PropertyDefinition<File<?>> FILE;

        static {

            FILE = ReferenceProperty.createDefinition("file");

        }

    }

    public static class MissingRightEvent extends Event {

    }

}
