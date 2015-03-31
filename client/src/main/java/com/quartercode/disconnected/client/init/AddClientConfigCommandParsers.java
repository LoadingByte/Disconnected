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

package com.quartercode.disconnected.client.init;

import com.quartercode.disconnected.client.registry.ClientRegistries;
import com.quartercode.disconnected.client.registry.config.ConfigureClientProgramCommand;
import com.quartercode.disconnected.client.registry.config.ConfigureThemeCommand;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.config.ConfigService;
import com.quartercode.disconnected.shared.util.config.extra.RemoveNamedValueCommand;
import com.quartercode.disconnected.shared.util.init.Initializer;
import com.quartercode.disconnected.shared.util.init.InitializerSettings;
import com.quartercode.disconnected.shared.util.registry.Registries;

@InitializerSettings (groups = "addConfigCommands", dependencies = "registerServices")
public class AddClientConfigCommandParsers implements Initializer {

    @Override
    public void initialize() {

        ConfigService service = ServiceRegistry.lookup(ConfigService.class);

        // Themes
        service.addCommand("configureTheme", new ConfigureThemeCommand(Registries.get(ClientRegistries.THEMES)));
        service.addCommand("removeTheme", new RemoveNamedValueCommand<>("theme", Registries.get(ClientRegistries.THEMES)));

        // Client programs
        service.addCommand("configureClientProgram", new ConfigureClientProgramCommand(Registries.get(ClientRegistries.CLIENT_PROGRAMS)));
        service.addCommand("removeClientProgram", new RemoveNamedValueCommand<>("client program", Registries.get(ClientRegistries.CLIENT_PROGRAMS)));
    }

}
