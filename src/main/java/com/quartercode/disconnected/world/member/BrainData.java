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

package com.quartercode.disconnected.world.member;

import com.quartercode.disconnected.world.member.interest.Interest;

/**
 * Brain data is a very abstract implementation of data the brain could store.
 * It's used by members to store interests, knowledge sets and a lot more.
 * This is implemented as an abstract class for supporting JAXB serialization.
 * 
 * @see Member
 * @see Interest
 */
public abstract class BrainData {

    /**
     * Creates a new empty brain data entry.
     */
    public BrainData() {

    }

}
