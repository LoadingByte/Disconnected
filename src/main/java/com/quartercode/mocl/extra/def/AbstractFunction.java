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

package com.quartercode.mocl.extra.def;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.quartercode.mocl.base.FeatureHolder;
import com.quartercode.mocl.base.def.AbstractFeature;
import com.quartercode.mocl.extra.Execution;
import com.quartercode.mocl.extra.Function;
import com.quartercode.mocl.extra.FunctionExecutor;
import com.quartercode.mocl.extra.Prioritized;
import com.quartercode.mocl.extra.Execution.ExecutionPolicy;

/**
 * An abstract function makes a method (also called a function) avaiable.
 * Functions are executed by different {@link FunctionExecutor}s. That makes the function concept expandable.
 * The function object itself stores a list of those {@link FunctionExecutor}s.
 * 
 * @param <R> The type of the return value of the defined abstract function.
 * @see FunctionExecutor
 */
public class AbstractFunction<R> extends AbstractFeature implements Function<R> {

    private List<FunctionExecutor<R>> executors = new ArrayList<FunctionExecutor<R>>();

    /**
     * Creates a new empty abstract function.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected AbstractFunction() {

    }

    /**
     * Creates a new abstract function with the given name, parent {@link FeatureHolder} and {@link FunctionExecutor}s.
     * 
     * @param name The name of the abstract function.
     * @param holder The {@link FeatureHolder} which has and uses the new abstract function.
     * @param executors The {@link FunctionExecutor}s which will be executing the function calls.
     */
    public AbstractFunction(String name, FeatureHolder holder, List<FunctionExecutor<R>> executors) {

        super(name, holder);

        this.executors = executors;
    }

    @Override
    public List<FunctionExecutor<R>> getExecutors() {

        return Collections.unmodifiableList(executors);
    }

    @Override
    public R invoke(Object... arguments) {

        Map<Integer, List<FunctionExecutor<R>>> sortedExecutors = new TreeMap<Integer, List<FunctionExecutor<R>>>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {

                return o1 - o2;
            }

        });

        // Sort the executors by priority
        for (FunctionExecutor<R> executor : executors) {
            int priority = 0;
            if (executor.getClass().isAnnotationPresent(Prioritized.class)) {
                priority = executor.getClass().getAnnotation(Prioritized.class).value();
            }
            if (!sortedExecutors.containsKey(priority)) {
                sortedExecutors.put(priority, new ArrayList<FunctionExecutor<R>>());
            }
            sortedExecutors.get(priority).add(executor);
        }

        // Invoke the executors and store
        R returnValue = null;
        boolean executedFirst = false;
        for (List<FunctionExecutor<R>> priorityGroup : sortedExecutors.values()) {
            for (FunctionExecutor<R> executor : priorityGroup) {
                if (!executedFirst) {
                    returnValue = executor.invoke(getHolder(), arguments);
                    executedFirst = true;
                } else {
                    executor.invoke(getHolder(), arguments);
                }

                // Stop the execution of the other executors if the current one wants that
                if (executor.getClass().isAnnotationPresent(Execution.class) && executor.getClass().getAnnotation(Execution.class).value() != ExecutionPolicy.OTHERS) {
                    break;
                }
            }
        }

        return returnValue;
    }

    @Override
    public String toInfoString() {

        return super.toInfoString() + ", " + executors.size() + " executors";
    }

}
