/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

/**
 * This exception occurs if {@link Network#addConnection(NetNode, NetNode)} is called although one {@link NetNode} has already reached its {@link NetNode#getMaxConnections() connection limit}.
 * As a result, this exception is thrown to indicate that the connection limit of that very net node has been exceeded.
 *
 * @see NetNode
 * @see Network
 */
public class ConnectionLimitExceededException extends RuntimeException {

    private static final long serialVersionUID = -9096846485636714257L;

    private final NetNode     netNode;

    /**
     * Creates a new connection limit exceeded exception.
     *
     * @param netNode The {@link NetNode} whose {@link NetNode#getMaxConnections() connection limit} has bee maxed out.
     */
    public ConnectionLimitExceededException(NetNode netNode) {

        super("The net node '" + netNode + "' cannot accept any more connections because its connection limit of " + netNode.getMaxConnections() + " has been reached");

        this.netNode = netNode;
    }

    /**
     * Returns the {@link NetNode} whose {@link NetNode#getMaxConnections() connection limit} has bee maxed out.
     * Therefore, no more net nodes can directly connect to it.
     *
     * @return The net node whose connection limit has been exceeded.
     */
    public NetNode getNetNode() {

        return netNode;
    }

}
