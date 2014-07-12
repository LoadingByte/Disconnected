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

package com.quartercode.disconnected.util.storage;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * The service registry class stores registered service instances and makes them available through a lookup method.
 */
public class ServiceRegistry {

    private static Map<Class<?>, Object> services = new HashMap<>();

    /**
     * Looks up the registered service implementation for the given service specification.
     * 
     * @param service The service specification which defines the service.
     * @return The service implementation for the given specification.
     */
    public static <T> T lookup(Class<T> service) {

        Object implementation = services.get(service);
        return implementation == null ? null : service.cast(implementation);
    }

    /**
     * Registers the given service implementation instance to the service registry and makes it available.
     * 
     * @param service The service specification interface which defines the service.
     * @param implementation The actual implementation of the service that can be retrieved through {@link #lookup(Class)}.
     */
    public static <T> void register(Class<? super T> service, T implementation) {

        Validate.notNull(service, "Cannot register null service");
        services.put(service, implementation);
    }

    private ServiceRegistry() {

    }

}
