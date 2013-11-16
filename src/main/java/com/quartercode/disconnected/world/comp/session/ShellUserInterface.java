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

package com.quartercode.disconnected.world.comp.session;

import com.quartercode.disconnected.world.comp.file.File;

/**
 * The shell user interface is implemented by classes which offer interaction with a shell.
 * 
 * @see Shell
 */
public interface ShellUserInterface {

    /**
     * Returns the shell the interface offers access to.
     * 
     * @return The shell the interface is used for.
     */
    public Shell getShell();

    /**
     * Returns true if the user interface can be serialized using JAXB.
     * 
     * @return True if the user interface can be serialized.
     */
    public boolean isSerializable();

    /**
     * Prints or proccesses the given message.
     * The method gets called if there's a new message avaiable on the shell.
     * The message is not human-readable It's a message containing information like {@code shell.error.command.notFound}.
     * 
     * @param message The message the new line contains (a message like {@code shell.error.command.notFound}).
     */
    public void printMessage(ShellMessage message);

    /**
     * Updates the current directory the interface displays to the given one.
     * 
     * @param currentDirectory The new current directory the interface should update to.
     */
    public void updateCurrentDirectory(File currentDirectory);

    /**
     * Closes the user interface and stops processing the shell.
     */
    public void close();

}
