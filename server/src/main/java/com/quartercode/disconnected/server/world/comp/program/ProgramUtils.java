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
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.shared.event.comp.program.ProgramEventPredicate;
import com.quartercode.disconnected.shared.util.PathUtils;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.extension.RequestEventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;
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
                File<?> programFile = fsModule.get(FileSystemModule.GET_FILE).invoke(programFilePath);

                if (programFile instanceof ContentFile && programFile.get(ContentFile.CONTENT).get() instanceof Program) {
                    return (ContentFile) programFile;
                }
            } catch (IllegalArgumentException | UnknownMountpointException e) {
                // Continue
            }
        }

        return null;
    }

    /**
     * Returns an {@link ImportantData} storage object that contains the following information for the given {@link ProgramExecutor}:
     * 
     * <ul>
     * <li>The pid of the process which created the program executor.</li>
     * <li>The id of the computer the process which created the program executor is running on.</li>
     * <li>The {@link Bridge} of the server.</li>
     * </ul>
     * 
     * This utility method should be used by program executor implementations.
     * 
     * @param executor The program executor that requests the information.
     * @return An important data object that contains the requested information.
     */
    public static ImportantData getImportantData(ProgramExecutor executor) {

        Process<?> process = executor.getParent();

        int pid = process.getParent().get(Process.PID).get();
        String computerId = process.get(Process.GET_OPERATING_SYSTEM).invoke().getParent().getId();
        Bridge bridge = process.getWorld().getBridge();

        return new ImportantData(pid, computerId, bridge);
    }

    /**
     * Registers the given {@link EventHandler} for the given {@link ProgramExecutor} and makes it listen for the given type of {@link Event}s.
     * This is a shortcut that should be used by program executor implementations.
     * 
     * @param executor The program executor that wants to listen for events.
     * @param eventType The event type all handled events must have.
     * @param handler The event handler that should handle the incoming events.
     */
    public static void registerEventHandler(ProgramExecutor executor, Class<? extends Event> eventType, EventHandler<?> handler) {

        ImportantData data = getImportantData(executor);

        data.getBridge().getModule(StandardHandlerModule.class).addHandler(handler,
                MultiPredicates.and(new TypePredicate<>(eventType), new ProgramEventPredicate<>(data.getComputerId(), data.getPid())));
    }

    /**
     * Registers the given {@link RequestEventHandler} for the given {@link ProgramExecutor} and makes it listen for the given type of request {@link Event}s.
     * This is a shortcut that should be used by program executor implementations.
     * 
     * @param executor The program executor that wants to listen for request events.
     * @param requestEventType The event type all handled request events must have.
     * @param requestHandler The request event handler that should handle the incoming request events.
     */
    public static void registerRequestEventHandler(ProgramExecutor executor, Class<? extends Event> requestEventType, RequestEventHandler<?> requestHandler) {

        ImportantData data = getImportantData(executor);

        data.getBridge().getModule(ReturnEventExtensionReturner.class).addRequestHandler(requestHandler,
                MultiPredicates.and(new TypePredicate<>(requestEventType), new ProgramEventPredicate<>(data.getComputerId(), data.getPid())));
    }

    /**
     * A storage class for some important data used by nearly every {@link ProgramExecutor}.
     * 
     * @see ProgramExecutor
     */
    public static class ImportantData {

        private final int    pid;
        private final String computerId;
        private final Bridge bridge;

        private ImportantData(int pid, String computerId, Bridge bridge) {

            this.pid = pid;
            this.computerId = computerId;
            this.bridge = bridge;
        }

        /**
         * Returns the pid of the process which created the program executor.
         * 
         * @return The pid.
         */
        public int getPid() {

            return pid;
        }

        /**
         * Returns the id of the computer the process which created the program executor is running on.
         * 
         * @return The computer id.
         */
        public String getComputerId() {

            return computerId;
        }

        /**
         * Returns the {@link Bridge} of the server.
         * 
         * @return The bridge.
         */
        public Bridge getBridge() {

            return bridge;
        }

    }

    private ProgramUtils() {

    }

}
