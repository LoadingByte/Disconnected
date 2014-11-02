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

package com.quartercode.disconnected.shared.registrydef.config;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import com.quartercode.disconnected.shared.comp.file.SeparatedPath;
import com.quartercode.disconnected.shared.config.util.ConfigureNamedValueCommand;
import com.quartercode.disconnected.shared.registry.extra.MapRegistry;
import com.quartercode.disconnected.shared.registry.extra.MapRegistry.DefaultMapping;
import com.quartercode.disconnected.shared.registry.extra.MappedValueRegistry.Mapping;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;

public class ConfigureCommonWorldProgramLocationCommand extends ConfigureNamedValueCommand<Mapping<String, SeparatedPath>> {

    public ConfigureCommonWorldProgramLocationCommand(MapRegistry<String, SeparatedPath> registry) {

        super("common world program location", registry);
    }

    @Override
    protected Mapping<String, SeparatedPath> supplyDefaultValue(String name) {

        return new DefaultMapping<>(name, null);
    }

    @Override
    protected Mapping<String, SeparatedPath> changeValue(Document config, Element commandElement, Mapping<String, SeparatedPath> oldValue) throws JDOMException {

        String name = oldValue.getLeft();
        SeparatedPath location = oldValue.getRight();

        Element locationElement = commandElement.getChild("location");
        if (locationElement != null) {
            String locationString = VariableReferenceResolver.process(locationElement.getText());
            location = new SeparatedPath(locationString);
        }

        return new DefaultMapping<>(name, location);
    }

}
