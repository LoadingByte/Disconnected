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

package com.quartercode.disconnected.graphics.session;

import com.quartercode.disconnected.sim.comp.session.Shell;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

/**
 * A shell widget can display the output of a shell.
 * You can also input commands using a shell widget.
 * 
 * @see Shell
 */
public class ShellWidget extends Widget {

    private Shell shell;

    /**
     * Creates a new shell widget and sets it up.
     * 
     * @param shell The shell to use for the widget.
     */
    public ShellWidget(Shell shell) {

        this.shell = shell;

        setTheme("");
    }

    /**
     * Returns the shell this widget is rendering.
     * 
     * @return The shell this widget is rendering.
     */
    public Shell getShell() {

        return shell;
    }

    // Nothing here yet

    /**
     * Returns true if the shell widget already stopped displaying the session's content and is closed.
     * 
     * @return True if the shell widget is already closed.
     */
    public boolean isClosed() {

        return shell == null;
    }

    /**
     * Closes the shell widget and stops displaying the session's content.
     */
    public void close() {

        if (!isClosed()) {
            // TODO: Close anything we display
            shell = null;
            shell.getHost().closeWidget(this);
        }
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {

        close();
    }

}
