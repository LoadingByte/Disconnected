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

package com.quartercode.disconnected.server.sim.profile;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.commons.lang3.StringUtils;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.util.TreeInitializer;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.MappedValueRegistry.Mapping;

/**
 * This utility class loads a saves {@link Profile}s.
 * 
 * @see Profile
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

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry("world.xml"));
            serializeWorld(zipOutputStream, profile.getWorld());
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry("random.ser"));
            serializeRandom(zipOutputStream, profile.getRandom());
            zipOutputStream.closeEntry();
        } catch (Exception e) {
            throw new ProfileSerializationException(e, profile);
        }
    }

    /**
     * Deserializes the data of a {@link Profile} (the {@link World} and the {@link Random} object) from the given {@link InputStream}.
     * This reads a zip from the stream which contains the data of the given profile.
     * 
     * @param inputStream The input stream to read the data from.
     * @param target The profile where the deserialized data should be put into.
     * @throws ProfileSerializationException Something goes wrong while deserializing the profile data.
     */
    public static void deserializeProfile(InputStream inputStream, Profile target) throws ProfileSerializationException {

        World world = null;
        Random random = null;

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
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
        }

        if (world == null || random == null) {
            throw new ProfileSerializationException(new IllegalStateException("No valid world or random object found"), target);
        }

        target.setWorld(world);
        target.setRandom(random);
    }

    /**
     * Creates a new {@link JAXBContext} which can be used for {@link World} xml model.
     * The new context uses the context path which is stored in {@link ServerRegistries#WORLD_CONTEXT_PATH}.
     * 
     * @return A jaxb context for the world model.
     * @throws JAXBException The world jaxb context cannot be created.
     */
    public static JAXBContext createWorldContext() throws JAXBException {

        List<String> contextPathEntries = Registries.get(ServerRegistries.WORLD_CONTEXT_PATH).getValues();
        return JAXBContext.newInstance(StringUtils.join(contextPathEntries, ":"));
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
        // Turn this option on for debugging:
        // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(world, outputStream);
    }

    /**
     * Reads a {@link World} which is saved as xml from an {@link InputStream}.
     * Also tries to initialize the read world if a {@link TreeInitializer} is available through the global storage (key is {@code worldInitializer}).
     * 
     * @param inputStream The input stream to read the xml from.
     * @return The deserialized world object.
     * @throws JAXBException An exception occurred while deserializing the world's xml document.
     */
    public static World deserializeWorld(InputStream inputStream) throws JAXBException {

        World world = (World) createWorldContext().createUnmarshaller().unmarshal(new JAXBNoCloseInputStream(inputStream));

        TreeInitializer worldInitializer = new TreeInitializer();
        for (Mapping<Class<? extends FeatureHolder>, FeatureDefinition<?>> mapping : Registries.get(ServerRegistries.WORLD_INITIALIZER_MAPPINGS).getValues()) {
            worldInitializer.addInitializationDefinition(mapping.getLeft(), mapping.getRight());
        }
        worldInitializer.apply(world);

        return world;
    }

    /**
     * Serializes the given {@link Random} object to the given {@link OutputStream}.
     * 
     * @param outputStream The output stream to write the random object to.
     * @param random The random object to serialize to the given output stream.
     * @throws IOException Something goes wrong while writing to the stream.
     */
    public static void serializeRandom(OutputStream outputStream, Random random) throws IOException {

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(random);
        objectOutputStream.flush();
    }

    /**
     * Deserializes a {@link Random} object from the given {@link InputStream}.
     * 
     * @param inputStream The input stream to read the random object from.
     * @return The deserialized random object.
     * @throws IOException Something goes wrong while reading from the stream.
     * @throws ClassNotFoundException A class which is used by the random object can't be found.
     */
    public static Random deserializeRandom(InputStream inputStream) throws IOException, ClassNotFoundException {

        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        return (Random) objectInputStream.readObject();
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
