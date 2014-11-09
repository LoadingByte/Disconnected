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
 * An abstract command parser that adds {@link NamedValue}s to a {@link MultipleValueRegistry} or changes ones that already exist.
 * For example, if a value had a property {@code X} and a property {@code Y}, a command that only sets {@code X} would only change that property,
 * leaving {@code Y} untouched from the previous command call:
 * 
 * <pre>
 * Command: Y=5
 * =&gt; X=0 Y=5
 * 
 * Command: X=3
 * =&gt; X=3 Y=5
 * </pre>
 * 
 * This class provides a basic structure for such a command parser.
 * 
 * @param <V> The type of the named value that can be configured using the command.
 * @see NamedValue
 * @see MultipleValueRegistry
 */
public abstract class ConfigureNamedValueCommand<V extends NamedValue> implements ConfigCommandParser {

    private static final Logger            LOGGER = LoggerFactory.getLogger(ConfigureNamedValueCommand.class);

    private final String                   valueType;
    private final MultipleValueRegistry<V> registry;

    /**
     * Creates a new configure named value command.
     * 
     * @param valueType A human-readable string that describes the value type (e.g. "scheduler task").
     * @param registry The {@link MultipleValueRegistry} all configured values should be written to.
     */
    public ConfigureNamedValueCommand(String valueType, MultipleValueRegistry<V> registry) {

        this.valueType = valueType;
        this.registry = registry;
    }

    /**
     * Returns the {@link MultipleValueRegistry} all configured values should be written to.
     * Note that previous values are also taken from this registry for being modified.
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
            LOGGER.warn("Config: Cannot configure {} with blank name (in '{}')", valueType, config.getBaseURI());
        } else {
            name = VariableReferenceResolver.process(name, null);

            V oldValue = null;
            for (V existingValue : registry.getValues()) {
                if (existingValue.getName().equals(name)) {
                    oldValue = existingValue;
                    break;
                }
            }

            if (oldValue == null) {
                oldValue = supplyDefaultValue(name);
            }

            V value = changeValue(config, commandElement, oldValue);

            if (value != null) {
                registry.removeValue(oldValue);
                registry.addValue(value);
            }
        }
    }

    /**
     * Creates a new empty "default" instance of the supported value type with the given name.
     * 
     * @param name The name of the new {@link NamedValue} instance.
     * @return A new empty instance of the value type.
     */
    protected abstract V supplyDefaultValue(String name);

    /**
     * Modifies the given old value with the changes provided by the given XML command {@link Element}.
     * 
     * @param config The complete configuration document the given command is a part of.
     * @param commandElement The element that contains the modifying command.
     * @param oldValue The old value that should be modified.
     * @return The new modified value.
     * @throws JDOMException Should be thrown when something related to JDOM2 happens.
     */
    protected abstract V changeValue(Document config, Element commandElement, V oldValue) throws JDOMException;

}
