
package com.quartercode.disconnected.bridge;

/**
 * The event utils class provides some utility methods that are related to {@link Event}s and their environment ({@link EventPredicate}, {@link EventHandler} etc.).
 * 
 * @see Event
 * @see EventPredicate
 * @see EventHandler
 */
public class EventUtils {

    /**
     * Tests the given {@link Event} on the given {@link EventPredicate} and returns the result.
     * If the type of the event doesn't match the generic parameter of the predicate, this method returns {@code false}.
     * 
     * @param predicate The event predicate that should test the given event.
     * @param event The event that should be tested by the given event predicate.
     * @return Whether the generic parameter of the given predicate matches the event type and returns {@code true} on the given event.
     */
    public static <T extends Event> boolean tryTest(EventPredicate<T> predicate, Event event) {

        try {
            @SuppressWarnings ("unchecked")
            T castedEvent = (T) event;
            return predicate.test(castedEvent);
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Lets the given {@link EventHandler} handle the given {@link Event}.
     * If the type of the event doesn't match the generic parameter of the handler, this method does nothing.
     * The method can also consider the {@link EventPredicate} which is supplied by the given handler and check the event before the handler is called.
     * 
     * @param handler The event handler that should handle the given event.
     * @param event The event that should be handled by the given event handler.
     * @param considerPredicate Whether the predicate which is supplied by the given handler should check the given event before the handler is called.
     */
    public static <T extends Event> void tryHandle(EventHandler<T> handler, Event event, boolean considerPredicate) {

        if (considerPredicate && !tryTest(handler.getPredicate(), event)) {
            return;
        }

        try {
            @SuppressWarnings ("unchecked")
            T castedEvent = (T) event;
            handler.handle(castedEvent);
        } catch (ClassCastException e) {}
    }

    private EventUtils() {

    }

}
