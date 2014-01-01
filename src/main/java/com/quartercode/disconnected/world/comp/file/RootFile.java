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

import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;

/**
 * This class represents the root file of a {@link FileSystem}.
 * Every {@link File} branches of a root file somehow.
 * 
 * @see FileSystem
 * @see File
 */
public class RootFile extends ParentFile<FileSystem> {

    // ----- Functions -----

    static {

        GET_NAME.addExecutor(RootFile.class, "overwrite", new FunctionExecutor<String>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public String invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return "root";
            }

        });
        SET_NAME.addExecutor(RootFile.class, "overwrite", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                throw new StopExecutionException("Overwrite: Do nothing");
            }

        });

        GET_PATH.addExecutor(RootFile.class, "overwrite", new FunctionExecutor<String>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public String invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return "";
            }

        });
        SET_PATH.addExecutor(RootFile.class, "overwrite", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                throw new StopExecutionException("Overwrite: Do nothing");
            }

        });

        REMOVE.addExecutor(RootFile.class, "overwrite", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                throw new StopExecutionException("Overwrite: Do nothing");
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new root file.
     */
    public RootFile() {

    }

}
