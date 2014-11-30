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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import javax.xml.bind.JAXBContext;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.world.World;

/**
 * This service implements mechanisms for serializing and deserializing entire {@link Profile}s.
 * The persistent profiles are stored in some kind of (archive) files, which contain the different components of the profiles.
 * 
 * @see Profile
 */
public interface ProfileSerializationService {

    // ----- Profile -----

    /**
     * Serializes the given {@link Profile} to the given {@link OutputStream}.
     * This methods writes an (archive) file to the stream, which contain the different components of the profile.
     * 
     * @param outputStream The output stream to write the result to.
     * @param profile The profile to serialize to the given output stream.
     * @throws ProfileSerializationException Thrown if something goes wrong while serializing the profile.
     */
    public void serializeProfile(OutputStream outputStream, Profile profile) throws ProfileSerializationException;

    /**
     * Deserializes the profile data from the given {@link InputStream} and puts the results into the given {@link Profile} object.
     * This method reads an (archive) file from the stream, which contains the different components of the profile for deserialization.
     * 
     * @param inputStream The input stream to read the data from.
     * @param target The profile where the deserialized profile data should be put into.
     * @throws ProfileSerializationException Thrown if something goes wrong while deserializing the profile data.
     */
    public void deserializeProfile(InputStream inputStream, Profile target) throws ProfileSerializationException;

    // ----- World -----

    /**
     * Creates a new {@link JAXBContext} which can be used for the {@link World} XML model.
     * The new context uses the classes whose retrieval is defined by the {@link ServerRegistries#PERSISTENT_CLASS_SCAN_DIRECTIVES persistent class scan directives}.
     * 
     * @return A JAXB context for the world XML model.
     * @throws ProfileSerializationException Thrown if the world JAXB context cannot be created for some reason.
     */
    public JAXBContext createWorldContext() throws ProfileSerializationException;

    /**
     * Serializes the given {@link World} to the given {@link OutputStream} as textual XML.
     * 
     * @param outputStream The output stream to write the world XML to.
     * @param world The world object to serialize.
     * @throws ProfileSerializationException Thrown if an exception occurs while serializing the world to the given output stream.
     */
    public void serializeWorld(OutputStream outputStream, World world) throws ProfileSerializationException;

    /**
     * Deserializes the {@link World} which is provided as textual XML by the given {@link InputStream}.
     * Also tries to initialize the read world with the defined {@link ServerRegistries#WORLD_INITIALIZER_MAPPINGS world initializer mappings}.
     * 
     * @param inputStream The input stream to read the world XML from.
     * @return The deserialized world object.
     * @throws ProfileSerializationException Thrown if an exception occurs while deserializing the world's XML document provided by the given input stream.
     */
    public World deserializeWorld(InputStream inputStream) throws ProfileSerializationException;

    // ----- Random -----

    /**
     * Serializes the given {@link Random} object to the given {@link OutputStream}.
     * 
     * @param outputStream The output stream to write the random object to.
     * @param random The random object to serialize to the given output stream.
     * @throws ProfileSerializationException Thrown if something goes wrong while writing to the stream.
     */
    public void serializeRandom(OutputStream outputStream, Random random) throws ProfileSerializationException;

    /**
     * Deserializes a {@link Random} object from the given {@link InputStream}.
     * 
     * @param inputStream The input stream to read the random object from.
     * @return The deserialized random object.
     * @throws ProfileSerializationException Thrown if something goes wrong while reading from the stream.
     */
    public Random deserializeRandom(InputStream inputStream) throws ProfileSerializationException;

}
