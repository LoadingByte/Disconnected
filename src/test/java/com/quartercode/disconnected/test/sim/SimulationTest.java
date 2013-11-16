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
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.ai.PlayerController;
import com.quartercode.disconnected.sim.member.ai.UserController;
import com.quartercode.disconnected.sim.run.util.SimulationGenerator;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.world.general.RootObject;

public class SimulationTest {

    private Simulation simulation;

    @Before
    public void setUp() {

        simulation = SimulationGenerator.generateSimulation(10, 2, new RandomPool(Simulation.RANDOM_POOL_SIZE));
    }

    @Test
    public void testEquals() {

        Assert.assertEquals("Simulation equals itself", simulation, simulation);
    }

    @Test
    public void testGetMembersByController() {

        Member localPlayer = null;
        for (Member member : simulation.getWorld().getRoot().get(RootObject.MEMBERS_PROPERTY).getByController(PlayerController.class)) {
            if ( ((PlayerController) member.getAiController()).isLocal()) {
                localPlayer = member;
                break;
            }
        }

        simulation.getWorld().getRoot().get(RootObject.MEMBERS_PROPERTY).getByController(UserController.class);
        Assert.assertEquals("Local player equals", localPlayer, simulation.getWorld().getRoot().get(RootObject.MEMBERS_PROPERTY).getLocalPlayer());
    }

}
