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

package com.quartercode.disconnected.shared.world.comp.net.web;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Webpage implements Serializable {

    private final URL                       url;
    private final Map<String, Serializable> attributes;

    public Map<String, Webpage> getNestedWebpages() {

        Map<String, Webpage> nested = new HashMap<>();

        for (Entry<String, Serializable> attribute : attributes.entrySet()) {
            if (attribute.getValue() instanceof Webpage) {
                nested.put(attribute.getKey(), (Webpage) attribute.getValue());
            }
        }

        return nested;
    }

}
