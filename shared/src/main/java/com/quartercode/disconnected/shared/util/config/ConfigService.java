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

package com.quartercode.disconnected.shared.util.config;

import org.jdom2.Document;

/**
 * A config service is responsible for parsing data configuration XML files.
 * Such a config file contains an array of configuration commands, which are the child elements of the root element.
 * These commands are passed into registered {@link ConfigCommandParser} that parse them and execute the defined action.
 * Often, commands just add or remove list entries or set a single value.
 * 
 * @see ConfigCommandParser
 */
public interface ConfigService {

    /**
     * Registers the given command and assigns the given {@link ConfigCommandParser}.
     * The command name defines the name of all XML elements that represent the command.
     * The command parser is responsible for parsing and execution the command.<br>
     * <br>
     * Note that {@link #removeCommand(String)} should be called before a command is explicitly overridden.
     * Otherwise, a log warning will be generated.
     * 
     * @param command The name of the new command.
     *        It sets the name of XML command elements.
     * @param parser The parser that is responsible for executing the command.
     */
    public void addCommand(String command, ConfigCommandParser parser);

    /**
     * Explicitly removes the given command.
     * When overriding a command, this method should be called with the command before {@link #addCommand(String, ConfigCommandParser)} is used.
     * Otherwise, a log warning will be generated.
     * 
     * @param command The name of the command for removal.
     *        It set the name of XML command elements.
     */
    public void removeCommand(String command);

    /**
     * Reads the given XML configuration {@link Document} (JDOM2) and parses all contained commands using the registered {@link ConfigCommandParser}s.
     * Note that no exception is thrown by this method.
     * When an error occurs, it prints out a log message and continues with the next command.
     * That way, non-critical errors do not cause the whole application to terminate.
     * Moreover, everything is parsed possibly resulting in some more context information for finding the error cause.
     * 
     * @param config The JDOM2 document whose commands should be executed.
     *        Note that the {@link Document#getBaseURI()} should not return {@code null} to provide more useful logging information.
     */
    public void parse(Document config);

}
