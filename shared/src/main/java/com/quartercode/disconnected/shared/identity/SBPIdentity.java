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

package com.quartercode.disconnected.shared.identity;

import java.io.Serializable;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;

/**
 * A server bridge identity represents something which is connected to a server using a {@link Bridge}.
 * Once a server bridge partner is identified, its identity is assigned to its {@link BridgeConnector}, which is used by him to connect to the server.<br>
 * <br>
 * For example, a graphical client used by players could be a server bridge partner.
 * Another possibility is a modue that connects with the server and implements a certain functionality (e.g. AI).
 */
public interface SBPIdentity extends Serializable {

}
