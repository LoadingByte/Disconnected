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

package com.quartercode.disconnected.world.comp.program.desktop;

import com.quartercode.disconnected.world.comp.net.PacketListener;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.session.Desktop.Window;
import com.quartercode.disconnected.world.comp.session.DesktopSessionProgram.DesktopSession;
import com.quartercode.disconnected.world.comp.session.SessionProgram.Session;

/**
 * This abstract class defines a desktop program executor which takes care of acutally running a gui program.
 * Programs using a desktop program executor can only be used in desktop sessions.
 * The executor class is set in the program.
 * 
 * @see Program
 * @see Process
 * @see PacketListener
 */
public abstract class DesktopProgramExecutor extends ProgramExecutor {

    /**
     * Creates a new desktop program executor.
     * 
     * @param host The host process which uses the created executor for running the program instance.
     */
    public DesktopProgramExecutor(Process host) {

        super(host);
    }

    /**
     * Opens a new already created window on the host's desktop.
     * Throws an exception if the host process isn't running under a desktop session.
     * 
     * @param window The window to open on the host's desktop.
     * @throws IllegalStateException The host process isn't running under a desktop session.
     */
    protected void openWindow(Window<?> window) {

        Session session = getHost().getSession();
        if (session instanceof DesktopSession) {
            ((DesktopSession) session).getDesktop().addWindow(window);
        } else {
            throw new IllegalStateException("The host process is running under " + session.toInfoString() + "; desktop session needed");
        }
    }

}
