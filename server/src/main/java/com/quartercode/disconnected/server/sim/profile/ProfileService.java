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

import java.nio.file.Path;
import java.util.List;
import com.quartercode.disconnected.shared.util.ServiceRegistry;

/**
 * This service manages different profiles which store simulations and random objects.
 * It is also capable of loading and saving those profiles in the form of {@link ProfileData object}.
 * 
 * @see ProfileData
 * @see ProfileSerializationService
 */
public interface ProfileService {

    /**
     * Returns the directory the profile service stores its profiles in.
     * 
     * @return The directory used as storage.
     */
    public Path getDirectory();

    /**
     * Returns the names of all available profiles that are located in the {@link #getDirectory() set profile directory}.
     * If something changes in that directory, the list returned by this method changes as well.
     * In order to add new profiles programmatically, {@link #serializeProfile(String, ProfileData)} must be called.
     * 
     * @return All available profiles.
     */
    public List<String> getProfiles();

    /**
     * Deletes the available profile with the given name and the associated profile file.
     * Note that the given profile name must be present in the {@link #getProfiles() profile list}.
     * 
     * @param profile The name of the indexed profile to delete.
     */
    public void deleteProfile(String profile);

    /**
     * Serializes (or "saves") the profile with the given name and {@link ProfileData} into the correct ZIP file in the {@link #getDirectory() set profile directory}.
     * If the profile with the given name doesn't exist yet in the {@link #getProfiles() profile list}, it is added to the list.
     * 
     * @param profile The name of the profile to serialize.
     * @param data A {@link ProfileData} object that contains the persistent data of the profile.
     *        It can be created using a simple constructor.
     * @throws ProfileSerializationException Something goes wrong while serializing the profile.
     * @see ProfileSerializationService
     */
    public void serializeProfile(String profile, ProfileData data) throws ProfileSerializationException;

    /**
     * Deserializes (or "loads") the {@link ProfileData} of the profile with the given name from the correct ZIP file in the {@link #getDirectory() set profile directory}.
     * 
     * @param profile The name of the profile to serialize.
     * @return A {@link ProfileData} object that contains the persistent data of the loaded profile.
     * @throws ProfileSerializationException Something goes wrong while deserializing the profile.
     * @see ProfileSerializationService
     */
    public ProfileData deserializeProfile(String profile) throws ProfileSerializationException;

    /**
     * Returns whether the service is currently watching the {@link #getDirectory() set profile directory} and updating the {@link #getProfiles() profile list} accordingly.
     * This should always return {@code true} for the profile service that is retrievable through the {@link ServiceRegistry}.
     * 
     * @return Whether the profile directory is watched for changes.
     */
    public boolean isWatching();

    /**
     * Sets whether the service should be watching the {@link #getDirectory() set profile directory} and updating the {@link #getProfiles() profile list} accordingly.
     * 
     * @param watching Whether the profile directory should be watched for changes.
     */
    public void setWatching(boolean watching);

}
