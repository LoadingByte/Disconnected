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

package com.quartercode.disconnected.mocl.extra.def;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeature;
import com.quartercode.disconnected.mocl.extra.Function;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;

/**
 * An abstract function makes a method (also called a function) avaiable.
 * Functions are executed by different {@link FunctionExecutor}s. That makes the function concept expandable.
 * The function object itself stores a set of those {@link FunctionExecutor}s.
 * 
 * @param <R> The type of the return value of the defined abstract function.
 * @see FunctionExecutor
 */
public class AbstractFunction<R> extends AbstractFeature implements Function<R> {

    private Set<FunctionExecutor<R>> executors = new HashSet<FunctionExecutor<R>>();

    /**
     * Creates a new abstract function with the given name, parent {@link FeatureHolder} and {@link FunctionExecutor}s.
     * 
     * @param name The name of the abstract function.
     * @param holder The {@link FeatureHolder} which has and uses the new abstract function.
     * @param executors The {@link FunctionExecutor}s which will be executing the function calls.
     */
    public AbstractFunction(String name, FeatureHolder holder, Set<FunctionExecutor<R>> executors) {

        super(name, holder);

        this.executors = executors;
    }

    @Override
    public Set<FunctionExecutor<R>> getExecutors() {

        return Collections.unmodifiableSet(executors);
    }

    @Override
    public R invoke(Object... arguments) throws FunctionExecutionException {

        Map<Integer, Set<FunctionExecutor<R>>> sortedExecutors = new TreeMap<Integer, Set<FunctionExecutor<R>>>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {

                return o2 - o1;
            }

        });

        // Sort the executors by priority
        for (FunctionExecutor<R> executor : executors) {
            int priority = 0;
            if (executor.getClass().isAnnotationPresent(Prioritized.class)) {
                priority = executor.getClass().getAnnotation(Prioritized.class).value();
            }
            if (!sortedExecutors.containsKey(priority)) {
                sortedExecutors.put(priority, new HashSet<FunctionExecutor<R>>());
            }
            sortedExecutors.get(priority).add(executor);
        }

        // Invoke the executors
        R returnValue = null;
        boolean executedFirst = false;
        invokeExecutors:
        for (Set<FunctionExecutor<R>> priorityGroup : sortedExecutors.values()) {
            for (FunctionExecutor<R> executor : priorityGroup) {
                try {
                    if (!executedFirst) {
                        returnValue = executor.invoke(getHolder(), arguments);
                        executedFirst = true;
                    } else {
                        executor.invoke(getHolder(), arguments);
                    }
                }
                catch (StopExecutionException e) {
                    if (e.getCause() == null) {
                        break invokeExecutors;
                    } else {
                        if (e.getMessage() == null) {
                            throw new FunctionExecutionException(e.getCause());
                        } else {
                            throw new FunctionExecutionException(e.getMessage(), e.getCause());
                        }
                    }
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
