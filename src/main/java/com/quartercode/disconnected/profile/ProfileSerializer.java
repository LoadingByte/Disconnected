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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.sim.comp.session.SessionProgram.Session;
import com.quartercode.disconnected.util.RandomPool;

/**
 * This utility class loads a saves stored profile simulations for serializing simulations.
 * 
 * @see ProfileManager
 */
public class ProfileSerializer {

    private static final Logger LOGGER = Logger.getLogger(ProfileSerializer.class.getName());

    /**
     * Serializes the given profile to the given output stream.
     * This writes a zip to the stream which contains the data of the profile.
     * 
     * @param outputStream The output stream for writing.
     * @param profile The profile to serialize to the given output stream.
     * @throws IOException Something goes wrong while writing to the stream.
     * @throws JAXBException An exception occurres while serializing the simulation as xml.
     */
    public static void serializeProfile(OutputStream outputStream, Profile profile) throws IOException, JAXBException {

        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        zipOutputStream.putNextEntry(new ZipEntry("simulation.xml"));
        serializeSimulation(zipOutputStream, profile.getSimulation());
        zipOutputStream.closeEntry();

        zipOutputStream.putNextEntry(new ZipEntry("random.ser"));
        serializeRandom(zipOutputStream, profile.getSimulation().getRandom());
        zipOutputStream.closeEntry();

        zipOutputStream.close();
    }

    /**
     * Deserializes the data of a profile (like the simulation) from the given input stream.
     * This reads a zip from the stream which contains the data of the profile.
     * 
     * @param inputStream The input stream for reading.
     * @return The deserialized data [0=Simulation, 1=RandomPool].
     * @throws IOException Something goes wrong while reading from the stream.
     * @throws JAXBException An exception occurres while deserializing the simulation's xml document.
     * @throws ClassNotFoundException A class which is used for the random pool can't be found.
     */
    public static Object[] deserializeProfileData(InputStream inputStream) throws IOException, JAXBException, ClassNotFoundException {

        Object[] data = new Object[2];

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        while ( (zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().equals("simulation.xml")) {
                data[0] = deserializeSimulation(zipInputStream);
            } else if (zipEntry.getName().equals("random.ser")) {
                data[1] = deserializeRandom(zipInputStream);
            }
        }
        zipInputStream.close();

        return data;
    }

    /**
     * Creates a new {@link JAXBContext} for the {@link Simulation} model.
     * 
     * @return A {@link JAXBContext} which can be used for the {@link Simulation} model.
     */
    public static JAXBContext createSimulationContext() {

        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Simulation.class);
        classes.addAll(Disconnected.getRegistry().getClasses());

        try {
            return JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
        }
        catch (JAXBException e) {
            LOGGER.log(Level.SEVERE, "A JAXB exception occurred while creating context", e);
        }

        return null;
    }

    /**
     * Writes a profile simulation to an output stream.
     * 
     * @param outputStream The output stream for writing.
     * @param simulation The profile simulation to serialize.
     * @throws IllegalStateException There are open sessions which aren't serializable.
     * @throws JAXBException An exception occurred while serializing the xml document.
     */
    public static void serializeSimulation(OutputStream outputStream, Simulation simulation) throws JAXBException {

        Marshaller marshaller = createSimulationContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(simulation, outputStream);

        for (Computer computer : simulation.getComputers()) {
            if (computer.getOperatingSystem().getProcessManager().getRootProcess() != null) {
                for (Process process : computer.getOperatingSystem().getProcessManager().getAllProcesses()) {
                    if (process.getExecutor() instanceof Session && ! ((Session) process.getExecutor()).isSerializable()) {
                        throw new IllegalStateException("Can't serialize: There are open sessions which aren't serializable");
                    }
                }
            }
        }
    }

    /**
     * Reads a profile simulation from an input stream.
     * 
     * @param inputStream The input stream for reading.
     * @return The deserialized profile simulation.
     * @throws JAXBException An exception occurred while deserializing the xml document.
     */
    public static Simulation deserializeSimulation(InputStream inputStream) throws JAXBException {

        return (Simulation) createSimulationContext().createUnmarshaller().unmarshal(new JAXBNoCloseInputStream(inputStream));
    }

    /**
     * Serializes the given random pool to the given output stream.
     * 
     * @param outputStream The output stream for writing.
     * @param random The random pool to serialize to the given output stream.
     * @throws IOException Something goes wrong while writing to the stream.
     */
    public static void serializeRandom(OutputStream outputStream, RandomPool random) throws IOException {

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(random);
        objectOutputStream.flush();
    }

    /**
     * Deserializes a random pool from the given input stream.
     * 
     * @param inputStream The input stream for reading.
     * @return The deserialized random pool.
     * @throws IOException Something goes wrong while reading from the stream.
     * @throws ClassNotFoundException A class which is used in the random pool can't be found.
     */
    public static RandomPool deserializeRandom(InputStream inputStream) throws IOException, ClassNotFoundException {

        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        return (RandomPool) objectInputStream.readObject();
    }

    private ProfileSerializer() {

    }

    private static class JAXBNoCloseInputStream extends FilterInputStream {

        private JAXBNoCloseInputStream(InputStream inputStream) {

            super(inputStream);
        }

        @Override
        public void close() {

            // Do nothing
        }

    }

}
