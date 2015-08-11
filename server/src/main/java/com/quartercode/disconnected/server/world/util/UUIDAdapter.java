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

package com.quartercode.disconnected.server.world.util;

import java.util.UUID;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * An {@link XmlAdapter} that binds {@link UUID}s using their {@link UUID#toString() string representation}.
 *
 * @see UUID
 */
public class UUIDAdapter extends XmlAdapter<String, UUID> {

    @Override
    public String marshal(UUID v) {

        return v.toString();
    }

    @Override
    public UUID unmarshal(String v) {

        return UUID.fromString(v);
    }

}
