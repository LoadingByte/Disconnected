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
import com.quartercode.disconnected.sim.world.RootObject;
import com.quartercode.disconnected.sim.world.World;
import com.quartercode.disconnected.util.RandomPool;

/**
 * This utility class loads a saves stored {@link Profile}s for serializing {@link Simulation}s.
 * 
 * @see Profile
 * @see ProfileManager
 * @see Simulation
 */
public class ProfileSerializer {

    private static final Logger LOGGER = Logger.getLogger(ProfileSerializer.class.getName());

    /**
     * Serializes the given {@link Profile} to the given {@link OutputStream}.
     * This writes a zip to the stream which contains the data of the {@link Profile}.
     * 
     * @param outputStream The {@link OutputStream} for writing.
     * @param profile The {@link Profile} to serialize to the given {@link OutputStream}.
     * @throws IOException Something goes wrong while writing to the stream.
     * @throws JAXBException An exception occurres while serializing the {@link World} of the {@link Simulation} as xml.
     */
    public static void serializeProfile(OutputStream outputStream, Profile profile) throws IOException, JAXBException {

        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        zipOutputStream.putNextEntry(new ZipEntry("world.xml"));
        serializeWorld(zipOutputStream, profile.getSimulation().getWorld());
        zipOutputStream.closeEntry();

        zipOutputStream.putNextEntry(new ZipEntry("random.ser"));
        serializeRandom(zipOutputStream, profile.getSimulation().getRandom());
        zipOutputStream.closeEntry();

        zipOutputStream.close();
    }

    /**
     * Deserializes the data of a {@link Profile} (like the {@link Simulation}) from the given {@link InputStream}.
     * This reads a zip from the stream which contains the data of the {@link Profile}.
     * This returns null if the algorithm can't read the required files from the {@link InputStream}.
     * 
     * @param inputStream The {@link InputStream} for reading.
     * @return The deserialized {@link Simulation} object.
     * @throws IOException Something goes wrong while reading from the stream.
     * @throws JAXBException An exception occurres while deserializing the {@link Simulation}'s xml document.
     * @throws ClassNotFoundException A class which is used for the {@link RandomPool} can't be found.
     */
    public static Simulation deserializeSimulation(InputStream inputStream) throws IOException, JAXBException, ClassNotFoundException {

        World world = null;
        RandomPool random = null;

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        while ( (zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().equals("world.xml")) {
                world = deserializeWorld(zipInputStream);
            } else if (zipEntry.getName().equals("random.ser")) {
                random = deserializeRandom(zipInputStream);
            }
        }
        zipInputStream.close();

        if (world != null && random != null) {
            Simulation simulation = new Simulation(random);
            simulation.setWorld(world);
            return simulation;
        } else {
            return null;
        }
    }

    /**
     * Creates a new {@link JAXBContext} which can be used for {@link World} xml model.
     * 
     * @return A {@link JAXBContext} for the {@link World} model.
     */
    public static JAXBContext createWorldContext() {

        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(World.class);
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
     * Writes a {@link World} as xml to an {@link OutputStream}.
     * 
     * @param outputStream The {@link OutputStream} for writing.
     * @param world The {@link World} to serialize.
     * @throws IllegalStateException There are open {@link Session}s which aren't serializable.
     * @throws JAXBException An exception occurred while serializing the xml document.
     */
    public static void serializeWorld(OutputStream outputStream, World world) throws JAXBException {

        for (Computer computer : world.getRoot().get(RootObject.COMPUTERS_PROPERTY)) {
            if (computer.getOperatingSystem().getProcessManager().getRootProcess() != null) {
                for (Process process : computer.getOperatingSystem().getProcessManager().getAllProcesses()) {
                    if (process.getExecutor() instanceof Session && ! ((Session) process.getExecutor()).isSerializable()) {
                        throw new IllegalStateException("Can't serialize: There are open sessions which aren't serializable");
                    }
                }
            }
        }

        Marshaller marshaller = createWorldContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(world, outputStream);
    }

    /**
     * Reads a {@link World} which is saved as xml from an {@link InputStream}.
     * 
     * @param inputStream The {@link InputStream} for reading.
     * @return The deserialized {@link World} object.
     * @throws JAXBException An exception occurred while deserializing the xml document.
     */
    public static World deserializeWorld(InputStream inputStream) throws JAXBException {

        return (World) createWorldContext().createUnmarshaller().unmarshal(new JAXBNoCloseInputStream(inputStream));
    }

    /**
     * Serializes the given {@link RandomPool} to the given {@link OutputStream}.
     * 
     * @param outputStream The {@link OutputStream} for writing.
     * @param random The {@link RandomPool} to serialize to the given {@link OutputStream}.
     * @throws IOException Something goes wrong while writing to the stream.
     */
    public static void serializeRandom(OutputStream outputStream, RandomPool random) throws IOException {

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(random);
        objectOutputStream.flush();
    }

    /**
     * Deserializes a {@link RandomPool} from the given {@link InputStream}.
     * 
     * @param inputStream The {@link InputStream} for reading.
     * @return The deserialized {@link RandomPool}.
     * @throws IOException Something goes wrong while reading from the stream.
     * @throws ClassNotFoundException A class which is used in the {@link RandomPool} can't be found.
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
