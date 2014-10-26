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

package com.quartercode.disconnected.shared.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that stores a global {@link Properties} object, which contains basic application settings, and makes its values available.
 * Note that all settings should be initialized using {@link #initializeSetting(String, String)}.
 * 
 * @see Properties
 */
public class Settings {

    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

    private static Path         file;
    private static Properties   settings;

    /**
     * Sets the location of the file which contains the settings {@link Properties}.
     * Also loads that settings file into memory.
     * Note that this operation must be executed before any other operation.
     * 
     * @param file The location of the settings file.
     */
    public static void setSettingsFile(Path file) {

        if (Settings.file != null) {
            throw new IllegalStateException("Cannot set settings file object twice");
        }

        Settings.file = file;
        settings = new Properties();

        // Load the file if it exists
        if (Files.exists(file)) {
            try (InputStream inputStream = Files.newInputStream(file)) {
                settings.load(inputStream);
            } catch (IOException e) {
                LOGGER.warn("Error while loading settings file", e);
            }
        }
    }

    /**
     * Initializes the provided setting by storing a default value if the setting is not yet set.
     * Also saves the settings file to the file system.
     * 
     * @param name The name of the setting whose default value should be set.
     * @param defaultValue The default value for the given setting.
     */
    public static void initializeSetting(String name, String defaultValue) {

        if (!settings.containsKey(name)) {
            settings.setProperty(name, defaultValue);

            // Save the file
            try (OutputStream outputStream = Files.newOutputStream(Settings.file)) {
                settings.store(outputStream, ApplicationInfo.TITLE + " " + ApplicationInfo.VERSION + " settings file");
            } catch (IOException e) {
                LOGGER.warn("Error while saving settings file", e);
            }
        }
    }

    /**
     * Returns the value which is stored under the given setting name.
     * 
     * @param name The name of the setting whose value should be returned.
     * @return The value of the given setting.
     */
    public static String getSetting(String name) {

        return settings.getProperty(name);
    }

    private Settings() {

    }

}
