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

package com.quartercode.disconnected.bridge;

import java.io.Serializable;

/**
 * Events are notifications that are sent between the client and the server.
 * The client is the graphical user interface the user can interact with,
 * while the server stores and runs the actual object hierarchy of the simulation.
 * Events should be immutable.<br>
 * <br>
 * Events must be {@link Serializable} so they can be stored or sent over the network.
 * On the second bridge, {@link EventHandler}s receive the event and might take action depending on the event.
 * Finally, {@link EventPredicate}s are a tool for asserting different properties of events.
 * 
 * @see EventHandler
 * @see EventPredicate
 */
public interface Event extends Serializable {

}
