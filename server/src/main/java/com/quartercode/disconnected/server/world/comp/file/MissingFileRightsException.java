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

package com.quartercode.disconnected.server.world.comp.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

/**
 * This exception occurs if someone (e.g. a process) attempts to execute an action on a file (like read, execute, remove), but he hasn't got the rights to do so.
 *
 * @see FileRights
 */
public class MissingFileRightsException extends FileException {

    private static final long     serialVersionUID = -4363670639073352728L;

    private final File<?>         file;
    private final User            user;
    private final List<Character> missingRights;

    /**
     * Creates a new missing file rights exception.
     *
     * @param file The {@link File} the given user tried to access without having the required rights for doing that.
     * @param user The {@link User} The user who tried to access the file.
     * @param missingRights All file right characters (e.g. {@code 'r'} or {@code 'w'}) the user is missing on the given file in order to execute his actions.
     * @param action A short string that describes what the missing rights are needed for. It is used in the exception message.
     *        The context is "Cannot &lt;action&gt; '&lt;file path&gt;' because the following rights are missing: ..."
     *        For example, this string could be {@code remove file}.
     */
    public MissingFileRightsException(File<?> file, User user, List<Character> missingRights, String action) {

        super("Cannot " + action + " '" + file.getPath() + "' because the following rights are missing: " + missingRights);

        this.file = file;
        this.user = user;
        this.missingRights = Collections.unmodifiableList(new ArrayList<>(missingRights));
    }

    /**
     * Returns the {@link File} the set {@link #getUser() user} tried to access without having the {@link #getMissingRights() required rights} for doing that.
     *
     * @return The accessed file.
     */
    public File<?> getFile() {

        return file;
    }

    /**
     * Returns the {@link User} The user who tried to access the file but hasn't got enough rights to do so.
     *
     * @return The accessing user.
     */
    public User getUser() {

        return user;
    }

    /**
     * Returns all file right characters (e.g. {@code 'r'} or {@code 'w'}) the {@link #getUser() user} is missing on the set {@link #getFile() file} in order to execute his actions.
     *
     * @return The rights the user doesn't have on the set file.
     */
    public List<Character> getMissingRights() {

        return missingRights;
    }

}
