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

package com.quartercode.disconnected.world.comp;

import java.util.HashSet;
import java.util.Set;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory.CriteriumMatcher;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.Location;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.hardware.Hardware;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;

/**
 * This class stores information about a computer, like the {@link Location} or the {@link Hardware} parts.
 * 
 * @see Location
 * @see Hardware
 */
public class Computer extends WorldChildFeatureHolder<World> {

    // ----- Properties -----

    /**
     * The {@link Location} of the computer.
     */
    protected static final FeatureDefinition<ObjectProperty<Location>>        LOCATION;

    /**
     * The {@link Hardware} parts the computer contains.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<Hardware>>>   HARDWARE;

    /**
     * The active {@link OperatingSystem} instance which is currently running the computer.
     */
    protected static final FeatureDefinition<ObjectProperty<OperatingSystem>> OS;

    static {

        LOCATION = ObjectProperty.createDefinition("location");
        HARDWARE = ObjectProperty.<Set<Hardware>> createDefinition("hardware", new HashSet<Hardware>());
        OS = ObjectProperty.createDefinition("operatingSystem");

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link Location} of the computer.
     */
    public static final FunctionDefinition<Location>                          GET_LOCATION;

    /**
     * Changes the {@link Location} of the computer.
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
     * <td>{@link Location}</td>
     * <td>location</td>
     * <td>The new {@link Location}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                              SET_LOCATION;

    /**
     * Returns the {@link Hardware} parts the computer contains.
     */
    public static final FunctionDefinition<Set<Hardware>>                     GET_HARDWARE;

    /**
     * Returns the {@link Hardware} parts of the computer which have the given type as a superclass.
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
     * <td>{@link Class}</td>
     * <td>type</td>
     * <td>The type to use for the selection.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Set<Hardware>>                     GET_HARDWARE_BY_TYPE;

    /**
     * Adds {@link Hardware} parts to the computer.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Hardware}...</td>
     * <td>hardware</td>
     * <td>The {@link Hardware} parts to add to the computer.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                              ADD_HARDWARE;

    /**
     * Removes {@link Hardware} parts from the computer.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Hardware}...</td>
     * <td>hardware</td>
     * <td>The {@link Hardware} parts to remove from the computer.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                              REMOVE_HARDWARE;

    /**
     * Returns the active {@link OperatingSystem} instance which is currently running the computer.
     */
    public static final FunctionDefinition<OperatingSystem>                   GET_OS;

    /**
     * Changes the active {@link OperatingSystem} instance which runs the computer.
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
     * <td>{@link OperatingSystem}</td>
     * <td>os</td>
     * <td>The new active {@link OperatingSystem}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                              SET_OS;

    static {

        GET_LOCATION = FunctionDefinitionFactory.create("getLocation", Computer.class, PropertyAccessorFactory.createGet(LOCATION));
        SET_LOCATION = FunctionDefinitionFactory.create("setLocation", Computer.class, PropertyAccessorFactory.createSet(LOCATION), Location.class);

        GET_HARDWARE = FunctionDefinitionFactory.create("getHardware", Computer.class, CollectionPropertyAccessorFactory.createGet(HARDWARE));
        GET_HARDWARE_BY_TYPE = FunctionDefinitionFactory.create("getHardwareByType", Computer.class, CollectionPropertyAccessorFactory.createGet(HARDWARE, new CriteriumMatcher<Hardware>() {

            @Override
            public boolean matches(Hardware element, Object... arguments) throws ExecutorInvocationException {

                return ((Class<?>) arguments[0]).isAssignableFrom(element.getClass());
            }

        }), Class.class);
        ADD_HARDWARE = FunctionDefinitionFactory.create("addHardware", Computer.class, CollectionPropertyAccessorFactory.createAdd(HARDWARE), Hardware[].class);
        REMOVE_HARDWARE = FunctionDefinitionFactory.create("removeHardware", Computer.class, CollectionPropertyAccessorFactory.createRemove(HARDWARE), Hardware[].class);

        GET_OS = FunctionDefinitionFactory.create("getOs", Computer.class, PropertyAccessorFactory.createGet(OS));
        SET_OS = FunctionDefinitionFactory.create("setOs", Computer.class, PropertyAccessorFactory.createSet(OS), OperatingSystem.class);

    }

    // ----- Functions End -----

    /**
     * Creates a new computer.
     */
    public Computer() {

    }

}
