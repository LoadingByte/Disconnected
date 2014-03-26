
package com.quartercode.disconnected.world.event;

import com.quartercode.classmod.extra.ExecutorInvocationException;

/**
 * The {@link #matches(Event)} method of every true event matcher just always returns <code>true</code>.
 * For example, it could be used to make the {@link QueueEventListener#NEXT_EVENT} method return the last received {@link Event}.
 * 
 * @see Event
 * @see QueueEventListener#NEXT_EVENT
 */
public class TrueEventMatcher implements EventMatcher {

    /**
     * Creates a new true event matcher that just always returns <code>true</code> in the {@link #matches(Event)} method.
     */
    public TrueEventMatcher() {

    }

    @Override
    public boolean matches(Event event) throws ExecutorInvocationException {

        return true;
    }

}
