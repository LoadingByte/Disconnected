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

package com.quartercode.disconnected.world.comp.net;

import java.net.InetAddress;
import java.util.Arrays;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Lockable;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.hardware.NetworkInterface;

/**
 * This class represents an ip address which is used to define the "location of a computer in the internet".
 * For an exact breakdown of ip addresses, use the javadoc of {@link InetAddress}.
 * 
 * @see NetworkInterface
 * @see Address
 */
public class IP extends DefaultFeatureHolder {

    // ----- Properties -----

    /**
     * The 4 numbers to use for the ip (must be in range 0 <= number <= 255).
     */
    protected static final FeatureDefinition<ObjectProperty<Integer[]>> PARTS;

    static {

        PARTS = new AbstractFeatureDefinition<ObjectProperty<Integer[]>>("parts") {

            @Override
            public ObjectProperty<Integer[]> create(FeatureHolder holder) {

                return new ObjectProperty<Integer[]>(getName(), holder, new Integer[4]);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the 4 numbers to use for the ip (must be in range 0 <= number <= 255).
     * The returned array shouldn't be changed in any way. You can use {@link #SET_PARTS} for that purpose.
     */
    public static final FunctionDefinition<Integer[]>                   GET_PARTS;

    /**
     * Changes the 4 numbers to use for the ip (must be in range 0 <= number <= 255).
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
     * <td>{@link Integer}[]</td>
     * <td>parts</td>
     * <td>The new 4 numbers to use for the ip (must be in range 0 <= number <= 255).</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                        SET_PARTS;

    /**
     * Changes the stored ip to the one stored by the given ip object.
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
     * <td>{@link IP}</td>
     * <td>ip</td>
     * <td>The ip object whose stored ip should be copied into this ip object.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                        FROM_OBJECT;

    /**
     * Changes the stored ip to the one set by the given dotted quad notation string.
     * The string is using the format XXXX.XXXX.XXXX.XXXX (e.g. 127.0.0.1).
     * Each number (they are seperated by dots) represents a quad in the dotted quad notation and must be in range 0 <= number <= 255.
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
     * <td>ip</td>
     * <td>The new ip given in the dotted quad notation.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                        FROM_STRING;

    /**
     * Returns the stored ip in the dotted quad notation.
     * The string is using the format XXXX.XXXX.XXXX.XXXX (e.g. 127.0.0.1).
     * Each number (they are seperated by dots) represents a quad in the dotted quad notation and must be in range 0 <= number <= 255.
     */
    public static final FunctionDefinition<String>                      TO_STRING;

    static {

        GET_PARTS = FunctionDefinitionFactory.create("getParts", IP.class, PropertyAccessorFactory.createGet(PARTS));
        SET_PARTS = FunctionDefinitionFactory.create("setParts", IP.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(PARTS)), Integer[].class);
        SET_PARTS.addExecutor(IP.class, "checkQuadRange", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.CHECK)
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                Integer[] parts = (Integer[]) arguments[0];
                Validate.isTrue(parts.length == 4, "The ip must have 4 parts (e.g. [127, 0, 0, 1]): ", Arrays.toString(parts));
                for (int part : parts) {
                    Validate.isTrue(part >= 0 || part <= 255, "Every ip part must be in range 0 <= part <= 255 (e.g. [127, 0, 0, 1]): ", Arrays.toString(parts));
                }

                return null;
            }

        });

        FROM_OBJECT = FunctionDefinitionFactory.create("fromObject", IP.class, new FunctionExecutor<Void>() {

            @Override
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                holder.get(PARTS).set(Arrays.copyOf( ((IP) arguments[0]).get(PARTS).get(), 4));

                return null;
            }

        }, IP.class);

        FROM_STRING = FunctionDefinitionFactory.create("fromString", IP.class, new FunctionExecutor<Void>() {

            @Override
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                int[] parts = new int[4];
                String[] stringParts = ((String) arguments[0]).split("\\.");
                for (int counter = 0; counter < parts.length; counter++) {
                    parts[counter] = Integer.parseInt(stringParts[counter]);
                }
                holder.get(SET_PARTS).invoke(parts);

                return null;
            }

        }, String.class);

        TO_STRING = FunctionDefinitionFactory.create("toString", IP.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                String parts = "";
                for (int part : holder.get(GET_PARTS).invoke()) {
                    parts += "." + part;
                }
                return parts.substring(1);
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new empty ip.
     * You should fill it using {@link #SET_PARTS} after creation.
     */
    public IP() {

    }

}
