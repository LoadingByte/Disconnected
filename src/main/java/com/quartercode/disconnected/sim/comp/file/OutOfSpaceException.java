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

package com.quartercode.disconnected.sim.comp.file;

/**
 * This runtime exception occures if there is not enough space on a hard drive for handling some new bytes (e.g. from a file).
 */
public class OutOfSpaceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Media       host;
    private final long        size;

    /**
     * Creates a new out of space exception and sets the host which should have handled the new bytes and the amount of new bytes.
     * 
     * @param host The hard drive host which should have handled the new bytes,
     * @param size The amount of new bytes.
     */
    public OutOfSpaceException(Media host, long size) {

        super("Out of space on " + host.getLetter() + ": " + host.getFilled() + "b/" + host.getSize() + "b filled, can't handle " + size + "b");
        this.host = host;
        this.size = size;
    }

    /**
     * Returns the hard drive host which should have handled the new bytes,
     * 
     * @return The hard drive host which should have handled the new bytes,
     */
    public Media getHost() {

        return host;
    }

    /**
     * Returns the amount of new bytes.
     * 
     * @return The amount of new bytes.
     */
    public long getSize() {

        return size;
    }

}
