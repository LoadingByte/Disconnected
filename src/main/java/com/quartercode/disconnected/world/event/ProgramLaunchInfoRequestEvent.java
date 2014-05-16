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

package com.quartercode.disconnected.world.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import com.quartercode.disconnected.bridge.ReturnEventUtil.Return;
import com.quartercode.disconnected.bridge.ReturnEventUtil.Returnable;

/**
 * The program launch info request event can be used to retrieve needed information before sending a {@link ProgramLaunchCommandEvent}.
 * The returned information is related to the computer of the client that sends the request event.
 * Every response event contains a newly generated pid which can be input into a program launch command event.
 * 
 * @see ProgramLaunchInfoResponseEvent
 * @see ProgramLaunchInfoRequestEventHandler
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor (access = AccessLevel.PRIVATE)
public class ProgramLaunchInfoRequestEvent implements Returnable {

    private static final long serialVersionUID = 8657866299342353712L;

    // TODO: Proper user identification

    @Wither
    @Setter (AccessLevel.NONE)
    private String            nextReturnId;

    /**
     * The program launch info response event response to the return event for the {@link ProgramLaunchInfoRequestEvent}.
     * It contains a newly generated pid which can be input into a {@link ProgramLaunchCommandEvent}.
     * 
     * @see ProgramLaunchInfoRequestEvent
     * @see ProgramLaunchInfoRequestEventHandler
     */
    @Data
    public static class ProgramLaunchInfoResponseEvent implements Return {

        private static final long serialVersionUID = -2662532919300573317L;

        /**
         * The id of the computer which represents the client that sent the request event.
         */
        private final String      computerId;

        /**
         * A newly generated process id that might be used to launch a new program.
         */
        private final int         pid;

        private final String      returnId;

    }

}
