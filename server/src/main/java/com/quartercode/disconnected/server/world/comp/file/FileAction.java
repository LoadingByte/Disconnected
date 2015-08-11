/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

import java.util.Map;
import com.quartercode.disconnected.server.world.comp.user.User;

/**
 * File actions are predefined "plans" of activities that are related to {@link File}s, for example moving a file.
 * They are basically a usage of the command pattern.
 * However, all actions provide two methods related to the activities they are representing.<br>
 * <br>
 * The first method is called {@link #execute()}.
 * It is just executing the defined action without doing anything else.
 * For example, a file movement action would actually move the file when it is executed.
 * Note that no right checks or anything like that are done by this method.<br>
 * <br>
 * The second method is called {@link #isExecutableBy(User)}.
 * It takes a {@link User} as argument and checks whether the user is allowed to execute the action under the current circumstances.
 * Every program implementation is responsible for checking whether a file action can be actually executed before actually executing it.
 * The {@link #execute()} method should only be called after the check passed.<br>
 * <br>
 * The third method is called {@link #getMissingRights(User)}.
 * If {@link #isExecutableBy(User)} is {@code false}, this method returns the files the user has insufficient access to.
 * Moreover, a char array containing the missing rights is returned alongside each file.
 * If {@link #isExecutableBy(User)} is {@code true}, this method just returns an empty map.
 *
 * @see File
 */
public abstract class FileAction {

    /**
     * Actually executes the defined action without doing anything else.
     * For example, a file movement action would simply do the file movement when it is executed.<br>
     * <br>
     * Note that no right checks or anything like that are done by this method.
     * If you need such permission checks, use {@link #isExecutableBy(User)} or {@link #getMissingRights(User)}.
     *
     * @throws Exception If some kind of exception occurs while executing the action.
     */
    public abstract void execute() throws Exception;

    /**
     * Takes a {@link User} and checks whether he allowed to execute the action under the current circumstances.<br>
     * <br>
     * Every program implementation is responsible for checking whether a file action can be actually executed before actually executing it.
     * The {@link #execute()} method should only be called after the check passed.
     *
     * @param user The user whose permission to execute the action should be checked.
     * @return Whether the given user is allowed to execute the action.
     */
    public boolean isExecutableBy(User user) {

        // By default, this method just checks whether the map returned by getMissingRights is empty
        return getMissingRights(user).isEmpty();
    }

    /**
     * If {@link #isExecutableBy(User)} is {@code false}, this method returns the files the user has insufficient access to.
     * Moreover, a char array containing the missing rights is returned alongside each file.<br>
     * If {@link #isExecutableBy(User)} is {@code true}, this method just returns an empty map.
     *
     * @param user The user whose missing rights for the action should be retrieved.
     * @return The files the given user has insufficient access to.
     */
    public abstract Map<File<?>, Character[]> getMissingRights(User user);

}
