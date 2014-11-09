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
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.extra.ConfigureNamedValueCommand;
import com.quartercode.disconnected.shared.util.config.extra.ParserUtils;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;
import com.quartercode.disconnected.shared.world.comp.file.SeparatedPath;

public class ConfigureWorldProgramCommand extends ConfigureNamedValueCommand<WorldProgram> {

    public ConfigureWorldProgramCommand(SetRegistry<WorldProgram> registry) {

        super("world program", registry);
    }

    @Override
    protected WorldProgram supplyDefaultValue(String name) {

        return new WorldProgram(name, null, 0, null);
    }

    @Override
    protected WorldProgram changeValue(Document config, Element commandElement, WorldProgram oldValue) {

        String name = oldValue.getName();
        Class<?> type = oldValue.getType();
        long size = oldValue.getSize();
        SeparatedPath commonLocation = oldValue.getCommonLocation();

        Element typeElement = commandElement.getChild("class");
        if (typeElement != null) {
            String typeString = VariableReferenceResolver.process(typeElement.getText(), null);
            type = ParserUtils.parseClass(config, "program executor class for '" + name + "'", ProgramExecutor.class, typeString, type);
        }

        Element sizeElement = commandElement.getChild("size");
        if (sizeElement != null) {
            String sizeString = VariableReferenceResolver.process(sizeElement.getText(), null);
            size = ParserUtils.parsePositiveNumber(config, "world group size for '" + name + "'", sizeString, size);
        }

        Element commonLocationElement = commandElement.getChild("commonLocation");
        if (commonLocationElement != null) {
            String locationString = VariableReferenceResolver.process(commonLocationElement.getText(), null);
            commonLocation = new SeparatedPath(locationString);
        }

        return new WorldProgram(name, type, size, commonLocation);
    }

}
