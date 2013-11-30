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
import com.quartercode.disconnected.mocl.base.FeatureHolder;

/**
 * {@link FunctionExecutor}s which have this annotation define their priority.
 * The priority is used for determinating which {@link FunctionExecutor} sets the rules.
 * This should be annotated at the actual {@link FunctionExecutor#invoke(FeatureHolder, Object...)} method.
 * 
 * @see FunctionExecutor
 */
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.RUNTIME)
public @interface Prioritized {

    /**
     * The default priority for every {@link FunctionExecutor} which doesn't have the annotation.
     */
    public static final int DEFAULT      = 1000000;

    /**
     * The priority to use for checks.
     * Checks are executors which throw a {@link StopExecutionException} if something is incorrect.
     */
    public static final int CHECK        = DEFAULT + 10000000;

    /**
     * The priority to use for the least important overwrites.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_1  = DEFAULT + 20000000;

    /**
     * The priority to use for very low overwrites.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_2  = DEFAULT + 21000000;

    /**
     * The priority to use for low overwrites.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_3  = DEFAULT + 22000000;

    /**
     * The priority to use for somewhat important overwrites.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_4  = DEFAULT + 23000000;

    /**
     * The priority to use for a bit more important overwrites.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_5  = DEFAULT + 24000000;

    /**
     * The priority to use for more important overwrites.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_6  = DEFAULT + 25000000;

    /**
     * The priority to use for important overwrites.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_7  = DEFAULT + 26000000;

    /**
     * The priority to use for actually important overwrites.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_8  = DEFAULT + 27000000;

    /**
     * The priority to use for very important overwrites.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_9  = DEFAULT + 28000000;

    /**
     * The priority to use for really important overwrites.
     * This priority should only be used if the overwrite is critical.
     * Overwrites are executors which replace the functionality of the default executor.
     * They normally throw a {@link StopExecutionException} after invokation.
     */
    public static final int OVERWRITE_10 = DEFAULT + 29000000;

    /**
     * The actual priority the {@link FunctionExecutor} has.
     */
    int value ();

}
