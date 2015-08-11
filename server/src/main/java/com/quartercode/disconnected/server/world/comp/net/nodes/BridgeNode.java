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

package com.quartercode.disconnected.server.world.comp.net.nodes;

import com.quartercode.disconnected.server.world.comp.net.NetNode;

/**
 * A version of the {@link UnspecializedNode} which only functions as a network "hub" or "switch".
 * Such "switches" are able connecting multiple {@link DeviceNode}s together.
 * That is necessary because those device nodes can only have one connection, while bridges are allowed to connect to multiple nodes.
 * Moreover, bridges are able to create firewalls which are located in front of entire groups of other {@link NetNode}s.
 * However, in practice bridges are barely used and multiple devices are instead directly connected to {@link UplinkRouterNode}s (which can also provide firewalls).
 */
public class BridgeNode extends UnspecializedNode {

    @Override
    public int getMaxConnections() {

        return INFINITY;
    }

}
