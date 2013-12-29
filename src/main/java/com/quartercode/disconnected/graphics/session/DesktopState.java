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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.quartercode.disconnected.graphics.component.GraphicsState;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.session.DesktopSessionProgram.DesktopSession;
import com.quartercode.disconnected.world.member.Member;
import com.quartercode.disconnected.world.member.ai.PlayerController;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

/**
 * The desktop state renders the desktop of the local player to the graphics manager.
 * 
 * @see DesktopWidget
 */
public class DesktopState extends GraphicsState {

    private static final Logger LOGGER = Logger.getLogger(DesktopState.class.getName());

    private final Simulation    simulation;

    private Widget              widget;

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
            Member localPlayer = null;
            for (Member member : simulation.getWorld().get(World.GET_MEMBERS).invoke()) {
                if (member.getAiController() instanceof PlayerController && ((PlayerController) member.getAiController()).isLocal()) {
                    localPlayer = member;
                    break;
                }
            }
            if (localPlayer != null) {
                OperatingSystem os = localPlayer.getComputer().get(Computer.GET_OS).invoke();
                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("user", os.getUserManager().getUsers().get(0).getName());
                Process<?> process = os.getProcessManager().getRootProcess().get(Process.CREATE_CHILD).invoke();
                process.setLocked(false);
                process.get(Process.SET_SOURCE).invoke(os.getFileSystemManager().getFile("/system/bin/desktops.exe"));
                process.get(Process.LAUNCH).invoke(arguments);
                process.setLocked(true);
                widget = ((DesktopSession) process.get(Process.GET_EXECUTOR).invoke()).createWidget();
                add(widget);
            }
        }
        catch (FunctionExecutionException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during desktop graphic state initialization", e);
        }
    }

}
