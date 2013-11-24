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

package com.quartercode.disconnected.mocl.extra;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link FunctionExecutor}s which have this annotation define if other executors should be executed.
 * The algorithm checks the {@link FunctionExecutor} with the highest priority first and then goes down.
 * If any {@link FunctionExecutor} in the line denies the execution of other {@link FunctionExecutor}s, the algorithm stops.
 * 
 * @see FunctionExecutor
 * @see Prioritized
 */
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface Execution {

    /**
     * The actual priority the {@link FunctionExecutor} has.
     */
    ExecutionPolicy value ();

    /**
     * Defines the different ways the execution algorithm can take when checking a {@link FunctionExecutor}.
     * 
     * @see Execution
     */
    public static enum ExecutionPolicy {

        /**
         * Only execute the checked {@link FunctionExecutor} and then stop.
         */
        THIS,
        /**
         * Also execute the other {@link FunctionExecutor}s (if they want to do so).
         */
        OTHERS;

    }

}
