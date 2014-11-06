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

package com.quartercode.disconnected.shared.world.comp.program;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A data object that identifies a client-side client process which runs a graphical client program.
 * 
 * @see SBPWorldProcessUserDetails
 */
public class ClientProcessDetails implements SBPWorldProcessUserDetails {

    private final int clientPid;

    /**
     * Creates a new client process details object.
     * 
     * @param clientPid The unique id of the identifiable client process.
     */
    public ClientProcessDetails(int clientPid) {

        this.clientPid = clientPid;
    }

    /**
     * Returns the unique id of the identifiable client process.
     * Most likely it is just a sequential number.
     * 
     * @return The client process id.
     */
    public int getClientPid() {

        return clientPid;
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
