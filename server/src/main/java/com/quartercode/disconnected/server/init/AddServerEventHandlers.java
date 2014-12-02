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

import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.event.program.control.WorldProcessInterruptCommandHandler;
import com.quartercode.disconnected.server.event.program.control.WorldProcessLaunchCommandHandler;
import com.quartercode.disconnected.server.sim.TickBridgeProvider;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.shared.event.comp.program.control.WorldProcessInterruptCommand;
import com.quartercode.disconnected.shared.event.comp.program.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.init.Initializer;
import com.quartercode.disconnected.shared.util.init.InitializerSettings;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

@InitializerSettings (groups = "addEventHandlers", dependencies = "initializeServices")
public class AddServerEventHandlers implements Initializer {

    @Override
    public void initialize() {

        Bridge bridge = ServiceRegistry.lookup(TickService.class).getAction(TickBridgeProvider.class).getBridge();

        addSBPAwareEventHandler(bridge, new WorldProcessLaunchCommandHandler(), new TypePredicate<>(WorldProcessLaunchCommand.class));
        addSBPAwareEventHandler(bridge, new WorldProcessInterruptCommandHandler(), new TypePredicate<>(WorldProcessInterruptCommand.class));
    }

    private static void addSBPAwareEventHandler(Bridge bridge, SBPAwareEventHandler<?> handler, EventPredicate<?> predicate) {

        bridge.getModule(SBPAwareHandlerExtension.class).addHandler(handler, predicate);
    }

}
