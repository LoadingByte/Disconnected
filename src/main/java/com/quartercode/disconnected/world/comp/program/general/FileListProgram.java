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

import java.util.ArrayList;
import java.util.List;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ReferenceCollectionProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.file.ParentFile;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.CommonLocation;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.event.Event;

/**
 * The file list program is used to list all {@link File} that are children of a given {@link ParentFile} ({@link #DIR}).
 * 
 * @see ProgramExecutor
 * @see ParentFile
 * @see File
 */
@CommonLocation (dir = CommonFiles.SYS_BIN_DIR, file = "filelist.exe")
public class FileListProgram extends ProgramExecutor {

    // ----- Properties -----

    /**
     * The {@link ParentFile} whose child {@link File}s should be listed.
     */
    public static final PropertyDefinition<ParentFile<?>> DIR;

    static {

        DIR = ReferenceProperty.createDefinition("dir");

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("listFiles", FileListProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileListProgram holder = (FileListProgram) invocation.getHolder();
                ParentFile<?> dir = holder.get(DIR).get();

                Process<?> process = holder.getParent();
                User sessionUser = process.get(Process.GET_USER).invoke();

                if (!FileUtils.hasRight(sessionUser, dir, FileRight.READ)) {
                    new MissingRightsEvent().get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                } else {
                    List<File<ParentFile<?>>> files = dir.get(ParentFile.CHILDREN).get();

                    SuccessEvent successEvent = new SuccessEvent();
                    for (File<ParentFile<?>> file : files) {
                        successEvent.get(SuccessEvent.FILES).add(file);
                    }
                    successEvent.get(Event.SEND).invoke(holder.get(OUT_EVENT_LISTENERS).get());
                }

                process.get(Process.STOP).invoke(true);
                return invocation.next(arguments);
            }
        });

    }

    /**
     * Creates a new file list program executor.
     */
    public FileListProgram() {

    }

    /**
     * The success event is fired by the {@link FileListProgram} when the file list process was successful.
     * It also carries the {@link #FILES} property which contains the listed {@link File}s.<br>
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
    public static class SuccessEvent extends Event {

        // ----- Properties -----

        /**
         * The requested {@link File}s which are children of the input {@link ParentFile}.
         */
        public static final CollectionPropertyDefinition<File<ParentFile<?>>, List<File<ParentFile<?>>>> FILES;

        static {

            FILES = ReferenceCollectionProperty.createDefinition("file", new ArrayList<File<ParentFile<?>>>());

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
     * @see FileListProgram
     */
    public static class MissingRightsEvent extends Event {

    }

}
