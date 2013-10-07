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

package com.quartercode.disconnected.test.sim;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.ai.UserController;
import com.quartercode.disconnected.sim.run.util.SimulationGenerator;

public class SimulationTest {

    private Simulation simulation;

    @Before
    public void setUp() {

        simulation = SimulationGenerator.generateSimulation(10, 2);
    }

    @Test
    public void testEquals() {

        Assert.assertEquals("Simulation equals itself", simulation, simulation);
    }

    @Test
    public void testGetMembersByController() {

        Member localPlayer = new Member("player");
        localPlayer.setComputer(new Computer("p"));
        simulation.addMember(localPlayer);

        simulation.getMembersByController(UserController.class);
        Assert.assertEquals("Local player equals", localPlayer, simulation.getMembersByController(null).get(0));
    }

}
