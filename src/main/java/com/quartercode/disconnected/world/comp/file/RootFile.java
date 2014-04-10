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

package com.quartercode.disconnected.world.comp.file;

import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.disconnected.util.NullPreventer;

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
            @Prioritized (Prioritized.LEVEL_6)
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) throws ExecutorInvocationException {

                return "root";
            }

        });
        NAME.addSetterExecutor("cancel", RootFile.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                return null;
            }

        });

    }

    // ----- Properties End -----

    // ----- Functions -----

    static {

        GET_PATH.addExecutor("returnStatic", RootFile.class, new FunctionExecutor<String>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) throws ExecutorInvocationException {

                return "";
            }

        });
        SET_PATH.addExecutor("cancel", RootFile.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                return null;
            }

        });

        REMOVE.addExecutor("cancel", RootFile.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                return null;
            }

        });

        // The root file has no name size
        GET_SIZE.addExecutor("name", RootFile.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) throws ExecutorInvocationException {

                return 0L + NullPreventer.prevent(invocation.next(arguments));
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new root file.
     */
    public RootFile() {

        setParentType(FileSystem.class);
    }

}
