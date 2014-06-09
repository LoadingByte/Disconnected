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

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.ValueSupplierDefinition;
import com.quartercode.classmod.extra.storage.ReferenceCollectionStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
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
    public static final String                                           SUPERUSER_NAME = "root";

    // ----- Properties -----

    /**
     * The name of the user.
     * The name is used for recognizing a user on the os-level.
     */
    public static final PropertyDefinition<String>                       NAME;

    /**
     * The hashed password of the user.
     * It is hashed using the SHA-256 algorithm and can be used to authenticate as the user.
     */
    public static final PropertyDefinition<String>                       PASSWORD;

    /**
     * All {@link Group}s the user is a member in.
     * Such groups are used to set rights for multiple users.<br>
     * <br>
     * Exceptions that can occur when adding:
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The user the function is invoked on is a superuser.</td>
     * </tr>
     * </table>
     * 
     * <br>
     * Exceptions that can occur when removing:
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The user the function is invoked on is a superuser.</td>
     * </tr>
     * <tr>
     * <td>{@link IllegalStateException}</td>
     * <td>The group for removal is the primary group of the user.</td>
     * </tr>
     * </table>
     */
    public static final CollectionPropertyDefinition<Group, List<Group>> GROUPS;

    static {

        NAME = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "name", "storage", new StandardStorage<>());
        NAME.addSetterExecutor("checkNotSuperuser", User.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (invocation.getHolder().get(IS_SUPERUSER).invoke()) {
                    // Cancel invocation
                    return null;
                }

                return invocation.next(arguments);
            }

        });

        PASSWORD = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "password", "storage", new StandardStorage<>());

        GROUPS = create(new TypeLiteral<CollectionPropertyDefinition<Group, List<Group>>>() {}, "name", "groups", "storage", new ReferenceCollectionStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));
        GROUPS.addAdderExecutor("checkNotSuperuser", User.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Validate.isTrue(!invocation.getHolder().get(IS_SUPERUSER).invoke(), "The superuser can't be a member in any group");
                return invocation.next(arguments);
            }

        });
        GROUPS.addRemoverExecutor("checkNotSuperuser", User.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Validate.isTrue(!invocation.getHolder().get(IS_SUPERUSER).invoke(), "The superuser can't be a member in any group");
                return invocation.next(arguments);
            }

        });
        GROUPS.addRemoverExecutor("checkNotPrimaryGroup", User.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (invocation.getHolder().get(GET_PRIMARY_GROUP).invoke().equals(arguments[0])) {
                    throw new IllegalStateException("Can't remove user from its primary group");
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    /**
     * Returns the name of the primary {@link Group} of the user.
     * The primary group is the first group in the {@link #GROUPS} list and is used when new rights are applied.
     */
    public static final FunctionDefinition<Group>                        GET_PRIMARY_GROUP;

    /**
     * Changes the primary {@link Group} of the user to the one which has the given name.
     * The user must already be a member of the group.
     * The primary group is the first group in the {@link #GROUPS} list and is used when new rights are applied.
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
     * <td>{@link Group}</td>
     * <td>primaryGroup</td>
     * <td>The name of the new primary group of the user.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                         SET_PRIMARY_GROUP;

    /**
     * Returns true if the user is a superuser.
     * The superuser of a system can do everything without having the rights applied for doing it.
     */
    public static final FunctionDefinition<Boolean>                      IS_SUPERUSER;

    static {

        GET_PRIMARY_GROUP = create(new TypeLiteral<FunctionDefinition<Group>>() {}, "name", "getPrimaryGroup", "parameters", new Class<?>[0]);
        GET_PRIMARY_GROUP.addExecutor("default", User.class, new FunctionExecutor<Group>() {

            @Override
            public Group invoke(FunctionInvocation<Group> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                Group primaryGroup = null;
                if (holder.get(GROUPS).get().size() > 0) {
                    primaryGroup = holder.get(GROUPS).get().get(0);
                }

                invocation.next(arguments);
                return primaryGroup;
            }

        });
        SET_PRIMARY_GROUP = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "setPrimaryGroup", "parameters", new Class<?>[] { Group.class });
        SET_PRIMARY_GROUP.addExecutor("default", User.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                Group primaryGroup = (Group) arguments[0];

                if (holder.get(GROUPS).get().contains(primaryGroup)) {
                    // Put the new primary group at the front of the list
                    List<Group> groups = holder.get(GROUPS).get();
                    groups.remove(primaryGroup);

                    for (Group group : groups) {
                        holder.get(GROUPS).remove(group);
                    }
                    for (Group group : groups) {
                        holder.get(GROUPS).add(group);
                    }
                }

                return invocation.next(arguments);
            }

        });

        IS_SUPERUSER = create(new TypeLiteral<FunctionDefinition<Boolean>>() {}, "name", "isSuperuser", "parameters", new Class<?>[0]);
        IS_SUPERUSER.addExecutor("default", User.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                String name = invocation.getHolder().get(NAME).get();
                boolean result = name != null && name.equals(SUPERUSER_NAME);
                invocation.next(arguments);
                return result;
            }
        });

        GET_COLUMNS.addExecutor("name", User.class, new FunctionExecutor<Map<ValueSupplierDefinition<?, ?>, Class<?>>>() {

            @Override
            public Map<ValueSupplierDefinition<?, ?>, Class<?>> invoke(FunctionInvocation<Map<ValueSupplierDefinition<?, ?>, Class<?>>> invocation, Object... arguments) {

                Map<ValueSupplierDefinition<?, ?>, Class<?>> columns = NullPreventer.prevent(invocation.next(arguments));
                columns.put(NAME, String.class);
                return columns;
            }

        });
        GET_COLUMNS.addExecutor("password", User.class, new FunctionExecutor<Map<ValueSupplierDefinition<?, ?>, Class<?>>>() {

            @Override
            public Map<ValueSupplierDefinition<?, ?>, Class<?>> invoke(FunctionInvocation<Map<ValueSupplierDefinition<?, ?>, Class<?>>> invocation, Object... arguments) {

                Map<ValueSupplierDefinition<?, ?>, Class<?>> columns = NullPreventer.prevent(invocation.next(arguments));
                columns.put(PASSWORD, String.class);
                return columns;
            }

        });
        GET_COLUMNS.addExecutor("groups", User.class, new FunctionExecutor<Map<ValueSupplierDefinition<?, ?>, Class<?>>>() {

            @Override
            public Map<ValueSupplierDefinition<?, ?>, Class<?>> invoke(FunctionInvocation<Map<ValueSupplierDefinition<?, ?>, Class<?>>> invocation, Object... arguments) {

                Map<ValueSupplierDefinition<?, ?>, Class<?>> columns = NullPreventer.prevent(invocation.next(arguments));
                columns.put(GROUPS, Group.class);
                return columns;
            }

        });

    }

    /**
     * Creates a new user.
     */
    public User() {

    }

}
