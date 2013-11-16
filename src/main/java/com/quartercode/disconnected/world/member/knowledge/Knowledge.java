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

package com.quartercode.disconnected.world.member.knowledge;

import com.quartercode.disconnected.world.member.BrainData;
import com.quartercode.disconnected.world.member.Member;

/**
 * This abstract class represents a knowledge entry of a member.
 * Knowledge is the foundation for simulating a learning process for members.
 * The object can contain information how to do something (e.g. using an Exploit), some data the member remembered etc.
 * 
 * @see Member
 */
public abstract class Knowledge extends BrainData {

    // TODO: Implement abstract superclass and subclasses

    /**
     * Creates a new empty knowledge entry.
     */
    public Knowledge() {

    }

}
