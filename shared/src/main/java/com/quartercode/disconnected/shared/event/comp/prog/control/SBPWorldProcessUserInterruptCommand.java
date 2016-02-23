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

package com.quartercode.disconnected.shared.event.comp.prog.control;

import com.quartercode.disconnected.shared.event.comp.prog.SBPWorldProcessUserCommand;
import com.quartercode.disconnected.shared.world.comp.proc.SBPWorldProcessUserId;

/**
 * This event interrupts the user (server bridge partner) of a world process.
 * That user could be a client that runs a graphical process on the client side for rendering the program, or it could a an AI.
 * The event is typically sent by a server bridge when a world process is interrupted from outside (e.g. using the process manager).<br>
 * <br>
 * Note that interrupting an SBP world process user means that it should immediately shut down.
 * For example, a client process would close all its windows.
 */
public class SBPWorldProcessUserInterruptCommand extends SBPWorldProcessUserCommand {

    private static final long           serialVersionUID = -6013630210265304769L;

    private final SBPWorldProcessUserId worldProcessUserId;

    /**
     * Creates a new world process user interrupt command.
     *
     * @param worldProcessUserId The {@link SBPWorldProcessUserId} that should receive the event and is therefore commanded to shut down.
     */
    public SBPWorldProcessUserInterruptCommand(SBPWorldProcessUserId worldProcessUserId) {

        this.worldProcessUserId = worldProcessUserId;
    }

    @Override
    public SBPWorldProcessUserId getWorldProcessUserId() {

        return worldProcessUserId;
    }

}
