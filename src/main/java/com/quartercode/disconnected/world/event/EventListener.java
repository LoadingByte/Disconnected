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

package com.quartercode.disconnected.world.event;

import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.util.FunctionDefinitionFactory;

/**
 * An event listener can receive and handle {@link Event}s. There are different implementations and concepts on how this can work.
 * The central point is the {@link #HANDLE_EVENT} method that processes incoming {@link Event}s.
 * The simplest event listener could just take an incoming event and process it directly in the {@link #HANDLE_EVENT} method.
 * More advanced listeners could distribute events to other methods or even own classes, or they could store the events for later processing.
 * 
 * @see Event
 */
public interface EventListener extends FeatureHolder {

    /**
     * Lets the event listener receive and handle an {@link Event}.
     * The method could just execute some processing code, or it could distribute or store the received {@link Event}.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Event}</td>
     * <td>event</td>
     * <td>The new incoming {@link Event} the listener should handle.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void> HANDLE_EVENT = FunctionDefinitionFactory.create("handleEvent", Event.class);

}
