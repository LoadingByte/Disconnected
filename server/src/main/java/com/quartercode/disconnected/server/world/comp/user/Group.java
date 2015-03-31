/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.world.comp.user;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.Map;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.prop.ValueSupplierDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.util.NullPreventer;
import com.quartercode.disconnected.server.world.comp.config.ConfigEntry;

/**
 * A group represents a collection of multiple {@link User}s which have the same rights.
 * The group object also takes care of the right system and other things related to {@link User}s.
 * 
 * @see User
 */
public class Group extends ConfigEntry {

    // ----- Properties -----

    /**
     * The name of the group.
     * The name is used for recognizing a group on the os-level.
     */
    public static final PropertyDefinition<String> NAME;

    static {

        NAME = factory(PropertyDefinitionFactory.class).create("name", new StandardStorage<>());

    }

    // ----- Functions -----

    static {

        GET_COLUMNS.addExecutor("name", Group.class, new FunctionExecutor<Map<ValueSupplierDefinition<?, ?>, Class<?>>>() {

            @Override
            public Map<ValueSupplierDefinition<?, ?>, Class<?>> invoke(FunctionInvocation<Map<ValueSupplierDefinition<?, ?>, Class<?>>> invocation, Object... arguments) {

                Map<ValueSupplierDefinition<?, ?>, Class<?>> columns = NullPreventer.prevent(invocation.next(arguments));
                columns.put(NAME, String.class);
                return columns;
            }

        });

    }

}
