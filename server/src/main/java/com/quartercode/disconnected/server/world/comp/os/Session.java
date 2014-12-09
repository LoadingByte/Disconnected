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

import static com.quartercode.classmod.extra.func.Priorities.*;
import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.util.HashUtils;
import com.quartercode.disconnected.server.world.comp.os.user.User;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;

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

        USER = factory(PropertyDefinitionFactory.class).create("user", new ReferenceStorage<>());
        PASSWORD = factory(PropertyDefinitionFactory.class).create("password", new StandardStorage<>());

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("checkUser", Session.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Validate.notNull(invocation.getCHolder().getObj(USER), "Session user cannot be null");

                return invocation.next(arguments);
            }

        }, LEVEL_7 + SUBLEVEL_7);

        RUN.addExecutor("checkPassword", Session.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Session holder = (Session) invocation.getCHolder();

                // Determine whether a check is required (parent session != null or parent session user != root)
                Session parentSession = holder.getParent().invoke(Process.GET_SESSION);
                boolean checkRequired = parentSession != null && !parentSession.getObj(USER).invoke(User.IS_SUPERUSER);

                if (checkRequired && holder.getObj(USER).getObj(User.PASSWORD) != null) {
                    String password = holder.getObj(PASSWORD);
                    if (password == null) {
                        // Wrong password
                        return null;
                    }
                    String hashedPassword = HashUtils.sha256(password);

                    String correctPassword = holder.getObj(USER).getObj(User.PASSWORD);
                    if (!correctPassword.equals(hashedPassword)) {
                        // Wrong password
                        return null;
                    }
                }

                return invocation.next(arguments);
            }

        }, LEVEL_7 + SUBLEVEL_5);

    }

}
