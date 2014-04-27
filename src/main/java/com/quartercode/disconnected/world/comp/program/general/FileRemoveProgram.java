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
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.CommonLocation;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.event.Event;

/**
 * The file remove program is used to remove a {@link #FILE} from its file system.
 * 
 * @see ProgramExecutor
 * @see File
 */
@CommonLocation (dir = CommonFiles.SYS_BIN_DIR, file = "filedelete.exe")
public class FileRemoveProgram extends ProgramExecutor {

    // ----- Properties -----

    /**
     * The {@link File} object that should be removed.
     */
    public static final PropertyDefinition<File<?>> FILE;

    static {

        FILE = ReferenceProperty.createDefinition("file");

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("removeFile", FileRemoveProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileRemoveProgram holder = (FileRemoveProgram) invocation.getHolder();

                File<?> removeFile = holder.get(FILE).get();
                FileRemoveAction removeAction = removeFile.get(File.CREATE_REMOVE).invoke();

                Process<?> process = holder.getParent();
                User user = process.get(Process.GET_USER).invoke();
                if (removeAction.get(FileAddAction.IS_EXECUTABLE_BY).invoke(user)) {
                    removeAction.get(FileRemoveAction.EXECUTE).invoke();
                    new SuccessEvent().get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                } else {
                    new MissingRightsEvent().get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                }

                process.get(Process.STOP).invoke(true);
                return invocation.next(arguments);
            }
        });

    }

    /**
     * Creates a new file remove program executor.
     */
    public FileRemoveProgram() {

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
    public static class SuccessEvent extends Event {

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
     * @see FileRemoveProgram
     */
    public static class MissingRightsEvent extends Event {

    }

}
