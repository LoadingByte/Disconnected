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

package com.quartercode.disconnected.sim.member.ai;

import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.member.Member;

/**
 * The player controller represents a human player playing the game.
 * This class is just for representation and doesn't simulate anything.
 */
public class PlayerController extends AIController {

    private boolean local;

    /**
     * Creates a new empty player controller.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected PlayerController() {

    }

    /**
     * Creates a new user controller and sets the represented member and if he is local.
     * 
     * @param member The member which should be represented by this controller.
     * @param local The player is interacting with the computer this program runs on.
     */
    public PlayerController(Member member, boolean local) {

        super(member);

        this.local = local;
    }

    /**
     * Returns if the player is interacting with the computer this program runs on.
     * 
     * @return The player is interacting with the computer this program runs on.
     */
    public boolean isLocal() {

        return local;
    }

    @Override
    public void update(Simulation simulation) {

        // The player can play the game without automatic simulation.
    }

}
