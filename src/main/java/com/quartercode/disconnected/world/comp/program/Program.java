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

import java.util.HashSet;
import java.util.Set;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.util.NullPreventer;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.Vulnerability;

/**
 * This class stores information about a program.
 * A program object can be stored in a file. The execution is done by a program executor. To run an executor, you need to create a new process.
 * This also contains a list of all vulnerabilities this program has.
 * 
 * @see Vulnerability
 * @see ProgramExecutor
 * @see Process
 */
public class Program extends DefaultFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The {@link Version} of the program.
     */
    protected static final FeatureDefinition<ObjectProperty<Version>>                          VERSION;

    /**
     * The {@link Vulnerability}s the program has.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<Vulnerability>>>               VULNERABILITIES;

    /**
     * The {@link Class} of the {@link ProgramExecutor} which can execute the program.
     * The {@link Class} must have a default constructor.
     */
    protected static final FeatureDefinition<ObjectProperty<Class<? extends ProgramExecutor>>> EXECUTOR_CLASS;

    static {

        VERSION = new AbstractFeatureDefinition<ObjectProperty<Version>>("version") {

            @Override
            public ObjectProperty<Version> create(FeatureHolder holder) {

                return new ObjectProperty<Version>(getName(), holder);
            }

        };

        VULNERABILITIES = new AbstractFeatureDefinition<ObjectProperty<Set<Vulnerability>>>("version") {

            @Override
            public ObjectProperty<Set<Vulnerability>> create(FeatureHolder holder) {

                return new ObjectProperty<Set<Vulnerability>>(getName(), holder, new HashSet<Vulnerability>());
            }

        };

        EXECUTOR_CLASS = new AbstractFeatureDefinition<ObjectProperty<Class<? extends ProgramExecutor>>>("executorClass") {

            @Override
            public ObjectProperty<Class<? extends ProgramExecutor>> create(FeatureHolder holder) {

                return new ObjectProperty<Class<? extends ProgramExecutor>>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link Version} of the program.
     */
    public static final FunctionDefinition<Version>                                            GET_VERSION;

    /**
     * Changes the {@link Version} of the program.
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
     * <td>{@link Version}</td>
     * <td>version</td>
     * <td>The new {@link Version} of the program.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                               SET_VERSION;

    /**
     * Returns the {@link Vulnerability}s the program has.
     */
    public static final FunctionDefinition<Set<Vulnerability>>                                 GET_VULNERABILITIES;

    /**
     * Adds {@link Vulnerability}s to the program.
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
     * <td>{@link Vulnerability}...</td>
     * <td>vulnerabilities</td>
     * <td>The {@link Vulnerability}s to add to the program.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                               ADD_VULNERABILITIES;

    /**
     * Removes {@link Vulnerability}s from the program.
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
     * <td>{@link Vulnerability}...</td>
     * <td>vulnerabilities</td>
     * <td>The {@link Vulnerability}s to remove from the program.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                               REMOVE_VULNERABILITIES;

    /**
     * Returns the {@link Class} of the {@link ProgramExecutor} which can execute the program.
     * The {@link Class} must have a default constructor.
     */
    public static final FunctionDefinition<Class<? extends ProgramExecutor>>                   GET_EXECUTOR_CLASS;

    /**
     * Changes the {@link Class} of the {@link ProgramExecutor} which can execute the program.
     * The {@link Class} must have a default constructor.
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
     * <td>{@link Class}&lt;? extends {@link ProgramExecutor}&gt;</td>
     * <td>class</td>
     * <td>The new {@link ProgramExecutor} {@link Class} which can executor the program.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                               SET_EXECUTOR_CLASS;

    /**
     * Creates a new {@link FunctionExecutor} out of the set executor {@link Class}.
     * The executor {@link Class} must have a default constructor.
     */
    public static final FunctionDefinition<ProgramExecutor>                                    CREATE_EXECUTOR;

    static {

        GET_VERSION = FunctionDefinitionFactory.create("getVersion", Program.class, PropertyAccessorFactory.createGet(VERSION));
        SET_VERSION = FunctionDefinitionFactory.create("setVersion", Program.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(VERSION)), Version.class);

        GET_VULNERABILITIES = FunctionDefinitionFactory.create("getVulnerabilities", Program.class, CollectionPropertyAccessorFactory.createGet(VULNERABILITIES));
        ADD_VULNERABILITIES = FunctionDefinitionFactory.create("addVulnerabilities", Program.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createAdd(VULNERABILITIES)), Vulnerability[].class);
        REMOVE_VULNERABILITIES = FunctionDefinitionFactory.create("removeVulnerabilities", Program.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createRemove(VULNERABILITIES)), Vulnerability[].class);

        GET_EXECUTOR_CLASS = FunctionDefinitionFactory.create("getExecutorClass", Program.class, PropertyAccessorFactory.createGet(EXECUTOR_CLASS));
        SET_EXECUTOR_CLASS = FunctionDefinitionFactory.create("setExecutorClass", Program.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(EXECUTOR_CLASS)), Class.class);
        CREATE_EXECUTOR = FunctionDefinitionFactory.create("createExecutor", Program.class, new FunctionExecutor<ProgramExecutor>() {

            @Override
            public ProgramExecutor invoke(FunctionInvocation<ProgramExecutor> invocation, Object... arguments) throws ExecutorInvocationException {

                Class<? extends ProgramExecutor> executorClass = invocation.getHolder().get(GET_EXECUTOR_CLASS).invoke();

                try {
                    return executorClass.newInstance();
                } catch (Exception e) {
                    throw new ExecutorInvocationException("Unexpected exception during initialization of new program executor (class '" + executorClass.getName() + "'", e);
                }
            }

        });

        GET_SIZE.addExecutor(Program.class, "executor", new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) throws ExecutorInvocationException {

                // TODO: Make something related to the size
                return 0 + NullPreventer.prevent(invocation.next(arguments));
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new program.
     */
    public Program() {

    }

}
