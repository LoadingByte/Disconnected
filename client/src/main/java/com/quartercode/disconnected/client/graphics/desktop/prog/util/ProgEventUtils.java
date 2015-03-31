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

package com.quartercode.disconnected.client.graphics.desktop.prog.util;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.Mutable;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramExecutor;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramStateContext;
import com.quartercode.disconnected.shared.event.comp.prog.SBPWorldProcessUserCommand;
import com.quartercode.disconnected.shared.event.comp.prog.SBPWorldProcessUserCommandPredicate;
import com.quartercode.disconnected.shared.event.comp.prog.WorldProcessCommand;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchAcknowledgmentEvent;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserDetails;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.MultiPredicates;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

/**
 * This utility class provides some utility methods and classes regarding {@link Event}s and {@link EventHandler}s for {@link ClientProgramExecutor} implementations.
 * They should be used to remove the need for boilerplate code.
 * 
 * @see ClientProgramExecutor
 */
public class ProgEventUtils {

    /**
     * Adds the given {@link EventHandler} to the given {@link Bridge} and makes it listen for incoming {@link Event}s that match the given {@link EventPredicate}.
     * Moreover, the handler is removed as soon as the client program executor, which is associated to the given {@link ClientProgramStateContext}, stops.
     * 
     * @param bridge The bridge the event handler should be added to.
     * @param stateContext The state context of the {@link ClientProgramExecutor} that wants to register the event handler.
     *        It is used to remove the event handler when the program stops.
     * @param handler The new event handler that should start listening on the given bridge.
     * @param predicate An event predicate that decides which events are passed into the handler.
     * @see StandardHandlerModule#addHandler(EventHandler, EventPredicate)
     */
    public static void addEventHandler(final Bridge bridge, ClientProgramStateContext stateContext, final EventHandler<?> handler, EventPredicate<?> predicate) {

        Validate.notNull(bridge, "Bridge cannot be null");
        Validate.notNull(stateContext, "Client process state context cannot be null");
        Validate.notNull(handler, "Event handler cannot be null");
        Validate.notNull(predicate, "Event predicate cannot be null");

        bridge.getModule(StandardHandlerModule.class).addHandler(handler, predicate);

        stateContext.addStoppingListener(new Runnable() {

            @Override
            public void run() {

                bridge.getModule(StandardHandlerModule.class).removeHandler(handler);
            }

        });
    }

    /**
     * Adds the given {@link EventHandler} to the given {@link Bridge} and makes it listen for incoming {@link SBPWorldProcessUserCommand}s of the given type.
     * However, only events that are addressed at the given {@link SBPWorldProcessUserDetails world process user id} are accepted.
     * Moreover, the handler is removed as soon as the client program executor, which is associated to the given {@link ClientProgramStateContext}, stops.
     * 
     * @param bridge The bridge the event handler should be added to.
     * @param stateContext The state context of the {@link ClientProgramExecutor} that wants to register the event handler.
     *        It is used to remove the event handler when the program stops.
     * @param handler The new event handler that should start listening on the given bridge.
     * @param eventType The class all accepted events must be an instance of.
     * @param wpu The {@link SBPWorldProcessUserDetails world process user id} all accepted events must be addressed at.
     * @see StandardHandlerModule#addHandler(EventHandler, EventPredicate)
     */
    public static void addEventHandler(final Bridge bridge, ClientProgramStateContext stateContext, final EventHandler<?> handler, Class<? extends SBPWorldProcessUserCommand> eventType, SBPWorldProcessUserDetails wpu) {

        Validate.notNull(eventType, "Event type cannot be null");
        Validate.notNull(wpu, "World process user details cannot be null");

        addEventHandler(bridge, stateContext, handler,
                MultiPredicates.and(new TypePredicate<>(eventType), new SBPWorldProcessUserCommandPredicate<>(wpu)));
    }

