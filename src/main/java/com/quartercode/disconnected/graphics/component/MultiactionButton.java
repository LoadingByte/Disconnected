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

package com.quartercode.disconnected.graphics.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Event.Type;
import de.matthiasmann.twl.model.ButtonModel;

/**
 * The multiaction button is a TWL component which allows to listen to all mouse buttons.
 */
public class MultiactionButton extends Button {

    private final Map<Integer, Runnable> callbacks = new HashMap<>();

    public MultiactionButton() {

    }

    public MultiactionButton(ButtonModel model) {

        super(model);
    }

    public MultiactionButton(AnimationState animState) {

        super(animState);
    }

    public MultiactionButton(String text) {

        super(text);
    }

    public MultiactionButton(AnimationState animState, boolean inherit) {

        super(animState, inherit);
    }

    public MultiactionButton(AnimationState animState, ButtonModel model) {

        super(animState, model);
    }

    public MultiactionButton(AnimationState animState, boolean inherit, ButtonModel model) {

        super(animState, inherit, model);
    }

    /**
     * Adds a new button callback using a button code and a callback runnable.
     * 
     * @param button The mouse button code to listen on; if the button gets pressed using this mouse button, the callback will get executed.
     * @param callback The callback listener.
     */
    public void addCallback(int button, Runnable callback) {

        callbacks.put(button, callback);
    }

    @Override
    protected boolean handleEvent(Event event) {

        boolean handled = super.handleEvent(event);

        if (event.getType() == Type.MOUSE_BTNUP && isMouseInside(event)) {
            for (Entry<Integer, Runnable> callback : callbacks.entrySet()) {
                if (callback.getKey() == event.getMouseButton()) {
                    handled = true;
                    callback.getValue().run();
                }
            }
        }

        return handled;
    }

}
