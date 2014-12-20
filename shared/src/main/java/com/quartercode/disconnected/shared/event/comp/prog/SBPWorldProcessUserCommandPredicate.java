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

package com.quartercode.disconnected.shared.event.comp.prog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserDetails;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;
import com.quartercode.eventbridge.basic.EventPredicateBase;

/**
 * An event predicate that filters out {@link SBPWorldProcessUserCommand}s sent to a world process user characterized by
 * a specific {@link SBPWorldProcessUserDetails} object.
 * Note that a check for a complete {@link SBPWorldProcessUserId}, which normally identifies world process users and includes
 * an {@link SBPIdentity}, is not needed because an SBP which uses this predicate can only receive events he should receive.
 * Therefore, an extra check for the SBP identity would be redundant.
 * 
 * @param <T> The type of world process user command that can be tested by the predicate.
 * @see SBPWorldProcessUserDetails
 */
@RequiredArgsConstructor
@Getter
public class SBPWorldProcessUserCommandPredicate<T extends SBPWorldProcessUserCommand> extends EventPredicateBase<T> {

    private static final long                serialVersionUID = -890729154221302611L;

    /**
     * The {@link SBPWorldProcessUserDetails} each {@link SBPWorldProcessUserCommand} must carry in its {@link SBPWorldProcessUserId} in
     * order to be accepted by the predicate.
     * That means that the accepted commands are sent to an SBP routine which is characterized by this details object.
     */
    private final SBPWorldProcessUserDetails worldProcessUserDetails;

    @Override
    public boolean test(SBPWorldProcessUserCommand event) {

        return event.getWorldProcessUserId().getDetails().equals(worldProcessUserDetails);
    }

}
