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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import com.quartercode.disconnected.client.graphics.AbstractGraphicsModule;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramLauncher;
import com.quartercode.disconnected.client.registry.ClientProgram;
import com.quartercode.disconnected.client.registry.ClientRegistries;
import com.quartercode.disconnected.client.util.ResourceBundles;
import com.quartercode.disconnected.shared.util.registry.Registries;
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

    private Button    activeCategoryButton;
    private BoxLayout categorySubmenuLayout;

    @Override
    public void add(final GraphicsState state) {

        button = new Button();
        button.setTheme("/desktop-launchButton");
        button.setText(ResourceBundles.DESKTOP.get("launchButton.text"));
        button.addCallback(new Runnable() {

            @Override
            public void run() {

                toggleLaunchMenu(state);
            }

        });
        ((Widget) state.getModule("desktopWidget").getValue("widget")).add(button);
        setValue("button", button);

        menuLayout = createMenuBoxLayout();
        state.add(menuLayout);
        setValue("menuLayout", menuLayout);

        categorySubmenuLayout = createMenuBoxLayout();
        state.add(categorySubmenuLayout);
        setValue("categorySubmenuLayout", categorySubmenuLayout);
    }

    private BoxLayout createMenuBoxLayout() {

        BoxLayout layout = new BoxLayout(Direction.VERTICAL);

        layout.setTheme("");
        layout.setSpacing(0);
        layout.setAlignment(Alignment.FILL);

        return layout;
    }

    private void toggleLaunchMenu(GraphicsState state) {

        menuVisible = !menuVisible;

        if (menuVisible) {
            // Build a map of [category -> programs] (sorted by categories)
            // Note that localized category names are used instead of keys in order to sort by the visible category names
            SortedMap<String, List<ClientProgram>> categoriesToPrograms = new TreeMap<>();
            for (ClientProgram program : Registries.get(ClientRegistries.CLIENT_PROGRAMS)) {
                String category = ResourceBundles.DESKTOP.get("launchMenu.categories." + program.getCategory());

                if (!categoriesToPrograms.containsKey(category)) {
                    categoriesToPrograms.put(category, new ArrayList<ClientProgram>());
                }

                categoriesToPrograms.get(category).add(program);
            }

            // Add new menu buttons for all categories
            for (Entry<String, List<ClientProgram>> entry : categoriesToPrograms.entrySet()) {
                Button menuButton = new Button();
                menuButton.setTheme("/desktop-launchMenuButton");
                menuButton.setText(entry.getKey());

                // Add a button callback to open the category submenu on hovering
                menuButton.getModel().addStateCallback(new CategoryButtonHoverCallback(state, menuButton, entry.getValue()));

                menuLayout.add(menuButton);
            }
        } else {
            // Clear the menu
            menuLayout.removeAllChildren();
            // Clear any active category submenu
            activeCategoryButton = null;
            categorySubmenuLayout.removeAllChildren();
        }
    }

    @Override
    public void layout(GraphicsState state) {

        button.adjustSize();
        button.setPosition(10, state.getHeight() - button.getHeight() - 10);

        menuLayout.adjustSize();
        menuLayout.setPosition(button.getX() + 1, button.getY() - menuLayout.getHeight() - 1);

        categorySubmenuLayout.adjustSize();
        if (activeCategoryButton != null) {
            int x = activeCategoryButton.getX() + activeCategoryButton.getWidth();
            int y = activeCategoryButton.getY() + activeCategoryButton.getHeight() - categorySubmenuLayout.getHeight();
            categorySubmenuLayout.setPosition(x, y);
        }
    }

    @RequiredArgsConstructor
    private class CategoryButtonHoverCallback implements Runnable {

        private final GraphicsState       state;
        private final Button              button;
        private final List<ClientProgram> categoryPrograms;

        @Override
        public void run() {

            // Only open a new category submenu if the category is marked as active (hover)
            if (!button.getModel().isHover()) {
                return;
            }

            // Set the current category button as active in order to properly layout the category submenu
            activeCategoryButton = button;

            // Clear any submenu that has been previously opened
            categorySubmenuLayout.removeAllChildren();

            // Add new menu buttons for all programs of the active category
            for (final ClientProgram program : categoryPrograms) {
                Button submenuButton = new Button();
                submenuButton.setTheme("/desktop-launchMenuButton");
                submenuButton.setText(ResourceBundles.forProgram(program.getName()).get("name"));

                // Add a button callback to launch the program
                submenuButton.addCallback(new Runnable() {

                    @Override
                    public void run() {

                        ClientProgramLauncher.launch(program, state);
                    }

                });

                categorySubmenuLayout.add(submenuButton);
            }
        }
    }

}
