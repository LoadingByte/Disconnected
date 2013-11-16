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

package com.quartercode.disconnected.world.member.interest;

import com.quartercode.disconnected.world.member.Member;

/**
 * This class defines methods for an interest which has a member as target.
 * This could also be used for defining computer targets (every computer is controlled by a member).
 * 
 * @see Interest
 */
public interface HasTarget {

    /**
     * Returns the member the interest has as target.
     * 
     * @return The member the interest has as target.
     */
    public Member getTarget();

}
