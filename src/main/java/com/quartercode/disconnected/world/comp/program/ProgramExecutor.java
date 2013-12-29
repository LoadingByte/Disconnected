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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ResourceBundle;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.sim.run.TickSimulator;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.program.ArgumentException.MissingArgumentException;
import com.quartercode.disconnected.world.comp.program.ArgumentException.MissingParameterException;
import com.quartercode.disconnected.world.comp.program.ArgumentException.WrongArgumentTypeException;
import com.quartercode.disconnected.world.comp.program.event.ProcessEvent;

/**
 * This abstract class defines a program executor which takes care of acutally running a program.
 * The executor class is set in the {@link Program}.
 * 
 * @see Program
 * @see Process
 * @see PacketListener
 */
public abstract class ProgramExecutor extends WorldChildFeatureHolder<Process<?>> {

    // ----- Properties -----

    /**
     * The initial arguments the program executor needs to operate.
     * They should be used from inside the {@link #UPDATE} method.
     * The arguments are validated against the {@link Parameter}s provided by {@link #GET_PARAMETERS} by {@link #SET_ARGUMENTS}.
     */
    protected static final FeatureDefinition<ObjectProperty<Map<String, Object>>> ARGUMENTS;

    /**
     * The {@link ProcessEvent} which were received by the executing {@link Process} recently.
     */
    protected static final FeatureDefinition<ObjectProperty<Queue<ProcessEvent>>> EVENTS;

