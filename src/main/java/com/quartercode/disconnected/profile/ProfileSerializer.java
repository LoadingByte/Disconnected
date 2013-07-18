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

package com.quartercode.disconnected.profile;

import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXB;
import com.quartercode.disconnected.sim.Simulation;

/**
 * This utility class loads a saves stored profile simulations for serializing simulations.
 * 
 * @see ProfileManager
 */
public class ProfileSerializer {

    /**
     * Writes a profile simulation to an output stream.
     * 
     * @param outputStream The output stream for writing.
     * @param simulation The profile simulation to serialize.
     */
    public static void serialize(OutputStream outputStream, Simulation simulation) {

        JAXB.marshal(simulation, outputStream);
    }

    /**
     * Reads a profile simulation from an input stream.
     * 
     * @param inputStream The input stream for reading.
     * @return The deserialized profile simulation.
     */
    public static Simulation deserialize(InputStream inputStream) {

        return JAXB.unmarshal(inputStream, Simulation.class);
    }

    private ProfileSerializer() {

    }

}
