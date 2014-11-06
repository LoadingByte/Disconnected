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

package com.quartercode.disconnected.shared.world.comp.program.names;

/**
 * A list of all general program names (e.g. {@code "fileManager"}).
 * General programs are the core utilities that should be available on every computer.
 * Such a utility could be a file manager or a process viewer.
 */
public class GeneralProgs {

    /**
     * The file manager program which can be used to browse and manipulate the (virtual) file system.
     * For example, it is able to create or remove files.
     */
    public static final String FILE_MANAGER = "fileManager";

    private GeneralProgs() {

    }

}
