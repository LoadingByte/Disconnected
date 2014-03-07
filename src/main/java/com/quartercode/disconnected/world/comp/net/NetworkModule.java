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

package com.quartercode.disconnected.world.comp.net;

import com.quartercode.disconnected.world.comp.os.OSModule;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;

/**
 * This class represents an {@link OperatingSystem} module which is used to send and receive network {@link Packet}s.
 * The module also abstracts the concept of {@link Packet}s and introduces connections for easier data transfer.
 * It is an essential part of the {@link OperatingSystem} and is directly used by it.
 * 
 * @see Packet
 * @see OSModule
 * @see OperatingSystem
 */
public class NetworkModule extends OSModule {

    // TODO: Fill this with life when the new network system is done

    /**
     * Creates a new network module.
     */
    public NetworkModule() {

    }

}
