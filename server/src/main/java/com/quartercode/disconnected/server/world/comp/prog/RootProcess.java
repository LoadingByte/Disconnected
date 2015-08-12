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

package com.quartercode.disconnected.server.world.comp.prog;

import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.MissingFileRightsException;
import com.quartercode.disconnected.server.world.comp.os.config.User;

/**
 * The root process is a simple {@link Process} which can be only used as root for the process tree.
 *
 * @see Process
 */
public class RootProcess extends Process<ProcessModule> {

    // JAXB constructor
    protected RootProcess() {

    }

    /**
     * Creates a new root process and immediately launches it.
     * This constructor should only be used by a {@link ProcessModule}.
     *
     * @param pid The unique process id for the new process. This is probably {@code 0}.
     * @param source The {@link ContentFile} which contains the {@link Program} the new process should run. This is probably the session program.
     * @throws IllegalArgumentException If the given PID is invalid or already used by another process.
     *         Alternatively, if the provided source file does not contain a program object.
     * @throws IllegalStateException If the {@link Program#getName() name} of the program stored in the given source file is unknown and no executor can therefore be retrieved.
     * @throws MissingFileRightsException If the {@link User} the new process runs under hasn't got the read and execute rights on the source file.
     *         If you are not starting a new {@link Session}, the user of the new process will be the same one as the {@link #getUser() user of this process}.
     */
    protected RootProcess(int pid, ContentFile source) throws MissingFileRightsException {

        super(pid, source, null);

        initialize();
    }

    @Override
    public RootProcess getRoot() {

        return this;
    }

}
