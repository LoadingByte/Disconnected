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

package com.quartercode.disconnected.server.sim.profile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.world.World;

/**
 * This service implements mechanisms for serializing and deserializing {@link ProfileData profile data}.
 * That persistent data is stored in some kind of (archive) files, which contain the different profile data components.
 * 
 * @see ProfileData
 */
public interface ProfileSerializationService {

    // ----- Profile -----

    /**
     * Serializes the given persistent {@link ProfileData profile data} (e.g. the profile's world) to the given {@link OutputStream}.
     * This methods writes an (archive) file, which contain the different profile data components, to the stream.
     * 
     * @param outputStream The output stream to write the profile data to.
     * @param data The profile data to serialize to the given output stream.
     * @throws ProfileSerializationException Thrown if something goes wrong while serializing the profile data.
     */
    public void serializeProfile(OutputStream outputStream, ProfileData data) throws ProfileSerializationException;

    /**
     * Deserializes the {@link ProfileData profile data} from the given {@link InputStream} and returns the result.
     * This method reads an (archive) file, which contains the different profile data components for deserialization, from the stream.
     * Note that the deserialized {@link World} is automatically initialized.
     * 
     * @param inputStream The input stream to read the profile data from.
     * @return The deserialized profile data as a {@link ProfileData} object.
     * @throws ProfileSerializationException Thrown if something goes wrong while deserializing the profile data.
     */
    public ProfileData deserializeProfile(InputStream inputStream) throws ProfileSerializationException;

    // ----- World -----

    /**
     * Serializes the given {@link World} to the given {@link OutputStream} without closing the stream.
     * 
     * @param outputStream The output stream to write the world to.
     * @param world The world object to serialize.
     * @throws ProfileSerializationException Thrown if an exception occurs while serializing the world to the given output stream.
     */
    public void serializeWorld(OutputStream outputStream, World world) throws ProfileSerializationException;

    /**
     * Deserializes the {@link World}, which is provided by the given {@link InputStream}, without closing the stream.
     * The method is also able to initialize the read world with the defined {@link ServerRegistries#WORLD_INITIALIZER_MAPPINGS world initializer mappings}.
     * That is required for avoiding some endless cycles when using {@code hashCode()} or {@code equals()}.
     * 
     * @param inputStream The input stream to read the world from.
     * @param initialize Whether the deserialized world should be initialized.
     * @return The deserialized world object.
     * @throws ProfileSerializationException Thrown if an exception occurs while deserializing the world from the given input stream.
     */
    public World deserializeWorld(InputStream inputStream, boolean initialize) throws ProfileSerializationException;

    // ----- Random -----

    /**
     * Serializes the given {@link Random} object to the given {@link OutputStream} without closing the stream.
     * 
     * @param outputStream The output stream to write the random object to.
     * @param random The random object to serialize to the given output stream.
     * @throws ProfileSerializationException Thrown if something goes wrong while writing to the stream.
     */
    public void serializeRandom(OutputStream outputStream, Random random) throws ProfileSerializationException;

    /**
     * Deserializes a {@link Random} object from the given {@link InputStream} without closing the stream.
     * 
     * @param inputStream The input stream to read the random object from.
     * @return The deserialized random object.
     * @throws ProfileSerializationException Thrown if something goes wrong while reading from the stream.
     */
    public Random deserializeRandom(InputStream inputStream) throws ProfileSerializationException;

}
