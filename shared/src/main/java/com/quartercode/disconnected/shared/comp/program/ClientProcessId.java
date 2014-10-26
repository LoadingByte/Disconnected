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

package com.quartercode.disconnected.shared.comp.program;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.disconnected.shared.client.ClientIdentity;

/**
 * A data object that stores information in order to identify a client-side client process that runs a graphical client program.
 */
public class ClientProcessId implements Serializable {

    private final ClientIdentity client;
    private final int            pid;

    /**
     * Creates a new client process id.
     * 
     * @param client The {@link ClientIdentity} of the client that runs the identifiable client process.
     * @param pid The unique id of the identifiable client process.
     */
    public ClientProcessId(ClientIdentity client, int pid) {

        this.client = client;
        this.pid = pid;
    }

    /**
     * Returns the {@link ClientIdentity} of the client that runs the identifiable client process.
     * 
     * @return The identity of the client.
     */
    public ClientIdentity getClient() {

        return client;
    }

    /**
     * Returns the unique id of the identifiable client process.
     * Most likely it is just a sequential number.
     * 
     * @return The client process id.
     */
    public int getPid() {

        return pid;
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
