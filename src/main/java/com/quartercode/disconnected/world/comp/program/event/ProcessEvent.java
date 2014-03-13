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

import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;
import com.quartercode.disconnected.world.comp.program.Process;

/**
 * This class represents an event which can be received by a {@link Process}.
 * A process event contains the receiving {@link Process}, as well as a data map which holds the data which should be sent.
 */
public class ProcessEvent extends DefaultFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The {@link Process} which receives the event.
     */
    protected static final FeatureDefinition<ReferenceProperty<Process<?>>> RECEIVER;

    static {

        RECEIVER = new AbstractFeatureDefinition<ReferenceProperty<Process<?>>>("receiver") {

            @Override
            public ReferenceProperty<Process<?>> create(FeatureHolder holder) {

                return new ReferenceProperty<Process<?>>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link Process} which receives the event.
     */
    public static final FunctionDefinition<Process<?>>                      GET_RECEIVER;

    /**
     * Changes the {@link Process} which receives the event.
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
     * <td>receiver</td>
     * <td>The new receiving {@link Process}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                            SET_RECEIVER;

    static {

        GET_RECEIVER = FunctionDefinitionFactory.create("getReceiver", ProcessEvent.class, PropertyAccessorFactory.createGet(RECEIVER));
        SET_RECEIVER = FunctionDefinitionFactory.create("setReceiver", ProcessEvent.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(RECEIVER)), Process.class);

    }

    // ----- Functions End -----

    /**
     * Creates a new process event.
     */
    public ProcessEvent() {

    }

}
