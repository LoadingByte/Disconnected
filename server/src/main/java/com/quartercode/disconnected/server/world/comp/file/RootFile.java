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

package com.quartercode.disconnected.server.world.comp.file;

import static com.quartercode.classmod.extra.func.Priorities.LEVEL_6;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.disconnected.server.util.NullPreventer;

/**
 * This class represents the root file of a {@link FileSystem}.
 * Every {@link File} branches of a root file somehow.
 * 
 * @see FileSystem
 * @see File
 */
public class RootFile extends ParentFile<FileSystem> {

    // ----- Properties -----

    static {

        NAME.addGetterExecutor("returnStatic", RootFile.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                return "root";
            }

        }, LEVEL_6);
        NAME.addSetterExecutor("cancel", RootFile.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                return null;
            }

        }, LEVEL_6);

    }

    // ----- Functions -----

    static {

        GET_PATH.addExecutor("returnStatic", RootFile.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                return "";
            }

        }, LEVEL_6);

        CREATE_MOVE.addExecutor("returnNull", RootFile.class, new FunctionExecutor<FileMoveAction>() {

            @Override
            public FileMoveAction invoke(FunctionInvocation<FileMoveAction> invocation, Object... arguments) {

                return null;
            }

        }, LEVEL_6);

        CREATE_MOVE_TO_OTHER_FS.addExecutor("returnNull", RootFile.class, new FunctionExecutor<FileMoveAction>() {

            @Override
            public FileMoveAction invoke(FunctionInvocation<FileMoveAction> invocation, Object... arguments) {

                return null;
            }

        }, LEVEL_6);

        CREATE_REMOVE.addExecutor("returnNull", RootFile.class, new FunctionExecutor<FileRemoveAction>() {

            @Override
            public FileRemoveAction invoke(FunctionInvocation<FileRemoveAction> invocation, Object... arguments) {

                return null;
            }

        }, LEVEL_6);

        // The root file has no name size
        GET_SIZE.addExecutor("name", RootFile.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                return 0L + NullPreventer.prevent(invocation.next(arguments));
            }

        });

    }

    /**
     * Creates a new root file.
     */
    public RootFile() {

        setParentType(FileSystem.class);
    }

}
