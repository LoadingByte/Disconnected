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

package com.quartercode.disconnected.server.registry.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.ConfigCommandParser;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;

@RequiredArgsConstructor
public class AddWorldContextPathEntryCommand implements ConfigCommandParser {

    private static final Logger       LOGGER = LoggerFactory.getLogger(AddWorldContextPathEntryCommand.class);

    private final SetRegistry<String> registry;

    @Override
    public void parse(Document config, Element commandElement) throws JDOMException {

        String entry = commandElement.getText();

        if (StringUtils.isBlank(entry)) {
            LOGGER.warn("Config: Cannot add blank world context path entry (in '{}')", config.getBaseURI());
        } else {
            registry.addValue(VariableReferenceResolver.process(entry, null));
        }
    }

}
