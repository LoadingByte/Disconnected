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

package com.quartercode.disconnected.server.world.comp.program;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.disconnected.server.util.NullPreventer;
import com.quartercode.disconnected.server.world.comp.Vulnerability;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
import com.quartercode.disconnected.shared.comp.Version;

/**
 * This class stores information about a program.
 * A program object can be stored in a file. The execution is done by a program executor. To run an executor, you need to create a new process.
 * This also contains a list of all vulnerabilities this program has.
 * 
 * @see Vulnerability
 * @see ProgramExecutor
 * @see Process
 */
public class Program extends WorldFeatureHolder implements DerivableSize {

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

        VERSION = create(new TypeLiteral<PropertyDefinition<Version>>() {}, "name", "version", "storage", new StandardStorage<>());
        VULNERABILITIES = create(new TypeLiteral<CollectionPropertyDefinition<Vulnerability, Set<Vulnerability>>>() {}, "name", "vulnerabilities", "storage", new StandardStorage<>(), "collection", new CloneValueFactory<>(new HashSet<>()));
        EXECUTOR_CLASS = create(new TypeLiteral<PropertyDefinition<Class<? extends ProgramExecutor>>>() {}, "name", "executorClass", "storage", new StandardStorage<>());

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("executor", Program.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                // TODO: Make something related to the size
                return 0 + NullPreventer.prevent(invocation.next(arguments));
            }

        });

    }

}
