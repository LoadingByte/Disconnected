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

package com.quartercode.disconnected.client.graphics;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.shared.bridge.HandleInvocationProviderExtension;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;
import com.quartercode.eventbridge.extra.extension.SendPredicateCheckExtension;

/**
 * This is the default implementation of the {@link GraphicsService}.
 * 
 * @see GraphicsService
 */
public class DefaultGraphicsService implements GraphicsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGraphicsService.class);

    private final List<URL>     themes = new ArrayList<>();

    private GraphicsThread      thread;
    private GraphicsState       state;
    private final Bridge        bridge = EventBridgeFactory.create(Bridge.class);

    /**
     * Creates a new default graphics service.
     */
    public DefaultGraphicsService() {

        bridge.addModule(EventBridgeFactory.create(ReturnEventExtensionRequester.class));
        bridge.addModule(EventBridgeFactory.create(ReturnEventExtensionReturner.class));
        bridge.addModule(EventBridgeFactory.create(SendPredicateCheckExtension.class));
        bridge.addModule(EventBridgeFactory.create(HandleInvocationProviderExtension.class));

        bridge.getModule(HandleInvocationProviderExtension.class).setInvocationProvider(this);
    }

    @Override
    public List<URL> getThemes() {

        return Collections.unmodifiableList(themes);
    }

    @Override
    public void addTheme(URL theme) {

        themes.add(theme);
    }

    @Override
    public void removeTheme(URL theme) {

        themes.remove(theme);
    }

    @Override
    public boolean isRunning() {

        return thread != null && thread.isAlive();
    }

    @Override
    public void setRunning(boolean running) {

        if (running && !isRunning()) {
            LOGGER.info("Starting up graphics thread");
            thread = new GraphicsThread(themes);
            thread.changeState(state);
            thread.start();
        } else if (!running && isRunning()) {
            LOGGER.info("Shutting down graphics thread");
            thread.exit();
            thread = null;
        }
    }

    @Override
    public GraphicsState getState() {

        return state;
    }

    @Override
    public void setState(GraphicsState state) {

        this.state = state;

        if (thread != null) {
            thread.changeState(state);
        }
    }

    @Override
    public Bridge getBridge() {

        return bridge;
    }

    @Override
    public void invoke(Runnable runnable) {

        thread.invoke(runnable);
    }

}
