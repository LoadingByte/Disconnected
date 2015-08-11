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

package com.quartercode.disconnected.shared.world.comp.prog;

import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A generic {@link SBPWorldProcessUserDetails} implementation that identifies a world process user using a set {@link UUID}.
 * That UUID might be {@link UUID#randomUUID() randomly generated}.
 *
 * @see SBPWorldProcessUserDetails
 */
public class UUIDSBPWorldProcessUserDetails implements SBPWorldProcessUserDetails {

    private static final long serialVersionUID = -8825526709133357773L;

    private final UUID        uuid;

    /**
     * Creates a new UUID SBP world process user details object.
     *
     * @param uuid The {@link UUID} that uniquely identifies the world process user.
     */
    public UUIDSBPWorldProcessUserDetails(UUID uuid) {

        this.uuid = uuid;
    }

    /**
     * Returns the {@link UUID} that uniquely identifies the world process user.
     *
     * @return The unique id.
     */
    public UUID getUUID() {

        return uuid;
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

}
