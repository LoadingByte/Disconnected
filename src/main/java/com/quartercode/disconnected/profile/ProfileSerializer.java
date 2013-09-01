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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.sim.Simulation;

/**
 * This utility class loads a saves stored profile simulations for serializing simulations.
 * 
 * @see ProfileManager
 */
public class ProfileSerializer {

    private static final Logger LOGGER = Logger.getLogger(ProfileSerializer.class.getName());

    private static JAXBContext  context;

    static {

        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Simulation.class);
        classes.addAll(Disconnected.getRegistry().getClasses());

        try {
            context = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
        }
        catch (JAXBException e) {
            LOGGER.log(Level.SEVERE, "A JAXB exception occurred while creating context", e);
        }
    }

    /**
     * Writes a profile simulation to an output stream.
     * 
     * @param outputStream The output stream for writing.
     * @param simulation The profile simulation to serialize.
     * @throws JAXBException An exception occurred while serializing the xml document.
     */
    public static void serialize(OutputStream outputStream, Simulation simulation) throws JAXBException {

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(simulation, outputStream);
    }

    /**
     * Reads a profile simulation from an input stream.
     * 
     * @param inputStream The input stream for reading.
     * @return The deserialized profile simulation.
     * @throws JAXBException An exception occurred while deserializing the xml document.
     */
    public static Simulation deserialize(InputStream inputStream) throws JAXBException {

        return (Simulation) context.createUnmarshaller().unmarshal(inputStream);
    }

    private ProfileSerializer() {

    }

}
