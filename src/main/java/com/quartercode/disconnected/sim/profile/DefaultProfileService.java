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

package com.quartercode.disconnected.sim.profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.quartercode.disconnected.sim.TickBridgeProvider;
import com.quartercode.disconnected.sim.TickService;
import com.quartercode.disconnected.sim.TickSimulator;
import com.quartercode.disconnected.util.storage.ServiceRegistry;

/**
 * This is the default implementation of the {@link ProfileService}.
 * 
 * @see ProfileService
 */
public class DefaultProfileService implements ProfileService {

    private final File          directory;

    private final List<Profile> profiles = new ArrayList<>();
    private Profile             active;

    /**
     * Creates a new default profile service which stores its profiles in the given directory.
     * The constructor also loads all profile as trunks which are present in the given directory.
     * 
     * @param directory The directory the new service will store its profiles in.
     */
    public DefaultProfileService(File directory) {

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

        for (Profile existingProfile : new ArrayList<>(profiles)) {
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

        try (FileOutputStream outputStream = new FileOutputStream(profileFile)) {
            ProfileSerializer.serializeProfile(outputStream, profile);
        } catch (IOException e) {
            throw new ProfileSerializationException(e, profile);
        }
    }

    @Override
    public Profile getActive() {

        return active;
    }

    @Override
    public void setActive(Profile profile) throws ProfileSerializationException {

        if (active != null) {
            active.getWorld().injectBridge(null);
            active.setWorld(null);
            active.setRandom(null);
        }

        active = profile;

        if (active != null && (active.getWorld() == null || active.getRandom() == null)) {
            File profileFile = new File(directory, active.getName() + ".zip");
            if (profileFile.exists()) {
                try (FileInputStream inputStream = new FileInputStream(profileFile)) {
                    ProfileSerializer.deserializeProfile(inputStream, active);
                } catch (IOException e) {
                    throw new ProfileSerializationException(e, profile);
                }
            }
        }

        TickService tickService = ServiceRegistry.lookup(TickService.class);
        if (tickService != null) {
            active.getWorld().injectBridge(tickService.getAction(TickBridgeProvider.class).getBridge());

            TickSimulator simulator = tickService.getAction(TickSimulator.class);
            if (simulator != null) {
                simulator.setWorld(active == null ? null : active.getWorld());
            }
        }
    }

}
