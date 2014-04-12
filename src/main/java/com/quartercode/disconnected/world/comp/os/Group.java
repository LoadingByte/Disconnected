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

import java.util.HashMap;
import java.util.Map;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
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

        GET_COLUMNS.addExecutor("default", User.class, new FunctionExecutor<Map<String, Object>>() {

            @Override
            public Map<String, Object> invoke(FunctionInvocation<Map<String, Object>> invocation, Object... arguments) throws ExecutorInvocationException {

                Map<String, Object> columns = new HashMap<String, Object>();
                FeatureHolder holder = invocation.getHolder();

                columns.put("name", holder.get(NAME).get());

                columns.putAll(NullPreventer.prevent(invocation.next(arguments)));
                return columns;
            }

        });
        SET_COLUMNS.addExecutor("default", User.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                // Trust the user of the method
                @SuppressWarnings ("unchecked")
                Map<String, Object> columns = (Map<String, Object>) arguments[0];
                FeatureHolder holder = invocation.getHolder();

                holder.get(NAME).set((String) columns.get("name"));

                return invocation.next(arguments);
            }

        });
    }

    /**
     * Creates a new group object.
     */
    public Group() {

    }

}
