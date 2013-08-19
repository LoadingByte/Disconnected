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

package com.quartercode.disconnected;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import com.quartercode.disconnected.profile.ProfileManager;
import com.quartercode.disconnected.util.LogExceptionHandler;

/**
 * The main class which initalizes the whole game.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * The main method which initalizes the whole game.
     * 
     * @param args The command line arguments.
     */
    public static void main(String[] args) {

        // Logging configuration
        try {
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/config/logging.properties"));
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Can't load logging configuration", e);
            return;
        }

        // Default exception handler if the vm throws an exception to the entry point of thread (e.g. main() or run())
        Thread.setDefaultUncaughtExceptionHandler(new LogExceptionHandler());

        // Print information about the software
        LOGGER.info("Version " + Disconnected.getVersion());

        // Initalize profile manager and load stored profiles (TODO: Add code for loading).
        Disconnected.setProfileManager(new ProfileManager());
    }

    private Main() {

    }

}
