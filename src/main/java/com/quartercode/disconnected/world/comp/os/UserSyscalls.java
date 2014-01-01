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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.Property;
import com.quartercode.disconnected.mocl.extra.ReturnNextException;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.extra.def.ReferenceProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.program.Process;

/**
 * This class provides syscalls which are used for holding {@link User}s and {@link Group}s.
 * The syscalls can be used on {@link SyscallInvoker} objects.
 * 
 * @see User
 * @see Group
 * @see SyscallInvoker
 */
public abstract class UserSyscalls {

    // ----- OperatingSystem Properties -----

    /**
     * The {@link User}s which are registered on the system.
     * Such {@link User}s are used for managing rights of an individual person.
     * Instances of this {@link Property} should be stored in an {@link OperatingSystem} object.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<User>>>  USERS;

    /**
     * The {@link Group}s which are registered on the system.
     * Such {@link Group}s are used for managing rights of multiple users.
     * Instances of this {@link Property} should be stored in an {@link OperatingSystem} object.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<Group>>> GROUPS;

    static {

        USERS = new AbstractFeatureDefinition<ObjectProperty<Set<User>>>("users") {

            @Override
            public ObjectProperty<Set<User>> create(FeatureHolder holder) {

                return new ObjectProperty<Set<User>>(getName(), holder, new HashSet<User>());
            }

        };

        GROUPS = new AbstractFeatureDefinition<ObjectProperty<Set<Group>>>("groups") {

            @Override
            public ObjectProperty<Set<Group>> create(FeatureHolder holder) {

                return new ObjectProperty<Set<Group>>(getName(), holder, new HashSet<Group>());
            }

        };

    }

    // ----- OperatingSystem Properties End -----

    // ----- SyscallInvoker Functions -----

    /**
     * Returns the {@link User}s which are registered on the system.
     * Such {@link User}s are used for managing rights of an individual person.
     */
    public static final FunctionDefinition<Set<User>>                    GET_USERS;

    /**
     * Returns the superuser of the system.
     * The superuser can do everything without having the rights applied for doing it.
     */
    public static final FunctionDefinition<User>                         GET_SUPERUSER;

    /**
     * Creates and returns a new {@link User} and registers it to the system.
     * Such {@link User}s are used for managing rights of an individual person.
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
     * <td>The name for the new {@link User}.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link MissingRightsException}</td>
     * <td>The {@link SyscallInvoker} the function is used on is bound to a {@link User} which isn't a superuser.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<User>                         ADD_USER;

    /**
     * Unregisters some {@link User}s from the system.
     * Such {@link User}s are used for managing rights of an individual person.
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
     * <td>{@link User}...</td>
     * <td>users</td>
     * <td>The {@link User}s to unregister from the system.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link MissingRightsException}</td>
     * <td>The {@link SyscallInvoker} the function is used on is bound to a {@link User} which isn't a superuser.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                         REMOVE_USERS;

    /**
     * Returns the {@link Group}s which are registered on the system.
     * Such {@link Group}s are used for managing rights of multiple users.
     */
    public static final FunctionDefinition<Set<Group>>                   GET_GROUPS;

