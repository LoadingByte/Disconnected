/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.shared.event.comp.prog.generic;

import java.text.MessageFormat;
import com.quartercode.disconnected.shared.event.comp.prog.SBPWorldProcessUserCommand;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;

/**
 * A generic error event sent by world programs to world process users.
 * It carries an error type string and a list of error-specific arguments.
 * It is used to transport a simple error notification to the world process user.
 * As a result, a graphical WPU (e.g. a client) could open a popup and show an appropriate error message that is customized using the arguments.
 */
public class GP_SBPWPU_ErrorEvent extends SBPWorldProcessUserCommand {

    private static final long           serialVersionUID = 7741026863547733953L;

    private final SBPWorldProcessUserId worldProcessUserId;

    private final String                type;
    private final String[]              arguments;

    /**
     * Creates a new generic program WPU error event.
     * 
     * @param worldProcessUserId The {@link SBPWorldProcessUserId} that should receive the event.
     *        It has launched the world process that sends the new event.
     * @param type A string that defines the type of the error.
     *        If the WPU would be a graphical client, this could also be used as the key of a localization string lookup.
     *        That way, the client wouldn't need to worry about the different types.
     * @param arguments A string array that further defines the error.
     *        If the WPU would be a graphical client, this could be put into {@link MessageFormat#format(String, Object...)} along
     *        with the localization string looked up using the error type.
     */
    public GP_SBPWPU_ErrorEvent(SBPWorldProcessUserId worldProcessUserId, String type, String... arguments) {

        this.worldProcessUserId = worldProcessUserId;
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public SBPWorldProcessUserId getWorldProcessUserId() {

        return worldProcessUserId;
    }

    /**
     * Returns the string that defines the type of the error.
     * If the WPU would be a graphical client, this could also be used as the key of a localization string lookup.
     * That way, the client wouldn't need to worry about the different types.
     * 
     * @return The error type.
     */
    public String getType() {

        return type;
    }

    /**
     * Returns the string array that further defines the error.
     * If the WPU would be a graphical client, this could be put into {@link MessageFormat#format(String, Object...)} along
     * with the localization string looked up using the error type.
     * 
     * @return The error details.
     */
    public String[] getArguments() {

        return arguments;
    }

}
