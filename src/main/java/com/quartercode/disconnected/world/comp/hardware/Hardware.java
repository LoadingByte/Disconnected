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

package com.quartercode.disconnected.world.comp.hardware;

import java.util.HashSet;
import java.util.Set;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Vulnerability;

/**
 * This class stores information about a part of hardware, like a mainboard, a cpu or a ram module.
 * This also contains a list of all vulnerabilities this hardware part has.
 * 
 * @see Computer
 */
public class Hardware extends WorldChildFeatureHolder<Computer> {

    // ----- Properties -----

    /**
     * The name of the hardware part.
     */
    protected static final FeatureDefinition<ObjectProperty<String>>             NAME;

    /**
     * The {@link Vulnerability}s the hardware part has.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<Vulnerability>>> VULNERABILITIES;

    static {

        NAME = ObjectProperty.createDefinition("name");
        VULNERABILITIES = ObjectProperty.<Set<Vulnerability>> createDefinition("vulnerabilities", new HashSet<Vulnerability>());

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the name of the hardware part.
     */
    public static final FunctionDefinition<String>                               GET_NAME;

    /**
     * Changes the name of the hardware part.
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
     * <td>{@link String}</td>
     * <td>name</td>
     * <td>The new name.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 SET_NAME;

    /**
     * Returns the {@link Vulnerability}s the hardware part has.
     */
    public static final FunctionDefinition<Set<Vulnerability>>                   GET_VULNERABILITIES;

    /**
     * Adds {@link Vulnerability}s to the hardware part.
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
     * <td>{@link Vulnerability}...</td>
     * <td>vulnerabilities</td>
     * <td>The {@link Vulnerability}s to add to the hardware part.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 ADD_VULNERABILITIES;

    /**
     * Removes {@link Vulnerability}s from the hardware part.
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
     * <td>{@link Vulnerability}...</td>
     * <td>vulnerabilities</td>
     * <td>The {@link Vulnerability}s to remove from the hardware part.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 REMOVE_VULNERABILITIES;

    static {

        GET_NAME = FunctionDefinitionFactory.create("getName", Hardware.class, PropertyAccessorFactory.createGet(NAME));
        SET_NAME = FunctionDefinitionFactory.create("setName", Hardware.class, PropertyAccessorFactory.createSet(NAME), String.class);

        GET_VULNERABILITIES = FunctionDefinitionFactory.create("getVulnerabilities", Hardware.class, CollectionPropertyAccessorFactory.createGet(VULNERABILITIES));
        ADD_VULNERABILITIES = FunctionDefinitionFactory.create("addVulnerabilities", Hardware.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createAdd(VULNERABILITIES)), Vulnerability[].class);
        REMOVE_VULNERABILITIES = FunctionDefinitionFactory.create("removeVulnerabilities", Hardware.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createRemove(VULNERABILITIES)), Vulnerability[].class);

    }

    // ----- Functions End -----

    /**
     * Creates a new hardware part.
     */
    public Hardware() {

    }

}
