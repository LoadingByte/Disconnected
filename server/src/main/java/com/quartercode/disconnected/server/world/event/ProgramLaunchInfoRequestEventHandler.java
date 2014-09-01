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

import com.quartercode.disconnected.server.sim.profile.ProfileService;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.world.event.ProgramLaunchInfoRequestEvent;
import com.quartercode.disconnected.shared.world.event.ProgramLaunchInfoRequestEvent.ProgramLaunchInfoResponseEvent;
import com.quartercode.eventbridge.extra.extension.RequestEventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventSender;

/**
 * The program launch info request event handler responses {@link ProgramLaunchInfoRequestEvent}.
 * It should run on every tick server.
 * 
 * @see ProgramLaunchInfoRequestEvent
 */
public class ProgramLaunchInfoRequestEventHandler implements RequestEventHandler<ProgramLaunchInfoRequestEvent> {

    @Override
    public void handle(ProgramLaunchInfoRequestEvent request, ReturnEventSender sender) {

        Computer playerComputer = getPlayerComputer();

        ProcessModule procModule = getProcessModule(playerComputer);
        int pid = procModule.get(ProcessModule.NEXT_PID).invoke();

        sender.send(new ProgramLaunchInfoResponseEvent(playerComputer.getId(), pid));
    }

    protected Computer getPlayerComputer() {

        World world = ServiceRegistry.lookup(ProfileService.class).getActive().getWorld();
        // Just use first available computer as the player's one
        return world.get(World.COMPUTERS).get().get(0);
    }

    protected ProcessModule getProcessModule(Computer computer) {

        return computer.get(Computer.OS).get().get(OperatingSystem.PROC_MODULE).get();
    }

}
