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

package com.quartercode.disconnected.world.comp.program;

import com.quartercode.disconnected.world.comp.file.FileUtils;

/**
 * The executor utils class provides some utility methods for {@link ProgramExecutor}s.
 */
public class ExecutorUtils {

    /**
     * Returns the complete file path the given {@link ProgramExecutor} can be commonly found under.
     * Returns {@code null} if the provided class doesn't have the {@link CommonLocation} annotation.<br>
     * This method is useful for quickly resolving a program file in a oneliner.
     * 
     * @param executor The program executor type whose common location should be resolved.
     * @return The common file path of the given program executor.
     */
    public static String getCommonLocation(Class<? extends ProgramExecutor> executor) {

        if (!executor.isAnnotationPresent(CommonLocation.class)) {
            return null;
        } else {
            CommonLocation location = executor.getAnnotation(CommonLocation.class);
            return FileUtils.resolvePath(location.dir(), location.file());
        }
    }

    private ExecutorUtils() {

    }

}
