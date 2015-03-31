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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.disconnected.shared.identity.SBPIdentity;

/**
 * A data object that identifies an SBP-side module which uses a certain server-side world process from the perspective of a server.
 * For example, a client-side module could use the server-side world process to render the program results in a graphical window.<br>
 * <br>
 * Such a world process user id consists of two elements.
 * Firstly, an {@link SBPIdentity} identifies the server bridge partner who uses the world process.
 * That identification is used by the server to send the correct events to the world process user.
 * Secondly, an {@link SBPWorldProcessUserDetails} object identifies which module on the SBP-side uses the world process.
 * It is used by the SBP to pass incoming events to the correct module.
 * 
 * @see SBPIdentity
 * @see SBPWorldProcessUserDetails
 */
public class SBPWorldProcessUserId implements Serializable {

    private static final long                serialVersionUID = 5261470732278716675L;

    private final SBPIdentity                sbp;
    private final SBPWorldProcessUserDetails details;

    /**
     * Creates a new server bridge partner world process user id.
     * 
     * @param sbp The {@link SBPIdentity} of the server bridge partner who uses the world process.
     * @param details An {@link SBPWorldProcessUserDetails} object that identifies which part of the SBP uses the world process.
     */
    public SBPWorldProcessUserId(SBPIdentity sbp, SBPWorldProcessUserDetails details) {

        this.sbp = sbp;
        this.details = details;
    }

    /**
     * Returns the {@link SBPIdentity} which identifies the server bridge partner who uses the world process.
     * That identification is used by the server to send the correct events to the world process user.
     * 
     * @return The SBP who uses the world process.
     */
    public SBPIdentity getSBP() {

        return sbp;
    }

    /**
     * Returns an {@link SBPWorldProcessUserDetails} object which identifies which module on the SBP-side uses the world process.
     * It is used by the SBP to pass incoming events to the correct module.
     * 
     * @return The SBP part which uses the world process.
     */
    public SBPWorldProcessUserDetails getDetails() {

        return details;
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
