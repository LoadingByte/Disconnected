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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.shared.util.ServiceRegistry;

/**
 * This is the default implementation of the {@link ProfileService}.
 * 
 * @see ProfileService
 */
public class DefaultProfileService implements ProfileService {

    private static final Logger LOGGER   = LoggerFactory.getLogger(DefaultProfileService.class);

    private final Path          directory;
    // This list must be concurrent because the watcher thread is accessing it
    private final List<String>  profiles = new CopyOnWriteArrayList<>();

    private Thread              watcherThread;

    /**
     * Creates a new default profile service which stores its profiles in the given directory.
     * If the profile directory doesn't exist yet, it is created.
     * 
     * @param directory The directory the new service will store its profiles in.
     */
    public DefaultProfileService(Path directory) {

        Validate.notNull(directory, "Cannot use null profile directory");

        this.directory = directory;
    }

    @Override
    public Path getDirectory() {

        return directory;
    }

    @Override
    public List<String> getProfiles() {

        return Collections.unmodifiableList(profiles);
    }

    private Path getProfileFile(String profile) {

        return directory.resolve(profile + ".zip");
    }

    @Override
    public void deleteProfile(String profile) {

        Validate.notBlank(profile, "Cannot delete profile with blank name");
        Validate.isTrue(getProfiles().contains(profile), "Cannot delete unknown profile '{}'", profile);

        Path profileFile = getProfileFile(profile);
        LOGGER.debug("Removing profile '{}' from '{}'", profile, profileFile);

        try {
            // This call also updates the "profiles" list because the profile directory is watched
            Files.delete(profileFile);
        } catch (IOException e) {
            LOGGER.error("Cannot delete profile file '{}'", profileFile, e);
        }
    }

    @Override
    public void serializeProfile(String profile, ProfileData data) throws ProfileSerializationException {

        Validate.notBlank(profile, "Cannot serialize profile with blank name");

        Path profileFile = getProfileFile(profile);
        LOGGER.debug("Serializing profile '{}' to '{}'", profile, profileFile);

        // Delete old file
        if (Files.exists(profileFile)) {
            try {
                Files.delete(profileFile);
            } catch (IOException e) {
                throw new ProfileSerializationException("Cannot delete old file '" + profileFile + "' of profile '" + profile + "'", e);
            }
        }

        // Create new file; this call also updates the "profiles" list because the profile directory is watched
        try (OutputStream outputStream = Files.newOutputStream(profileFile)) {
            ServiceRegistry.lookup(ProfileSerializationService.class).serializeProfile(outputStream, data);
        } catch (IOException e) {
            throw new ProfileSerializationException("Cannot open output stream to profile file '" + profileFile + "' for serializing profile '" + profile + "'", e);
        } catch (ProfileSerializationException e) {
            throw new ProfileSerializationException("Error while serializing profile '" + profile + "' to '" + profileFile + "'", e);
        }
    }

    @Override
    public ProfileData deserializeProfile(String profile) throws ProfileSerializationException {

        Validate.notBlank(profile, "Cannot deserialize profile with blank name");
        Validate.isTrue(getProfiles().contains(profile), "Cannot deserialize unknown profile '{}'", profile);

        Path profileFile = getProfileFile(profile);
        LOGGER.debug("Deserializing profile '{}' from '{}'", profile, profileFile);

        // Deserialize the profile
        try (InputStream inputStream = Files.newInputStream(profileFile)) {
            return ServiceRegistry.lookup(ProfileSerializationService.class).deserializeProfile(inputStream);
        } catch (IOException e) {
            throw new ProfileSerializationException("Cannot open input stream to profile file '" + profileFile + "' for deserializing profile '" + profile + "'", e);
        } catch (ProfileSerializationException e) {
            throw new ProfileSerializationException("Error while deserializing profile '" + profile + "' from '" + profileFile + "'", e);
        }
    }

    @Override
    public boolean isWatching() {

        return watcherThread != null && watcherThread.isAlive();
    }

    @Override
    public void setWatching(boolean watching) {

        if (watching && !isWatching()) {
            LOGGER.debug("Starting up profile dir watcher daemon thread");
            watcherThread = new Thread(new DirectoryWatcher(), "profile-dir-watcher");
            // Use a daemon in order to avoid having to stop the thread explicitly
            watcherThread.setDaemon(true);
            watcherThread.start();
        } else if (!watching && isWatching()) {
            LOGGER.debug("Shutting down profile dir watcher daemon thread");
            watcherThread.interrupt();
            watcherThread = null;
        }
    }

    /*
     * A simple thread runnable that watches the profile service's directory for changes and updates the "profiles" list accordingly.
     */
    private class DirectoryWatcher implements Runnable {

        @Override
        public void run() {

            LOGGER.debug("The profile directory is '{}'", directory.toAbsolutePath());

            // Create the profile directory if it doesn't exist yet
            if (!Files.exists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException e) {
                    LOGGER.error("Cannot create profile directory '{}'", directory, e);
                    return;
                }
            }

            // Index all existing profiles
            reindexProfiles();

            // Start the directory watcher
            try (WatchService watcher = directory.getFileSystem().newWatchService()) {
                directory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

                while (true) {
                    WatchKey watckKey = watcher.take();

                    // Process all events
                    for (WatchEvent<?> event : watckKey.pollEvents()) {
                        processEvent(event);
                    }

                    watckKey.reset();
                }
            } catch (IOException e) {
                LOGGER.error("Cannot register watch service for watching profile directory '{}'", directory, e);
            } catch (InterruptedException e) {
                // Should never happen
                LOGGER.error("Profile directory watching thread interrupted while watching directory '{}'", directory, e);
            }
        }

        private void processEvent(WatchEvent<?> event) {

            if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                reindexProfiles();
            } else {
                String profile = getProfile( ((Path) event.context()).toString());

                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    LOGGER.debug("Indexing new profile '{}'", profile);
                    profiles.add(profile);
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    LOGGER.debug("Removing profile '{}'", profile);
                    profiles.remove(profile);
                }
            }
        }

        private void reindexProfiles() {

            LOGGER.debug("Re-indexing all profiles in directory '{}'", directory);

            // Clear the old profile list
            profiles.clear();

            // Iterate over all profile files and index them
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
                for (Path profileFile : directoryStream) {
                    String profile = getProfile(profileFile.getFileName().toString());

                    LOGGER.debug("Re-indexing profile '{}'", profile);
                    profiles.add(profile);
                }
            } catch (IOException e) {
                LOGGER.error("Cannot re-index existing profiles in directory '{}'", directory, e);
            }
        }

        private String getProfile(String fileName) {

            return fileName.substring(0, fileName.lastIndexOf("."));
        }

    }

}
