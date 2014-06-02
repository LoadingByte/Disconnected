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

package com.quartercode.disconnected.bridge;

import java.util.LinkedList;
import java.util.Queue;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;

/**
 * A queue event handler stores received {@link Event}s in a queue in order to make them accessible at a later time.
 * Users of queue events handlers do not need to process events immediately when they arrive.
 * They can hold them back and process them when they want to.
 * Furthermore, users of this implementation can also use an {@link EventPredicate} to only get events that match their criteria.
 * 
 * @param <T> The type of event that can be handled by the handler.
 * @see EventHandler
 * @see EventPredicate
 */
public class QueueEventHandler<T extends Event> implements EventHandler<T> {

    private final Queue<T> events = new LinkedList<>();

    /**
     * Creates a new queue event handler.
     */
    public QueueEventHandler() {

    }

    /**
     * Adds the given incoming {@link Event} to the internal queue.
     * 
     * @param event The event that should be added to the queue.
     */
    @Override
    public void handle(T event) {

        events.offer(event);
    }

    /**
     * Returns the next received {@link Event} from the {@link Queue} which matches the criteria of the given {@link EventPredicate}.
     * Every returned event is removed from the internal storage queue.
     * Example:
     * 
     * <pre>
     * Format: EventType[Identifier]
     * Queue:  A[1], A[2], B[3], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (predicate for A) returns A[1]
     * =&gt; A[2], B[3], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (predicate for B) returns B[3]
     * =&gt; A[2], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (predicate for A) returns A[2]
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
     * <td>{@link EventPredicate}</td>
     * <td>predicate</td>
     * <td>The event predicate that checks for the event which should be returned.</td>
     * </tr>
     * </table>
     */

    /**
     * Returns the next received {@link Event} from the event queue.
     * Every returned event is removed from the internal storage queue.
     * Example:
     * 
     * <pre>
     * Queue: { event1, event2, event3 } (added in this order)
     * 
     * nextEvent() returns event1
     * =&gt; { event2, event3 }
     * 
     * nextEvent() returns event2
     * =&gt; { event3 }
     * 
     * nextEvent() returns event3
     * =&gt; { }
     * 
     * nextEvent() returns null
     * =&gt; { }
     * </pre>
     * 
     * @return The next received event (or {@code null} if there is no event left).
     */
    public T next() {

        return events.poll();
    }

    /**
     * Returns the next received {@link Event} from the event queue which matches the criteria of the given {@link EventPredicate}.
     * Every returned event is removed from the internal storage queue.
     * Example:
     * 
     * <pre>
     * Queue:  { A event1, A event2, B event3, A event4, B event5, B event6 }
     * 
     * nextEvent(predicate for type A) returns event1
     * =&gt; { A event2, B event3, A event4, B event5, B event6 }
     * 
     * nextEvent(predicate for type B) returns event3
     * =&gt; { A event2, A event4, B event5, B event6 }
     * 
     * nextEvent(predicate for type A) returns event2
     * =&gt; { A event4, B event5, B event6 }
     * </pre>
     * 
     * @param matcher The event predicate that defines the criteria for the event that should be returned next.
     * @return The next received event that matches the given matcher (or {@code null} if there is no such event left).
     */
    public T next(EventPredicate<?> matcher) {

        Queue<T> clone = new LinkedList<>(events);

        while (!clone.isEmpty()) {
            T currentEvent = clone.poll();
            if (EventUtils.tryTest(matcher, currentEvent)) {
                events.remove(currentEvent);
                return currentEvent;
            }
        }

        return null;
    }

}
