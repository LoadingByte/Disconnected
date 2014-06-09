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

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.world.general.StringRepresentable;

/**
 * This class represents an address which locates a specific service which is available through a specific network interface.
 * The network interface is defined by an {@link NetID}, the service by a port on which it's listening.
 * 
 * @see NetID
 */
public class Address extends DefaultFeatureHolder implements StringRepresentable {

    // ----- Properties -----

    /**
     * The target {@link NetID} that represents the network interface which holds the service.
     */
    public static final PropertyDefinition<NetID>   NET_ID;

    /**
     * The target port which specifies the service.<br>
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
     * <td>The provided port is lesser than 0 or greater than 65535.</td>
     * </tr>
     * </table>
     */
    public static final PropertyDefinition<Integer> PORT;

    static {

        NET_ID = create(new TypeLiteral<PropertyDefinition<NetID>>() {}, "name", "netID", "storage", new StandardStorage<>());

        PORT = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "port", "storage", new StandardStorage<>());
        PORT.addSetterExecutor("checkRange", Address.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                int port = (Integer) arguments[0];
                Validate.isTrue(port >= 0 && port <= 65535, "The port (%d) must be in range 0 <= port <= 65535 (e.g. 8080)", port);

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    /**
     * Changes the stored {@link NetID} and port to the ones stored in the given address object.
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
     * <td>{@link Address}</td>
     * <td>address</td>
     * <td>The address object whose stored data should be copied into this address object.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>    FROM_OBJECT;

    /**
     * Returns the stored address as a string.
     * The returned string is using the format {@code NET_ID:PORT} (e.g. {@code 4353.8:80}).
     */
    public static final FunctionDefinition<String>  TO_STRING   = StringRepresentable.TO_STRING;

    /**
     * Changes the stored address to the one set by the given string.
     * The string must be using the format {@code NET_ID:PORT} (e.g. {@code 4353.8:80}).
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
     * <td>address</td>
     * <td>The new address given in the {@code NET_ID:PORT} notation.</td>
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
     * <td>The provided string does not match the {@code NET_ID:PORT} notation.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>    FROM_STRING = StringRepresentable.FROM_STRING;

    static {

        FROM_OBJECT = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "fromObject", "parameters", new Class<?>[] { Address.class });
        FROM_OBJECT.addExecutor("default", Address.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                Address object = (Address) arguments[0];

                NetID netID = new NetID();
                netID.get(NetID.FROM_OBJECT).invoke(object.get(NET_ID).get());
                holder.get(NET_ID).set(netID);

                holder.get(PORT).set(object.get(PORT).get());

                return invocation.next(arguments);
            }

        });

        TO_STRING.addExecutor("default", Address.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                String netID = holder.get(NET_ID).get().get(NetID.TO_STRING).invoke();
                int port = holder.get(PORT).get();

                invocation.next(arguments);
                return new StringBuilder(netID).append(":").append(port).toString();
            }

        });
        FROM_STRING.addExecutor("default", Address.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                String[] stringParts = ((String) arguments[0]).split(":");
                Validate.isTrue(stringParts.length == 2, "Address (%s) must be provided in the format NET_ID:PORT", arguments[0]);

                NetID netID = new NetID();
                netID.get(NetID.FROM_STRING).invoke(stringParts[0]);
                holder.get(NET_ID).set(netID);

                holder.get(PORT).set(Integer.parseInt(stringParts[1]));

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new empty address.
     * You should fill the {@link #NET_ID} and {@link #PORT} properties after creation.
     */
    public Address() {

    }

}
