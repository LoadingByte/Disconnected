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

package com.quartercode.disconnected.shared.event.program.generic;

import java.text.MessageFormat;
import com.quartercode.disconnected.shared.comp.program.ClientProcessId;
import com.quartercode.disconnected.shared.event.program.ClientProcessCommand;

/**
 * A generic error event sent by world programs to client programs.
 * It carries an error type string and a list of error-specific arguments.
 * It is used to transport a simple error notification to the client.
 * As a result, the client could open a popup and show an appropriate error message that is customized using the arguments.
 */
public class GPClientErrorEvent extends ClientProcessCommand {

    private final ClientProcessId clientProcessId;

    private final String          type;
    private final String[]        arguments;

    /**
     * Creates a new generic client error event.
     * 
     * @param clientProcessId The unique id of the identifiable client process.
     * @param type A string that defines the type of the error.
     *        This could also be used to set the key of an i18n string looked up by the client.
     *        That way, the client program wouldn't need to worry about the different types.
     * @param arguments A string array that further defines the error.
     *        This could be put into {@link MessageFormat#format(String, Object...)} along with the i18n string looked up using the error type.
     */
    public GPClientErrorEvent(ClientProcessId clientProcessId, String type, String[] arguments) {

        this.clientProcessId = clientProcessId;
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public ClientProcessId getClientProcessId() {

        return clientProcessId;
    }

    /**
     * Returns the string that defines the type of the error.
     * This could also be used to set the key of an i18n string looked up by the client.
     * That way, the client program wouldn't need to worry about the different types.
     * 
     * @return The error type.
     */
    public String getType() {

        return type;
    }

    /**
     * Returns the string array that further defines the error.
     * This could be put into {@link MessageFormat#format(String, Object...)} along with the i18n string looked up using the error type.
     * 
     * @return The error details.
     */
    public String[] getArguments() {

        return arguments;
    }

}
