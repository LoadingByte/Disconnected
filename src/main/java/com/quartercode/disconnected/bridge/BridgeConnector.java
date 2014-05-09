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

package com.quartercode.disconnected.bridge;

/**
 * A bridge connector represents a connection of one {@link Bridge} to another bridge.
 * For such connections between two bridges, every bridge holds its own connector that sends and receives {@link Event}s.<br>
 * <br>
 * Bridge connectors can be started and stopped. A stopped connector cannot be started again.
 * 
 * @see Bridge
 */
public interface BridgeConnector {

    /**
     * Starts up the bridge connector and connects the given local {@link Bridge} with some other bridge.
     * The data for the second bridge the connector connects to should be stored in the connector object.
     * This method can only be called once.<br>
     * <br>
     * Please note that this method must also create a connector on the second bridge somehow.
     * The second connector takes care of receiving the packets that are sent by the first connector.
     * 
     * @param localBridge The local bridge that should be connected to some other bridge.
     * @throws BridgeConnectorException Something goes wrong while establishing the connection to the second bridge.
     */
    public void start(Bridge localBridge) throws BridgeConnectorException;

    /**
     * Shuts down the bridge connector and disconnects it from the second {@link Bridge}.
     * This method can only be called once after the {@link #start(Bridge)} method has been invoked.<br>
     * <br>
     * Please note that the connector of the second bridge must stop itself after the first connector was shut down.
     * 
     * @throws BridgeConnectorException Something goes wrong while disconnecting from the second bridge.
     */
    public void stop() throws BridgeConnectorException;

    /**
     * Sends the given {@link Event} to the second bridge connector this connector is connected to.
     * That second connector should hand the event over to its local {@link Bridge}.
     * 
     * @param event The event object that should be sent to the connected bridge connector.
     *        For doing this, the event may be serialized.
     * @throws BridgeConnectorException Something goes wrong while sending the given event.
     */
    public void send(Event event) throws BridgeConnectorException;

}
