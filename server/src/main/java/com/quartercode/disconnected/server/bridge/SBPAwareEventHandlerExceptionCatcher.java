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
/*
 * This file is part of EventBridge.
 * Copyright (c) 2014 QuarterCode <http://www.quartercode.com/>
 *
 * EventBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * EventBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EventBridge. If not, see <http://www.gnu.org/licenses/>.
 */

package com.quartercode.disconnected.server.bridge;

import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;

/**
 * SBP-aware event handler exception catchers are called when an {@link SBPAwareEventHandler} throws a {@link RuntimeException}.
 * 
 * @see SBPAwareEventHandler
 */
public interface SBPAwareEventHandlerExceptionCatcher {

    /**
     * Processes the given {@link RuntimeException}, which was thrown by the given {@link SBPAwareEventHandler} while handling the given {@link Event}.
     * 
     * @param exception The runtime exception the given SBP-aware event handler threw while handling the given event.
     *        It should be processed by the exception catcher.
     * @param handler The SBP-aware event handler which threw the exception.
     * @param event The event which caused the given event handler to throw the given exception on handling.
     * @param source The {@link BridgeConnector} which received the given event.
     *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
     */
    public void handle(RuntimeException exception, SBPAwareEventHandler<?> handler, Event event, BridgeConnector source);

}
