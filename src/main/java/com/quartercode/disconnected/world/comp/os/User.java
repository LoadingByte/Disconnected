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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.util.NullPreventer;
import com.quartercode.disconnected.world.comp.os.Configuration.ConfigurationEntry;

/**
 * A user represents a system user (basically someone who can use a system).
 * The user object represents a user and all its properties. It can be used as {@link ConfigurationEntry}.
 * 
 * @see Group
 */
public class User extends ConfigurationEntry {

    /**
     * This is the name of the superuser on a system.
     * The superuser of a system can do everything without having the rights applied for doing it.
     * You can check if a user is the superuser by using {@link #IS_SUPERUSER}.
     */
    public static final String                                             SUPERUSER_NAME = "root";

    // ----- Properties -----

    /**
     * The name of the user.
     * The name is used for recognizing a user on the os-level.
     */
    public static final PropertyDefinition<String>                         NAME;

    /**
     * The hashed password of the user.
     * It is hashed using the SHA-256 algorithm and can be used to authenticate as the user.
     */
    public static final PropertyDefinition<String>                         PASSWORD;

    /**
     * The names of all {@link Group}s the user is a member in.
     * Such {@link Group}s are used to set rights for multiple users.
     */
    public static final CollectionPropertyDefinition<String, List<String>> GROUPS;

    static {

        NAME = ObjectProperty.createDefinition("name");
        NAME.addSetterExecutor("checkNotSuperuser", User.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                if (invocation.getHolder().get(IS_SUPERUSER).invoke()) {
                    // Cancel invocation
                    return null;
                }

                return invocation.next(arguments);
            }

        });

        PASSWORD = ObjectProperty.createDefinition("password");

        GROUPS = ObjectCollectionProperty.createDefinition("groups", new ArrayList<String>(), true);
        GROUPS.addAdderExecutor("checkAllowed", User.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                Validate.isTrue(!holder.get(IS_SUPERUSER).invoke(), "The superuser can't be a member in any group");
                if (holder.get(GROUPS).get().contains(arguments[0])) {
                    throw new IllegalStateException("The user is already a member in that group");
                }

                return invocation.next(arguments);
            }

        });
        GROUPS.addRemoverExecutor("checkAllowed", User.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                Validate.isTrue(!holder.get(IS_SUPERUSER).invoke(), "The superuser can't be a member in any group");
                if (holder.get(GET_PRIMARY_GROUP).invoke().equals(arguments[0])) {
                    throw new IllegalStateException("Can't remove user from its primary group");
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    /**
     * Returns the name of the primary {@link Group} of the user.
     * The primary {@link Group} is the first {@link Group} in the {@link #GET_GROUPS} list and is used when new rights are applied.
     */
    public static final FunctionDefinition<String>                         GET_PRIMARY_GROUP;

    /**
     * Changes the primary {@link Group} of the user to the one which has the given name.
     * The user must already be a member of the {@link Group}.
     * The primary {@link Group} is the first {@link Group} in the {@link #GET_GROUPS} list and is used when new rights are applied.
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
     * <td>primaryGroup</td>
     * <td>The name of the new primary {@link Group} of the user.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                           SET_PRIMARY_GROUP;

    /**
     * Returns true if the user is a superuser.
     * The superuser of a system can do everything without having the rights applied for doing it.
     */
    public static final FunctionDefinition<Boolean>                        IS_SUPERUSER;

    static {

        GET_PRIMARY_GROUP = FunctionDefinitionFactory.create("getPrimaryGroup", User.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                String primaryGroup = null;
                if (holder.get(GROUPS).get().size() > 0) {
                    primaryGroup = holder.get(GROUPS).get().get(0);
                }

                invocation.next(arguments);
                return primaryGroup;
            }

        });
        SET_PRIMARY_GROUP = FunctionDefinitionFactory.create("setPrimaryGroup", User.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                if (holder.get(GROUPS).get().contains(arguments[0])) {
                    holder.get(GROUPS).get().remove(arguments[0]);
                    holder.get(GROUPS).get().add(0, (String) arguments[0]);
                }

                return invocation.next(arguments);
            }

        }, Group.class);

        IS_SUPERUSER = FunctionDefinitionFactory.create("isSuperuser", User.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) throws ExecutorInvocationException {

                String name = invocation.getHolder().get(NAME).get();
                boolean result = name != null && name.equals(SUPERUSER_NAME);
                invocation.next(arguments);
                return result;
            }
        });

        GET_COLUMNS.addExecutor("default", User.class, new FunctionExecutor<Map<String, Object>>() {

            @Override
            public Map<String, Object> invoke(FunctionInvocation<Map<String, Object>> invocation, Object... arguments) throws ExecutorInvocationException {

                Map<String, Object> columns = new HashMap<String, Object>();
                FeatureHolder holder = invocation.getHolder();

                columns.put("name", holder.get(NAME).get());
                columns.put("groups", holder.get(GROUPS).get());

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

                Validate.isTrue(columns.get("groups") instanceof List, "Groups must be a list");
                // Trust the user again
                @SuppressWarnings ("unchecked")
                List<String> groups = (List<String>) columns.get("groups");
                if (groups.size() > 0) {
                    for (String group : groups) {
                        holder.get(GROUPS).add(group);
                    }
                    holder.get(SET_PRIMARY_GROUP).invoke(groups.get(0));
                }

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new user.
     */
    public User() {

    }

}
