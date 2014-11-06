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

import java.net.URL;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import com.quartercode.disconnected.client.registry.Theme;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.extra.ConfigureNamedValueCommand;
import com.quartercode.disconnected.shared.util.config.extra.ParserUtils;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;

public class ConfigureThemeCommand extends ConfigureNamedValueCommand<Theme> {

    public ConfigureThemeCommand(SetRegistry<Theme> registry) {

        super("theme", registry);
    }

    @Override
    protected Theme supplyDefaultValue(String name) {

        return new Theme(name, null, 0);
    }

    @Override
    protected Theme changeValue(Document config, Element commandElement, Theme oldValue) throws JDOMException {

        String name = oldValue.getName();
        URL url = oldValue.getURL();
        int priority = oldValue.getPriority();

        Element urlElement = commandElement.getChild("url");
        if (urlElement != null) {
            String urlString = VariableReferenceResolver.process(urlElement.getText());
            url = ParserUtils.parseURL(config, "theme URL for '" + name + "'", urlString, url);
        }

        Element priorityElement = commandElement.getChild("priority");
        if (priorityElement != null) {
            String priorityString = VariableReferenceResolver.process(priorityElement.getText());
            priority = (int) ParserUtils.parsePositiveNumber(config, "theme priority for '" + name + "'", priorityString, priority);
        }

        return new Theme(name, url, priority);
    }

}
