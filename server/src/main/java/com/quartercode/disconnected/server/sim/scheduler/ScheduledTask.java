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
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * An internal data class that stores the various values which are assigned to a {@link SchedulerTask} (e.g. {@code name}).
 * 
 * @see SchedulerTask
 */
@XmlPersistent
class ScheduledTask {

    @XmlAttribute
    private String        name;
    @XmlAttribute
    private String        group;
    @XmlAttribute
    private int           initialDelay;
    @XmlAttribute
    private int           periodicDelay;

    private SchedulerTask task;

    // JAXB constructor
    protected ScheduledTask() {

    }

    public ScheduledTask(String name, String group, int initialDelay, int periodicDelay, SchedulerTask task) {

        Validate.notBlank(group, "Scheduler task group cannot be blank");
        Validate.isTrue(initialDelay > 0, "Scheduler task initial delay (%d) must be > 0", initialDelay);
        Validate.isTrue(periodicDelay == -1 || periodicDelay > 0, "Scheduler task periodic delay (%d) must be -1 or > 0", periodicDelay);
        Validate.notNull(task, "Cannot schedule null task");

        this.name = name;
        this.group = group;
        this.initialDelay = initialDelay;
        this.periodicDelay = periodicDelay;

        this.task = task.cloneStateless();
    }

    public String getName() {

        return name;
    }

    public String getGroup() {

        return group;
    }

    public int getInitialDelay() {

        return initialDelay;
    }

    public int getPeriodicDelay() {

        return periodicDelay;
    }

    public boolean isPeriodic() {

        return periodicDelay > 0;
    }

    public SchedulerTask getTask() {

        return task;
    }

    // Use an object reference because JAXB doesn't support interfaces and SchedulerTask is an interface

    @XmlElement (name = "task")
    protected Object getTaskAsObject() {

        return task;
    }

    protected void setTaskAsObject(Object task) {

        this.task = (SchedulerTask) task;
    }

}
