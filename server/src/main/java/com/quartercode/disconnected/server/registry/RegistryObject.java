
package com.quartercode.disconnected.server.registry;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This class simply implements reflection versions of {@link #hashCode()}, {@link #equals(Object)} and {@link #toString()} and can be extended in order to use those methods.
 * Actually, this class just avoids defining calls to the reflection-using methods over and over again.
 */
class RegistryObject {

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
