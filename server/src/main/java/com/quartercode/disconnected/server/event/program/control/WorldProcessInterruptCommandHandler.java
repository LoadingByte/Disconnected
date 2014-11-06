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

package com.quartercode.disconnected.server.event.program.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.sim.profile.ProfileService;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.shared.comp.program.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.event.program.control.WorldProcessInterruptCommand;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.util.ServiceRegistry;

/**
 * The world process interrupt command handler executes incoming {@link WorldProcessInterruptCommand} events.
 * 
 * @see WorldProcessInterruptCommand
 */
public class WorldProcessInterruptCommandHandler implements SBPAwareEventHandler<WorldProcessInterruptCommand> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldProcessInterruptCommandHandler.class);

    @Override
    public void handle(WorldProcessInterruptCommand event, SBPIdentity sender) {

        // Retrieve the process
        Process<?> process = getSBPProcess(sender, event.getWorldPid());

        // Check the world process user
        // That check ensures that the SBP does not stop processes it shouldn't stop on its own computer (e.g. the root process)
        SBPWorldProcessUserId processUser = process.getObj(Process.WORLD_PROCESS_USER);
        if (processUser == null || !processUser.getSBP().equals(sender)) {
            LOGGER.warn("SBP '{}' tried to interrupt process with pid {} although he doesn't own it", sender, event.getWorldPid());
            return;
        }

        // Interrupt the process
        process.invoke(Process.INTERRUPT, event.isRecursive());
    }

    protected Process<?> getSBPProcess(SBPIdentity sbp, int pid) {

        World world = ServiceRegistry.lookup(ProfileService.class).getActive().getWorld();
        // Just use first available computer as the player's one
        Computer computer = world.getColl(World.COMPUTERS).get(0);

        for (Process<?> process : computer.getObj(Computer.OS).getObj(OperatingSystem.PROC_MODULE).invoke(ProcessModule.GET_ALL)) {
            if (process.getObj(Process.PID) == pid) {
                return process;
            }
        }

        return null;
    }

}
