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
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;

/**
 * An adapter class for {@link SchedulerTask} which implements the cancellation flag.
 * It extends {@link DefaultCFeatureHolder} for implementing the feature holder functionality.
 * 
 * @see SchedulerTask
 */
public abstract class SchedulerTaskAdapter extends DefaultCFeatureHolder implements SchedulerTask {

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
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (cancelled ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null || ! (obj instanceof SchedulerTaskAdapter) || !super.equals(obj)) {
            return false;
        } else {
            SchedulerTaskAdapter other = (SchedulerTaskAdapter) obj;
            return cancelled == other.cancelled;
        }
    }

    // Do not override toString()

}
