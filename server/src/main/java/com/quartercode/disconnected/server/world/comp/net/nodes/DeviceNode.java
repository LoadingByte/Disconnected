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
 * A version of the {@link UnspecializedNode} which represents a typical end-user device.
 * Such a device might be a simple PC or a server, but it might also be a phone with a connection to the Internet.
 * It is important to note that each device can only connect to one other {@link NetNode}.
 * Therefore, it is necessary to connect devices to {@link UplinkRouterNode}s or {@link BridgeNode}s.
 */
public class DeviceNode extends UnspecializedNode {

    @Override
    public int getMaxConnections() {

        return 1;
    }

}
