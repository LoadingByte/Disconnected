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

package com.quartercode.disconnected.shared.program;

import com.quartercode.disconnected.shared.file.CommonFiles;
import com.quartercode.disconnected.shared.file.SeparatedPath;

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
