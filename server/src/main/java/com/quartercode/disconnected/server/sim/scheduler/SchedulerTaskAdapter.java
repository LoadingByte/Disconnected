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

package com.quartercode.disconnected.server.sim.scheduler;

import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An adapter class for {@link SchedulerTask} which implements common behavior.
 * 
 * @see SchedulerTask
 */
public abstract class SchedulerTaskAdapter implements SchedulerTask, Cloneable {

    @XmlAttribute
    private boolean cancelled;

    @Override
    public boolean isCancelled() {

        return cancelled;
    }

    @Override
    public void cancel() {

        cancelled = true;
    }

    @Override
    public SchedulerTaskAdapter clone() throws CloneNotSupportedException {

        return (SchedulerTaskAdapter) super.clone();
    }

    @Override
    public SchedulerTask cloneStateless() {

        SchedulerTaskAdapter clone = ObjectUtils.clone(this);
        clone.cancelled = false;

        return clone;
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
