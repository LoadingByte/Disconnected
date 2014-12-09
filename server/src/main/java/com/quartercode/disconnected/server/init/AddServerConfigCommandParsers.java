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

package com.quartercode.disconnected.server.init;

import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.config.AddPersistentClassScanDirectiveCommand;
import com.quartercode.disconnected.server.registry.config.AddWorldInitializerMappingCommand;
import com.quartercode.disconnected.server.registry.config.ConfigureFileTypeCommand;
import com.quartercode.disconnected.server.registry.config.ConfigureSchedulerGroupCommand;
import com.quartercode.disconnected.server.registry.config.ConfigureVulnSourceCommand;
import com.quartercode.disconnected.server.registry.config.ConfigureWorldProgramCommand;
import com.quartercode.disconnected.server.registry.config.RemovePersistentClassScanDirectiveCommand;
import com.quartercode.disconnected.server.registry.config.RemoveWorldInitializerMappingCommand;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.config.ConfigService;
import com.quartercode.disconnected.shared.util.config.extra.RemoveNamedValueCommand;
import com.quartercode.disconnected.shared.util.init.Initializer;
import com.quartercode.disconnected.shared.util.init.InitializerSettings;
import com.quartercode.disconnected.shared.util.registry.Registries;

@InitializerSettings (groups = "addConfigCommands", dependencies = "registerServices")
public class AddServerConfigCommandParsers implements Initializer {

    @Override
    public void initialize() {

        ConfigService service = ServiceRegistry.lookup(ConfigService.class);

        // World context path
        service.addCommand("addPersistentClassScanDirective", new AddPersistentClassScanDirectiveCommand(Registries.get(ServerRegistries.PERSISTENT_CLASS_SCAN_DIRECTIVES)));
        service.addCommand("removePersistentClassScanDirective", new RemovePersistentClassScanDirectiveCommand(Registries.get(ServerRegistries.PERSISTENT_CLASS_SCAN_DIRECTIVES)));

        // World initializer mappings
        service.addCommand("addWorldInitializerMapping", new AddWorldInitializerMappingCommand(Registries.get(ServerRegistries.WORLD_INITIALIZER_MAPPINGS)));
        service.addCommand("removeWorldInitializerMapping", new RemoveWorldInitializerMappingCommand(Registries.get(ServerRegistries.WORLD_INITIALIZER_MAPPINGS)));

        // Scheduler groups
        service.addCommand("configureSchedulerGroup", new ConfigureSchedulerGroupCommand(Registries.get(ServerRegistries.SCHEDULER_GROUPS)));
        service.addCommand("removeSchedulerGroup", new RemoveNamedValueCommand<>("scheduler group", Registries.get(ServerRegistries.SCHEDULER_GROUPS)));

        // File types
        service.addCommand("configureFileType", new ConfigureFileTypeCommand(Registries.get(ServerRegistries.FILE_TYPES)));
        service.addCommand("removeFileType", new RemoveNamedValueCommand<>("file type", Registries.get(ServerRegistries.FILE_TYPES)));

        // World programs
        service.addCommand("configureWorldProgram", new ConfigureWorldProgramCommand(Registries.get(ServerRegistries.WORLD_PROGRAMS)));
        service.addCommand("removeWorldProgram", new RemoveNamedValueCommand<>("world program", Registries.get(ServerRegistries.WORLD_PROGRAMS)));

        // Vuln sources
        service.addCommand("configureVulnSource", new ConfigureVulnSourceCommand(Registries.get(ServerRegistries.VULN_SOURCES)));
        service.addCommand("removeVulnSource", new RemoveNamedValueCommand<>("vulnerability source", Registries.get(ServerRegistries.VULN_SOURCES)));
    }

}
