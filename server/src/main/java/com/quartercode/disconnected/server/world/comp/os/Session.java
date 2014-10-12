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

package com.quartercode.disconnected.server.world.comp.os;

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.server.util.HashUtil;
import com.quartercode.disconnected.server.world.comp.program.ChildProcess;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.shared.event.comp.program.ServerProcessCommand;

/**
 * This class represents a program which opens a session.
 * For opening an actual session, you just have to create a process using this with these arguments:
 * 
 * <ul>
 * <li>user: The name of the {@link User} the session should be running under.</li>
 * <li>password: The password of the given {@link User}. Can be null if the parent session is null or a root session.</li>
 * </ul>
 */
public class Session extends ProgramExecutor {

    // ----- Properties -----

    /**
     * The {@link User} the session is running under.
     * Every {@link ChildProcess} of the session can use the rights provided by the session.
     */
    public static final PropertyDefinition<User>   USER;

    /**
     * The password of the set {@link #USER}.
     * It is used to verify that opening the new session is authorized and can be null if the parent session is null or a root session.
     * This password must be supplied in <b>raw form</b> and may not be hashed!
     */
    public static final PropertyDefinition<String> PASSWORD;

    static {

        USER = create(new TypeLiteral<PropertyDefinition<User>>() {}, "name", "user", "storage", new ReferenceStorage<>());
        PASSWORD = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "password", "storage", new StandardStorage<>());

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("checkUser", Session.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7 + Prioritized.SUBLEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Validate.notNull(invocation.getCHolder().getObj(USER), "Session user cannot be null");

                return invocation.next(arguments);
            }

        });

        RUN.addExecutor("checkPassword", Session.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7 + Prioritized.SUBLEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Session holder = (Session) invocation.getCHolder();

                // Determine whether a check is required (parent session != null or parent session user != root)
                Session parentSession = holder.getParent().invoke(Process.GET_SESSION);
                boolean checkRequired = parentSession != null && !parentSession.getObj(USER).invoke(User.IS_SUPERUSER);

                if (checkRequired && holder.getObj(USER).getObj(User.PASSWORD) != null) {
                    String password = holder.getObj(PASSWORD);
                    if (password == null) {
                        fireWrongPasswordEvent(holder, password);
                        return null;
                    }
                    String hashedPassword = HashUtil.sha256(password);

                    String correctPassword = holder.getObj(USER).getObj(User.PASSWORD);
                    if (!correctPassword.equals(hashedPassword)) {
                        fireWrongPasswordEvent(holder, password);
                        return null;
                    }
                }

                return invocation.next(arguments);
            }

            private void fireWrongPasswordEvent(Session holder, String wrongPassword) {

                String computerId = holder.getParent().invoke(Process.GET_OPERATING_SYSTEM).getParent().getId();
                int pid = holder.getParent().getObj(Process.PID);
                holder.getBridge().send(new WrongPasswordEvent(computerId, pid, wrongPassword));
            }

        });

        RUN.addExecutor("fireFinishStart", Session.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Session holder = (Session) invocation.getCHolder();
                String computerId = holder.getParent().invoke(Process.GET_OPERATING_SYSTEM).getParent().getId();
                int pid = holder.getParent().getObj(Process.PID);
                holder.getBridge().send(new FinishStartEvent(computerId, pid));

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Session events are events that are fired by the {@link Session} program.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>Session.</b>SessionEvent )
     * </pre>
     * 
     * @see Session
     */
    public static class SessionEvent extends WorldProcessCommand {

        /**
         * Creates a new session event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public SessionEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The success event is fired by the {@link Session} when the startup process is finished.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>Session.</b>FinishStartEvent )
     * </pre>
     * 
     * @see Session
     */
    public static class FinishStartEvent extends SessionEvent {

        /**
         * Creates a new session finish start event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public FinishStartEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * The wrong password event is fired by the {@link Session} when a password is required and the provided password is not correct.<br>
     * <br>
     * Please note that all program events should be used through their program classes in order to prevent name collisions from happening.
     * For example, an instanceof check should look like this:
     * 
     * <pre>
     * if ( event instanceof <b>Session.</b>WrongPasswordEvent )
     * </pre>
     * 
     * @see Session
     */
    public static class WrongPasswordEvent extends SessionEvent {

        private final String wrongPassword;

        /**
         * Creates a new session wrong password event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param wrongPassword The provided password which is not correct (may be {@code null}).
         */
        public WrongPasswordEvent(String computerId, int pid, String wrongPassword) {

            super(computerId, pid);
            this.wrongPassword = wrongPassword;
        }

        /**
         * Returns the provided password which is not correct (may be {@code null}).
         * 
         * @return The wrong password.
         */
        public String getWrongPassword() {

            return wrongPassword;
        }

    }

}
