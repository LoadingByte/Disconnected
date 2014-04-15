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

package com.quartercode.disconnected.world.comp.os;

import java.util.List;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.ValueSupplierDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.disconnected.util.NullPreventer;
import com.quartercode.disconnected.world.comp.os.Configuration.ConfigurationEntry;

/**
 * A group represents a collection of multiple {@link User}s which have the same rights.
 * The group object also takes care of the right system and other things related to {@link User}s.
 * 
 * @see User
 */
public class Group extends ConfigurationEntry {

    // ----- Properties -----

    /**
     * The name of the group.
     * The name is used for recognizing a group on the os-level.
     */
    public static final PropertyDefinition<String> NAME;

    static {

        NAME = ObjectProperty.createDefinition("name");

    }

    // ----- Functions -----

    static {

        GET_COLUMNS.addExecutor("name", Group.class, new FunctionExecutor<List<ValueSupplierDefinition<?, ?>>>() {

            @Override
            public List<ValueSupplierDefinition<?, ?>> invoke(FunctionInvocation<List<ValueSupplierDefinition<?, ?>>> invocation, Object... arguments) {

                List<ValueSupplierDefinition<?, ?>> columns = NullPreventer.prevent(invocation.next(arguments));
                columns.add(NAME);
                return columns;
            }

        });

    }

    /**
     * Creates a new group object.
     */
    public Group() {

    }

}
