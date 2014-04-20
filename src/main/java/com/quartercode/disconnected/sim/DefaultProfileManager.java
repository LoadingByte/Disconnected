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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.quartercode.disconnected.util.ServiceRegistry;

/**
 * This is the default implementation of the {@link ProfileManager} service.
 * 
 * @see ProfileManager
 */
public class DefaultProfileManager implements ProfileManager {

    private final File          directory;

    private final List<Profile> profiles = new ArrayList<Profile>();
    private Profile             active;

    /**
     * Creates a new default profile manager which stores its profiles in the given directory.
     * The constructor also loads all profile as trunks which are present in the given directory.
     * 
     * @param directory The directory the new manager will store its profiles in.
     */
    public DefaultProfileManager(File directory) {

        this.directory = directory;

        if (directory.exists()) {
            for (File profileFile : directory.listFiles()) {
                profiles.add(new Profile(profileFile.getName().substring(0, profileFile.getName().lastIndexOf("."))));
            }
        } else {
            directory.mkdirs();
        }
    }

    @Override
    public File getDirectory() {

        return directory;
    }

    @Override
    public List<Profile> getProfiles() {

        return Collections.unmodifiableList(profiles);
    }

    @Override
    public void addProfile(Profile profile) {

        removeProfile(profile);
        profiles.add(profile);
    }

    @Override
    public void removeProfile(Profile profile) {

        for (Profile existingProfile : new ArrayList<Profile>(profiles)) {
            if (existingProfile.getName().equalsIgnoreCase(profile.getName())) {
                profiles.remove(profile);
                new File(directory, profile.getName() + ".zip").delete();
            }
        }
    }

    @Override
    public void serializeProfile(Profile profile) throws ProfileSerializationException {

        File profileFile = new File(directory, profile.getName() + ".zip");
        if (profileFile.exists()) {
            profileFile.delete();
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(profileFile);
            ProfileSerializer.serializeProfile(outputStream, profile);
        } catch (FileNotFoundException e) {
            throw new ProfileSerializationException(e, profile);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public Profile getActive() {

        return active;
    }

    @Override
    public void setActive(Profile profile) throws ProfileSerializationException {

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
                } catch (FileNotFoundException e) {
                    throw new ProfileSerializationException(e, profile);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }
            }
        }

        Ticker ticker = ServiceRegistry.lookup(Ticker.class);
        if (ticker != null) {
            TickSimulator simulator = ticker.getAction(TickSimulator.class);
            if (simulator != null) {
                simulator.setWorld(active == null ? null : active.getWorld());
            }
        }
    }

}
