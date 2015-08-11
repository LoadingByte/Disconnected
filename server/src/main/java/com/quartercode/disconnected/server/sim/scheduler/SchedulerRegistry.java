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

package com.quartercode.disconnected.server.sim.scheduler;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import com.quartercode.jtimber.api.node.Node;

/**
 * The scheduler registry is a simple class which stores a {@link WeakReference weak} set of {@link Scheduler}s.
 * It is used to quickly retrieve the active schedulers of a world in order to update them.
 * That world {@link SchedulerRegistryProvider provides} such a registry for allowing the schedulers to register themselves.
 *
 * @see Scheduler
 * @see SchedulerRegistryProvider
 */
public class SchedulerRegistry {

    private final Set<Scheduler<?>> schedulers = Collections.newSetFromMap(new WeakHashMap<Scheduler<?>, Boolean>());

    /**
     * Returns an <b>unmodifiable</b> view of all registered {@link Scheduler}s.
     * In order to register or unregister a scheduler, the {@link #addScheduler(Scheduler)} or {@link #removeScheduler(Scheduler)} method must be called.
     * Note that a modifiable {@link Iterator} over this collection can be obtained using {@link #getNewModifiableSchedulersIterator()}.
     *
     * @return The registered schedulers.
     */
    public Collection<Scheduler<?>> getSchedulers() {

        return schedulers;
    }

    /**
     * Returns a new modifiable {@link Iterator} over the {@link #getSchedulers() scheduler collection}.
     * That means that the {@link Iterator#remove()} method is available.
     *
     * @return A new modifiable schedulers iterator.
     */
    public Iterator<Scheduler<?>> getNewModifiableSchedulersIterator() {

        return schedulers.iterator();
    }

    /**
     * Registers the given {@link Scheduler} in the registry.
     * If the scheduler has already been registered, nothing happens.
     * Note that this method should only be used by {@link Scheduler} implementations.
     *
     * @param scheduler The scheduler to register.
     */
    public void addScheduler(Scheduler<?> scheduler) {

        schedulers.add(scheduler);
    }

    /**
     * Unregisters the given {@link Scheduler} from the registry.
     * If the scheduler isn't actually registered, nothing happens.
     * Note that this method should only be used by {@link Scheduler} implementations.
     *
     * @param scheduler The scheduler to unregister.
     */
    public void removeScheduler(Scheduler<?> scheduler) {

        schedulers.remove(scheduler);
    }

    /**
     * Recursively walks the {@link Node} tree that starts at the given root node and {@link #addScheduler(Scheduler) registers} all found {@link Scheduler}s.
     * This method should be used after a world has been deserialized in order to create a valid scheduler registry.
     * However, this method should only be called once because traversing the whole tree is rather expensive.
     *
     * @param start The root node to start at.
     */
    public void addSchedulersFromTree(Node<?> start) {

        for (Object child : start.getChildren()) {
            if (child instanceof Scheduler) {
                schedulers.add((Scheduler<?>) child);
            }

            if (child instanceof Node) {
                addSchedulersFromTree((Node<?>) child);
            }
        }
    }

}
