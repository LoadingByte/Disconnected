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
 * {@link FunctionExecutor}s which have this annotation are only invoked after a given numbers of global executions.
 * That means that a {@link FunctionExecutor} with a delay of one is only invoked every second time {@link Function#invoke(Object...)} is called.
 * You can also define an amount of executions the {@link FunctionExecutor} waits until it invokes for the first time.
 * This should be annotated at the actual {@link FunctionExecutor#invoke(FeatureHolder, Object...)} method.
 * 
 * @see FunctionExecutor
 */
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.RUNTIME)
public @interface Delay {

    /**
     * The amount of executions the {@link FunctionExecutor} waits until it invokes for the first time (default is set to 0).
     * A {@link FunctionExecutor} with a first delay of two starts invoking on the third time {@link Function#invoke(Object...)} is called.
     */
    int firstDelay () default 0;

    /**
     * The amount of executions the {@link FunctionExecutor} waits until it invokes again (default is set to 0).
     * A {@link FunctionExecutor} with a delay of one is only invoked every second time {@link Function#invoke(Object...)} is called.
     */
    int delay () default 0;

}
