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

package com.quartercode.disconnected.world.event;

import java.util.LinkedList;
import java.util.Queue;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.FunctionDefinitionFactory;

/**
 * A queue event listener stores received {@link Event}s in a {@link Queue} in order to make them accessible at a later time.
 * Event listeners which use this type of event handling do not need to process {@link Event}s immediately when they are received.
 * They can hold back events and process them at a given other point in time.
 * Furthermore, users of this implementation of the concept can also use an {@link EventMatcher} to only retrieve the first {@link Event} they want to process.
 * 
 * @see Event
 * @see EventListener
 * @see EventMatcher
 */
public class QueueEventListener extends DefaultFeatureHolder implements EventListener {

    // ----- Properties -----

    /**
     * A {@link Queue} that contains the received {@link Event}s in the correct order.
     * The {@link #HANDLE_EVENT} method offers a new {@link Event} to this {@link Queue}.
     * The {@link #NEXT_EVENT} method "polls" from this {@link Queue} in order to retrieve the first received {@link Event}s.
     */
    protected static final CollectionPropertyDefinition<Event, Queue<Event>> EVENTS;

    static {

        EVENTS = ObjectCollectionProperty.createDefinition("events", new LinkedList<Event>());

    }

    // ----- Functions -----

    /**
     * Lets the queue event listener receive an {@link Event} for further processing.
     * The new {@link Event} is added to an internal {@link Queue} that can be accessed using the "poll" function {@link #NEXT_EVENT}.
     * This method is mainly used by the {@link Event#SEND} method.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Event}</td>
     * <td>event</td>
     * <td>The incoming event that should be added to the queue.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                             HANDLE_EVENT = EventListener.HANDLE_EVENT;

    /**
     * Returns the next received {@link Event} from the {@link Queue} which matches the criteria of the given {@link EventMatcher}.
     * Every returned {@link Event} is removed from the internal storage {@link Queue}.
     * Example:
     * 
     * <pre>
     * Format: EventType[Identifier]
     * Queue:  A[1], A[2], B[3], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (matcher for A) returns A[1]
     * =&gt; A[2], B[3], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (matcher for B) returns B[3]
     * =&gt; A[2], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (matcher for A) returns A[2]
     * =&gt; A[4], B[5], B[6]
     * </pre>
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link EventMatcher}</td>
     * <td>matcher</td>
     * <td>The {@link EventMatcher} that checks for the {@link Event} which should be returned.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Event>                            NEXT_EVENT;

    static {

        HANDLE_EVENT.addExecutor("addToQueue", QueueEventListener.class, CollectionPropertyAccessorFactory.createAdd(EVENTS));
        NEXT_EVENT = FunctionDefinitionFactory.create("nextEvent", QueueEventListener.class, new FunctionExecutor<Event>() {

            @Override
            public Event invoke(FunctionInvocation<Event> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                EventMatcher matcher = (EventMatcher) arguments[0];

                Queue<Event> clone = new LinkedList<Event>(holder.get(EVENTS).get());
                Event nextEvent = null;
                while (!clone.isEmpty()) {
                    Event current = clone.poll();
                    if (matcher.matches(current)) {
                        holder.get(EVENTS).remove(current);
                        nextEvent = current;
                        break;
                    }
                }

                invocation.next(arguments);
                return nextEvent;
            }

        }, EventMatcher.class);

    }

    /**
     * Creates a new queue event listener.
     */
    public QueueEventListener() {

    }

}
