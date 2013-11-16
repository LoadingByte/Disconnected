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

/**
 * A network connection is a persistent connection to the internet using a cable etc.
 * The network connection is basically the connection to the internet.
 * Every network connection has a speed a byte needs to get transfered through it.
 */
public class NetConnection {

    private long speed;

    /**
     * Creates a new empty network connection.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected NetConnection() {

    }

    /**
     * Creates a new network connection and sets the speed.
     * 
     * @param speed The number of bytes the network interface can transfer in one second (the "speed").
     */
    public NetConnection(long speed) {

        this.speed = speed;
    }

    /**
     * Returns the number of bytes the network interface can transfer in one second (the "speed").
     * 
     * @return The number of bytes the network interface can transfer in one second (the "speed").
     */
    public long getSpeed() {

        return speed;
    }

    /**
     * Calculates the number of bytes that can pass through this network interface in the given duration.
     * 
     * @param duration The duration to use in milliseconds.
     * @return The calculated number of bytes that can pass through this network interface in the given duration.
     */
    public long getBytesPer(int duration) {

        return speed / 1000 * duration;
    }

    /**
     * Changes the number of bytes the network interface can transfer in one second (the "speed") to a new amount.
     * This can happen due to a cable extension etc.
     * 
     * @param speed The new number of bytes the network interface can transfer in one second (the "speed").
     */
    public void setSpeed(long speed) {

        this.speed = speed;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (speed ^ speed >>> 32);
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NetConnection other = (NetConnection) obj;
        if (speed != other.speed) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return "NetConnection [" + speed + " b/s speed]";
    }

}
