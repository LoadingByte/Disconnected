
package com.quartercode.disconnected.shared.constant;

import com.quartercode.disconnected.shared.util.SeparatedPath;

/**
 * Constants for system programs (e.g. common locations).
 */
public class SystemProgramConstants {

    /**
     * The location the session program can be commonly found under.
     */
    public static final SeparatedPath COMLOC_SESSION = new SeparatedPath(CommonFiles.SYS_BIN_DIR, "session.exe");

    private SystemProgramConstants() {

    }

}
