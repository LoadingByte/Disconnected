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

package com.quartercode.disconnected.server.world.comp.program;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.List;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.NonPersistent;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
import com.quartercode.disconnected.shared.event.comp.program.WorldProcessCommand;
import com.quartercode.disconnected.shared.event.comp.program.WorldProcessCommandPredicate;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.program.WorldProcessId;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.MultiPredicates;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

/**
 * This utility class provides some utility methods for {@link Program}s and {@link ProgramExecutor}s.
 * A lot of the available methods should be used to executors to remove the need for boilerplate code.
 * 
 * @see Program
 * @see ProgramExecutor
 */
public class ProgramUtils {

    /**
     * Tries to resolve a {@link Program} file (a {@link ContentFile} which contains a program) with the given name from one of the provided directory paths.
     * That means that the algorithm iterates over all of the directories and uses the first one which contains a program file with the given name.
     * This method is used to resolve program files from the {@code PATH} environment variable.
     * Note that this method suppresses all exceptions that might occur.
     * 
     * @param fsModule The {@link FileSystemModule} which manages the file systems that should be searched.
     * @param directories A list of paths to the directories that should be searched.
     * @param fileName The name of the program file that should be found.
     * @return A program file with the given name in one of the given directories.
     */
    public static ContentFile getProgramFileFromPaths(FileSystemModule fsModule, List<String> directories, String fileName) {

        for (String directoryPath : directories) {
            String programFilePath = PathUtils.resolve(directoryPath, fileName);

            try {
                File<?> programFile = fsModule.invoke(FileSystemModule.GET_FILE, programFilePath);

                if (programFile instanceof ContentFile && programFile.getObj(ContentFile.CONTENT) instanceof Program) {
                    return (ContentFile) programFile;
                }
            } catch (IllegalArgumentException | UnknownMountpointException e) {
                // Continue
            }
        }

        return null;
    }

    /**
     * Returns a {@link WorldProcessId} object that identifies the given {@link Process}.
     * A process is identified using the id of the computer it is running on, as well as the actual process id (pid).
     * 
     * @param process The process that should be identified.
     * @return The identification object for the given process.
     */
    public static WorldProcessId getProcessId(Process<?> process) {

        int pid = process.getObj(Process.PID);
        String computerId = process.invoke(Process.GET_OPERATING_SYSTEM).getParent().getId();

        return new WorldProcessId(computerId, pid);
    }

    /**
     * Returns a {@link WorldProcessId} object that identifies the {@link Process} that runs the given {@link ProgramExecutor}.
     * A process is identified using the id of the computer it is running on, as well as the actual process id (pid).
     * This utility method should be used by program executor implementations.
     * 
     * @param executor The program executor that requests the identification object.
     * @return The identification object for the given program executor.
     */
    public static WorldProcessId getProcessId(ProgramExecutor executor) {

        return getProcessId(executor.getParent());
    }

    /**
     * Adds a state listener to the given {@link Process} for stopping it as soon as it is interrupted.
     * This is a shortcut that should be used by {@link ProgramExecutor} implementations.
     * 
     * @param process The process the listener should be added to.
     */
    public static void registerInterruptionStopper(Process<?> process) {

        process.addToColl(Process.STATE_LISTENERS, new StopOnInterruptPSListener());
    }

    /**
     * Registers the given {@link EventHandler} for the given {@link ProgramExecutor} and makes it listen for the given type of {@link WorldProcessCommand} events.
     * The handler will be automatically removed once the process is stopped.
     * This is a shortcut that should be used by program executor implementations.
     * 
     * @param executor The program executor that wants to listen for events.
     * @param eventType The event type all handled events must have.
     * @param handler The event handler that should handle the incoming events.
     */
    public static void registerEventHandler(ProgramExecutor executor, Class<? extends WorldProcessCommand> eventType, EventHandler<?> handler) {

        StandardHandlerModule handlerModule = executor.getBridge().getModule(StandardHandlerModule.class);

        // Add the handler
        handlerModule.addHandler(handler,
                MultiPredicates.and(new TypePredicate<>(eventType), new WorldProcessCommandPredicate<>(getProcessId(executor))));

        // Register a callback that removes the listener once the process is stopped
        RemoveEventHandlerOnStopPSListener removalListener = new RemoveEventHandlerOnStopPSListener();
        removalListener.setObj(RemoveEventHandlerOnStopPSListener.EVENT_HANDLER, handler);
        executor.getParent().addToColl(Process.STATE_LISTENERS, removalListener);
    }

    /**
     * Registers the given {@link SBPAwareEventHandler} for the given {@link ProgramExecutor} and makes it listen for the given type of {@link WorldProcessCommand} events.
     * The handler will be automatically removed once the process is stopped.
     * It is also possible to let the handler automatically verify that the sending SBP is allowed to access the program executor.
     * This is a shortcut that should be used by program executor implementations.
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
        handlerExtension.addHandler(effectiveHandler,
                MultiPredicates.and(new TypePredicate<>(eventType), new WorldProcessCommandPredicate<>(getProcessId(executor))));

        // Register a callback that removes the handler once the process is stopped
        RemoveEventHandlerOnStopPSListener removalListener = new RemoveEventHandlerOnStopPSListener();
        removalListener.setObj(RemoveEventHandlerOnStopPSListener.EVENT_HANDLER, handler);
        executor.getParent().addToColl(Process.STATE_LISTENERS, removalListener);
    }

    /*
     * This class must be public for making it persistent.
     */
    public static class StopOnInterruptPSListener extends WorldFeatureHolder implements ProcessStateListener {

        static {

            ON_STATE_CHANGE.addExecutor("stopOnInterrupt", StopOnInterruptPSListener.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    Object newState = arguments[2];

                    if (newState == ProcessState.INTERRUPTED) {
                        ((Process<?>) arguments[0]).invoke(Process.STOP, true);
                    }

                    return invocation.next(arguments);
                }

            });

        }

    }

    /*
     * This listener removes a given EventHandler or SBPAwareEventHandler once the program is stopped.
     * It doesn't need to be persistent because all event handlers are removed anyway when the server stops.
     * That would make it useless junk.
     */
    @NonPersistent
    private static class RemoveEventHandlerOnStopPSListener extends WorldFeatureHolder implements ProcessStateListener {

        // ----- Properties -----

        // Must store an object because both EventHandler and SBPAwareEventHandler are allowed
        private static final PropertyDefinition<Object> EVENT_HANDLER;

        static {

            EVENT_HANDLER = create(new TypeLiteral<PropertyDefinition<Object>>() {}, "name", "eventHandler", "storage", new StandardStorage<>());

        }

        // ----- Functions -----

        static {

            ON_STATE_CHANGE.addExecutor("removeEventHandlerOnStop", RemoveEventHandlerOnStopPSListener.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    Object newState = arguments[2];

                    if (newState == ProcessState.STOPPED) {
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

    private ProgramUtils() {

    }

}
