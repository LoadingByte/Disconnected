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

package com.quartercode.disconnected.client.graphics.desktop.prog;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.client.graphics.GraphicsService;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.registry.ClientProgram;
import com.quartercode.disconnected.client.util.ResourceBundles;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.ValueInjector;

/**
 * A utility class that can be used to launch new {@link ClientProgramExecutor}s from a {@link ClientProgram} description object.
 * The class executes all necessary steps in order to start the program.
 * That means that it also injects appropriate context values into the newly created executors.
 * Those values are taken from the public registries of the application (e.g. {@link ServiceRegistry}).
 * 
 * @see ClientProgram
 * @see ClientProgramExecutor
 * @see #launch(ClientProgram, GraphicsState)
 */
public class ClientProgramLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientProgramLauncher.class);

    /**
     * Launches a new {@link ClientProgramExecutor executor instance} of the given {@link ClientProgram} as part of the given desktop {@link GraphicsState}.
     * The method executes all necessary steps in order to start the program.
     * That means that it also injects appropriate context values into the newly created executors.
     * Those values are taken from the public registries of the application (e.g. {@link ServiceRegistry}) and the provided desktop state.
     * Note that the {@link ClientProgramExecutor#run()} method <b>is</b> ran at the end of this method.
     * 
     * @param program The client program descriptor which describes the program that should be launched.
     * @param graphicsContext The desktop graphics state which serves as a graphical context where the newly launched client program can run in.
     *        For example, it uses this state to open new windows.
     */
    public static void launch(ClientProgram program, GraphicsState graphicsContext) {

        // Create a new executor instance
        ClientProgramExecutor executor;
        try {
            executor = (ClientProgramExecutor) program.getType().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Cannot create instance of client program executor '{}' (program '{}')", program.getType().getClass().getName(), program.getName(), e);
            return;
        }

        // Inject the required values into the executor
        ValueInjector injector = new ValueInjector();
        injector.put("name", program.getName());
        injector.put("category", program.getCategory());
        injector.put("stateContext", new ClientProgramStateContextImpl());
        injector.put("graphicsContext", graphicsContext);
        injector.put("l10nContext", ResourceBundles.forProgram(program.getName()));
        injector.put("bridge", ServiceRegistry.lookup(GraphicsService.class).getBridge());
        injector.inject(executor);

        // Run the executor
        executor.run();
    }

    private ClientProgramLauncher() {

    }

    private static class ClientProgramStateContextImpl implements ClientProgramStateContext {

        private final List<Runnable> stoppingListeners = new ArrayList<>();

        @Override
        public void stop() {

            for (Runnable stoppingListener : stoppingListeners) {
                stoppingListener.run();
            }
        }

        @Override
        public void addStoppingListener(Runnable listener) {

            stoppingListeners.add(listener);
        }

    }

}
