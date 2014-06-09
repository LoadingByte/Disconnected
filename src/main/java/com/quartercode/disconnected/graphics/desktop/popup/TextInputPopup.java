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

import com.quartercode.disconnected.graphics.GraphicsState;
import com.quartercode.disconnected.graphics.component.NoPopupEditField;
import com.quartercode.disconnected.graphics.desktop.DesktopWindow;
import com.quartercode.disconnected.graphics.desktop.DesktopWindowDefaultSizeMediator;
import com.quartercode.disconnected.util.ResourceBundles;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Dimension;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;

/**
 * The text input popup is a {@link DesktopWindow} which shows a message and prompts the user for inputting a text.
 * 
 * @see DesktopWindow
 */
public class TextInputPopup extends DesktopWindow {

    /**
     * Creates a new text input popup with the given parameters.
     * The new popup is <b>not</b> added to the desktop automatically.
     * 
     * @param state The desktop state the new popup will be showed on.
     * @param message The message that is showed by the popup when the user is prompted.
     * @param defaultText The default text that is placed in the text input field before the user inputs anything. May be {@code null}.
     * @param showCancelButton Whether or not a button for closing the popup without inputing anything should be shown.
     * @param callback A {@link Callback} that is called when the cancel or confirm button is pressed and the popup is closed.
     */
    public TextInputPopup(GraphicsState state, String message, String defaultText, boolean showCancelButton, final Callback callback) {

        super(state);

        new DesktopWindowDefaultSizeMediator(this, new Dimension(300, 0));

        Label messageLabel = new Label(message);
        messageLabel.setTheme("/label");

        final EditField inputField = new NoPopupEditField();
        inputField.setTheme("/editfield");
        if (defaultText != null) {
            inputField.setText(defaultText);
        }

        Button confirmButton = new Button(ResourceBundles.DESKTOP.getString("general.confirm"));
        confirmButton.setTheme("/button");
        confirmButton.addCallback(new Runnable() {

            @Override
            public void run() {

                close();

                if (callback != null) {
                    String input = inputField.getText();
                    callback.onClose(false, input.isEmpty() ? null : input);
                }
            }

        });

        Button cancelButton = null;
        if (showCancelButton) {
            cancelButton = new Button(ResourceBundles.DESKTOP.getString("general.cancel"));
            cancelButton.setTheme("/button");
            cancelButton.addCallback(new Runnable() {

                @Override
                public void run() {

                    close();

                    if (callback != null) {
                        callback.onClose(true, null);
                    }
                }

            });
        }

        DialogLayout layout = new DialogLayout();
        layout.setTheme("");
        layout.setDefaultGap(new Dimension(5, 5));
        Group hButtons = layout.createSequentialGroup().addGap().addWidgets(cancelButton, confirmButton);
        Group vButtons = layout.createParallelGroup(cancelButton, confirmButton);
        layout.setHorizontalGroup(layout.createParallelGroup(messageLabel, inputField).addGroup(hButtons));
        layout.setVerticalGroup(layout.createSequentialGroup(messageLabel, inputField).addGroup(vButtons));
        add(layout);
    }

    /**
     * The text input popup callback is used by the {@link TextInputPopup} class.
     * It is called when certain events happen.
     * 
     * @see #onClose(boolean, String)
     */
    public static interface Callback {

        /**
         * This method is called when the text input popup is closed.
         * 
         * @param cancelled {@code True} if the cancel button was pressed, {@code false} if the confirm button was pressed.
         * @param text The text that was input by the user.
         *        May be {@code null} if the text field was left empty.
         */
        public void onClose(boolean cancelled, String text);

    }

}
