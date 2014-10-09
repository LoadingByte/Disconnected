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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.quartercode.classmod.base.Feature;
import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.classmod.extra.ValueSupplier;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerUser;
import com.quartercode.disconnected.server.world.World;

/**
 * This class updates all {@link Scheduler}s of {@link World} {@link CFeatureHolder}s which implement the {@link SchedulerUser} interface.
 * 
 * @see SchedulerUser
 */
public class TickSchedulerUpdater implements TickAction {

    private final Map<String, Integer>    groups       = new HashMap<>();
    private final List<String>            sortedGroups = new ArrayList<>();

    private volatile WeakReference<World> world;

    // ----- Static -----

    /**
     * Returns the defined groups with their priorities.
     * 
     * @return All defined groups.
     * @see SchedulerTask#getGroup()
     */
    public Map<String, Integer> getGroups() {

        return Collections.unmodifiableMap(groups);
    }

    /**
     * Returns the defined groups sorted by their priorities in descending order.
     * 
     * @return All defined groups in a sorted list.
     * @see #getGroups()
     * @see SchedulerTask#getGroup()
     */
    public List<String> getSortedGroups() {

        return Collections.unmodifiableList(sortedGroups);
    }

    /**
     * Defines the given group and assigns the given priority to it.
     * Groups with high priority values are updated before groups with small priority values.
     * 
     * @param group The name of the group which should be defined.
     * @param priority The priority of the new group.
     * @see SchedulerTask#getGroup()
     */
    public void addGroup(String group, int priority) {

        groups.put(group, priority);
        updateSortedGroups();
    }

    /**
     * Removes the definition of the given group.
     * That just means that all {@link SchedulerTask}s which are assigned to that group are no longer executed.
     * 
     * @param group The name of the group which should be removed.
     * @see SchedulerTask#getGroup()
     */
    public void removeGroup(String group) {

        groups.remove(group);
        updateSortedGroups();
    }

    private void updateSortedGroups() {

        List<Entry<String, Integer>> groupEntries = new ArrayList<>(groups.entrySet());
        Collections.sort(groupEntries, new Comparator<Entry<String, Integer>>() {

            @Override
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {

                return o2.getValue().compareTo(o1.getValue());
            }

        });

        sortedGroups.clear();
        for (Entry<String, Integer> entry : groupEntries) {
            sortedGroups.add(entry.getKey());
        }
    }

    // ----- Dynamic -----

    /**
     * Returns the {@link World} that is currently updated by the tick scheduler updater.
     * 
     * @return The currently updated world.
     */
    public World getWorld() {

        return world == null ? null : world.get();
    }

    /**
     * Changes the {@link World} that is currently updated by the tick scheduler updater.
     * The change will take place in the next tick.
     * 
     * @param world The new world to updated.
     */
    public void setWorld(World world) {

        this.world = world == null ? null : new WeakReference<>(world);
    }

    /**
     * Executes the tick update on all {@link SchedulerUser}s of the set {@link World}.
     */
    @Override
    public void update() {

        if (getWorld() != null) {
            // Collect all schedulers which are present inside the world
            List<Scheduler> schedulers = new ArrayList<>();
            collectSchedulers(world, schedulers);

            // Update each scheduler with each group in the correct order
            for (String group : sortedGroups) {
                for (Scheduler scheduler : schedulers) {
                    scheduler.update(group);
                }
            }
        }
    }

    private void collectSchedulers(Object object, List<Scheduler> list) {

        if (object instanceof SchedulerUser) {
            list.add( ((SchedulerUser) object).get(SchedulerUser.SCHEDULER));
        }

        if (object instanceof CFeatureHolder) {
            for (Feature feature : (CFeatureHolder) object) {
                collectSchedulers(feature, list);
            }
        } else if (object instanceof ValueSupplier) {
            collectSchedulers( ((ValueSupplier<?>) object).get(), list);
        }
    }

}
