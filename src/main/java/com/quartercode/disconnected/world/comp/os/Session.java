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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.Validate;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.disconnected.world.comp.program.ChildProcess;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;

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

        USER = ReferenceProperty.createDefinition("user");
        PASSWORD = ObjectProperty.createDefinition("password");

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("checkUser", Session.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7 + Prioritized.SUBLEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Validate.notNull(invocation.getHolder().get(USER).get(), "Session user cannot be null");

                return invocation.next(arguments);
            }

        });

        RUN.addExecutor("checkPassword", Session.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7 + Prioritized.SUBLEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                // Determine whether a check is required (parent session != null or parent session user != root)
                Session parentSession = ((Session) holder).getParent().get(Process.GET_SESSION).invoke();
                boolean checkRequired = parentSession != null && !parentSession.get(USER).get().get(User.IS_SUPERUSER).invoke();

                if (checkRequired) {
                    String password = holder.get(PASSWORD).get();
                    Validate.notNull(password, "Password for session user cannot be null (authorization required)");
                    String hashedPassword = DigestUtils.sha256Hex(password);

                    String correctPassword = holder.get(USER).get().get(User.PASSWORD).get();
                    Validate.isTrue(correctPassword.equals(hashedPassword), "Wrong password (authorization required)");
                }

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new session program.
     */
    public Session() {

    }

}
