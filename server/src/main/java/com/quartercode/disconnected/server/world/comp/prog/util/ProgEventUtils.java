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

package com.quartercode.disconnected.server.world.comp.prog.util;

import static com.quartercode.classmod.extra.func.Priorities.LEVEL_7;
import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.NonPersistent;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.world.comp.prog.ProcessStateListener;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
import com.quartercode.disconnected.shared.event.comp.prog.WorldProcessCommand;
import com.quartercode.disconnected.shared.event.comp.prog.WorldProcessCommandPredicate;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessState;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.MultiPredicates;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

/**
 * This utility class provides some utility methods and classes regarding {@link EventHandler}s/{@link SBPAwareEventHandler}s for {@link ProgramExecutor} implementations.
 * They should be used to remove the need for boilerplate code.
 * 
 * @see ProgramExecutor
 */
public class ProgEventUtils {

    /**
     * Registers the given {@link EventHandler} for the given {@link ProgramExecutor} and makes it listen for the given type of {@link WorldProcessCommand} events.
     * The handler will be automatically removed once the process is stopped.
     * 
     * @param executor The program executor that wants to listen for events.
     * @param eventType The event type all handled events must have.
     * @param handler The event handler that should handle the incoming events.
     */
    public static void registerEventHandler(ProgramExecutor executor, Class<? extends WorldProcessCommand> eventType, EventHandler<?> handler) {

        StandardHandlerModule handlerModule = executor.getBridge().getModule(StandardHandlerModule.class);

        // Add the handler
        WorldProcessId wpi = executor.getParent().invoke(Process.GET_WORLD_PROCESS_ID);
        handlerModule.addHandler(handler,
                MultiPredicates.and(new TypePredicate<>(eventType), new WorldProcessCommandPredicate<>(wpi)));

        // Register a callback that removes the listener once the process is stopped
        RemoveEventHandlerOnStopPSListener removalListener = new RemoveEventHandlerOnStopPSListener();
        removalListener.setObj(RemoveEventHandlerOnStopPSListener.EVENT_HANDLER, handler);
        executor.getParent().addToColl(Process.STATE_LISTENERS, removalListener);
    }

    /**
     * Registers the given {@link SBPAwareEventHandler} for the given {@link ProgramExecutor} and makes it listen for the given type of {@link WorldProcessCommand} events.
     * The handler will be automatically removed once the process is stopped.
     * It is also possible to let the handler automatically verify that the sending SBP is allowed to access the program executor.
     * 
     * @param executor The program executor that wants to listen for events.
     * @param eventType The event type all handled events must have.
     * @param handler The SBP-aware event handler that should handle the incoming events.
     * @param verifySBP Whether the handler should automatically verify that the sending SBP is allowed to access the program executor.
     */
    public static <E extends WorldProcessCommand> void registerSBPAwareEventHandler(final ProgramExecutor executor, Class<? extends E> eventType, final SBPAwareEventHandler<E> handler, boolean verifySBP) {

        final SBPAwareEventHandler<E> effectiveHandler;
        if (verifySBP) {
            effectiveHandler = new SBPAwareEventHandler<E>() {

                @Override
                public void handle(E event, SBPIdentity sender) {

                    // Verify the SBP
                    if (sender.equals(executor.getParent().getObj(Process.WORLD_PROCESS_USER).getSBP())) {
                        handler.handle(event, sender);
                    }
                }

            };
        } else {
            effectiveHandler = handler;
        }

        final SBPAwareHandlerExtension handlerExtension = executor.getBridge().getModule(SBPAwareHandlerExtension.class);

        // Add the handler
        WorldProcessId wpi = executor.getParent().invoke(Process.GET_WORLD_PROCESS_ID);
        handlerExtension.addHandler(effectiveHandler,
                MultiPredicates.and(new TypePredicate<>(eventType), new WorldProcessCommandPredicate<>(wpi)));

        // Register a callback that removes the handler once the process is stopped
        RemoveEventHandlerOnStopPSListener removalListener = new RemoveEventHandlerOnStopPSListener();
        removalListener.setObj(RemoveEventHandlerOnStopPSListener.EVENT_HANDLER, handler);
        executor.getParent().addToColl(Process.STATE_LISTENERS, removalListener);
    }

    private ProgEventUtils() {

    }

    /**
     * An internal {@link ProcessStateListener} that removes a given {@link EventHandler} or {@link SBPAwareEventHandler} once the program is stopped.
     * It doesn't need to be persistent because all event handlers are removed anyway when the server stops. That would make it useless junk.
     * 
     * @see ProgEventUtils#registerEventHandler(ProgramExecutor, Class, EventHandler)
     * @see ProgEventUtils#registerSBPAwareEventHandler(ProgramExecutor, Class, SBPAwareEventHandler, boolean)
     */
    @NonPersistent
    public static class RemoveEventHandlerOnStopPSListener extends WorldFeatureHolder implements ProcessStateListener {

        // ----- Properties -----

        /**
         * The {@link EventHandler} or {@link SBPAwareEventHandler} that should be removed from the respective bridge module/extension.
         * Note that no other objects apart from those two types are allowed.
         */
        public static final PropertyDefinition<Object> EVENT_HANDLER;

        static {

            EVENT_HANDLER = factory(PropertyDefinitionFactory.class).create("eventHandler", new StandardStorage<>());
            EVENT_HANDLER.addSetterExecutor("checkType", RemoveEventHandlerOnStopPSListener.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    Object handler = arguments[0];
                    boolean validType = handler instanceof EventHandler || handler instanceof SBPAwareEventHandler;
                    Validate.isTrue(validType, "Event handler ('%s') must be of type 'EventHandler' or 'SBPAwareEventHandler'", handler == null ? null : handler.getClass().getName());

                    return invocation.next(arguments);
                }

            }, LEVEL_7);

        }

        // ----- Functions -----

        static {

            ON_STATE_CHANGE.addExecutor("removeEventHandlerOnStop", RemoveEventHandlerOnStopPSListener.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    Object newState = arguments[2];

                    if (newState == WorldProcessState.STOPPED) {
                        Object handler = invocation.getCHolder().getObj(EVENT_HANDLER);
                        Bridge bridge = ((Process<?>) arguments[0]).getBridge();

                        if (handler instanceof EventHandler) {
                            bridge.getModule(StandardHandlerModule.class).removeHandler((EventHandler<?>) handler);
                        } else if (handler instanceof SBPAwareEventHandler) {
                            bridge.getModule(SBPAwareHandlerExtension.class).removeHandler((SBPAwareEventHandler<?>) handler);
                        }
                    }

                    return invocation.next(arguments);
                }

            });

        }

    }

}
