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

package com.quartercode.disconnected.test.world.member;

import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.world.member.Member;
import com.quartercode.disconnected.world.member.MemberGroup;
import com.quartercode.disconnected.world.member.action.Action;
import com.quartercode.disconnected.world.member.interest.Interest;

public class EmptyInterest extends Interest {

    public EmptyInterest(float priority) {

        super(priority);
    }

    @Override
    public int getReputationChange(Simulation simulation, Member member, MemberGroup group) {

        return 0;
    }

    @Override
    public Action getAction(Simulation simulation, Member member) {

        return null;
    }

}
