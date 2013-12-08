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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeature;
import com.quartercode.disconnected.mocl.extra.Function;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Lockable;
import com.quartercode.disconnected.mocl.extra.LockableClass;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;

/**
 * An abstract function makes a method (also called a function) avaiable.
 * Functions are executed by different {@link FunctionExecutor}s. That makes the function concept flexible.
 * The function object itself stores a set of those {@link FunctionExecutor}s.
 * 
 * @param <R> The type of the return value of the used {@link FunctionExecutor}s. The function returns a {@link List} with these values.
 * @see FunctionExecutor
 * @see Function
 * @see LockableClass
 */
public class AbstractFunction<R> extends AbstractFeature implements Function<R>, LockableClass {

    private static final Logger                                           LOGGER    = Logger.getLogger(AbstractFunction.class.getName());

    private Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>> executors = new HashMap<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>>();
    private boolean                                                       locked;

    /**
     * Creates a new abstract function with the given name, parent {@link FeatureHolder} and {@link FunctionExecutor}s.
     * 
     * @param name The name of the abstract function.
     * @param holder The {@link FeatureHolder} which has and uses the new abstract function.
     * @param executors The {@link FunctionExecutor}s which will be executing the function calls for the different variants.
     */
    public AbstractFunction(String name, FeatureHolder holder, Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>> executors) {

        super(name, holder);

        this.executors = executors;
        setLocked(true);
    }

    @Override
    public boolean isLocked() {

        return locked;
    }

    @Override
    public void setLocked(boolean locked) {

        this.locked = locked;
    }

    @Override
    public Set<FunctionExecutor<R>> getExecutors() {

        Set<FunctionExecutor<R>> actualExecutors = new HashSet<FunctionExecutor<R>>();

        for (Entry<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>> variant : executors.entrySet()) {
            if (variant.getKey().isAssignableFrom(getHolder().getClass())) {
                actualExecutors.addAll(variant.getValue());
            }
        }

        return actualExecutors;
    }

    /**
     * Collects the {@link FunctionExecutor}s which can be invoked through {@link #invoke(Object...)} or {@link #invokeRA(Object...)}.
     * This can be overriden to modify which {@link FunctionExecutor}s should be invoked.
     * 
     * @return The {@link FunctionExecutor}s which can be invoked.
     */
    protected Set<FunctionExecutor<R>> getExecutableExecutors() {

        Set<FunctionExecutor<R>> executors = getExecutors();

        if (locked) {
            for (FunctionExecutor<R> executor : new HashSet<FunctionExecutor<R>>(executors)) {
                try {
                    Method invoke = executor.getClass().getMethod("invoke", FeatureHolder.class, Object[].class);
                    if (invoke.isAnnotationPresent(Lockable.class)) {
                        executors.remove(executor);
                    }
                }
                catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Programmer's fault: Can't find invoke() method (should be defined by interface)", e);
                }
            }
        }

        return executors;
    }

    @Override
    public R invoke(Object... arguments) throws FunctionExecutionException {

        List<R> returnValues = invokeRA(arguments);
        if (returnValues.size() > 0) {
            return returnValues.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<R> invokeRA(Object... arguments) throws FunctionExecutionException {

        Set<FunctionExecutor<R>> executors = getExecutableExecutors();
        if (executors.isEmpty()) {
            LOGGER.warning("No executable executors found for function '" + getName() + "' in holder class '" + getHolder().getClass().getName() + "'" + (locked ? ". Locked?" : ""));
        }

        SortedMap<Integer, Set<FunctionExecutor<R>>> sortedExecutors = new TreeMap<Integer, Set<FunctionExecutor<R>>>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {

                return o2 - o1;
            }

        });

        // Sort the executors by priority
        for (FunctionExecutor<R> executor : getExecutableExecutors()) {
            int priority = Prioritized.DEFAULT;

            // Read custom priorities
            try {
                Method invoke = executor.getClass().getMethod("invoke", FeatureHolder.class, Object[].class);
                if (invoke.isAnnotationPresent(Prioritized.class)) {
                    priority = invoke.getAnnotation(Prioritized.class).value();
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Programmer's fault: Can't find invoke() method (should be defined by interface)", e);
            }

            if (!sortedExecutors.containsKey(priority)) {
                sortedExecutors.put(priority, new HashSet<FunctionExecutor<R>>());
            }
            sortedExecutors.get(priority).add(executor);
        }

        // Invoke the executors
        List<R> returnValues = new ArrayList<R>();
        invokeExecutors:
        for (Set<FunctionExecutor<R>> priorityGroup : sortedExecutors.values()) {
            for (FunctionExecutor<R> executor : priorityGroup) {
                try {
                    returnValues.add(executor.invoke(getHolder(), arguments));
                }
                catch (Exception e) {
                    // Allow StopExecutionException and IllegalArgumentException (argument validation)
                    if (e instanceof StopExecutionException || e instanceof IllegalArgumentException) {
                        if (priorityGroup.size() > 1) {
                            String otherExecutors = "";
                            for (FunctionExecutor<R> otherExecutor : priorityGroup) {
                                if (!otherExecutor.equals(executor)) {
                                    otherExecutors += ", '" + otherExecutor.getClass().getName() + "'";
                                }
                            }
                            otherExecutors = otherExecutors.substring(2);
                            LOGGER.warning("Function executor '" + executor.getClass().getName() + "' stopped while having the same priority as the executors " + otherExecutors);
                        }

                        if (e.getCause() == null) {
                            break invokeExecutors;
                        } else {
                            if (e.getMessage() == null) {
                                throw new FunctionExecutionException(e.getCause());
                            } else {
                                throw new FunctionExecutionException(e.getMessage(), e.getCause());
                            }
                        }
                    } else {
                        LOGGER.log(Level.SEVERE, "Function executor '" + executor.getClass().getName() + "' threw an unexpected exception", e);
                    }
                }
            }
        }

        return returnValues;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [name=" + getName() + ", " + getExecutableExecutors().size() + "/" + getExecutors().size() + " executors, locked=" + locked + "]";
    }

}
