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

package com.quartercode.disconnected.server.event.prog.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.sim.TickBridgeProvider;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.profile.ProfileService;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FSModule;
import com.quartercode.disconnected.server.world.comp.os.OS;
import com.quartercode.disconnected.server.world.comp.prog.ProcModule;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.prog.ProgramUtils;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchAcknowledgmentEvent;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
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
        ContentFile source = getSourceFile(playerComputer, event.getProgramName());
        // Cancel if the program cannot be found
        if (source == null) {
            return;
        }

        // Create a new process
        Process<?> sessionProcess = getSessionProcess(playerComputer);
        Process<?> process = sessionProcess.invoke(Process.CREATE_CHILD);

        // Set the world process user
        SBPWorldProcessUserId worldProcessUserId = new SBPWorldProcessUserId(sender, event.getWorldProcessUserDetails());
        process.setObj(Process.WORLD_PROCESS_USER, worldProcessUserId);

        // Set the source file
        process.setObj(Process.SOURCE, source);

        // Retrieve a new pid
        int pid = getProcModule(playerComputer).invoke(ProcModule.NEXT_PID);

        // Initialize the process
        try {
            process.invoke(Process.INITIALIZE, pid);
        } catch (Exception e) {
            LOGGER.warn("Error while initializing process with pid {}; sbp failure?", pid, e);
            abort(sessionProcess, process);
            return;
        }

        // Run the program
        ProgramExecutor executor = process.getObj(Process.EXECUTOR);
        try {
            executor.invoke(ProgramExecutor.RUN);
        } catch (Exception e) {
            LOGGER.warn("Program executor '{}' threw unexpected exception on start", executor, e);
            abort(sessionProcess, process);
        }

        // Send an acknowledgment event
        getBridge().send(new WorldProcessLaunchAcknowledgmentEvent(worldProcessUserId, getProcessId(executor)));
    }

    private void abort(Process<?> parent, Process<?> process) {

        parent.removeFromColl(Process.CHILDREN, process);
    }

    protected Bridge getBridge() {

        return ServiceRegistry.lookup(TickService.class).getAction(TickBridgeProvider.class).getBridge();
    }

    protected Computer getSBPComputer(SBPIdentity sbp) {

        World world = ServiceRegistry.lookup(ProfileService.class).getActive().getWorld();
        // Just use first available computer as the player's one
        return world.getColl(World.COMPUTERS).get(0);
    }

    protected ProcModule getProcModule(Computer computer) {

        return computer.getObj(Computer.OS).getObj(OS.PROC_MODULE);
    }

    protected Process<?> getSessionProcess(Computer computer) {

        // Just use the root process as the player's session
        return getProcModule(computer).getObj(ProcModule.ROOT_PROCESS);
    }

    protected ContentFile getSourceFile(Computer computer, String programName) {

        WorldProgram programData = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), programName);
        if (programData == null) {
            return null;
        }

        FSModule fsModule = computer.getObj(Computer.OS).getObj(OS.FS_MODULE);
        return (ContentFile) fsModule.invoke(FSModule.GET_FILE, programData.getCommonLocation().toString());
    }

    protected WorldProcessId getProcessId(ProgramExecutor executor) {

        return ProgramUtils.getProcessId(executor);
    }

}
