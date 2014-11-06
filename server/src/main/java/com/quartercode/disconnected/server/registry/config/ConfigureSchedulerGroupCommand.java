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
import com.quartercode.disconnected.server.registry.SchedulerGroup;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;
import com.quartercode.disconnected.shared.util.config.extra.ConfigureNamedValueCommand;
import com.quartercode.disconnected.shared.util.config.extra.ParserUtils;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;

public class ConfigureSchedulerGroupCommand extends ConfigureNamedValueCommand<SchedulerGroup> {

    public ConfigureSchedulerGroupCommand(SetRegistry<SchedulerGroup> registry) {

        super("scheduler group", registry);
    }

    @Override
    protected SchedulerGroup supplyDefaultValue(String name) {

        return new SchedulerGroup(name, 0);
    }

    @Override
    protected SchedulerGroup changeValue(Document config, Element commandElement, SchedulerGroup oldValue) {

        String name = oldValue.getName();
        int priority = oldValue.getPriority();

        Element priorityElement = commandElement.getChild("priority");
        if (priorityElement != null) {
            String priorityString = VariableReferenceResolver.process(priorityElement.getText());
            priority = (int) ParserUtils.parsePositiveNumber(config, "scheduler group priority for '" + name + "'", priorityString, priority);
        }

        return new SchedulerGroup(name, priority);
    }

}
