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

package com.quartercode.disconnected.sim;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import com.quartercode.disconnected.util.GlobalStorage;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.world.World;

/**
 * This utility class loads a saves stored {@link Profile}s for serializing {@link Simulation}s.
 * 
 * @see Profile
 * @see ProfileManager
 * @see Simulation
 */
public class ProfileSerializer {

    /**
     * Serializes the given {@link Profile} to the given {@link OutputStream}.
     * This writes a zip to the stream which contains the data of the profile.
     * 
     * @param outputStream The output stream to write the result to.
     * @param profile The profile to serialize to the given output stream.
     * @throws ProfileSerializationException Something goes wrong while serializing the profile.
     */
    public static void serializeProfile(OutputStream outputStream, Profile profile) throws ProfileSerializationException {

        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        try {
            zipOutputStream.putNextEntry(new ZipEntry("world.xml"));
            serializeWorld(zipOutputStream, profile.getWorld());
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry("random.ser"));
            serializeRandom(zipOutputStream, profile.getRandom());
            zipOutputStream.closeEntry();
        } catch (Exception e) {
            throw new ProfileSerializationException(e, profile);
        } finally {
            try {
                zipOutputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Deserializes the data of a {@link Profile} (the {@link World} and the {@link RandomPool}) from the given {@link InputStream}.
     * This reads a zip from the stream which contains the data of the given profile.
     * 
     * @param inputStream The input stream to read the data from.
     * @param target The profile where the deserialized data should be put into.
     * @throws ProfileSerializationException Something goes wrong while deserializing the profile data.
     */
    public static void deserializeProfile(InputStream inputStream, Profile target) throws ProfileSerializationException {

        World world = null;
        RandomPool random = null;

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        try {
            ZipEntry zipEntry = null;
            while ( (zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals("world.xml")) {
                    world = deserializeWorld(zipInputStream);
                } else if (zipEntry.getName().equals("random.ser")) {
                    random = deserializeRandom(zipInputStream);
                }
            }
        } catch (Exception e) {
            throw new ProfileSerializationException(e, target);
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        if (world == null || random == null) {
            throw new ProfileSerializationException(new IllegalStateException("No valid world or random pool object found"), target);
        }

        target.setWorld(world);
        target.setRandom(random);
    }

    /**
     * Creates a new {@link JAXBContext} which can be used for {@link World} xml model.
     * 
     * @return A {@link JAXBContext} for the {@link World} model.
     * @throws JAXBException The world jaxb context cannot be created.
     */
    public static JAXBContext createWorldContext() throws JAXBException {

        StringBuilder contextPathStringBuilder = new StringBuilder();
        for (String contextPathEntry : GlobalStorage.get("contextPath", String.class)) {
            contextPathStringBuilder.append(":").append(contextPathEntry);
        }
        String contextPathString = contextPathStringBuilder.length() > 0 ? contextPathStringBuilder.substring(1) : "";

        return JAXBContext.newInstance(contextPathString);
    }

    /**
     * Writes a {@link World} to an {@link OutputStream} as xml.
     * 
     * @param outputStream The output stream to write the world xml to.
     * @param world The world to serialize.
     * @throws JAXBException An exception occurred while serializing the world.
     */
    public static void serializeWorld(OutputStream outputStream, World world) throws JAXBException {

        Marshaller marshaller = createWorldContext().createMarshaller();
        // We do not want formatted output
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        marshaller.marshal(world, outputStream);
    }

    /**
     * Reads a {@link World} which is saved as xml from an {@link InputStream}.
     * 
     * @param inputStream The input stream to read the xml from.
     * @return The deserialized world object.
     * @throws JAXBException An exception occurred while deserializing the world's xml document.
     */
    public static World deserializeWorld(InputStream inputStream) throws JAXBException {

        return (World) createWorldContext().createUnmarshaller().unmarshal(new JAXBNoCloseInputStream(inputStream));
    }

    /**
     * Serializes the given {@link RandomPool} to the given {@link OutputStream}.
     * 
     * @param outputStream The output stream to write the random pool to.
     * @param random The random pool to serialize to the given output stream.
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
     * @param inputStream The input stream to read the random pool from.
     * @return The deserialized random pool.
     * @throws IOException Something goes wrong while reading from the stream.
     * @throws ClassNotFoundException A class which is used by the random pool can't be found.
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
