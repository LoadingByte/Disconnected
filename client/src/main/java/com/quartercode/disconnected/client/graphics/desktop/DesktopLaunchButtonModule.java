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

package com.quartercode.disconnected.client.graphics.desktop;

import com.quartercode.disconnected.client.graphics.AbstractGraphicsModule;
import com.quartercode.disconnected.client.graphics.GraphicsService;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.registry.ClientRegistries;
import com.quartercode.disconnected.client.util.ResourceBundles;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.ValueInjector;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.MappedValueRegistry.Mapping;
import de.matthiasmann.twl.Alignment;
import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.BoxLayout.Direction;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;

/**
 * The desktop launch button module adds a launch {@link Button} with the theme {@code launch-button}.
 * The launch button can be used to start front-end-programs.
 * The actual button object is available under the key {@code button}.
 */
public class DesktopLaunchButtonModule extends AbstractGraphicsModule {

    private Button    button;

    private boolean   menuVisible;
    private BoxLayout menuLayout;

    @Override
    public void add(final GraphicsState state) {

        button = new Button();
        button.setTheme("/desktop-launchButton");
        button.setText(ResourceBundles.DESKTOP.getString("launchButton.text"));
        button.addCallback(new Runnable() {

            @Override
            public void run() {

                toggleLaunchMenu(state);
            }

        });
        ((Widget) state.getModule("desktopWidget").getValue("widget")).add(button);
        setValue("button", button);

        menuLayout = new BoxLayout(Direction.VERTICAL);
        menuLayout.setTheme("");
        menuLayout.setSpacing(0);
        menuLayout.setAlignment(Alignment.FILL);
        state.add(menuLayout);
        setValue("menuLayout", menuLayout);
    }

    private void toggleLaunchMenu(final GraphicsState state) {

        menuVisible = !menuVisible;

        if (menuVisible) {
            // Add new menu buttons
            // TODO: Add client program categories
            for (Mapping<String, Object> programMapping : Registries.get(ClientRegistries.CLIENT_PROGRAMS)) {
                final ClientProgramDescriptor program = (ClientProgramDescriptor) programMapping.getRight();

                Button menuButton = new Button();
                menuButton.setTheme("/desktop-launchMenuButton");
                menuButton.setText(program.getName());

                menuButton.addCallback(new Runnable() {

                    @Override
                    public void run() {

                        // Create the context object
                        ValueInjector valueInjector = new ValueInjector();
                        valueInjector.put("bridge", ServiceRegistry.lookup(GraphicsService.class).getBridge());
                        ClientProgramContext context = new ClientProgramContext(valueInjector);

                        // Launch the client program
                        program.create(state, context).setVisible(true);
                    }

                });

                menuLayout.add(menuButton);
            }
        } else {
            // Clear the menu buttons
            menuLayout.removeAllChildren();
        }
    }

    @Override
    public void layout(GraphicsState state) {

        button.adjustSize();
        button.setPosition(10, state.getHeight() - button.getHeight() - 10);

        menuLayout.adjustSize();
        menuLayout.setPosition(button.getX() + 1, button.getY() - menuLayout.getHeight() - 1);
    }

}
