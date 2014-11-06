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

package com.quartercode.disconnected.shared.world.comp.file;

import static com.quartercode.disconnected.shared.world.comp.file.PathUtils.SEPARATOR;

/**
 * This class offers some constants which represent commonly used files.
 * Such files always need to be in the same place.
 */
public class CommonFiles {

    // ----- Mountpoints -----

    /**
     * The mountpoint of the file system which houses all files that are related to the system and shouldn't be changed.
     * There may not be any configuration files here, just binaries.<br>
     * That way, the system can be updated by just replacing all the contents of this file system.
     */
    public static final String SYSTEM_MOUNTPOINT  = "system";

    /**
     * The mountpoint of the file system which houses all config files, custom programs and home directories.
     */
    public static final String USER_MOUNTPOINT    = "user";

    // ----- System Paths -----

    /**
     * The path of the binary directory which stores all programs that come with a system.
     */
    public static final String SYS_BIN_DIR        = SEPARATOR + SYSTEM_MOUNTPOINT + SEPARATOR + "bin";

    // ----- User Paths -----

    /**
     * The path of the binary directory which stores all custom programs that are added by the users.
     */
    public static final String USER_BIN_DIR       = SEPARATOR + USER_MOUNTPOINT + SEPARATOR + "bin";

    /**
     * The path of the config directory which contains all global configuration files for system and user programs.
     */
    public static final String CONFIG_DIR         = SEPARATOR + USER_MOUNTPOINT + SEPARATOR + "etc";

    /**
     * The path of the config file which stores the users of a system.
     */
    public static final String USER_CONFIG        = CONFIG_DIR + SEPARATOR + "users.cfg";

    /**
     * The path of the config file which stores the groups of a system.
     */
    public static final String GROUP_CONFIG       = CONFIG_DIR + SEPARATOR + "groups.cfg";

    /**
     * The config file which stores the default environment variables that are assigned to every new process.
     */
    public static final String ENVIRONMENT_CONFIG = CONFIG_DIR + SEPARATOR + "environment.cfg";

    private CommonFiles() {

    }

}
