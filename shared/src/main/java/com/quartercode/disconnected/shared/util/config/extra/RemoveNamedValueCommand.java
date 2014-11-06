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

package com.quartercode.disconnected.shared.util.config.extra;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.ConfigCommandParser;
import com.quartercode.disconnected.shared.util.registry.extra.MultipleValueRegistry;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValue;

/**
 * A command parser that removes {@link NamedValue}s from a {@link MultipleValueRegistry} based on the names supplied in the XML command {@link Element}s.
 * 
 * @param <V> The type of the named value that can be removed using the command.
 * @see NamedValue
 * @see MultipleValueRegistry
 */
public class RemoveNamedValueCommand<V extends NamedValue> implements ConfigCommandParser {

    private static final Logger            LOGGER = LoggerFactory.getLogger(RemoveNamedValueCommand.class);

    private final String                   valueType;
    private final MultipleValueRegistry<V> registry;

    /**
     * Creates a new remove named value command.
     * 
     * @param valueType A human-readable string that describes the value type (e.g. "scheduler task").
     * @param registry The {@link MultipleValueRegistry} that contains the values for potential removal.
     */
    public RemoveNamedValueCommand(String valueType, MultipleValueRegistry<V> registry) {

        this.valueType = valueType;
        this.registry = registry;
    }

    /**
     * Returns the {@link MultipleValueRegistry} that contains the values for potential removal.
     * 
     * @return The registry the command manipulates.
     */
    protected MultipleValueRegistry<V> getRegistry() {

        return registry;
    }

    @Override
    public void parse(Document config, Element commandElement) throws JDOMException {

        String name = commandElement.getAttributeValue("name");

        if (StringUtils.isBlank(name)) {
            LOGGER.warn("Config: Cannot remove {} with blank name (in '{}')", valueType, config.getBaseURI());
        } else {
            name = VariableReferenceResolver.process(name);

            V valueForRemoval = null;
            for (V existingValue : registry.getValues()) {
                if (existingValue.getName().equals(name)) {
                    valueForRemoval = existingValue;
                    break;
                }
            }

            if (valueForRemoval != null) {
                registry.removeValue(valueForRemoval);
            }
        }
    }

}
