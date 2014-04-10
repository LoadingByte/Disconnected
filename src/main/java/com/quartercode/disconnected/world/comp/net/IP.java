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
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.world.StringRepresentable;
import com.quartercode.disconnected.world.comp.hardware.NetworkInterface;

/**
 * This class represents an ip address which is used to define the "location of a computer in the internet".
 * For an exact breakdown of ip addresses, use the javadoc of {@link InetAddress}.
 * 
 * @see NetworkInterface
 * @see Address
 */
public class IP extends DefaultFeatureHolder implements StringRepresentable {

    // ----- Properties -----

    /**
     * The 4 numbers to use for the ip (must be in range 0 <= number <= 255).
     */
    public static final PropertyDefinition<Integer[]> PARTS;

    static {

        PARTS = ObjectProperty.createDefinition("parts", new Integer[4]);
        PARTS.addSetterExecutor("checkQuadRange", IP.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                Integer[] parts = (Integer[]) arguments[0];
                Validate.isTrue(parts.length == 4, "The ip must have 4 parts (e.g. [127, 0, 0, 1]): ", Arrays.toString(parts));
                for (int part : parts) {
                    Validate.isTrue(part >= 0 || part <= 255, "Every ip part must be in range 0 <= part <= 255 (e.g. [127, 0, 0, 1]): ", Arrays.toString(parts));
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Properties End -----

    // ----- Functions -----

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
    public static final FunctionDefinition<Void>      FROM_OBJECT;

    /**
     * Returns the stored ip in the dotted quad notation.
     * The string is using the format XXXX.XXXX.XXXX.XXXX (e.g. 127.0.0.1).
     * Each number (they are seperated by dots) represents a quad in the dotted quad notation and must be in range 0 <= number <= 255.
     */
    public static final FunctionDefinition<String>    TO_STRING   = StringRepresentable.TO_STRING;

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
    public static final FunctionDefinition<Void>      FROM_STRING = StringRepresentable.FROM_STRING;

    static {

        FROM_OBJECT = FunctionDefinitionFactory.create("fromObject", IP.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                invocation.getHolder().get(PARTS).set(Arrays.copyOf( ((IP) arguments[0]).get(PARTS).get(), 4));

                return invocation.next(arguments);
            }

        }, IP.class);

        TO_STRING.addExecutor("default", IP.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) throws ExecutorInvocationException {

                StringBuilder parts = new StringBuilder();
                for (int part : invocation.getHolder().get(PARTS).get()) {
                    parts.append(".").append(part);
                }

                invocation.next(arguments);
                return parts.substring(1);
            }

        });
        FROM_STRING.addExecutor("default", IP.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                Integer[] parts = new Integer[4];
                String[] stringParts = ((String) arguments[0]).split("\\.");
                for (int counter = 0; counter < parts.length; counter++) {
                    parts[counter] = Integer.parseInt(stringParts[counter]);
                }
                invocation.getHolder().get(PARTS).set(parts);

                return invocation.next(arguments);
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
