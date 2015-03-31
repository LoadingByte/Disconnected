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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.registry.VulnSource;
import com.quartercode.disconnected.server.registry.VulnSource.Action;
import com.quartercode.disconnected.server.util.NullPreventer;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.extra.ConfigureNamedValueCommand;
import com.quartercode.disconnected.shared.util.config.extra.ParserUtils;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;

public class ConfigureVulnSourceCommand extends ConfigureNamedValueCommand<VulnSource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureVulnSourceCommand.class);

    public ConfigureVulnSourceCommand(SetRegistry<VulnSource> registry) {

        super("vulnerability source", registry);
    }

    @Override
    protected VulnSource supplyDefaultValue(String name) {

        return new VulnSource(name, null, 1, Collections.<Action> emptyList());
    }

    @Override
    protected VulnSource changeValue(Document config, Element commandElement, VulnSource oldValue) throws JDOMException {

        String name = oldValue.getName();
        String usage = oldValue.getUsage();
        int weight = oldValue.getWeight();
        List<Action> actions = new ArrayList<>(oldValue.getActions());

        // Parse usage string
        Element usageElement = commandElement.getChild("usage");
        if (usageElement != null) {
            String usageString = VariableReferenceResolver.process(usageElement.getText(), null);
            if (StringUtils.isBlank(usageString)) {
                LOGGER.warn("Config: Cannot use blank string as vulnerability source usage for '{}' (in '{}')", name, config.getBaseURI());
            } else {
                usage = usageString;
            }
        }

        // Parse weight number
        Element weightElement = commandElement.getChild("weight");
        if (weightElement != null) {
            String weightString = VariableReferenceResolver.process(weightElement.getText(), null);
            weight = (int) ParserUtils.parsePositiveNumber(config, "vulnerability source weight for '" + name + "'", weightString, weight);
        }

        // Parse action map
        parseActions(config, name, commandElement, actions);

        return new VulnSource(name, usage, weight, actions);
    }

    private void parseActions(Document config, String parentName, Element parentElement, List<Action> actions) {

        // Parse all "configureAction" elements
        for (Element configureActionElement : parentElement.getChildren("configureAction")) {
            // Parse name
            String name = VariableReferenceResolver.process(NullPreventer.prevent(configureActionElement.getAttributeValue("name")), null);
            if (StringUtils.isBlank(name)) {
                LOGGER.warn("Config: Cannot configure vulnerability source action with blank name for '{}' (in '{}')", parentName, config.getBaseURI());
                continue;
            }

            float vulnChance = 0;
            int attackWeight = 1;

            // Read old action with the same name and use its values as default ones (if it already exists)
            Action oldAction = NamedValueUtils.getByName(actions, name);
            if (oldAction != null) {
                vulnChance = oldAction.getVulnProbability();
                attackWeight = oldAction.getAttackWeight();
            }

            // Parse vulnerability probability
            String vulnChanceString = configureActionElement.getAttributeValue("vulnChance");
            if (vulnChanceString != null) {
                vulnChanceString = VariableReferenceResolver.process(vulnChanceString, null);
                vulnChance = (float) ParserUtils.parseDecimalNumber(config, "vulnerability source vuln chance for action '" + name + "' in '" + parentName + "'", vulnChanceString, vulnChance);
            }

            // Parse attack weight
            String attackWeightString = configureActionElement.getAttributeValue("attackWeight");
            if (attackWeightString != null) {
                attackWeightString = VariableReferenceResolver.process(attackWeightString, null);
                attackWeight = (int) ParserUtils.parsePositiveNumber(config, "vulnerability source attack weight for action '" + name + "' in '" + parentName + "'", attackWeightString, attackWeight);
            }

            // Remove the old action with the same name (if it already exists)
            if (oldAction != null) {
                actions.remove(oldAction);
            }

            // Add the new action
            actions.add(new Action(name, vulnChance, attackWeight));
        }

        // Parse all "removeAction" elements
        for (Element removeActionElement : parentElement.getChildren("removeAction")) {
            // Parse name
            String name = VariableReferenceResolver.process(NullPreventer.prevent(removeActionElement.getAttributeValue("name")), null);
            if (StringUtils.isBlank(name)) {
                LOGGER.warn("Config: Cannot remove vulnerability source action with blank name from '{}' (in '{}')", parentName, config.getBaseURI());
                continue;
            }

            // Remove parsed action
            Action oldAction = NamedValueUtils.getByName(actions, name);
            if (oldAction != null) {
                actions.remove(oldAction);
            }
        }
    }

}
