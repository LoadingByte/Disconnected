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

package com.quartercode.disconnected.client.registry;

import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.disconnected.shared.util.registry.Registry;
import com.quartercode.disconnected.shared.util.registry.RegistryDefinition;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;

/**
 * A class that stores the default {@link RegistryDefinition}s which define the default {@link Registry}s used by the client.
 * 
 * @see RegistryDefinition
 * @see Registry
 */
public class ClientRegistries {

    /**
     * A map that maps client program names (e.g. {@code fileManager}) to client program descriptors.
     * All descriptors that should be available in the launch menu must be registered here.<br>
     * <br>
     * Client programs are the counterpart to regular world programs that run on a server.
     * They only have a GUI and don't implement any logic.
     * That means that all logic is performed by the server-side world processes. The results are then sent to the client programs.
     * In order to establish the client-server program connection, client programs create normal world processes on the server.
     */
    public static final RegistryDefinition<SetRegistry<ClientProgram>> CLIENT_PROGRAMS;

    /**
     * The {@link Theme}s that should be loaded into the twl theme manager.
     * Note that only loaded themes can be used by graphical components.
     */
    public static final RegistryDefinition<SetRegistry<Theme>>         THEMES;

    static {

        CLIENT_PROGRAMS = new RegistryDefinition<>("clientPrograms", new TypeLiteral<SetRegistry<ClientProgram>>() {});
        THEMES = new RegistryDefinition<>("themes", new TypeLiteral<SetRegistry<Theme>>() {});

    }

    private ClientRegistries() {

    }

}
