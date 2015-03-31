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

import java.io.Serializable;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A data object that stores information in order to identify a server-side world process that runs a program.
 */
public class WorldProcessId implements Serializable {

    private static final long serialVersionUID = -6220241091232315812L;

    private final UUID        computerUUID;
    private final int         pid;

    /**
     * Creates a new world process id.
     * 
     * @param computerUUID The {@link UUID} of the computer which runs the identifiable process.
     * @param pid The process id of the identifiable process.
     */
    public WorldProcessId(UUID computerUUID, int pid) {

        this.computerUUID = computerUUID;
        this.pid = pid;
    }

    /**
     * Returns the {@link UUID} of the computer which runs the identifiable process.
     * 
     * @return The computer UUID.
     */
    public UUID getComputerUUID() {

        return computerUUID;
    }

    /**
     * Returns the process id of the identifiable process.
     * 
     * @return The process id.
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