    /**
     * Creates and returns a new {@link Group} and registers it to the system.
     * Such {@link Group}s are used for managing rights of multiple users.
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
     * <td>The name for the new {@link Group}.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link MissingRightsException}</td>
     * <td>The {@link SyscallInvoker} the function is used on is bound to a {@link User} which isn't a superuser.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Group>                        ADD_GROUP;

    /**
     * Unregisters some {@link Group}s from the system.
     * Such {@link Group}s are used for managing rights of multiple users.
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
     * <td>{@link Group}...</td>
     * <td>groups</td>
     * <td>The {@link Group}s to unregister from the system.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link MissingRightsException}</td>
     * <td>The {@link SyscallInvoker} the function is used on is bound to a {@link User} which isn't a superuser.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                         REMOVE_GROUPS;

    static {

        GET_USERS = FunctionDefinitionFactory.create("getUsers", SyscallInvoker.class, new FunctionExecutor<Set<User>>() {

            @Override
            public Set<User> invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                Set<User> users = new HashSet<User>();
                for (User user : holder.get(SyscallInvoker.GET_OPERATING_SYSTEM).invoke().get(USERS).get()) {
                    SyscallUser syscallUser = new SyscallUser();
                    syscallUser.get(SyscallUser.INVOKER).set((SyscallInvoker) holder);
                    syscallUser.get(SyscallUser.USER).set(user);
                    users.add(syscallUser);
                }

                return users;
            }

        });

        GET_SUPERUSER = FunctionDefinitionFactory.create("getSuperuser", SyscallInvoker.class, new FunctionExecutor<User>() {

            @Override
            public User invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                for (User user : holder.get(SyscallInvoker.GET_OPERATING_SYSTEM).invoke().get(USERS).get()) {
                    if (user.get(User.IS_SUPERUSER).invoke()) {
                        SyscallUser syscallUser = new SyscallUser();
                        syscallUser.get(SyscallUser.INVOKER).set((SyscallInvoker) holder);
                        syscallUser.get(SyscallUser.USER).set(user);
                        return syscallUser;
                    }
                }

                return null;
            }

        });

        ADD_USER = FunctionDefinitionFactory.create("addUser", SyscallInvoker.class, new FunctionExecutor<User>() {

            @Override
            public User invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                User user = new User();
                user.setLocked(false);
                user.get(User.SET_NAME).invoke(arguments[0]);
                user.setLocked(false);
                holder.get(SyscallInvoker.GET_OPERATING_SYSTEM).invoke().get(USERS).get().add(user);

                SyscallUser syscallUser = new SyscallUser();
                syscallUser.get(SyscallUser.INVOKER).set((SyscallInvoker) holder);
                syscallUser.get(SyscallUser.USER).set(user);
                return syscallUser;
            }
        }, String.class);
        ADD_USER.addExecutor(SyscallInvoker.class, "checkRights", new FunctionExecutor<User>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            public User invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if (holder instanceof Process && ! ((Process<?>) holder).get(Process.GET_USER).invoke().get(User.IS_SUPERUSER).invoke()) {
                    throw new StopExecutionException(new MissingRightsException((Process<?>) holder));
                }

                throw new ReturnNextException();
            }

        });

        REMOVE_USERS = FunctionDefinitionFactory.create("removeUsers", SyscallInvoker.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                for (Object element : arguments) {
                    User user = element instanceof SyscallUser ? ((SyscallUser) element).get(SyscallUser.GET_USER).invoke() : (User) element;
                    holder.get(SyscallInvoker.GET_OPERATING_SYSTEM).invoke().get(USERS).get().remove(user);
                }

                return null;
            }
        }, User[].class);
        REMOVE_USERS.addExecutor(SyscallInvoker.class, "checkRights", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if (holder instanceof Process && ! ((Process<?>) holder).get(Process.GET_USER).invoke().get(User.IS_SUPERUSER).invoke()) {
                    throw new StopExecutionException(new MissingRightsException((Process<?>) holder));
                }

                return null;
            }

        });

        GET_GROUPS = FunctionDefinitionFactory.create("getGroups", SyscallInvoker.class, new FunctionExecutor<Set<Group>>() {

            @Override
            public Set<Group> invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return Collections.unmodifiableSet(holder.get(SyscallInvoker.GET_OPERATING_SYSTEM).invoke().get(GROUPS).get());
            }

        });

        ADD_GROUP = FunctionDefinitionFactory.create("addGroup", SyscallInvoker.class, new FunctionExecutor<Group>() {

            @Override
            public Group invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                Group group = new Group();
                group.setLocked(false);
                group.get(Group.SET_NAME).invoke(arguments[0]);
                group.setLocked(false);
                holder.get(SyscallInvoker.GET_OPERATING_SYSTEM).invoke().get(GROUPS).get().add(group);

                SyscallGroup syscallGroup = new SyscallGroup();
                syscallGroup.get(SyscallGroup.INVOKER).set((SyscallInvoker) holder);
                syscallGroup.get(SyscallGroup.GROUP).set(group);
                return syscallGroup;
            }
        }, String.class);
        ADD_GROUP.addExecutor(SyscallInvoker.class, "checkRights", new FunctionExecutor<Group>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            public Group invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if (holder instanceof Process && ! ((Process<?>) holder).get(Process.GET_USER).invoke().get(User.IS_SUPERUSER).invoke()) {
                    throw new StopExecutionException(new MissingRightsException((Process<?>) holder));
                }

                throw new ReturnNextException();
            }

        });

        REMOVE_GROUPS = FunctionDefinitionFactory.create("removeGroups", SyscallInvoker.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                for (Object element : arguments) {
                    Group group = element instanceof SyscallGroup ? ((SyscallGroup) element).get(SyscallGroup.GET_GROUP).invoke() : (Group) element;
                    holder.get(SyscallInvoker.GET_OPERATING_SYSTEM).invoke().get(GROUPS).get().remove(group);
                }

                return null;
            }
        }, Group[].class);
        REMOVE_GROUPS.addExecutor(SyscallInvoker.class, "checkRights", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if (holder instanceof Process && ! ((Process<?>) holder).get(Process.GET_USER).invoke().get(User.IS_SUPERUSER).invoke()) {
                    throw new StopExecutionException(new MissingRightsException((Process<?>) holder));
                }

                return null;
            }

        });

    }

    // ----- SyscallInvoker Functions End -----

    private UserSyscalls() {

    }

    /**
     * The syscall user is a {@link User} object whose methods which require certain rights have these right checks.
     * 
     * @see User
     */
    public static class SyscallUser extends User {

