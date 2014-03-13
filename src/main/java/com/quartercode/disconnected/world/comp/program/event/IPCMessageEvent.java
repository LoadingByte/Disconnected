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

package com.quartercode.disconnected.world.comp.program.event;

import java.util.HashMap;
import java.util.Map;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.SizeUtil;
import com.quartercode.disconnected.world.comp.program.Process;

/**
 * This class represents the income of an IPC message which can be sent between two different {@link Process}es.
 * An IPC event contains the sending and receiving {@link Process}es, as well as a data map which holds the data which should be sent.
 * 
 * @see ProcessEvent
 */
public class IPCMessageEvent extends ProcessEvent {

    // ----- Properties -----

    /**
     * The {@link Process} which sent the message.
     */
    protected static final FeatureDefinition<ReferenceProperty<Process<?>>>       SENDER;

    /**
     * The data map which holds the payload {@link Object}s which is sent with the message.
     */
    protected static final FeatureDefinition<ObjectProperty<Map<String, Object>>> DATA;

    static {

        SENDER = new AbstractFeatureDefinition<ReferenceProperty<Process<?>>>("sender") {

            @Override
            public ReferenceProperty<Process<?>> create(FeatureHolder holder) {

                return new ReferenceProperty<Process<?>>(getName(), holder);
            }

        };

        DATA = new AbstractFeatureDefinition<ObjectProperty<Map<String, Object>>>("data") {

            @Override
            public ObjectProperty<Map<String, Object>> create(FeatureHolder holder) {

                return new ObjectProperty<Map<String, Object>>(getName(), holder, new HashMap<String, Object>());
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link Process} which sent the message.
     */
    public static final FunctionDefinition<Process<?>>                            GET_SENDER;

    /**
     * Changes the {@link Process} which sent the message.
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
     * <td>{@link Process}</td>
     * <td>sender</td>
     * <td>The new sending {@link Process}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                  SET_SENDER;

    /**
     * Returns the data map which holds the payload {@link Object}s which is sent with the message.
     */
    public static final FunctionDefinition<Map<String, Object>>                   GET_DATA;

    /**
     * Changes the data map which holds the payload {@link Object}s which should be sent with the message.
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
     * <td>{@link Map}&lt;{@link String}, {@link Object}&gt;</td>
     * <td>data</td>
     * <td>The new data map which contains the payload {@link Object}s for sending.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                  SET_DATA;

    static {

        GET_SENDER = FunctionDefinitionFactory.create("getSender", IPCMessageEvent.class, PropertyAccessorFactory.createGet(SENDER));
        SET_SENDER = FunctionDefinitionFactory.create("setSender", IPCMessageEvent.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(SENDER)), Process.class);

        GET_DATA = FunctionDefinitionFactory.create("getData", ProcessEvent.class, PropertyAccessorFactory.createGet(DATA));
        SET_DATA = FunctionDefinitionFactory.create("setData", ProcessEvent.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(DATA)), Map.class);

        GET_SIZE.addExecutor(ProcessEvent.class, "data", SizeUtil.createGetSize(DATA));

    }

    // ----- Functions End -----

    /**
     * Creates a new IPC message event.
     */
    public IPCMessageEvent() {

    }

}
