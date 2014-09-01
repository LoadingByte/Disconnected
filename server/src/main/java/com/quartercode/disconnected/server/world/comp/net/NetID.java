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

package com.quartercode.disconnected.server.world.comp.net;

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.server.world.WorldFeatureHolder;
import com.quartercode.disconnected.server.world.general.StringRepresentable;

/**
 * This class represents a network id which is used to define the "location of a computer in the internet".
 * It contains a subnet part which defines the LAN in which the using device is located in,
 * as well as an actual id part which defines the location of the device inside that LAN.<br>
 * <br>
 * Example:
 * 
 * <pre>
 * NetID:               4353.8
 * Subnet (LAN or WAN): 4353
 * Location in Subnet:  8
 * </pre>
 * 
 * @see Address
 */
public class NetID extends WorldFeatureHolder implements StringRepresentable {

    // ----- Properties -----

    /**
     * The subnet id the net id is placed in.
     * A subnet id might contain multiple net ids that are connected to each other through a router.<br>
     * <br>
     * Exceptions that can occur when setting:
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>There provided subnet id is not greater than or equal 0.</td>
     * </tr>
     * </table>
     */
    public static final PropertyDefinition<Integer> SUBNET;

    /**
     * The acutal net id that is used to identify a device inside a subnet.
     * The id 0 identifies the main router of a subnet.<br>
     * <br>
     * Exceptions that can occur when setting:
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>There provided net id is not greater than or equal 0.</td>
     * </tr>
     * </table>
     */
    public static final PropertyDefinition<Integer> ID;

    static {

        SUBNET = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "subnet", "storage", new StandardStorage<>());
        SUBNET.addSetterExecutor("checkRange", NetID.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                int subnet = (Integer) arguments[0];
                Validate.isTrue(subnet >= 0, "The subnet id (%i) must be greater than or equal 0", subnet);

                return invocation.next(arguments);
            }

        });

        ID = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "id", "storage", new StandardStorage<>());
        ID.addSetterExecutor("checkRange", NetID.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                int id = (Integer) arguments[0];
                Validate.isTrue(id >= 0, "The net id (%i) must be greater than or equal 0", id);

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    /**
     * Changes the stored net id to the one stored by the given net id object.
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
     * <td>{@link NetID}</td>
     * <td>netId</td>
     * <td>The net id object whose stored net id should be copied into this net id object.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>    FROM_OBJECT;

    /**
     * Returns the stored net id as a string.
     * The returned string is using the format {@code SUBNET.ID} (e.g. {@code 4353.8}).
     */
    public static final FunctionDefinition<String>  TO_STRING   = StringRepresentable.TO_STRING;

    /**
     * Changes the stored net id to the one set by the string.
     * The string must be using the format {@code SUBNET.ID} (e.g. {@code 4353.8}).
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
     * <td>netId</td>
     * <td>The new net id provided in the default notation.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The provided string does not match the {@code SUBNET.ID} notation.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>    FROM_STRING = StringRepresentable.FROM_STRING;

    static {

        FROM_OBJECT = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "fromObject", "parameters", new Class[] { NetID.class });
        FROM_OBJECT.addExecutor("default", NetID.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                NetID object = (NetID) arguments[0];

                holder.get(SUBNET).set(object.get(SUBNET).get());
                holder.get(ID).set(object.get(ID).get());

                return invocation.next(arguments);
            }

        });

        TO_STRING.addExecutor("default", NetID.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                int subnet = holder.get(SUBNET).get();
                int id = holder.get(ID).get();

                invocation.next(arguments);
                return new StringBuilder().append(subnet).append(".").append(id).toString();
            }

        });
        FROM_STRING.addExecutor("default", NetID.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                String[] stringParts = ((String) arguments[0]).split(".");
                Validate.isTrue(stringParts.length == 2, "Net id (%s) must be provided in the format SUBNET.ID", arguments[0]);

                holder.get(SUBNET).set(Integer.parseInt(stringParts[0]));
                holder.get(ID).set(Integer.parseInt(stringParts[1]));

                return invocation.next(arguments);
            }

        });

    }

}
