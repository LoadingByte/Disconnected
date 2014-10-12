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

package com.quartercode.disconnected.shared.client;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.eventbridge.bridge.BridgeConnector;

/**
 * An object that represents an identified client which is connected to a server.
 * That identification is achieved using an authentication system.
 * Once a client is identified, its identity is assigned to its {@link BridgeConnector}, which is used by him to connect to the server.<br>
 * <br>
 * Currently, a client identity just stores a player name.
 */
public class ClientIdentity implements Serializable {

    private final String name;

    /**
     * Creates a new client identity.
     * 
     * @param name The player name of the identified client.
     */
    public ClientIdentity(String name) {

        this.name = name;
    }

    /**
     * Returns the player name of the identified client.
     * 
     * @return The player name.
     */
    public String getName() {

        return name;
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
