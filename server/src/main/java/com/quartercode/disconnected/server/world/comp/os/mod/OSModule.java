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

package com.quartercode.disconnected.server.world.comp.os.mod;

import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;

/**
 * The base interface for all {@link OperatingSystem} modules.
 * Such OS modules are an essential part of the operating system.
 * They define methods which are required by the operating system to interact with these modules.
 *
 * @see #setRunning(boolean)
 * @see OperatingSystem
 */
public interface OSModule {

    /**
     * Called on the bootstrap ({@code running = true}) or shutdown ({@code running = false}) of the {@link OperatingSystem} which uses the module.
     * <b>Directly after the construction of a new OS module, it is not running!</b>
     *
     * @param running Whether the OS is bootstrapping ({@code true}) or shutting down ({@code false}).
     */
    public void setRunning(boolean running);

}
