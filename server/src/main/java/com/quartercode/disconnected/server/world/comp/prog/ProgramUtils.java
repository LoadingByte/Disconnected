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

package com.quartercode.disconnected.server.world.comp.prog;

import java.util.List;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FSModule;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;

/**
 * This utility class provides some utility methods for {@link Program}s.
 * 
 * @see Program
 */
public class ProgramUtils {

    /**
     * Tries to resolve a {@link Program} file (a {@link ContentFile} which contains a program) with the given name from one of the provided directory paths.
     * That means that the algorithm iterates over all of the directories and uses the first one which contains a program file with the given name.
     * This method is used to resolve program files from the {@code PATH} environment variable.
     * Note that this method suppresses all exceptions that might occur.
     * 
     * @param fsModule The {@link FSModule file system module} which manages the file systems that should be searched.
     * @param directories A list of paths to the directories that should be searched.
     * @param fileName The name of the program file that should be found.
     * @return A program file with the given name in one of the given directories.
     */
    public static ContentFile getProgramFileFromPaths(FSModule fsModule, List<String> directories, String fileName) {

        for (String directoryPath : directories) {
            String programFilePath = PathUtils.resolve(directoryPath, fileName);

            try {
                File<?> programFile = fsModule.invoke(FSModule.GET_FILE, programFilePath);

                if (programFile instanceof ContentFile && programFile.getObj(ContentFile.CONTENT) instanceof Program) {
                    return (ContentFile) programFile;
                }
            } catch (IllegalArgumentException | UnknownMountpointException e) {
                // Continue
            }
        }

        return null;
    }

    private ProgramUtils() {

    }

}
