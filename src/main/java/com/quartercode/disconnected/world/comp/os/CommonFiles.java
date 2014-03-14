
package com.quartercode.disconnected.world.comp.os;

import com.quartercode.disconnected.world.comp.file.Directory;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystem;

/**
 * This class offers some constants which represent commonly used {@link File}s.
 * Such {@link File}s need to always be in the same place.
 * 
 * @see File
 */
public class CommonFiles {

    /**
     * The {@link FileSystem} which has the system mountpoint houses all files that are directly related to the system (e.g. system configs).
     */
    public static final String SYSTEM_MOUNTPOINT  = "system";

    /**
     * The configuration {@link Directory} which contains all of the program's configurations.
     */
    public static final String CONFIG_DIR         = "/" + SYSTEM_MOUNTPOINT + "/etc";

    /**
     * The {@link Configuration} {@link File} which stores the {@link User}s of a system must always be available under this path.
     */
    public static final String USER_CONFIG        = CONFIG_DIR + "/users.cfg";

    /**
     * The {@link Configuration} {@link File} which stores the {@link Group}s of a system must always be available under this path.
     */
    public static final String GROUP_CONFIG       = CONFIG_DIR + "/groups.cfg";

    /**
     * The {@link Configuration} {@link File} which stores the default {@link EnvironmentVariable}s every new process gets assigned.
     */
    public static final String ENVIRONMENT_CONFIG = CONFIG_DIR + "/environment.cfg";

    private CommonFiles() {

    }

}
