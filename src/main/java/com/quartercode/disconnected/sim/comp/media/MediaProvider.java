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

package com.quartercode.disconnected.sim.comp.media;

import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.media.File.FileType;

public interface MediaProvider {

    /**
     * Returns the actual media the implementing class is providing.
     * 
     * @return The actual media the implementing class is providing.
     */
    public Media resolveMedia();

    /**
     * Returns the computer this media is hosted on.
     * This will only return the sotring computer, not any accessing computers.
     * 
     * @return The computer this media is hosted to.
     */
    public Computer getHost();

    /**
     * Returns the size of the media, given in bytes.
     * 
     * @return The size of the media, given in bytes.
     */
    public long getSize();

    /**
     * Returns the letter the computer uses to recognize the hard drive.
     * 
     * @return The letter the computer uses to recognize the hard drive.
     */
    public char getLetter();

    /**
     * Setts the letter the computer uses to recognize the hard drive to a new one.
     * 
     * @param letter The new letter the computer uses to recognize the hard drive.
     */
    public void setLetter(char letter);

    /**
     * Returns the root file which every other file path branches of.
     * 
     * @return The root file which every other file path branches of.
     */
    public File getRootFile();

    /**
     * Returns the file which is stored on the media under the given path.
     * A path is a collection of files seperated by a seperator.
     * This will look up the file using a local media path.
     * 
     * @param path The path to look in for the file.
     * @return The file which is stored on the media under the given path.
     */
    public File getFile(String path);

    /**
     * Creates a new file using the given path and type on this media and returns it.
     * If the file already exists, the existing file will be returned.
     * A path is a collection of files seperated by a seperator.
     * This will get the file location using a local media path.
     * 
     * @param path The path the new file will be located under.
     * @param type The file type the new file should has.
     * @return The new file (or the existing one, if the file already exists).
     */
    public File addFile(String path, FileType type);

    /**
     * Returns the total amount of bytes which are occupied by files.
     * 
     * @return The total amount of bytes which are occupied by files.
     */
    public long getFilled();

    /**
     * Returns the total amount of bytes which are not occupied by any files.
     * 
     * @return The total amount of bytes which are not occupied by any files.
     */
    public long getFree();

}