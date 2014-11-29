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
import com.quartercode.disconnected.server.registry.PersistentClassScanDirective;
import com.quartercode.disconnected.server.registry.PersistentClassScanDirective.ScanMethod;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.ConfigCommandParser;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;

@RequiredArgsConstructor
public class AddPersistentClassScanDirectiveCommand implements ConfigCommandParser {

    private static final Logger                             LOGGER = LoggerFactory.getLogger(AddPersistentClassScanDirectiveCommand.class);

    private final SetRegistry<PersistentClassScanDirective> registry;

    @Override
    public void parse(Document config, Element commandElement) throws JDOMException {

        String packages = commandElement.getText();
        String method = commandElement.getAttributeValue("method");

        if (StringUtils.isBlank(packages)) {
            LOGGER.warn("Config: Cannot add persistent class scan directive(s) with blank package(s) (in '{}')", config.getBaseURI());
        } else if (StringUtils.isBlank(method)) {
            LOGGER.warn("Config: Cannot add persistent class scan directive with blank method (in '{}')", config.getBaseURI());
        } else {
            ScanMethod methodObject = ScanMethod.valueOfCamelCase(method);
            if (methodObject == null) {
                LOGGER.warn("Config: Cannot add persistent class scan directive with unknown method '{}' (in '{}')", method, config.getBaseURI());
            } else {
                String[] packageArray = StringUtils.split(VariableReferenceResolver.process(packages, null), ':');
                for (String packageName : packageArray) {
                    registry.addValue(new PersistentClassScanDirective(packageName, methodObject));
                }
            }
        }
    }

}
