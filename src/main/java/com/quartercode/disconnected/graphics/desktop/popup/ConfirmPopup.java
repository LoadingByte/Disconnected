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

package com.quartercode.disconnected.graphics.desktop.popup;

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.graphics.GraphicsState;
import com.quartercode.disconnected.graphics.desktop.DesktopWindow;
import com.quartercode.disconnected.graphics.desktop.DesktopWindowDefaultSizeMediator;
import com.quartercode.disconnected.util.ResourceBundles;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Dimension;
import de.matthiasmann.twl.Label;

/**
 * The confirm popup is a {@link DesktopWindow} which shows a message and some defined buttons to close the popup.
 * 
 * @see DesktopWindow
 */
public class ConfirmPopup extends DesktopWindow {

    /**
     * The different options the user can select from when he is prompted with a confirm popup.
     * Note that the creator of the popup can choose which options should be shown to the user.
     */
    public static enum Option {

        /**
         * An option button that shows "yes" in the english version.
         */
        YES ("general.yes"),
        /**
         * An option button that shows "confirm" in the english version.
         */
        CONFIRM ("general.confirm"),
        /**
         * An option button that shows "no" in the english version.
         */
        NO ("general.no"),
        /**
         * An option button that shows "cancel" in the english version.
         */
        CANCEL ("general.cancel"),
        /**
         * An option button that shows "close" in the english version.
         */
        CLOSE ("general.close");

        private final String key;

        private Option(String key) {

            this.key = key;
        }

        /**
         * Returns the resource bundle key of the option.
         * Note that the key must be used on the {@link ResourceBundles#DESKTOP} resource bundle.
         * 
         * @return The resource key of the option.
         */
        public String getKey() {

            return key;
        }

    }

    /**
     * Creates a new confirm popup with the given message, option array, and close callback.
     * The new popup is <b>not</b> added to the desktop automatically.
     * 
     * @param state The desktop state the new popup will be showed on.
     * @param message The message that is showed by the popup.
     * @param options The {@link Option}s that should be showed to the user.
     *        He can select one option that will close the popup.
     * @param callback A {@link Callback} that is called when a button is pressed and the popup is closed.
     */
    public ConfirmPopup(GraphicsState state, String message, Option[] options, final Callback callback) {

        super(state);

        new DesktopWindowDefaultSizeMediator(this, new Dimension(300, 0));

        Label messageLabel = new Label(message);
        messageLabel.setTheme("/label");

        List<Button> optionButtons = new ArrayList<>();
        for (final Option option : options) {
            Button optionButton = new Button(ResourceBundles.DESKTOP.getString(option.getKey()));
            optionButton.setTheme("/button");
            optionButton.addCallback(new Runnable() {

                @Override
                public void run() {

                    close();

                    if (callback != null) {
                        callback.onClose(option);
                    }
                }

            });
            optionButtons.add(optionButton);
        }

        DialogLayout layout = new DialogLayout();
        layout.setTheme("");
        layout.setDefaultGap(new Dimension(5, 5));

        Group hOptionButtons = layout.createSequentialGroup().addGap();
        for (Button optionButton : optionButtons) {
            hOptionButtons.addWidget(optionButton);
        }
        layout.setHorizontalGroup(layout.createParallelGroup(messageLabel).addGroup(hOptionButtons));

        Group vOptionButtons = layout.createParallelGroup();
        for (Button optionButton : optionButtons) {
            vOptionButtons.addWidget(optionButton);
        }
        layout.setVerticalGroup(layout.createSequentialGroup(messageLabel).addGroup(vOptionButtons));

        add(layout);
    }

    /**
     * The confirm popup callback is used by the {@link ConfirmPopup} class.
     * It is called when certain events happen.
     * 
     * @see #onClose(ConfirmPopup.Option)
     */
    public static interface Callback {

        /**
         * This method is called when the user selected an option and closed the popup.
         * 
         * @param selected The {@link Option} that was selected by the user.
         */
        public void onClose(Option selected);

    }

}
