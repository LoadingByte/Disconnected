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
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
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
    protected static final FeatureDefinition<ObjectProperty<String>> NAME;

    static {

        NAME = new AbstractFeatureDefinition<ObjectProperty<String>>("name") {

            @Override
            public ObjectProperty<String> create(FeatureHolder holder) {

                return new ObjectProperty<String>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the name of the group.
     * The name is used for recognizing a group on the os-level.
     */
    public static final FunctionDefinition<String>                   GET_NAME;

    /**
     * Changes the name of the group.
     * The name is used for recognizing a group on the os-level.
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
     * <td>The new name of the group.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                     SET_NAME;

    static {

        GET_NAME = FunctionDefinitionFactory.create("getName", Group.class, PropertyAccessorFactory.createGet(NAME));
        SET_NAME = FunctionDefinitionFactory.create("setName", Group.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(NAME)), String.class);

        GET_COLUMNS.addExecutor(User.class, "default", new FunctionExecutor<Map<String, Object>>() {

            @Override
            public Map<String, Object> invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                Map<String, Object> columns = new HashMap<String, Object>();
                columns.put("name", holder.get(GET_NAME).invoke());
                return columns;
            }

        });
        SET_COLUMNS.addExecutor(User.class, "default", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Trust the user of the method
                @SuppressWarnings ("unchecked")
                Map<String, Object> columns = (Map<String, Object>) arguments[0];
                holder.get(SET_NAME).invoke(columns.get("name"));
                return null;
            }

        });
    }

    // ----- Functions End -----

    /**
     * Creates a new group object.
     */
    public Group() {

    }

}
