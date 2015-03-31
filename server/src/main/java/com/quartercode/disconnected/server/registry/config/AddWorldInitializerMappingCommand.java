/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.ConfigCommandParser;
import com.quartercode.disconnected.shared.util.config.extra.ParserUtils;
import com.quartercode.disconnected.shared.util.registry.extra.MapRegistry;
import com.quartercode.disconnected.shared.util.registry.extra.MapRegistry.DefaultMapping;
import com.quartercode.disconnected.shared.util.registry.extra.MappedValueRegistry.Mapping;

@RequiredArgsConstructor
public class AddWorldInitializerMappingCommand implements ConfigCommandParser {

    private static final Logger                                                     LOGGER = LoggerFactory.getLogger(AddWorldInitializerMappingCommand.class);

    private final MapRegistry<Class<? extends FeatureHolder>, FeatureDefinition<?>> registry;

    @Override
    public void parse(Document config, Element commandElement) throws JDOMException {

        Mapping<Class<? extends FeatureHolder>, FeatureDefinition<?>> mapping = parseMapping(config, commandElement);

        if (mapping != null) {
            registry.addValue(mapping);
        }
    }

    protected static Mapping<Class<? extends FeatureHolder>, FeatureDefinition<?>> parseMapping(Document config, Element parent) {

        Element holderTypeElement = parent.getChild("holderType");
        Element featureElement = parent.getChild("feature");

        if (holderTypeElement == null || StringUtils.isBlank(holderTypeElement.getText())) {
            LOGGER.warn("Config: Cannot use blank holder type for world initializer mapping (in '{}')", config.getBaseURI());
        } else if (featureElement == null || StringUtils.isBlank(featureElement.getText())) {
            LOGGER.warn("Config: Cannot use blank feature for world initializer mapping (in '{}')", config.getBaseURI());
        } else {
            String holderTypeString = VariableReferenceResolver.process(holderTypeElement.getText(), null);
            Class<? extends FeatureHolder> holderType = ParserUtils.parseGenericClass(config, "holder type for world initializer mapping", FeatureHolder.class, holderTypeString, null);

            FeatureDefinition<?> featureDefinition = null;
            try {
                String featureDefinitionString = VariableReferenceResolver.process(featureElement.getText(), null);
                featureDefinition = (FeatureDefinition<?>) ParserUtils.parseConstant(config, "feature definition field for world initializer mapping", FeatureDefinition.class, featureDefinitionString, null);
            } catch (ClassCastException e) {
                LOGGER.warn("Config: Static feature definition field '{}' does not contain an instance of FeatureDefinition (in '{}')", featureElement.getText(), config.getBaseURI());
            }

            if (holderType != null && featureDefinition != null) {
                return new DefaultMapping<Class<? extends FeatureHolder>, FeatureDefinition<?>>(holderType, featureDefinition);
            }
        }

        return null;
    }

}
