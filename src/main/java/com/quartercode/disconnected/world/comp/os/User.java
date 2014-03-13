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
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Lockable;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
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
    public static final String                                                SUPERUSER_NAME = "root";

    // ----- Properties -----

    /**
     * The name of the user.
     * The name is used for recognizing a user on the os-level.
     */
    protected static final FeatureDefinition<ObjectProperty<String>>          NAME;

    /**
     * The hashed password of the user.
     * It is hashed using the SHA-256 algorithm and can be used to authenticate as the user.
     */
    protected static final FeatureDefinition<ObjectProperty<String>>          PASSWORD;

    /**
     * The names of all {@link Group}s the user is a member in.
     * Such {@link Group}s are used to set rights for multiple users.
     */
    protected static final FeatureDefinition<ReferenceProperty<List<String>>> GROUPS;

    static {

        NAME = new AbstractFeatureDefinition<ObjectProperty<String>>("name") {

            @Override
            public ObjectProperty<String> create(FeatureHolder holder) {

                return new ObjectProperty<String>(getName(), holder);
            }

        };

        PASSWORD = new AbstractFeatureDefinition<ObjectProperty<String>>("password") {

            @Override
            public ObjectProperty<String> create(FeatureHolder holder) {

                return new ObjectProperty<String>(getName(), holder);
            }

        };

        GROUPS = new AbstractFeatureDefinition<ReferenceProperty<List<String>>>("groups") {

            @Override
            public ReferenceProperty<List<String>> create(FeatureHolder holder) {

                return new ReferenceProperty<List<String>>(getName(), holder, new ArrayList<String>());
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the name of the user.
     * The name is used for recognizing a user on the os-level.
     */
    public static final FunctionDefinition<String>                            GET_NAME;

    /**
     * Changes the name of the user.
     * The name is used for recognizing a user on the os-level.
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
     * <td>The new name of the user.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                              SET_NAME;

    /**
     * Returns the hashed password of the user.
     * It is hashed using the SHA-256 algorithm and can be used to authenticate as the user.
     */
    public static final FunctionDefinition<String>                            GET_PASSWORD;

    /**
     * Sets the hashed password of the user.
     * The new password must be hashed using the SHA-256 algorithm.
     * It can be used to authenticate as the user.
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
     * <td>password</td>
     * <td>The new password of the user. It must be hashed using the SHA-256 algorithm.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                              SET_PASSWORD;

    /**
     * Returns the names of all {@link Group}s the user is a member in.
     * Such {@link Group}s are used to set rights for multiple users.
     */
    public static final FunctionDefinition<List<String>>                      GET_GROUPS;

    /**
     * Adds the user as a member to the {@link Group}s with the given names.
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
     * <td>{@link String}...</td>
     * <td>groups</td>
     * <td>The names of the {@link Group}s to add the user to.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalStateException}</td>
     * <td>The user already is a member in the given {@link Group}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                              ADD_TO_GROUPS;

    /**
     * Removes the membership of the user from the {@link Group}s with the given names.
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
     * <td>{@link String}...</td>
     * <td>groups</td>
     * <td>The names of the {@link Group}s to remove the user from.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalStateException}</td>
     * <td>The {@link Group} currently is the primary one (you have to set another {@link Group} as primary first).</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                              REMOVE_FROM_GROUPS;

    /**
     * Returns the name of the primary {@link Group} of the user.
     * The primary {@link Group} is the first {@link Group} in the {@link #GET_GROUPS} list and is used when new rights are applied.
     */
    public static final FunctionDefinition<String>                            GET_PRIMARY_GROUP;

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
    public static final FunctionDefinition<Void>                              SET_PRIMARY_GROUP;

    /**
     * Returns true if the user is a superuser.
     * The superuser of a system can do everything without having the rights applied for doing it.
     */
    public static final FunctionDefinition<Boolean>                           IS_SUPERUSER;

    static {

        GET_NAME = FunctionDefinitionFactory.create("getName", User.class, PropertyAccessorFactory.createGet(NAME));
        SET_NAME = FunctionDefinitionFactory.create("setName", User.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(NAME)), String.class);
        SET_NAME.addExecutor(User.class, "checkNotSuperuser", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            @Lockable
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                if (invocation.getHolder().get(IS_SUPERUSER).invoke()) {
                    // Cancel invocation
                    return null;
                }

                return invocation.next(arguments);
            }

        });

        GET_PASSWORD = FunctionDefinitionFactory.create("getPassword", User.class, PropertyAccessorFactory.createGet(PASSWORD));
        SET_PASSWORD = FunctionDefinitionFactory.create("setPassword", User.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(PASSWORD)), String.class);

        GET_GROUPS = FunctionDefinitionFactory.create("getGroups", User.class, CollectionPropertyAccessorFactory.createGet(GROUPS));
        ADD_TO_GROUPS = FunctionDefinitionFactory.create("addToGroups", User.class, CollectionPropertyAccessorFactory.createAdd(GROUPS), Group[].class);
        ADD_TO_GROUPS.addExecutor(User.class, "checkAllowed", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                Validate.isTrue(!holder.get(IS_SUPERUSER).invoke(), "The superuser can't be a member in any group");
                if (holder.get(GET_GROUPS).invoke().contains(arguments[0])) {
                    throw new IllegalStateException("The user is already a member in that group");
                }

                return invocation.next(arguments);
            }

        });
        REMOVE_FROM_GROUPS = FunctionDefinitionFactory.create("removeFromGroups", User.class, CollectionPropertyAccessorFactory.createRemove(GROUPS), Group[].class);
        REMOVE_FROM_GROUPS.addExecutor(User.class, "checkAllowed", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                Validate.isTrue(!holder.get(IS_SUPERUSER).invoke(), "The superuser can't be a member in any group");
                if (holder.get(GET_PRIMARY_GROUP).invoke().equals(arguments[0])) {
                    throw new IllegalStateException("Can't remove user from its primary group");
                }

                return invocation.next(arguments);
            }

        });

        GET_PRIMARY_GROUP = FunctionDefinitionFactory.create("getPrimaryGroup", User.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                String primaryGroup = null;
                if (holder.get(GET_GROUPS).invoke().size() > 0) {
                    primaryGroup = holder.get(GET_GROUPS).invoke().get(0);
                }

                invocation.next(arguments);
                return primaryGroup;
            }

        });
        SET_PRIMARY_GROUP = FunctionDefinitionFactory.create("setPrimaryGroup", User.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                if (holder.get(GET_GROUPS).invoke().contains(arguments[0])) {
                    holder.get(GROUPS).get().remove(arguments[0]);
                    holder.get(GROUPS).get().add(0, (String) arguments[0]);
                }

                return invocation.next(arguments);
            }

        }, Group.class);

        IS_SUPERUSER = FunctionDefinitionFactory.create("isSuperuser", User.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) throws ExecutorInvocationException {

                boolean result = invocation.getHolder().get(GET_NAME).invoke().equals(SUPERUSER_NAME);
                invocation.next(arguments);
                return result;
            }
        });

        GET_COLUMNS.addExecutor(User.class, "default", new FunctionExecutor<Map<String, Object>>() {

            @Override
            public Map<String, Object> invoke(FunctionInvocation<Map<String, Object>> invocation, Object... arguments) throws ExecutorInvocationException {

                Map<String, Object> columns = new HashMap<String, Object>();
                FeatureHolder holder = invocation.getHolder();

                columns.put("name", holder.get(GET_NAME).invoke());
                columns.put("groups", holder.get(GET_GROUPS).invoke());

                columns.putAll(NullPreventer.prevent(invocation.next(arguments)));
                return columns;
            }

        });
        SET_COLUMNS.addExecutor(User.class, "default", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                // Trust the user of the method
                @SuppressWarnings ("unchecked")
                Map<String, Object> columns = (Map<String, Object>) arguments[0];
                FeatureHolder holder = invocation.getHolder();

                holder.get(SET_NAME).invoke(columns.get("name"));

                Validate.isTrue(columns.get("groups") instanceof List, "Groups must be a list");
                // Trust the user again
                @SuppressWarnings ("unchecked")
                List<String> groups = (List<String>) columns.get("groups");
                if (groups.size() > 0) {
                    for (String group : groups) {
                        holder.get(ADD_TO_GROUPS).invoke(group);
                    }
                    holder.get(SET_PRIMARY_GROUP).invoke(groups.get(0));
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new user.
     */
    public User() {

    }

}
