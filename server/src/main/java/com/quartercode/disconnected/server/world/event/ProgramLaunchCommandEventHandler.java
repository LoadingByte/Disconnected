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

package com.quartercode.disconnected.server.world.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.sim.profile.ProfileService;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.shared.event.comp.program.ProgramLaunchCommandEvent;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.eventbridge.bridge.module.EventHandler;

/**
 * The program launch command event handler executes {@link ProgramLaunchCommandEvent}s.
 * It should run on every tick server.
 * 
 * @see ProgramLaunchCommandEvent
 */
public class ProgramLaunchCommandEventHandler implements EventHandler<ProgramLaunchCommandEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramLaunchCommandEventHandler.class);

    @Override
    public void handle(ProgramLaunchCommandEvent event) {

        Computer playerComputer = getPlayerComputer();

        // Get the source file
        ContentFile source = getSourceFile(playerComputer, event.getFilePath());
        // Cancel if the program cannot be found
        if (source == null) {
            return;
        }

        // Create a new process
        Process<?> sessionProcess = getSessionProcess(playerComputer);
        Process<?> process = sessionProcess.get(Process.CREATE_CHILD).invoke();

        // Set the source file
        process.get(Process.SOURCE).set(source);

        // Initialize the process
        try {
            process.get(Process.INITIALIZE).invoke(event.getPid());
        } catch (Exception e) {
            LOGGER.warn("Error while initializing process with pid {}; client failure?", event.getPid(), e);
            abort(sessionProcess, process);
            return;
        }

        // Run the program
        ProgramExecutor executor = process.get(Process.EXECUTOR).get();
        try {
            executor.get(ProgramExecutor.RUN).invoke();
        } catch (Exception e) {
            LOGGER.warn("Program executor '{}' threw unexpected exception on start", executor, e);
            abort(sessionProcess, process);
        }
    }

    private void abort(Process<?> parent, Process<?> process) {

        parent.get(Process.CHILDREN).remove(process);
    }

    protected Computer getPlayerComputer() {

        World world = ServiceRegistry.lookup(ProfileService.class).getActive().getWorld();
        // Just use first available computer as the player's one
        return world.get(World.COMPUTERS).get().get(0);
    }

    protected Process<?> getSessionProcess(Computer computer) {

        OperatingSystem os = computer.get(Computer.OS).get();
        // Just use the root process as the player's session
        return os.get(OperatingSystem.PROC_MODULE).get().get(ProcessModule.ROOT_PROCESS).get();
    }

    protected ContentFile getSourceFile(Computer computer, String path) {

        FileSystemModule fsModule = computer.get(Computer.OS).get().get(OperatingSystem.FS_MODULE).get();
        return (ContentFile) fsModule.get(FileSystemModule.GET_FILE).invoke(path);
    }

}