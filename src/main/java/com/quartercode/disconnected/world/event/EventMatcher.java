
package com.quartercode.disconnected.world.event;

import com.quartercode.classmod.extra.ExecutorInvocationException;

/**
 * Event matchers are used to search for {@link Event}s of a specific type.
 * For example, matchers are used to retrieve the next {@link Event} a {@link QueueEventListener} wants to handle in {@link QueueEventListener#NEXT_EVENT}.
 * 
 * @see Event
 */
public interface EventMatcher {

    /**
     * Checks if the given {@link Event} matches the criteria that are defined by the event matcher.
     * 
     * @param event The {@link Event} to check.
     * @return True if the given {@link Event} matches the defined criteria, false if not.
     * @throws ExecutorInvocationException Something bad happens while checking.
     */
    public boolean matches(Event event) throws ExecutorInvocationException;

}