    static {

        ARGUMENTS = new AbstractFeatureDefinition<ObjectProperty<Map<String, Object>>>("arguments") {

            @Override
            public ObjectProperty<Map<String, Object>> create(FeatureHolder holder) {

                return new ObjectProperty<Map<String, Object>>(getName(), holder);
            }

        };

        EVENTS = new AbstractFeatureDefinition<ObjectProperty<Queue<ProcessEvent>>>("events") {

            @Override
            public ObjectProperty<Queue<ProcessEvent>> create(FeatureHolder holder) {

                return new ObjectProperty<Queue<ProcessEvent>>(getName(), holder, new LinkedList<ProcessEvent>());
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the possible or required execution {@link Parameter}s.
     * The function should be implemented by the actual program class.
     * For more detail on the parameters, see the {@link Parameter} class.
     */
    public static final FunctionDefinition<List<Parameter>>                       GET_PARAMETERS;

    /**
     * Returns the execution {@link Parameter} with the given name (or null if there's no such {@link Parameter}).
     * For more detail on the parameters, see the {@link Parameter} class.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link String}</td>
     * <td>name</td>
     * <td>The name of the returned {@link Parameter}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Parameter>                             GET_PARAMETER_BY_NAME;

    /**
     * Returns the {@link ResourceBundle} the program uses.
     * This should be implemented by the actual child program class.
     */
    protected static final FunctionDefinition<ResourceBundle>                     GET_RESOURCE_BUNDLE;

    /**
     * Returns the initial arguments the program executor needs to operate.
     * They should be used from inside the {@link #UPDATE} method.
     * The arguments are validated against the {@link Parameter}s provided by {@link #GET_PARAMETERS} by {@link #SET_ARGUMENTS}.
     */
    public static final FunctionDefinition<Map<String, Object>>                   GET_ARGUMENTS;

    /**
     * Changes the initial arguments the program executor needs to operate.
     * They should be used from inside the {@link #UPDATE} method.
     * The arguments are validated against the {@link Parameter}s provided by {@link #GET_PARAMETERS} by this function.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Map}&lt;{@link String}, {@link Object}&gt;</td>
     * <td>arguments</td>
     * <td>The initial arguments the program executor needs to operate.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                  SET_ARGUMENTS;

    /**
     * Lets the program executor receive some {@link ProcessEvent} which were catched by the executing {@link Process}.
     * The new {@link ProcessEvent}s are added to a {@link Queue} which can be accessed using the poll function {@link #NEXT_EVENT}.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link ProcessEvent}...</td>
     * <td>events</td>
     * <td>The new {@link ProcessEvent} the program executor just received.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                  RECEIVE_EVENTS;

    /**
     * Returns the next received {@link ProcessEvent} from the {@link Queue} which matches the criteria of the given {@link ProcessEventMatcher}.
     * Every returned {@link ProcessEvent} will also be removed from the {@link Queue}.
     * Example:
     * 
     * <pre>
     * Format: Type[Identifier]
     * Queue:  A[1], A[2], B[3], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (matcher for A) returns A[1]
     * => A[2], B[3], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (matcher for B) returns B[3]
     * => A[2], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (matcher for A) returns A[2]
     * => A[4], B[5], B[6]
     * </pre>
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link ProcessEventMatcher}</td>
     * <td>matcher</td>
     * <td>The {@link ProcessEventMatcher} to check for the {@link ProcessEvent} which should be returned.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<ProcessEvent>                          NEXT_EVENT;

    /**
     * Executes a tick update in the program executor.
     * Every program is written using the tick system.
     * You can add {@link FunctionExecutor}s to the definition which actually execute a program tick.
     */
    public static final FunctionDefinition<Void>                                  UPDATE;

    static {

        GET_PARAMETERS = FunctionDefinitionFactory.create("getParameters", ProgramExecutor.<List<Parameter>> castClass(List.class));
        GET_PARAMETER_BY_NAME = FunctionDefinitionFactory.create("getParameterByName", ProgramExecutor.class, new FunctionExecutor<Parameter>() {

            @Override
            public Parameter invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                for (List<Parameter> parameterList : holder.get(GET_PARAMETERS).invokeRA()) {
                    for (Parameter parameter : parameterList) {
                        if (parameter.getName().equals(arguments[0])) {
                            return parameter;
                        }
                    }
                }

                return null;
            }

        });

        GET_RESOURCE_BUNDLE = FunctionDefinitionFactory.create("getResourceBundle", ResourceBundle.class);

        GET_ARGUMENTS = FunctionDefinitionFactory.create("getArguments", ProgramExecutor.class, PropertyAccessorFactory.createGet(ARGUMENTS));
        SET_ARGUMENTS = FunctionDefinitionFactory.create("setArguments", ProgramExecutor.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(ARGUMENTS)), Map.class);
        SET_ARGUMENTS.addExecutor(ProgramExecutor.class, "checkArgumentsAgainstParameters", new FunctionExecutor<Void>() {

            @Override
            @SuppressWarnings ("unchecked")
            @Prioritized (Prioritized.CHECK)
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                // Create a new hash map with the contents of the old one (it will get modified)
                Map<String, Object> programArguments = arguments[0] == null ? new HashMap<String, Object>() : new HashMap<String, Object>((Map<String, Object>) arguments[0]);

                List<Parameter> parameters = new ArrayList<Parameter>();
                for (List<Parameter> parameterList : holder.get(GET_PARAMETERS).invokeRA()) {
                    parameters.addAll(parameterList);
                }

                try {
                    for (Parameter parameter : parameters) {
                        if (parameter.isSwitch()) {
                            // Put switch object if it's not set
                            if (!programArguments.containsKey(parameter.getName()) || ! (programArguments.get(parameter.getName()) instanceof Boolean)) {
                                programArguments.put(parameter.getName(), false);
                            }
                        } else if (parameter.isArgument()) {
                            // Throw exception if argument parameter is required, but not set
                            if (parameter.isRequired() && !programArguments.containsKey(parameter.getName())) {
                                throw new MissingParameterException(parameter);
                            }
                            // Throw exception if argument is required, but not set
                            else if (programArguments.containsKey(parameter.getName()) && parameter.isArgumentRequired() && programArguments.get(parameter.getName()) == null) {
                                throw new MissingArgumentException(parameter);
                            }
                            // Throw exception if argument has the wrong type
                            else if (programArguments.get(parameter.getName()) != null && !parameter.getType().getType().isAssignableFrom(programArguments.get(parameter.getName()).getClass())) {
                                throw new WrongArgumentTypeException(parameter, programArguments.get(parameter.getName()).toString());
                            }
                        } else if (parameter.isRest()) {
                            // Throw exception if rest is required, but not set
                            if (parameter.isRequired() && (!programArguments.containsKey(parameter.getName()) || ((String[]) programArguments.get(parameter.getName())).length == 0)) {
                                throw new MissingParameterException(parameter);
                            }
                        }
                    }
                }
                catch (ArgumentException e) {
                    throw new IllegalArgumentException("Illegal program arguments", e);
                }

                return null;
            }

        });

        RECEIVE_EVENTS = FunctionDefinitionFactory.create("receiveEvents", ProgramExecutor.class, CollectionPropertyAccessorFactory.createAdd(EVENTS), ProcessEvent.class);
        NEXT_EVENT = FunctionDefinitionFactory.create("nextEvent", ProgramExecutor.class, new FunctionExecutor<ProcessEvent>() {

            @Override
            public ProcessEvent invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                Queue<ProcessEvent> clone = new LinkedList<ProcessEvent>(holder.get(EVENTS).get());
                while (!clone.isEmpty()) {
                    ProcessEvent current = clone.poll();
                    if ( ((ProcessEventMatcher) arguments[0]).matches(current)) {
                        holder.get(EVENTS).get().remove(current);
                        return current;
                    }
                }

                return null;
            }

        }, ProcessEventMatcher.class);

        UPDATE = FunctionDefinitionFactory.create(TickSimulator.UPDATE_FUNCTION_NAME, Void.class);

    }

    // Help method because you can't attach @SuppressWarnings to static blocks
    @SuppressWarnings ("unchecked")
    private static <T> Class<T> castClass(Class<?> c) {

        return (Class<T>) c;
    }

    // ----- Function Definitions End ----

    /**
     * Creates a new program executor.
     */
    public ProgramExecutor() {

    }

    /**
     * Process event matchers are used to search for {@link ProcessEvent}s of a specific type.
     * The matcher is used to retrieve the next {@link ProcessEvent} a {@link ProgramExecutor} wants to handle in {@link ProgramExecutor#NEXT_EVENT}.
     */
    public static interface ProcessEventMatcher {

        /**
         * Checks if the given {@link ProcessEvent} is requested by the caller.
         * 
         * @param event The {@link ProcessEvent} to check.
         * @return True if the given {@link ProcessEvent} is requested, false if not.
         * @throws StopExecutionException Something bad happens while checking.
         */
        public boolean matches(ProcessEvent event) throws StopExecutionException;

    }

}
