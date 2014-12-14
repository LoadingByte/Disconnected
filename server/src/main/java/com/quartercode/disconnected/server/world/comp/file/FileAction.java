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

package com.quartercode.disconnected.server.world.comp.file;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.Map;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;

/**
 * File actions are predefined "plans" of activities that are related to {@link File}s, for example moving a file.
 * They are basically a usage of the command pattern.
 * However, all actions provide two methods related to the activities they are representing.<br>
 * <br>
 * The first method is called {@link #EXECUTE}.
 * It is just executing the defined action without doing anything else.
 * For example, a file movement action would actually move the file here.<br>
 * <br>
 * The second method is called {@link #IS_EXECUTABLE_BY}.
 * It takes a {@link User} as argument and checks whether the user is allowed to execute the action under the current circumstances.
 * Every program implementation is responsible for checking whether a file action can be actually executed.
 * The {@link #EXECUTE} method should only be called after the check passed.
 * 
 * @see File
 */
public abstract class FileAction extends WorldFeatureHolder {

    /**
     * Actually executes the defined action without doing anything else.
     * For example, a file movement action would simply do the file movement here.
     */
    public static final FunctionDefinition<Void>                      EXECUTE            = factory(FunctionDefinitionFactory.class).create("execute", new Class[0]);

    /**
     * Takes a {@link User} and checks whether the user is allowed to execute the action under the current circumstances.<br>
     * Every program implementation is responsible for checking whether a file action can be actually executed.
     * The {@link #EXECUTE} method should only be called after the check passed.
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
     * <td>{@link User}</td>
     * <td>user</td>
     * <td>The {@link User} whose permission to execute the action should be checked.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Boolean>                   IS_EXECUTABLE_BY   = factory(FunctionDefinitionFactory.class).create("isExecutableBy", new Class[] { User.class });

    /**
     * If {@link #IS_EXECUTABLE_BY} is {@code false}, this method returns the files the user has insufficient access to.
     * Moreover, a char array containing the missing rights is returned alongside each file.
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
     * <td>{@link User}</td>
     * <td>user</td>
     * <td>The {@link User} whose missing rights for the action should be retrieved.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Map<File<?>, Character[]>> GET_MISSING_RIGHTS = factory(FunctionDefinitionFactory.class).create("getMissingRights", new Class[] { User.class });

    static {

        // By default, IS_EXECUTABLE_BY just checks whether the GET_MISSING_RIGHTS map is empty
        IS_EXECUTABLE_BY.addExecutor("default", FileAction.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                boolean result = invocation.getCHolder().invoke(GET_MISSING_RIGHTS, arguments[0]).isEmpty();

                invocation.next(arguments);
                return result;
            }

        });

    }

}
