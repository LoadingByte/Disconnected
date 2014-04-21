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

import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.world.general.StringRepresentable;

/**
 * This class represents an address which locates a specific service which is available through a specific network interface.
 * The network interface is defined by an ip, the service by a port on which it's listening.
 * 
 * @see IP
 */
public class Address extends DefaultFeatureHolder implements StringRepresentable {

    // ----- Properties -----

    /**
     * The target {@link IP} which represents the network interface which holds the service.
     */
    public static final PropertyDefinition<IP>      IP;

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
     * <td>The provided port lesser than 0 or greater than 65535.</td>
     * </tr>
     * </table>
     */
    public static final PropertyDefinition<Integer> PORT;

    static {

        IP = ObjectProperty.createDefinition("ip");

        PORT = ObjectProperty.createDefinition("port");
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
     * Changes the stored ip and port to the ones stored by the given address object.
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
     * The string is using the format {@code IP:PORT} (e.g. {@code 127.0.0.1:8080}).
     */
    public static final FunctionDefinition<String>  TO_STRING   = StringRepresentable.TO_STRING;

    /**
     * Changes the stored address to the one set by the given string.
     * The string is using the format {@code IP:PORT} (e.g. {@code 127.0.0.1:8080}).
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
     * <td>The new address given in the {@code IP:PORT} notation.</td>
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
     * <td>The provided string does not match the {@code IP:PORT} notation.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>    FROM_STRING = StringRepresentable.FROM_STRING;

    static {

        FROM_OBJECT = FunctionDefinitionFactory.create("fromObject", Address.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                String ipString = ((Address) arguments[0]).get(IP).get().get(com.quartercode.disconnected.world.comp.net.IP.TO_STRING).invoke();
                IP ip = new IP();
                ip.get(com.quartercode.disconnected.world.comp.net.IP.FROM_STRING).invoke(ipString);
                holder.get(IP).set(ip);
                holder.get(PORT).set( ((Address) arguments[0]).get(PORT).get());

                return invocation.next(arguments);
            }

        }, Address.class);

        TO_STRING.addExecutor("default", Address.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                String result = holder.get(IP).get().get(com.quartercode.disconnected.world.comp.net.IP.TO_STRING).invoke() + ":" + holder.get(PORT).get();
                invocation.next(arguments);
                return result;
            }

        });
        FROM_STRING.addExecutor("default", Address.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                String[] parts = ((String) arguments[0]).split(":");
                Validate.isTrue(parts.length == 2, "Address (%s) must have the format IP:PORT", arguments[0]);
                IP ip = new IP();
                ip.get(com.quartercode.disconnected.world.comp.net.IP.FROM_STRING).invoke(parts[0]);
                invocation.getHolder().get(IP).set(ip);
                invocation.getHolder().get(PORT).set(Integer.parseInt(parts[1]));

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new empty address.
     * You should fill the {@link #IP} and {@link #PORT} properties after creation.
     */
    public Address() {

    }

}
