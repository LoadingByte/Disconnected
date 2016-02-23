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

package com.quartercode.disconnected.server.event.prog.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.sim.TickBridgeProvider;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.TickWorldUpdater;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchAcknowledgmentEvent;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.proc.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessId;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * The world process launch command handler executes incoming {@link WorldProcessLaunchCommand} events.
 *
 * @see WorldProcessLaunchCommand
 */
public class WorldProcessLaunchCommandHandler implements SBPAwareEventHandler<WorldProcessLaunchCommand> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldProcessLaunchCommandHandler.class);

    @Override
    public void handle(WorldProcessLaunchCommand event, SBPIdentity sender) {

        Computer playerComputer = getSBPComputer(sender);

        // Get the source file
        ContentFile source;
        try {
            source = getSourceFile(playerComputer, event.getProgramName());
        } catch (UnknownMountpointException | InvalidPathException e) {
            // Cancel if the program cannot be found
            LOGGER.warn("Cannot find program '{}' for launching it (commanded by SBP '{}')", event.getProgramName(), sender);
            return;
        }

        Process<?> sessionProcess = getSessionProcess(playerComputer);
        SBPWorldProcessUserId worldProcessUser = new SBPWorldProcessUserId(sender, event.getWorldProcessUserDetails());

        // Create a new process
        Process<?> process;
        try {
            process = sessionProcess.launchChild(source, worldProcessUser);
        } catch (Exception e) {
            // Cancel if the process cannot be launched
            LOGGER.warn("Error while initializing process for program '{}' (commanded by SBP '{}')", event.getProgramName(), sender, e);
            return;
        }

        // Send an acknowledgment event
        getBridge().send(new WorldProcessLaunchAcknowledgmentEvent(worldProcessUser, getProcessId(process)));
    }

    protected Bridge getBridge() {

        return ServiceRegistry.lookup(TickService.class).getAction(TickBridgeProvider.class).getBridge();
    }

    protected Computer getSBPComputer(SBPIdentity sbp) {

        World world = ServiceRegistry.lookup(TickService.class).getAction(TickWorldUpdater.class).getWorld();
        // Just use first available computer as the player's one
        return world.getComputers().get(0);
    }

    protected Process<?> getSessionProcess(Computer computer) {

        // Just use the root process as the player's session
        return computer.getOs().getProcModule().getRootProcess();
    }

    protected ContentFile getSourceFile(Computer computer, String programName) throws UnknownMountpointException, InvalidPathException {

        WorldProgram programData = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), programName);

        if (programData == null) {
            return null;
        } else {
            return (ContentFile) computer.getOs().getFsModule().getFile(programData.getCommonLocation().toString());
        }
    }

    protected WorldProcessId getProcessId(Process<?> process) {

        return process.getWorldProcessId();
    }

}
