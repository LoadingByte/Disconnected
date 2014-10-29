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

package com.quartercode.disconnected.shared.event.program.control;

import com.quartercode.disconnected.shared.comp.program.SBPWorldProcessUserDetails;
import com.quartercode.eventbridge.basic.EventBase;

/**
 * This event starts a world process on the computer of the server bridge partner that sends it.
 * Such an event must be sent to a server bridge which handles it.
 */
public class WorldProcessLaunchCommand extends EventBase {

    private final SBPWorldProcessUserDetails worldProcessUserDetails;
    private final String                     programFilePath;

    /**
     * Creates a new process launch command.
     * 
     * @param worldProcessUserDetails A {@link SBPWorldProcessUserDetails} object that is used by the SBP to identify the correct world process user.
     * @param programFilePath The path under which the program file, which will be used for the new world process, can be found.
     */
    public WorldProcessLaunchCommand(SBPWorldProcessUserDetails worldProcessUserDetails, String programFilePath) {

        this.worldProcessUserDetails = worldProcessUserDetails;
        this.programFilePath = programFilePath;
    }

    /**
     * Returns a {@link SBPWorldProcessUserDetails} object that is used by the SBP to identify the correct world process user.
     * 
     * @return The world process user identity details.
     */
    public SBPWorldProcessUserDetails getWorldProcessUserDetails() {

        return worldProcessUserDetails;
    }

    /**
     * Returns the path under which the program file, which will be used for the new world process, can be found.
     * If the path doesn't point to a valid program file, the launched process is stopped immediately after being started.
     * 
     * @return The source file path for the new world process.
     */
    public String getProgramFilePath() {

        return programFilePath;
    }

}
