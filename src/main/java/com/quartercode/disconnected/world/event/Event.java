
package com.quartercode.disconnected.world.event;

import java.util.Collection;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.util.FunctionDefinitionFactory;

/**
 * An event is a message or a notification on a more abstract scale.
 * There are different applications for events. For example, there could be a packet event that notifies on an incoming network packet.
 * Typically, you just create a new event from a custom class and call the {@link #SEND} method.
 * On the receiver side, {@link EventListener}s receive and process such events.
 * 
 * @see EventListener
 */
public abstract class Event extends DefaultFeatureHolder {

    /**
     * Sends the defined event to all provided {@link EventListener} in the order of appearance.
     * The method just calls the {@link EventListener#HANDLE_EVENT} method on all of the listeners.
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
     * <td>{@link Collection}&lt;{@link EventListener}&gt;</td>
     * <td>listeners</td>
     * <td>The {@link EventListener} that should handle the event.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void> SEND;

    static {

        SEND = FunctionDefinitionFactory.create("send", Event.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                // Trust the user here
                @SuppressWarnings ("unchecked")
                Collection<EventListener> listeners = (Collection<EventListener>) arguments[0];

                for (EventListener listener : listeners) {
                    listener.get(EventListener.HANDLE_EVENT).invoke(invocation.getHolder());
                }

                return invocation.next(arguments);
            }

        }, Collection.class);

    }

    /**
     * Creates a new event.
     */
    public Event() {

    }

}
