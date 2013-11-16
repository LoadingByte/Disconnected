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

import java.util.HashMap;
import java.util.Map;
import com.quartercode.disconnected.graphics.component.GraphicsState;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.program.ArgumentException;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.WrongSessionTypeException;
import com.quartercode.disconnected.world.comp.session.DesktopSessionProgram.DesktopSession;
import com.quartercode.disconnected.world.general.RootObject;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

/**
 * The desktop state renders the desktop of the local player to the graphics manager.
 * 
 * @see DesktopWidget
 */
public class DesktopState extends GraphicsState {

    private final Simulation simulation;

    private Widget           widget;

    /**
     * Creates a new desktop state and sets it up.
     * 
     * @param simulation The simulation whose local player's desktop is rendered.
     */
    public DesktopState(Simulation simulation) {

        this.simulation = simulation;

        setTheme("");
    }

    /**
     * Returns the simulation whose local player's desktop is rendered.
     * 
     * @return The simulation whose local player's desktop is rendered.
     */
    public Simulation getSimulation() {

        return simulation;
    }

    @Override
    protected void layout() {

        widget.setSize(getParent().getWidth(), getParent().getHeight());
    }

    @Override
    protected void afterAddToGUI(GUI gui) {

        try {
            // TODO: Boot the computer & listen for new session (shell or desktop).
            // Open a new desktop session (temp)
            OperatingSystem os = simulation.getWorld().getRoot().get(RootObject.MEMBERS).getLocalPlayer().getComputer().get(Computer.OS).get();
            Map<String, Object> arguments = new HashMap<String, Object>();
            arguments.put("user", os.getUserManager().getUsers().get(0).getName());
            Process process = os.getProcessManager().getRootProcess().createChild(os.getFileSystemManager().getFile("/system/bin/desktops.exe"), arguments);
            widget = ((DesktopSession) process.getExecutor()).createWidget();
            add(widget);
        }
        catch (WrongSessionTypeException e) {
            // Wont ever happen
        }
        catch (ArgumentException e) {
            // Wont ever happen
        }
    }

}
