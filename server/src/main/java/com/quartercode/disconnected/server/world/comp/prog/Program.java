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

package com.quartercode.disconnected.server.world.comp.prog;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.ValueFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.util.NullPreventer;
import com.quartercode.disconnected.server.world.comp.vuln.Vuln;
import com.quartercode.disconnected.server.world.comp.vuln.VulnContainer;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.Version;

/**
 * This class stores information about a program.
 * A program object can be stored in a file. The execution is done by a {@link ProgramExecutor}. To run an executor, you need to create a new {@link Process}.
 * This also contains a {@link VulnContainer vulnerability container} that manages the {@link Vuln vulnerabilities} of the program.
 * 
 * @see ProgramExecutor
 * @see Process
 * @see VulnContainer
 */
public class Program extends WorldFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The name of the program.
     * It is used to retrieve the {@link WorldProgram} object which defines the actual {@link ProgramExecutor}.
     */
    public static final PropertyDefinition<String>        NAME;

    /**
     * The {@link Version} of the program.
     */
    public static final PropertyDefinition<Version>       VERSION;

    /**
     * A {@link VulnContainer vulnerability container} that manages the {@link Vuln vulnerabilities} of the program.
     */
    public static final PropertyDefinition<VulnContainer> VULN_CONTAINER;

    static {

        NAME = factory(PropertyDefinitionFactory.class).create("name", new StandardStorage<>());
        VERSION = factory(PropertyDefinitionFactory.class).create("version", new StandardStorage<>());
        VULN_CONTAINER = factory(PropertyDefinitionFactory.class).create("vulnContainer", new StandardStorage<>(), new ValueFactory<VulnContainer>() {

            @Override
            public VulnContainer get() {

                return new VulnContainer();
            }

        });

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("executor", Program.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                long size = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), holder.getObj(NAME)).getSize();
                return size + NullPreventer.prevent(invocation.next(arguments));
            }

        });

    }

}
