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

package com.quartercode.disconnected.test.bridge;

import com.quartercode.disconnected.bridge.AbstractEventHandler;
import com.quartercode.disconnected.bridge.Bridge;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.EventHandler;

public class BridgeMock extends Bridge {

    private EventHandler<Event> handler;

    public BridgeMock() {

        setHandler(null);
    }

    public void setHandler(EventHandler<Event> handler) {

        if (handler == null) {
            this.handler = new AbstractEventHandler<Event>(Event.class) {

                @Override
                public void handle(Event event) {

                    // Empty
                }

            };
        } else {
            this.handler = handler;
        }
    }

    @Override
    public void send(Event event) {

        handler.handle(event);
    }

}
