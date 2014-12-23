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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.sim.TickBridgeProvider;
import com.quartercode.disconnected.server.sim.TickSchedulerUpdater;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.shared.util.ServiceRegistry;

/**
 * This is the default implementation of the {@link ProfileService}.
 * 
 * @see ProfileService
 */
public class DefaultProfileService implements ProfileService {

    private static final Logger LOGGER   = LoggerFactory.getLogger(DefaultProfileService.class);

    private final Path          directory;

    private final List<Profile> profiles = new ArrayList<>();
    private Profile             active;

    /**
     * Creates a new default profile service which stores its profiles in the given directory.
     * The constructor also loads all profile as trunks which are present in the given directory.
     * 
     * @param directory The directory the new service will store its profiles in.
     */
    public DefaultProfileService(Path directory) {

        this.directory = directory;

        if (Files.exists(directory)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
                for (Path profileFile : directoryStream) {
                    LOGGER.debug("Indexing profile under '{}'", profileFile);

                    String profileFileName = profileFile.getFileName().toString();
                    profiles.add(new Profile(profileFileName.substring(0, profileFileName.lastIndexOf("."))));
                }
            } catch (IOException e) {
                LOGGER.error("Cannot index existing profiles in directory '{}'", directory, e);
            }
        } else {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                LOGGER.error("Cannot create profile directory '{}'", directory, e);
            }
        }
    }

    @Override
    public Path getDirectory() {

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
                Path profileFile = directory.resolve(profile.getName() + ".zip");
                LOGGER.debug("Removing profile '{}' from '{}'", profile.getName(), profileFile);

                profiles.remove(profile);

                try {
                    Files.delete(profileFile);
                } catch (IOException e) {
                    LOGGER.error("Cannot delete profile file '{}'", profileFile, e);
                }

                break;
            }
        }
    }

    @Override
    public void serializeProfile(Profile profile) throws ProfileSerializationException {

        Path profileFile = directory.resolve(profile.getName() + ".zip");
        LOGGER.debug("Serializing (saving) profile '{}' to '{}'", profile.getName(), profileFile);

        if (Files.exists(profileFile)) {
            try {
                Files.delete(profileFile);
            } catch (IOException e) {
                throw new ProfileSerializationException("Cannot delete old file '" + profileFile + "' of profile '" + profile.getName() + "'", e);
            }
        }

        try (OutputStream outputStream = Files.newOutputStream(profileFile)) {
            ServiceRegistry.lookup(ProfileSerializationService.class).serializeProfile(outputStream, profile);
        } catch (IOException e) {
            throw new ProfileSerializationException("Cannot open output stream to profile file '" + profileFile + "' for serializing profile '" + profile.getName() + "'", e);
        }
    }

    @Override
    public Profile getActive() {

        return active;
    }

    @Override
    public void setActive(Profile profile) throws ProfileSerializationException {

        if (active != null) {
            active.getWorld().setBridge(null);
            active.setWorld(null);
            active.setRandom(null);
        }

        active = profile;

        if (active != null && (active.getWorld() == null || active.getRandom() == null)) {
            Path profileFile = directory.resolve(active.getName() + ".zip");
            LOGGER.debug("Loading profile '{}' from '{}'", profile.getName(), profileFile);

            if (Files.exists(profileFile)) {
                try (InputStream inputStream = Files.newInputStream(profileFile)) {
                    ServiceRegistry.lookup(ProfileSerializationService.class).deserializeProfile(inputStream, active);
                } catch (IOException e) {
                    throw new ProfileSerializationException("Cannot open input stream to profile file '" + profileFile + "' for deserializing profile '" + profile.getName() + "'", e);
                }
            }
        }

        TickService tickService = ServiceRegistry.lookup(TickService.class);

        if (tickService == null) {
            LOGGER.error("No tick service found for injecting profile '{}'", profile.getName());
        } else {
            LOGGER.debug("Injecting profile '{}' into tick service", profile.getName());

            active.getWorld().setBridge(tickService.getAction(TickBridgeProvider.class).getBridge());

            TickSchedulerUpdater schedulerUpdater = tickService.getAction(TickSchedulerUpdater.class);
            if (schedulerUpdater != null) {
                schedulerUpdater.setWorld(active == null ? null : active.getWorld());
            }
        }
    }

}
