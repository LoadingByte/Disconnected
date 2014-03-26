
package com.quartercode.disconnected.world.event;

import org.apache.commons.lang.Validate;
import com.quartercode.classmod.extra.ExecutorInvocationException;

/**
 * The type event matcher only accepts {@link Event}s that derive from a given supertype or superinterface or are equal to a provided type.
 * For example, it could be used to make the {@link QueueEventListener#NEXT_EVENT} method return the last received {@link Event} of a given type
 * 
 * @see Event
 * @see QueueEventListener#NEXT_EVENT
 */
public class TypeEventMatcher implements EventMatcher {

    private final Class<? extends Event> type;

    /**
     * Creates a new type event matcher that only accepts {@link Event}s that derive from the given type or are equal to the given type.
     * 
     * @param type The type every matching {@link Event} must have as supertype or be equal to.
     */
    public TypeEventMatcher(Class<? extends Event> type) {

        Validate.notNull(type, "Type for matching cannot be null");
        this.type = type;
    }

    @Override
    public boolean matches(Event event) throws ExecutorInvocationException {

        return type.isAssignableFrom(event.getClass());
    }

}
