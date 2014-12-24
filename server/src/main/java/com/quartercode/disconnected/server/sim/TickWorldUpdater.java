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

package com.quartercode.disconnected.server.sim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.disconnected.server.registry.SchedulerGroup;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistry;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;
import com.quartercode.disconnected.shared.util.registry.Registries;

/**
 * This class updates the {@link World} it stores.
 * It does that by updating all available {@link Scheduler}s which are registered inside the world's {@link SchedulerRegistry}.
 * 
 * @see Scheduler
 */
public class TickWorldUpdater implements TickAction {

    private List<String>   sortedGroups;
    private volatile World world;

    /**
     * Returns the {@link World} that is currently updated by the tick world updater.
     * 
     * @return The currently updated world.
     */
    public World getWorld() {

        return world;
    }

    /**
     * Changes the {@link World} that is currently updated by the tick world updater.
     * The change will take place in the next tick.
     * 
     * @param world The new world to updated.
     */
    public void setWorld(World world) {

        this.world = world;
    }

    /**
     * Executes the tick update on the set {@link World}.
     * That is done by updating all available {@link Scheduler}s which are registered inside the world's {@link SchedulerRegistry}.
     */
    @Override
    public void update() {

        if (world != null) {
            // Remove the schedulers that are no longer part of the world tree
            // That is determined by checking whether they are able to retrieve the world root object
            Iterator<Scheduler> schedulerIterator = world.getSchedulerRegistry().getNewModifiableSchedulersIterator();
            while (schedulerIterator.hasNext()) {
                FeatureHolder schedulerHolder = schedulerIterator.next().getHolder();
                if (schedulerHolder instanceof WorldChildFeatureHolder && ((WorldChildFeatureHolder<?>) schedulerHolder).getWorld() == null) {
                    schedulerIterator.remove();
                }
            }

            // Update each scheduler with each group in the correct order
            Collection<Scheduler> schedulers = world.getSchedulerRegistry().getSchedulers();
            for (String group : getSortedGroups()) {
                for (Scheduler scheduler : schedulers) {
                    scheduler.update(group);
                }
            }
        }
    }

    private List<String> getSortedGroups() {

        if (sortedGroups == null) {
            List<SchedulerGroup> groupObjects = new ArrayList<>(Registries.get(ServerRegistries.SCHEDULER_GROUPS).getValues());
            Collections.sort(groupObjects, new Comparator<SchedulerGroup>() {

                @Override
                public int compare(SchedulerGroup o1, SchedulerGroup o2) {

                    return Integer.compare(o2.getPriority(), o1.getPriority());
                }

            });

            sortedGroups = new ArrayList<>();
            for (SchedulerGroup entry : groupObjects) {
                sortedGroups.add(entry.getName());
            }
        }

        return sortedGroups;
    }

}