    /**
     * Launches the given world program using the given communication parameters without calling a callback.
     * 
     * @param bridge The bridge through which the server, which should launch the world program, can be reached.
     * @param stateContext The state context of the {@link ClientProgramExecutor} that wants to launch the world process.
     *        It is used to remove the added event handlers when the program stops.
     * @param wpu A {@link SBPWorldProcessUserDetails world process user id} the server will use to communicate with the client through {@link SBPWorldProcessUserCommand}s.
     *        Therefore, the WPU must be stored to be used in {@link #addEventHandler(Bridge, ClientProgramStateContext, EventHandler, Class, SBPWorldProcessUserDetails) custom event handlers}.
     * @param worldProgramName The name of the world program which should be launched (e.g. {@code fileManager}).
     * @param worldProcessIdField A {@link Mutable} reference to a field that should hold the {@link WorldProcessId} of the launched process.
     *        The {@link Mutable#setValue(Object)} method is called once that id is known.
     *        Therefore, that method must be caught somehow in order to store the id.
     *        Note that the world process id can be used to send {@link WorldProcessCommand} events to the world process.
     */
    public static void launchWorldProcess(Bridge bridge, ClientProgramStateContext stateContext, SBPWorldProcessUserDetails wpu, String worldProgramName, Mutable<WorldProcessId> worldProcessIdField) {

        launchWorldProcess(bridge, stateContext, wpu, worldProgramName, worldProcessIdField, null);
    }

    /**
     * Launches the given world program using the given communication parameters and calls the given {@link Runnable callback} once the program has been started.
     * 
     * @param bridge The bridge through which the server, which should launch the world program, can be reached.
     * @param stateContext The state context of the {@link ClientProgramExecutor} that wants to launch the world process.
     *        It is used to remove the added event handlers when the program stops.
     * @param wpu A {@link SBPWorldProcessUserDetails world process user id} the server will use to communicate with the client through {@link SBPWorldProcessUserCommand}s.
     *        Therefore, the WPU must be stored to be used in {@link #addEventHandler(Bridge, ClientProgramStateContext, EventHandler, Class, SBPWorldProcessUserDetails) custom event handlers}.
     * @param worldProgramName The name of the world program which should be launched (e.g. {@code fileManager}).
     * @param worldProcessIdField A {@link Mutable} reference to a field that should hold the {@link WorldProcessId} of the launched process.
     *        The {@link Mutable#setValue(Object)} method is called once that id is known.
     *        Therefore, that method must be caught somehow in order to store the id.
     *        Note that the world process id can be used to send {@link WorldProcessCommand} events to the world process.
     * @param callback A {@link Runnable} that should be called as soon as the process has been started ({@link WorldProcessLaunchAcknowledgmentEvent}).
     *        Note that the world process id is guaranteed to be set when this method is called.
     */
    public static void launchWorldProcess(final Bridge bridge, ClientProgramStateContext stateContext, SBPWorldProcessUserDetails wpu, String worldProgramName, final Mutable<WorldProcessId> worldProcessIdField, final Runnable callback) {

        Validate.notNull(bridge, "Bridge cannot be null");
        Validate.notNull(stateContext, "Client process state context cannot be null");
        Validate.notNull(wpu, "World process user details cannot be null");
        Validate.notBlank(worldProgramName, "World program name cannot be blank");
        Validate.notNull(worldProcessIdField, "Mutable world process id cannot be null");

        // Register a handler for catching the acknowledgment event and store the world process identity
        addEventHandler(bridge, stateContext, new EventHandler<WorldProcessLaunchAcknowledgmentEvent>() {

            @Override
            public void handle(WorldProcessLaunchAcknowledgmentEvent event) {

                // Set the world process id
                worldProcessIdField.setValue(event.getWorldProcessId());

                // If a callback is present, call it
                if (callback != null) {
                    callback.run();
                }

                // Remove the handler after the acknowledgment arrived
                bridge.getModule(StandardHandlerModule.class).removeHandler(this);
            }

        }, WorldProcessLaunchAcknowledgmentEvent.class, wpu);

        // Launch the process
        bridge.send(new WorldProcessLaunchCommand(wpu, worldProgramName));
    }

    private ProgEventUtils() {

    }

}
