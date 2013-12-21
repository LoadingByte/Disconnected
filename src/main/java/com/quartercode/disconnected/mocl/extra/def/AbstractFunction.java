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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeature;
import com.quartercode.disconnected.mocl.extra.Delay;
import com.quartercode.disconnected.mocl.extra.Function;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Limit;
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
public class AbstractFunction<R> extends AbstractFeature implements Function<R> {

    private static final Logger                            LOGGER = Logger.getLogger(AbstractFunction.class.getName());

    private final List<Class<?>>                           parameters;
    private final Set<DefaultFunctionExecutorContainer<R>> executors;
    private boolean                                        locked;
    private int                                            timesInvoked;

    /**
     * Creates a new abstract function with the given name, parent {@link FeatureHolder}, parameters and {@link FunctionExecutor}s.
     * 
     * @param name The name of the abstract function.
     * @param holder The {@link FeatureHolder} which has and uses the new abstract function.
     * @param parameters The argument types an {@link #invoke(Object...)} call must have (see {@link FunctionDefinition#setParameter(int, Class)} for further explanation).
     * @param executors The {@link FunctionExecutor}s which will be executing the function calls for this particular function.
     */
    public AbstractFunction(String name, FeatureHolder holder, List<Class<?>> parameters, Map<String, FunctionExecutor<R>> executors) {

        super(name, holder);

        for (Class<?> parameter : parameters) {
            Validate.isTrue(parameter != null, "Null parameters are not allowed");
        }
        this.parameters = parameters;

        this.executors = new HashSet<DefaultFunctionExecutorContainer<R>>();
        for (Entry<String, FunctionExecutor<R>> executor : executors.entrySet()) {
            this.executors.add(new DefaultFunctionExecutorContainer<R>(executor.getKey(), executor.getValue()));
        }

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

    /**
     * Returns the amount of times the {@link #invoke(Object...)} method was called on the function.
     * 
     * @return The amount of times the function was invoked.
     */
    public int getTimesInvoked() {

        return timesInvoked;
    }

    @Override
    public List<Class<?>> getParameters() {

        return Collections.unmodifiableList(parameters);
    }

    @Override
    public Set<FunctionExecutorContainer<R>> getExecutors() {

        return Collections.unmodifiableSet(new HashSet<FunctionExecutorContainer<R>>(this.executors));
    }

    @Override
    public FunctionExecutorContainer<R> getExecutor(String name) {

        for (FunctionExecutorContainer<R> executor : executors) {
            if (executor.getName().equals(name)) {
                return executor;
            }
        }

        return null;
    }

    /**
     * Collects the {@link FunctionExecutor}s which can be invoked through {@link #invoke(Object...)} or {@link #invokeRA(Object...)}.
     * This can be overriden to modify which {@link FunctionExecutor}s should be invoked.
     * 
     * @return The {@link FunctionExecutor}s which can be invoked.
     */
    protected Set<FunctionExecutorContainer<R>> getExecutableExecutors() {

        Set<DefaultFunctionExecutorContainer<R>> executors = new HashSet<DefaultFunctionExecutorContainer<R>>(this.executors);

        for (DefaultFunctionExecutorContainer<R> executor : new HashSet<DefaultFunctionExecutorContainer<R>>(executors)) {
            try {
                Method invoke = executor.getExecutor().getClass().getMethod("invoke", FeatureHolder.class, Object[].class);
                if (locked && invoke.isAnnotationPresent(Lockable.class)) {
                    executors.remove(executor);
                } else if (invoke.isAnnotationPresent(Limit.class) && executor.getTimesInvoked() + 1 > invoke.getAnnotation(Limit.class).value()) {
                    executors.remove(executor);
                } else if (invoke.isAnnotationPresent(Delay.class)) {
                    Delay annotation = invoke.getAnnotation(Delay.class);
                    // Start invokation count at 0 for the first invokation
                    int invokation = timesInvoked - 1;
                    if (invokation < annotation.firstDelay()) {
                        executors.remove(executor);
                    } else if (annotation.delay() > 0 && (invokation - annotation.firstDelay()) % (annotation.delay() + 1) != 0) {
                        executors.remove(executor);
                    }
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Programmer's fault: Can't find invoke() method (should be defined by interface)", e);
            }
        }

        return new HashSet<FunctionExecutorContainer<R>>(executors);
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

        timesInvoked++;

        // Argument validation
        try {
            String errorString = "";
            for (Class<?> parameter : parameters) {
                errorString += ", " + parameter.getSimpleName();
            }
            errorString = "Wrong arguments: '" + (errorString.isEmpty() ? "" : errorString.substring(2)) + "' required";

            for (int index = 0; index < parameters.size(); index++) {
                if (!parameters.get(index).isAssignableFrom(arguments[index].getClass())) {
                    if (parameters.get(index).isArray()) {
                        for (int varargIndex = index; varargIndex < arguments.length; varargIndex++) {
                            Validate.isTrue(parameters.get(index).getComponentType().isAssignableFrom(arguments[varargIndex].getClass()), errorString);
                        }
                    } else {
                        throw new IllegalArgumentException(errorString);
                    }
                }
            }
        }
        catch (IllegalArgumentException e) {
            throw new FunctionExecutionException(e);
        }

        // Check if there are any executable executors
        Set<FunctionExecutorContainer<R>> executors = getExecutableExecutors();
        if (executors.isEmpty()) {
            // Would not do anything -> Don't run unnecessary stuff
            return new ArrayList<R>();
        }

        // Sort the executors by priority
        SortedMap<Integer, Set<FunctionExecutorContainer<R>>> sortedExecutors = new TreeMap<Integer, Set<FunctionExecutorContainer<R>>>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {

                return o2 - o1;
            }

        });
        for (FunctionExecutorContainer<R> executor : executors) {
            int priority = Prioritized.DEFAULT;

            // Read custom priorities
            try {
                Method invoke = executor.getExecutor().getClass().getMethod("invoke", FeatureHolder.class, Object[].class);
                if (invoke.isAnnotationPresent(Prioritized.class)) {
                    priority = invoke.getAnnotation(Prioritized.class).value();
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Programmer's fault: Can't find invoke() method (should be defined by interface)", e);
            }

            if (!sortedExecutors.containsKey(priority)) {
                sortedExecutors.put(priority, new HashSet<FunctionExecutorContainer<R>>());
            }
            sortedExecutors.get(priority).add(executor);
        }

        // Invoke the executors
        List<R> returnValues = new ArrayList<R>();
        invokeExecutors:
        for (Set<FunctionExecutorContainer<R>> priorityGroup : sortedExecutors.values()) {
            for (FunctionExecutorContainer<R> executor : priorityGroup) {
                try {
                    returnValues.add(executor.invoke(getHolder(), arguments));
                }
                catch (Exception e) {
                    // Allow StopExecutionException and IllegalArgumentException (argument validation)
                    if (e instanceof StopExecutionException || e instanceof IllegalArgumentException) {
                        if (priorityGroup.size() > 1) {
                            String otherExecutors = "";
                            for (FunctionExecutorContainer<R> otherExecutor : priorityGroup) {
                                if (!otherExecutor.equals(executor)) {
                                    otherExecutors += ", '" + otherExecutor.getExecutor().getClass().getName() + "'";
                                }
                            }
                            otherExecutors = otherExecutors.substring(2);
                            LOGGER.warning("Function executor '" + executor.getExecutor().getClass().getName() + "' stopped while having the same priority as the executors " + otherExecutors);
                        }

                        if (e.getCause() == null) {
                            break invokeExecutors;
                        } else {
                            throw new FunctionExecutionException(e.getCause());
                        }
                    } else {
                        LOGGER.log(Level.SEVERE, "Function executor '" + executor.getExecutor().getClass().getName() + "' threw an unexpected exception", e);
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

    /**
     * The default implementation of the {@link FunctionExecutorContainer} for storing data values along with a {@link FunctionExecutor}.
     * The data isn't stored in the actual {@link FunctionExecutor} object because it should only do the execution and nothing else.
     * 
     * @param <R> The type of the value the stored {@link FunctionExecutor} returns.
     */
    public static class DefaultFunctionExecutorContainer<R> implements FunctionExecutorContainer<R> {

        private final String              name;
        private final FunctionExecutor<R> executor;
        private int                       timesInvoked = 0;

        /**
         * Creates a new default function executor container and fills in the {@link FunctionExecutor} to store and its name.
         * 
         * @param name The name of the {@link FunctionExecutor} to store.
         * @param executor The {@link FunctionExecutor} which is stored by the container
         */
        public DefaultFunctionExecutorContainer(String name, FunctionExecutor<R> executor) {

            this.name = name;
            this.executor = executor;
        }

        /**
         * Returns the name of the {@link FunctionExecutor} which is stored by the container.
         * 
         * @return The name of the stored {@link FunctionExecutor}.
         */
        @Override
        public String getName() {

            return name;
        }

        /**
         * Returns the actual {@link FunctionExecutor} which is stored by the container
         * 
         * @return The stored {@link FunctionExecutor}.
         */
        @Override
        public FunctionExecutor<R> getExecutor() {

            return executor;
        }

        /**
         * Returns the amount of times the stored {@link FunctionExecutor} was invoked through {@link #invoke(FeatureHolder, Object...)}.
         * 
         * @return The amount of times the {@link FunctionExecutor} was invoked.
         */
        public int getTimesInvoked() {

            return timesInvoked;
        }

        @Override
        public void resetLimit() {

            timesInvoked = 0;
        }

        /**
         * Invokes the stored {@link FunctionExecutor} in the given {@link FeatureHolder} with the given arguments.
         * Also increases the amount of times the {@link FunctionExecutor} was invoked. You can get the value with {@link #getTimesInvoked()}.
         * 
         * @param holder The {@link FeatureHolder} the stored {@link FunctionExecutor} is invoked in.
         * @param arguments Some arguments for the stored {@link FunctionExecutor}.
         * @return The value the invoked {@link FunctionExecutor} returns. Can be null.
         * @throws StopExecutionException The execution of the invokation queue should stop.
         */
        @Override
        public R invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

            timesInvoked++;
            return executor.invoke(holder, arguments);
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + (name == null ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DefaultFunctionExecutorContainer<?> other = (DefaultFunctionExecutorContainer<?>) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {

            return getClass().getName() + " [executor=" + executor + ", timesInvoked=" + timesInvoked + "]";
        }

    }

}
