
package com.quartercode.disconnected.world.event;

import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.util.FunctionDefinitionFactory;

/**
 * An event listener can receive and handle {@link Event}s. There are different implementations and concepts on how this can work.
 * The central point is the {@link #HANDLE_EVENT} method that processes incoming {@link Event}s.
 * The simplest event listener could just take an incoming event and process it directly in the {@link #HANDLE_EVENT} method.
 * More advanced listeners could distribute events to other methods or even own classes, or they could store the events for later processing.
 * 
 * @see Event
 */
public interface EventListener extends FeatureHolder {

    /**
     * Lets the event listener receive and handle an {@link Event}.
     * The method could just execute some processing code, or it could distribute or store the received {@link Event}.
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
     * <td>The new incoming {@link Event} the listener should handle.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void> HANDLE_EVENT = FunctionDefinitionFactory.create("handleEvent", Event.class);

}
