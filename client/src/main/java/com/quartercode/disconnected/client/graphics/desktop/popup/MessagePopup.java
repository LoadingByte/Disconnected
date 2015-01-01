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

package com.quartercode.disconnected.client.graphics.desktop.popup;

import static com.quartercode.disconnected.client.graphics.desktop.DesktopWindowUtils.setDefaultSize;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindow;
import com.quartercode.disconnected.client.util.ResourceBundles;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Dimension;
import de.matthiasmann.twl.Label;

/**
 * The message popup is a {@link DesktopWindow} which just shows a message and a close button which closes the popup.
 * 
 * @see DesktopWindow
 */
public class MessagePopup extends DesktopWindow {

    /**
     * Creates a new message popup with the given message.
     * The new popup is <b>not</b> added to the desktop automatically.
     * 
     * @param state The desktop state the new popup will be showed on.
     * @param message The message that is showed by the popup.
     */
    public MessagePopup(GraphicsState state, String message) {

        this(state, message, null);
    }

    /**
     * Creates a new message popup with the given message and close callback.
     * The new popup is <b>not</b> added to the desktop automatically.
     * 
     * @param state The desktop state the new popup will be showed on.
     * @param message The message that is showed by the popup.
     * @param callback A {@link Callback} that is called when the close button is pressed and the popup is closed.
     */
    public MessagePopup(GraphicsState state, String message, final Callback callback) {

        super(state);

        setDefaultSize(this, new Dimension(300, 0));

        Label messageLabel = new Label(message);
        messageLabel.setTheme("/label");

        Button closeButton = new Button(ResourceBundles.DESKTOP.get("general.close"));
        closeButton.setTheme("/button");
        closeButton.addCallback(new Runnable() {

            @Override
            public void run() {

                close();

                if (callback != null) {
                    callback.onClose();
                }
            }

        });

        DialogLayout layout = new DialogLayout();
        layout.setTheme("");
        layout.setDefaultGap(new Dimension(5, 5));
        Group hButton = layout.createSequentialGroup().addGap().addWidget(closeButton);
        layout.setHorizontalGroup(layout.createParallelGroup(messageLabel).addGroup(hButton));
        layout.setVerticalGroup(layout.createSequentialGroup(messageLabel, closeButton));
        add(layout);
    }

    /**
     * The message popup callback is used by the {@link MessagePopup} class.
     * It is called when certain events happen.
     * 
     * @see #onClose()
     */
    public static interface Callback {

        /**
         * This method is called when the message popup is closed.
         */
        public void onClose();

    }

}
