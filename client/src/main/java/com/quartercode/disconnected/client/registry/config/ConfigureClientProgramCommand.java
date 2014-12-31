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
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramExecutor;
import com.quartercode.disconnected.client.registry.ClientProgram;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.extra.ConfigureNamedValueCommand;
import com.quartercode.disconnected.shared.util.config.extra.ParserUtils;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;

public class ConfigureClientProgramCommand extends ConfigureNamedValueCommand<ClientProgram> {

    public ConfigureClientProgramCommand(SetRegistry<ClientProgram> registry) {

        super("client program", registry);
    }

    @Override
    protected ClientProgram supplyDefaultValue(String name) {

        return new ClientProgram(name, null, null);
    }

    @Override
    protected ClientProgram changeValue(Document config, Element commandElement, ClientProgram oldValue) throws JDOMException {

        String name = oldValue.getName();
        String category = oldValue.getCategory();
        Class<?> type = oldValue.getType();

        Element typeElement = commandElement.getChild("class");
        if (typeElement != null) {
            String typeString = VariableReferenceResolver.process(typeElement.getText(), null);
            type = ParserUtils.parseClass(config, "client program executor class for '" + name + "'", ClientProgramExecutor.class, typeString, type);
        }

        Element categoryElement = commandElement.getChild("category");
        if (categoryElement != null) {
            category = VariableReferenceResolver.process(categoryElement.getText(), null);
        }

        return new ClientProgram(name, category, type);
    }

}
