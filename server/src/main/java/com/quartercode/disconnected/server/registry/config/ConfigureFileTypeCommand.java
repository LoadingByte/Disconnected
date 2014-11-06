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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.extra.ConfigureNamedValueCommand;
import com.quartercode.disconnected.shared.util.config.extra.ParserUtils;
import com.quartercode.disconnected.shared.util.registry.extra.MapRegistry;
import com.quartercode.disconnected.shared.util.registry.extra.MapRegistry.DefaultMapping;
import com.quartercode.disconnected.shared.util.registry.extra.MappedValueRegistry.Mapping;

public class ConfigureFileTypeCommand extends ConfigureNamedValueCommand<Mapping<String, Class<?>>> {

    public ConfigureFileTypeCommand(MapRegistry<String, Class<?>> registry) {

        super("file type", registry);
    }

    @Override
    protected Mapping<String, Class<?>> supplyDefaultValue(String name) {

        return new DefaultMapping<>(name, null);
    }

    @Override
    protected Mapping<String, Class<?>> changeValue(Document config, Element commandElement, Mapping<String, Class<?>> oldValue) throws JDOMException {

        String name = oldValue.getLeft();
        Class<?> type = oldValue.getRight();

        Element typeElement = commandElement.getChild("class");
        if (typeElement != null) {
            String typeString = VariableReferenceResolver.process(typeElement.getText());
            type = ParserUtils.parseClass(config, "file type class for '" + name + "'", File.class, typeString, type);
        }

        return new DefaultMapping<String, Class<?>>(name, type);
    }

}
