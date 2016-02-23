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

package com.quartercode.disconnected.server.world.comp.proc;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang3.StringUtils;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.comp.proc.prog.StaticTask;
import com.quartercode.disconnected.server.world.comp.proc.task.exec.TaskProcess;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessState;

/**
 * This class represents a program which opens a session with a certain {@link User}; other programs can run under that user by being a child of the session process.
 * For opening an actual session, you just have to create a process using this with these arguments:
 *
 * <ul>
 * <li>{@code user}: The {@link User} object the session should be running under.</li>
 * <li>{@code password}: The <b>hashed</b> password of the given user. Can be {@code null} if the parent session is null or a root session.</li>
 * </ul>
 */
public class Session extends StaticTask implements ProcessStateListener {

    // ----- Interface -----

    @InputParameter
    @XmlAttribute
    @XmlIDREF
    private User   user;
    @InputParameter
    @XmlAttribute
    private String password;

    @Callback
    private void success() {

        callback("success");
    }

    @Callback
    private void wrongPassword() {

        callback("wrongPassword");
    }

    // ----- Logic -----

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
     * Returns the <b>hashed</b> password which has been input by the creator of the session for the set {@link #getUser() user}.
     * It is used to verify that opening the new session is authorized.
     * Note that it can be {@code null} if the parent session is {@code null} or if the parent session's user is the {@link User#isSuperuser() superuser}.
     * That makes sense because the superuser is allowed to do everything, including opening new sessions.
     *
     * @return The presumed password of the session user in hashed form.
     */
    public String getPassword() {

        return password;
    }

    @Override
    public void run() {

        if (!checkPassword()) {
            getProcess().interrupt();
            wrongPassword();
            return;
        }

        success();
    }

    private boolean checkPassword() {

        // Determine whether a check is required (parentSession != null and parentSession.user != superuser)
        Session parentSession = getProcess().getSession();
        boolean checkRequired = parentSession != null && !parentSession.getUser().isSuperuser();

        // If a check is required, verify the password
        if (checkRequired && (StringUtils.isBlank(password) || !password.equals(user.getPassword()))) {
            // Wrong password
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onStateChange(Process process, WorldProcessState oldState, WorldProcessState newState) {

        if (newState == WorldProcessState.INTERRUPTED) {
            for (Process childProcess : process.getChildProcesses()) {
                // This listener stops the session process as soon as all child processes have been stopped
                childProcess.addStateListener(new InformSessionProcessStateListener());

                // Mark the session process as interrupted
                childProcess.interrupt();
            }
        }
    }

    private static class InformSessionProcessStateListener implements ProcessStateListener {

        @Override
        public void onStateChange(Process process, WorldProcessState oldState, WorldProcessState newState) {

            if (newState == WorldProcessState.STOPPED) {
                TaskProcess sessionProcess = process.getSessionProcess();

                if (!sessionProcess.hasActiveChildProcesses()) {
                    sessionProcess.stop();
                }
            }
        }

    }

}
