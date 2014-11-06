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

package com.quartercode.disconnected.client.registry.config;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.client.graphics.desktop.ClientProgramDescriptor;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.extra.ConfigureNamedValueCommand;
import com.quartercode.disconnected.shared.util.config.extra.ParserUtils;
import com.quartercode.disconnected.shared.util.registry.extra.MapRegistry;
import com.quartercode.disconnected.shared.util.registry.extra.MapRegistry.DefaultMapping;
import com.quartercode.disconnected.shared.util.registry.extra.MappedValueRegistry.Mapping;

public class ConfigureClientProgramCommand extends ConfigureNamedValueCommand<Mapping<String, Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureClientProgramCommand.class);

    public ConfigureClientProgramCommand(MapRegistry<String, Object> registry) {

        super("client program", registry);
    }

    @Override
    protected Mapping<String, Object> supplyDefaultValue(String name) {

        return new DefaultMapping<>(name, null);
    }

    @Override
    protected Mapping<String, Object> changeValue(Document config, Element commandElement, Mapping<String, Object> oldValue) throws JDOMException {

        String name = oldValue.getLeft();
        Object descriptor = oldValue.getRight();

        Element typeElement = commandElement.getChild("class");
        if (typeElement != null) {
            String typeString = VariableReferenceResolver.process(typeElement.getText());
            Class<?> oldType = descriptor == null ? null : descriptor.getClass();
            Class<?> type = ParserUtils.parseClass(config, "client program class for '" + name + "'", ClientProgramDescriptor.class, typeString, oldType);

            if (type != null) {
                try {
                    descriptor = type.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.warn("Config: Unable to create instance of client program class '{}' for '{}' (in '{}')", type.getName(), name, config.getBaseURI(), e);
                }
            }
        }

        return new DefaultMapping<>(name, descriptor);
    }

}
