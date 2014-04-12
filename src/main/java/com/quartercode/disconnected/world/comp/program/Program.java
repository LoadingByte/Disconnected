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
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
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
    public static final PropertyDefinition<Version>                                     VERSION;

    /**
     * The {@link Vulnerability}s the program has.
     */
    public static final CollectionPropertyDefinition<Vulnerability, Set<Vulnerability>> VULNERABILITIES;

    /**
     * The {@link Class} of the {@link ProgramExecutor} which can execute the program.
     * The {@link Class} must have a default constructor.
     */
    public static final PropertyDefinition<Class<? extends ProgramExecutor>>            EXECUTOR_CLASS;

    static {

        VERSION = ObjectProperty.createDefinition("version");
        VULNERABILITIES = ObjectCollectionProperty.createDefinition("vulnerabilities", new HashSet<Vulnerability>());
        EXECUTOR_CLASS = ObjectProperty.createDefinition("executorClass");

    }

    // ----- Functions -----

    /**
     * Creates a new {@link FunctionExecutor} out of the set executor {@link Class}.
     * The executor {@link Class} must have a default constructor.
     */
    public static final FunctionDefinition<ProgramExecutor>                             CREATE_EXECUTOR;

    static {

        CREATE_EXECUTOR = FunctionDefinitionFactory.create("createExecutor", Program.class, new FunctionExecutor<ProgramExecutor>() {

            @Override
            public ProgramExecutor invoke(FunctionInvocation<ProgramExecutor> invocation, Object... arguments) throws ExecutorInvocationException {

                Class<? extends ProgramExecutor> executorClass = invocation.getHolder().get(EXECUTOR_CLASS).get();

                try {
                    return executorClass.newInstance();
                } catch (Exception e) {
                    throw new ExecutorInvocationException("Unexpected exception during initialization of new program executor (class '" + executorClass.getName() + "'", e);
                }
            }

        });

        GET_SIZE.addExecutor("executor", Program.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) throws ExecutorInvocationException {

                // TODO: Make something related to the size
                return 0 + NullPreventer.prevent(invocation.next(arguments));
            }

        });

    }

    /**
     * Creates a new program.
     */
    public Program() {

    }

}
