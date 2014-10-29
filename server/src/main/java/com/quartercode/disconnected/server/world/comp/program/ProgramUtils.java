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

import java.util.List;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.shared.comp.file.PathUtils;
import com.quartercode.disconnected.shared.comp.program.WorldProcessId;
import com.quartercode.disconnected.shared.event.program.WorldProcessCommand;
import com.quartercode.disconnected.shared.event.program.WorldProcessCommandPredicate;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.MultiPredicates;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

/**
 * The program utils class provides some utility methods for programs and program executors.
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
    public static ContentFile getProgramFileFromPath(FileSystemModule fsModule, List<String> directories, String fileName) {

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
     * Returns a {@link WorldProcessId} object that identifies the {@link Process} that runs the given {@link ProgramExecutor}.
     * A process is identified using the id of the computer it is running on, as well as the actual process id (pid).
     * This utility method should be used by program executor implementations.
     * 
     * @param executor The program executor that requests the identification object.
     * @return The identification object for the given program executor.
     */
    public static WorldProcessId getProcessId(ProgramExecutor executor) {

        Process<?> process = executor.getParent();

        int pid = process.getObj(Process.PID);
        String computerId = process.invoke(Process.GET_OPERATING_SYSTEM).getParent().getId();

        return new WorldProcessId(computerId, pid);
    }

    /**
     * Registers the given {@link EventHandler} for the given {@link ProgramExecutor} and makes it listen for the given type of {@link WorldProcessCommand} events.
     * This is a shortcut that should be used by program executor implementations.
     * 
     * @param executor The program executor that wants to listen for events.
     * @param eventType The event type all handled events must have.
     * @param handler The event handler that should handle the incoming events.
     */
    public static void registerEventHandler(ProgramExecutor executor, Class<? extends WorldProcessCommand> eventType, EventHandler<?> handler) {

        executor.getBridge().getModule(StandardHandlerModule.class).addHandler(handler,
                MultiPredicates.and(new TypePredicate<>(eventType), new WorldProcessCommandPredicate<>(getProcessId(executor))));
    }

    /**
     * Registers the given {@link SBPAwareEventHandler} for the given {@link ProgramExecutor} and makes it listen for the given type of {@link WorldProcessCommand} events.
     * It is also possible to let the handler automatically verify that the sending SBP is allowed to access the program executor.
     * This is a shortcut that should be used by program executor implementations.
     * 
     * @param executor The program executor that wants to listen for events.
     * @param eventType The event type all handled events must have.
     * @param handler The SBP-aware event handler that should handle the incoming events.
     * @param verifySBP Whether the handler should automatically verify that the sending SBP is allowed to access the program executor.
     */
    public static <E extends WorldProcessCommand> void registerSBPAwareEventHandler(final ProgramExecutor executor, Class<? extends E> eventType, final SBPAwareEventHandler<E> handler, boolean verifySBP) {

        SBPAwareEventHandler<E> effectiveHandler;
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

        executor.getBridge().getModule(SBPAwareHandlerExtension.class).addHandler(effectiveHandler,
                MultiPredicates.and(new TypePredicate<>(eventType), new WorldProcessCommandPredicate<>(getProcessId(executor))));
    }

    private ProgramUtils() {

    }

}
