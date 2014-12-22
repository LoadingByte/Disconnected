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
import java.util.List;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.util.FeatureHolderVisitorAdapter;
import com.quartercode.classmod.util.TreeWalker;
import com.quartercode.disconnected.server.registry.SchedulerGroup;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerUser;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.shared.util.registry.Registries;

/**
 * This class updates all {@link Scheduler}s of {@link World} {@link CFeatureHolder}s which implement the {@link SchedulerUser} interface.
 * 
 * @see SchedulerUser
 */
public class TickSchedulerUpdater implements TickAction {

    private List<String>                  sortedGroups;
    private volatile WeakReference<World> world;

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
            List<Scheduler> schedulers = collectSchedulers(getWorld());

            // Update each scheduler with each group in the correct order
            for (String group : getSortedGroups()) {
                for (Scheduler scheduler : schedulers) {
                    scheduler.update(group);
                }
            }
        }
    }

    private List<Scheduler> collectSchedulers(World world) {

        final List<Scheduler> schedulers = new ArrayList<>();

        TreeWalker.walk(world, new FeatureHolderVisitorAdapter() {

            @Override
            public VisitResult preVisit(FeatureHolder holder) {

                // If the current feature holder is a SchedulerUser, add its scheduler to the output list
                if (holder instanceof SchedulerUser) {
                    schedulers.add(holder.get(SchedulerUser.SCHEDULER));
                }

                return VisitResult.CONTINUE;
            }

        }, false);

        return schedulers;
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
