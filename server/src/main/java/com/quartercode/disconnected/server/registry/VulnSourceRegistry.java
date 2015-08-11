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

package com.quartercode.disconnected.server.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;

/**
 * A {@link SetRegistry} that stores {@link VulnSource vulnerability source} objects and makes them accessible through some special methods.
 *
 * @see VulnSource
 * @see SetRegistry
 */
public class VulnSourceRegistry extends SetRegistry<VulnSource> {

    // A cache that maps usage prefixes to vulnerability sources with that prefix
    private transient Map<String, Set<VulnSource>> usageCache = new HashMap<>();

    /**
     * Returns all {@link VulnSource vulnerability source} objects whose usage string starts with the given nodes.
     * The method concatenates the provided usage nodes with a {@code .} as separator and then checks which source objects
     * have usage strings that start with that joined string.
     * See {@link VulnSource#getUsage()} for more information about the purpose of the usage string.
     *
     * @param usageNodes The usage strings of all returned sources must start with these nodes.
     *        They are concatenated with the {@code .} separator.
     * @return All vulnerability sources that start with the given usage string nodes.
     * @see VulnSource#getUsage()
     */
    public Set<VulnSource> getValuesByUsage(String... usageNodes) {

        Validate.notEmpty(usageNodes, "Usage node array cannot be empty");

        String usagePrefix = StringUtils.join(usageNodes, ".");

        if (usageCache.containsKey(usagePrefix)) {
            return usageCache.get(usagePrefix);
        } else {
            Set<VulnSource> usageValues = new HashSet<>();

            for (VulnSource value : getValues()) {
                if (value.getUsage().startsWith(usagePrefix)) {
                    usageValues.add(value);
                }
            }

            usageCache.put(usagePrefix, usageValues);
            return usageValues;
        }
    }

    @Override
    public void addValue(VulnSource value) {

        super.addValue(value);

        // Invalidate the usage cache
        usageCache.clear();
    }

    @Override
    public void removeValue(VulnSource value) {

        // Invalidate the usage cache
        usageCache.clear();
    }

}
