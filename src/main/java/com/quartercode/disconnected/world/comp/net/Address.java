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

import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Lockable;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.StringRepresentable;
import com.quartercode.disconnected.world.comp.hardware.NetworkInterface;

/**
 * This class represents an address which locates a specific service which is available through a specific network interface.
 * The network interface is defined by an ip, the service by a port on which it's listening.
 * 
 * @see IP
 * @see NetworkInterface
 */
public class Address extends DefaultFeatureHolder implements StringRepresentable {

    // ----- Properties -----

    /**
     * The target {@link IP} which represents the network interface which holds the service.
     */
    protected static final FeatureDefinition<ObjectProperty<IP>>      IP;

    /**
     * The target port which specifies the service.
     */
    protected static final FeatureDefinition<ObjectProperty<Integer>> PORT;

    static {

        IP = new AbstractFeatureDefinition<ObjectProperty<IP>>("ip") {

            @Override
            public ObjectProperty<IP> create(FeatureHolder holder) {

                return new ObjectProperty<IP>(getName(), holder);
            }

        };

        PORT = new AbstractFeatureDefinition<ObjectProperty<Integer>>("port") {

            @Override
            public ObjectProperty<Integer> create(FeatureHolder holder) {

                return new ObjectProperty<Integer>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the target {@link IP} which represents the network interface which holds the service.
     * The returned object shouldn't be changed in any way. You can use {@link #SET_IP} for that purpose.
     */
    public static final FunctionDefinition<IP>                        GET_IP;

    /**
     * Changes the target {@link IP} which represents the network interface which holds the service.
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
     * <td>The new target {@link IP}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_IP;

    /**
     * Returns the target port which specifies the service.
     */
    public static final FunctionDefinition<Integer>                   GET_PORT;

    /**
     * Changes the target port which specifies the service.
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
     * <td>{@link Integer}</td>
     * <td>port</td>
     * <td>The new target port.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_PORT;

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
    public static final FunctionDefinition<Void>                      FROM_OBJECT;

    /**
     * Returns the stored address as a string.
     * The string is using the format IP:PORT (e.g. 127.0.0.1:8080).
     */
    public static final FunctionDefinition<String>                    TO_STRING   = StringRepresentable.TO_STRING;

    /**
     * Changes the stored address to the one set by the given string.
     * The string is using the format IP:PORT (e.g. 127.0.0.1:8080).
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
     * <td>The new address given in the "IP:PORT" notation.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      FROM_STRING = StringRepresentable.FROM_STRING;

    static {

        GET_IP = FunctionDefinitionFactory.create("getIp", Address.class, PropertyAccessorFactory.createGet(IP));
        SET_IP = FunctionDefinitionFactory.create("setIp", Address.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(IP)), IP.class);

        GET_PORT = FunctionDefinitionFactory.create("getPort", Address.class, PropertyAccessorFactory.createGet(PORT));
        SET_PORT = FunctionDefinitionFactory.create("setPort", Address.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(PORT)), Integer.class);
        SET_PORT.addExecutor(IP.class, "checkRange", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_6)
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                int port = (Integer) arguments[0];
                Validate.isTrue(port >= 0 || port <= 65535, "The port must be in range 0 <= port <= 65535 (e.g. 8080): ", port);

                return null;
            }

        });

        FROM_OBJECT = FunctionDefinitionFactory.create("fromObject", Address.class, new FunctionExecutor<Void>() {

            @Override
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                String ipString = ((Address) arguments[0]).get(IP).get().get(com.quartercode.disconnected.world.comp.net.IP.TO_STRING).invoke();
                IP ip = new IP();
                ip.setLocked(false);
                ip.get(com.quartercode.disconnected.world.comp.net.IP.FROM_STRING).invoke(ipString);
                ip.setLocked(true);
                holder.get(SET_IP).invoke(ip);
                holder.get(SET_PORT).invoke( ((Address) arguments[0]).get(PORT).get());

                return null;
            }

        }, Address.class);

        TO_STRING.addExecutor(Address.class, "default", new FunctionExecutor<String>() {

            @Override
            public String invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return holder.get(GET_IP).invoke().get(com.quartercode.disconnected.world.comp.net.IP.TO_STRING).invoke() + ":" + holder.get(GET_PORT).invoke();
            }

        });
        FROM_STRING.addExecutor(Address.class, "default", new FunctionExecutor<Void>() {

            @Override
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                String[] parts = ((String) arguments[0]).split(":");
                Validate.isTrue(parts.length == 2, "Address must have the format IP:PORT: ", arguments[0]);
                IP ip = new IP();
                ip.setLocked(false);
                ip.get(com.quartercode.disconnected.world.comp.net.IP.FROM_STRING).invoke(parts[0]);
                ip.setLocked(true);
                holder.get(SET_IP).invoke(ip);
                holder.get(SET_PORT).invoke(Integer.parseInt(parts[1]));

                return null;
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new empty address.
     * You should fill it using {@link #SET_IP} and {@link #SET_PORT} after creation.
     */
    public Address() {

    }

}
