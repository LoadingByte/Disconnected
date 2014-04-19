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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;

/**
 * This class manages different progiles which store simulations and random objects.
 * For loading or saving profiles, you need to use the profile serializer.
 * 
 * @see Profile
 * @see ProfileSerializer
 */
public class ProfileManager {

    /**
     * The singleton instance of profile manager.
     */
    public static final ProfileManager INSTANCE = new ProfileManager(new File("profiles"));

    private final File                 directory;

    private final List<Profile>        profiles = new ArrayList<Profile>();
    private Profile                    active;

    private ProfileManager(File directory) {

        this.directory = directory;

        if (directory.exists()) {
            for (File profileFile : directory.listFiles()) {
                String profileName = profileFile.getName().substring(0, profileFile.getName().lastIndexOf("."));
                profiles.add(new Profile(profileName));
            }
        } else {
            directory.mkdirs();
        }
    }

    /**
     * Returns The directory the profile manager stores its profiles in.
     * 
     * @return The directory the manager uses for its profiles.
     */
    public File getDirectory() {

        return directory;
    }

    /**
     * Returns a list of all loaded profiles.
     * 
     * @return All loaded profiles.
     */
    public List<Profile> getProfiles() {

        return Collections.unmodifiableList(profiles);
    }

    /**
     * Adds a new profile object to the storage.
     * If there's already a profile with the same name (ignoring case), the old one will be deleted.
     * 
     * @param profile The new profile to add.
     */
    public void addProfile(Profile profile) {

        removeProfile(profile);
        profiles.add(profile);
    }

    /**
     * Removes the given profile object from the storage.
     * This actually removes any profile with the same name as the given one (ignoring case).
     * 
     * @param profile The loaded profile to remove.
     */
    public void removeProfile(Profile profile) {

        for (Profile existingProfile : new ArrayList<Profile>(profiles)) {
            if (existingProfile.getName().equalsIgnoreCase(profile.getName())) {
                profiles.remove(profile);
                new File(directory, profile.getName() + ".zip").delete();
            }
        }
    }

    /**
     * Serializes (or "saves") the given profile into the correct zip file in the set directory.
     * 
     * @param profile The profile to serialize.
     * @throws IOException Something goes wrong while writing the zip file.
     * @throws JAXBException Something goes wrong while marshaling the simulation's xml.
     */
    public void serializeProfile(Profile profile) throws IOException, JAXBException {

        File profileFile = new File(directory, profile.getName() + ".zip");
        if (profileFile.exists()) {
            profileFile.delete();
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(profileFile);
            ProfileSerializer.serializeProfile(outputStream, profile);
        } finally {
            outputStream.close();
        }
    }

    /**
     * Returns the currently active {@link Profile} which is simulated at the time.
     * 
     * @return The currently active profile.
     */
    public Profile getActive() {

        return active;
    }

    /**
     * Changes the currently active {@link Profile}.
     * By setting a profile active, it will deserialize the data of it.
     * By setting a profile inactive, it will unlink the data of it from the memory.
     * If you want to deactivate the current profile without activating a new one, you can use null.
     * The change will take place in the next tick.
     * 
     * @param profile The new active profile which will be simulated.
     * @throws IOException Something goes wrong while reading from the profile zip.
     * @throws JAXBException An exception occurres while deserializing the simulation's xml document.
     * @throws ClassNotFoundException A class which is used for the random pool can't be found.
     * @throws IllegalStateException Either the stored world or the stored random pool is invalid or does not exist.
     */
    public void setActive(Profile profile) throws IOException, JAXBException, ClassNotFoundException, IllegalStateException {

        if (active != null) {
            active.setWorld(null);
            active.setRandom(null);
        }

        active = profile;

        if (active != null && (active.getWorld() == null || active.getRandom() == null)) {
            File profileFile = new File(directory, active.getName() + ".zip");
            if (profileFile.exists()) {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(profileFile);
                    ProfileSerializer.deserializeProfile(inputStream, active);
                } finally {
                    inputStream.close();
                }
            }
        }

        TickSimulator simulator = Ticker.INSTANCE.getAction(TickSimulator.class);
        if (simulator != null) {
            simulator.setWorld(active == null ? null : active.getWorld());
        }
    }

}
