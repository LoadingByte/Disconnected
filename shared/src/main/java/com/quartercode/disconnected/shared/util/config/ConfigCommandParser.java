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
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * A config command parser takes one XML configuration command {@link Element} and executes the defined action.
 * It is used by the {@link ConfigService} for parsing whole config files.
 * See that class for more information on the purpose.
 *
 * @see ConfigService
 */
public interface ConfigCommandParser {

    /**
     * Parses the given XML configuration command {@link Element}, which is located inside the given JDOM2 {@link Document}.
     * That includes executing the defined action.
     *
     * @param config The complete configuration document the given command is a part of.
     * @param commandElement The element that contains the command.
     * @throws JDOMException Should be thrown when something related to JDOM2 happens.
     */
    public void parse(Document config, Element commandElement) throws JDOMException;

}
