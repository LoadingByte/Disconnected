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

import java.io.File;
import java.util.List;

/**
 * This service manages different {@link Profile}s which store simulations and random objects.
 * For loading or saving profiles, you need to use the {@link ProfileSerializer}.
 * 
 * @see Profile
 * @see ProfileSerializer
 */
public interface ProfileService {

    /**
     * Returns The directory the profile service stores its {@link Profile}s in.
     * 
     * @return The directory the service uses as storage.
     */
    public File getDirectory();

    /**
     * Returns a list of all loaded {@link Profile}s.
     * 
     * @return All loaded profiles.
     */
    public List<Profile> getProfiles();

    /**
     * Adds a new {@link Profile} object to the storage.
     * If there's already a profile with the same name (ignoring case), the old one will be deleted.
     * 
     * @param profile The new profile to add.
     */
    public void addProfile(Profile profile);

    /**
     * Removes the given {@link Profile} object from the storage.
     * This actually removes any profile with the same name as the given one (ignoring case).
     * 
     * @param profile The loaded profile to remove.
     */
    public void removeProfile(Profile profile);

    /**
     * Serializes (or "saves") the given {@link Profile} into the correct zip file in the set directory.
     * 
     * @param profile The profile to serialize.
     * @throws ProfileSerializationException Something goes wrong while serializing the profile.
     */
    public void serializeProfile(Profile profile) throws ProfileSerializationException;

    /**
     * Returns the current active {@link Profile} which is simulated at the time.
     * 
     * @return The current active profile.
     */
    public Profile getActive();

    /**
     * Changes the currently active {@link Profile}.
     * By setting a profile active, it will deserialize the data of it.
     * By setting a profile inactive, it will unlink the data of it from the memory.<br>
     * <br>
     * If you want to deactivate the current profile without activating a new one, you can use {@code null}.
     * The change will take place in the next tick.
     * 
     * @param profile The new active profile which will be simulated.
     * @throws ProfileSerializationException Something goes wrong while deserializing the profile data.
     */
    public void setActive(Profile profile) throws ProfileSerializationException;

}
