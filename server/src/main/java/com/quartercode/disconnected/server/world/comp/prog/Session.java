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

package com.quartercode.disconnected.server.world.comp.prog;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang3.StringUtils;
import com.quartercode.disconnected.server.world.comp.os.config.User;

/**
 * This class represents a program which opens a session with a certain {@link User}; other programs can run under that user by being a child of the session process.
 * For opening an actual session, you just have to create a process using this with these arguments:
 *
 * <ul>
 * <li>user: The name of the user the session should be running under.</li>
 * <li>password: The password of the given user. Can be {@code null} if the parent session is null or a root session.</li>
 * </ul>
 */
public class Session extends ProgramExecutor {

    // TODO: There needs to be a way of constructing sessions

    @XmlAttribute
    @XmlIDREF
    private User   user;
    @XmlAttribute
    private String password;

    /**
     * Returns the {@link User} the session is running under.
     * Every child {@link Process} of the session can use the rights provided by the session.
     *
     * @return The user who "uses" this session.
     */
    public User getUser() {

        return user;
    }

    /**
     * Returns the password which has been input by the creator of the session for the set {@link #getUser() user}.
     * It is used to verify that opening the new session is authorized.
     * Note that it can be {@code null} if the parent session is {@code null} or if the parent session's user is the {@link User#isSuperuser() superuser}.
     * That makes sense because the superuser is allowed to do everything, including opening new sessions.<br>
     * <br>
     * Important: This password is supplied in <b>hashed form</b>!
     *
     * @return The presumed password of the session user.
     */
    public String getPassword() {

        return password;
    }

    @Override
    public void run() {

        Process<?> process = getSingleParent();

        if (!checkPassword()) {
            process.interrupt(true);
            process.stop();
        }
    }

    private boolean checkPassword() {

        // Determine whether a check is required (parentSession != null and parentSession.user != superuser)
        Session parentSession = getSingleParent().getSession();
        boolean checkRequired = parentSession != null && !parentSession.getUser().isSuperuser();

        // If a check is required, verify the password
        if (checkRequired && (StringUtils.isBlank(password) || !password.equals(user.getPassword()))) {
            // Wrong password
            return false;
        } else {
            return true;
        }
    }

}
