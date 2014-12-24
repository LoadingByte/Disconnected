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

import com.quartercode.disconnected.server.sim.TickBridgeProvider;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.TickWorldUpdater;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.init.Initializer;
import com.quartercode.disconnected.shared.util.init.InitializerSettings;

@InitializerSettings (groups = "initializeServices", dependencies = { "registerServices", "addEventBridgeFactoryMappings" })
public class InitializeTickService implements Initializer {

    @Override
    public void initialize() {

        TickService service = ServiceRegistry.lookup(TickService.class);

        service.addAction(new TickBridgeProvider());
        service.addAction(new TickWorldUpdater());
    }

}
