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

package com.quartercode.disconnected.shared.util.config;

import java.util.HashMap;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default implementation of the {@link ConfigService}.
 * 
 * @see ConfigService
 */
public class DefaultConfigService implements ConfigService {

    private static final Logger                    LOGGER   = LoggerFactory.getLogger(DefaultConfigService.class);

    private final Map<String, ConfigCommandParser> commands = new HashMap<>();

    @Override
    public void addCommand(String command, ConfigCommandParser parser) {

        if (commands.containsKey(command)) {
            ConfigCommandParser oldParser = commands.get(command);
            LOGGER.warn("Command parser '{}' for command '{}' is overriden with new parser '{}' without explicit removal", oldParser, command, parser);
        }

        commands.put(command, parser);
    }

    @Override
    public void removeCommand(String command) {

        commands.remove(command);
    }

    @Override
    public void parse(Document config) {

        Element root = config.getRootElement();

        for (Element commandElement : root.getChildren()) {
            String command = commandElement.getName();
            ConfigCommandParser commandParser = commands.get(command);

            if (commandParser == null) {
                LOGGER.warn("Unknown config command '{}' in config '{}'", command, config.getBaseURI());
            } else {
                try {
                    commandParser.parse(config, commandElement);
                } catch (JDOMException e) {
                    LOGGER.warn("Error while parsing command '{}' from config '{}", command, config.getBaseURI(), e);
                }
            }
        }
    }

}