        // ----- Properties -----

        /**
         * The {@link SyscallInvoker} who created the wrapper.
         */
        protected static final FeatureDefinition<ReferenceProperty<SyscallInvoker>> INVOKER;

        /**
         * The actual {@link User} object the wrapper wraps around.
         */
        protected static final FeatureDefinition<ReferenceProperty<User>>           USER;

        static {

            INVOKER = new AbstractFeatureDefinition<ReferenceProperty<SyscallInvoker>>("invoker") {

                @Override
                public ReferenceProperty<SyscallInvoker> create(FeatureHolder holder) {

                    return new ReferenceProperty<SyscallInvoker>(getName(), holder);
                }

            };

            USER = new AbstractFeatureDefinition<ReferenceProperty<User>>("user") {

                @Override
                public ReferenceProperty<User> create(FeatureHolder holder) {

                    return new ReferenceProperty<User>(getName(), holder);
                }

            };

        }

        // ----- Properties End -----

        // ----- Functions -----

        /**
         * Returns the {@link SyscallInvoker} who created the wrapper.
         */
        public static final FunctionDefinition<SyscallInvoker>                      GET_INVOKER;

        /**
         * Returns the actual {@link User} object the wrapper wraps around.
         * Warning: This function should only be used for creating other wrapper methods!
         */
        public static final FunctionDefinition<User>                                GET_USER;

        static {

            GET_INVOKER = FunctionDefinitionFactory.create("getInvoker", SyscallUser.class, PropertyAccessorFactory.createGet(INVOKER));

            GET_USER = FunctionDefinitionFactory.create("getUser", SyscallUser.class, PropertyAccessorFactory.createGet(USER));

            SyscallUtils.addDelegation(SyscallUser.class, GET_USER, GET_NAME);
            SyscallUtils.addDelegation(SyscallUser.class, GET_USER, SET_NAME);

            SyscallUtils.addDelegation(SyscallUser.class, GET_USER, GET_GROUPS);
            SyscallUtils.addDelegation(SyscallUser.class, GET_USER, ADD_TO_GROUPS);
            SyscallUtils.addDelegation(SyscallUser.class, GET_USER, REMOVE_FROM_GROUPS);

            SyscallUtils.addDelegation(SyscallUser.class, GET_USER, GET_PRIMARY_GROUP);
            SyscallUtils.addDelegation(SyscallUser.class, GET_USER, SET_PRIMARY_GROUP);

            SyscallUtils.addDelegation(SyscallUser.class, GET_USER, IS_SUPERUSER);

            ADD_TO_GROUPS.addExecutor(SyscallUser.class, "checkSyscallPermission", new FunctionExecutor<Void>() {

                @Override
                @Prioritized (Prioritized.LEVEL_8)
                public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                    SyscallInvoker invoker = holder.get(GET_INVOKER).invoke();
                    if (invoker instanceof Process && !invoker.get(Process.GET_USER).invoke().get(User.IS_SUPERUSER).invoke()) {
                        throw new StopExecutionException(new MissingRightsException((Process<?>) invoker));
                    }

                    return null;
                }

            });
            REMOVE_FROM_GROUPS.addExecutor(SyscallUser.class, "checkSyscallPermission", new FunctionExecutor<Void>() {

                @Override
                @Prioritized (Prioritized.LEVEL_8)
                public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                    SyscallInvoker invoker = holder.get(GET_INVOKER).invoke();
                    if (invoker instanceof Process && !invoker.get(Process.GET_USER).invoke().get(User.IS_SUPERUSER).invoke()) {
                        throw new StopExecutionException(new MissingRightsException((Process<?>) invoker));
                    }

                    return null;
                }

            });

