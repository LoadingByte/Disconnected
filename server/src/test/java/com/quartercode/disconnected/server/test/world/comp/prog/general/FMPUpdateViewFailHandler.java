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

package com.quartercode.disconnected.server.test.world.comp.prog.general;

import static org.junit.Assert.fail;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMPWPUUpdateViewCommand;
import com.quartercode.eventbridge.bridge.module.EventHandler;

public class FMPUpdateViewFailHandler implements EventHandler<FMPWPUUpdateViewCommand> {

    @Override
    public void handle(FMPWPUUpdateViewCommand event) {

        fail("View was updated although nothing should have happened");
    }

}