            SET_PRIMARY_GROUP.addExecutor(SyscallUser.class, "checkSyscallPermission", new FunctionExecutor<Void>() {

                @Override
                @Prioritized (Prioritized.LEVEL_8)
                public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                    SyscallInvoker invoker = holder.get(GET_INVOKER).invoke();
                    if (invoker instanceof Process) {
                        User user = invoker.get(Process.GET_USER).invoke();
                        if (!user.get(User.IS_SUPERUSER).invoke() && !user.equals(holder)) {
                            throw new StopExecutionException(new MissingRightsException((Process<?>) invoker));
                        }
                    }

                    return null;
                }

            });

        }

        // ----- Functions End -----

        /**
         * Creates a new syscall user wrapper.
         */
        public SyscallUser() {

        }

        @Override
        public int hashCode() {

            return get(USER).get().hashCode();
        }

        @Override
        public boolean equals(Object obj) {

            return get(USER).get().equals(obj);
        }

        @Override
        public String toString() {

            return get(USER).get().toString();
        }

    }

    /**
     * The syscall group is a {@link Group} object whose methods which require certain rights have these right checks.
     * 
     * @see Group
     */
    public static class SyscallGroup extends Group {

        // ----- Properties -----

        /**
         * The {@link SyscallInvoker} who created the wrapper.
         */
        protected static final FeatureDefinition<ReferenceProperty<SyscallInvoker>> INVOKER;

        /**
         * The actual {@link Group} object the wrapper wraps around.
         */
        protected static final FeatureDefinition<ReferenceProperty<Group>>          GROUP;

        static {

            INVOKER = new AbstractFeatureDefinition<ReferenceProperty<SyscallInvoker>>("invoker") {

                @Override
                public ReferenceProperty<SyscallInvoker> create(FeatureHolder holder) {

                    return new ReferenceProperty<SyscallInvoker>(getName(), holder);
                }

            };

            GROUP = new AbstractFeatureDefinition<ReferenceProperty<Group>>("group") {

                @Override
                public ReferenceProperty<Group> create(FeatureHolder holder) {

                    return new ReferenceProperty<Group>(getName(), holder);
                }

            };

        }

        // ----- Properties End -----

        // ----- Functions -----

        /**
         * Returns the {@link SyscallInvoker} who created the wrapper.
         */
        public static final FunctionDefinition<SyscallInvoker>                      GET_INVOKER;

        /**
         * Returns the actual {@link Group} object the wrapper wraps around.
         * Warning: This function should only be used for creating other wrapper methods!
         */
        public static final FunctionDefinition<Group>                               GET_GROUP;

        static {

            GET_INVOKER = FunctionDefinitionFactory.create("getInvoker", SyscallGroup.class, PropertyAccessorFactory.createGet(INVOKER));

            GET_GROUP = FunctionDefinitionFactory.create("getGroup", SyscallGroup.class, PropertyAccessorFactory.createGet(GROUP));

            SyscallUtils.addDelegation(SyscallGroup.class, GET_GROUP, GET_NAME);
            SyscallUtils.addDelegation(SyscallGroup.class, GET_GROUP, SET_NAME);

        }

        // ----- Functions End -----

        /**
         * Creates a new syscall group wrapper.
         */
        public SyscallGroup() {

        }

        @Override
        public int hashCode() {

            return get(GROUP).get().hashCode();
        }

        @Override
        public boolean equals(Object obj) {

            return get(GROUP).get().equals(obj);
        }

        @Override
        public String toString() {

            return get(GROUP).get().toString();
        }

    }

}
