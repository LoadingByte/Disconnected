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

package com.quartercode.disconnected.server.world.comp.proc.task.def;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import com.quartercode.disconnected.server.world.comp.config.Config;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileException;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.comp.os.config.EnvVariable;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;

/**
 * This utility class provides some utility methods related to {@link TaskContainer}s (e.g. programs).
 *
 * @see TaskContainer
 */
public class TaskContainerUtils {

    /**
     * Returns the {@code PROGRAM_DIRS} environment variable, which stores all the directories that contain important program {@link ContentFile}s.
     * If the computer wants to start a regular and known "installed" program, it looks for the program file inside the returned directory paths.
     * Note that the {@link #getFileFromDirs(FileSystemModule, Collection, String)} method can be used afterwards to really resolve the wanted program file.
     *
     * @param fsModule The file system module which contains the user FS, which in turn contains the environment config file the variable should be read from.
     * @return The paths of the directories which contain all commonly known program files.
     *         If no environment config file can be found or if the environment config doesn't contain the {@code PROGRAM_DIRS} variable, a sensible placeholder is returned.
     */
    public static List<String> getProgramDirs(FileSystemModule fsModule) {

        try {
            @SuppressWarnings ("unchecked")
            Config<EnvVariable> envConfig = ((ContentFile) fsModule.getFile(CommonFiles.ENVIRONMENT_CONFIG)).getContentAs(Config.class);
            EnvVariable progDirsEnvVar = envConfig.getEntryByColumn("name", "PROGRAM_DIRS");
            if (progDirsEnvVar != null) {
                return progDirsEnvVar.getValueList();
            }
            // No PROGRAM_DIRS variable -> code down below recovers
        } catch (FileException e) {
            // No environment config file -> code down below recovers
        }

        // If PROGRAM_DIRS cannot be resolved, the default program directories are used
        return Arrays.asList(CommonFiles.SYSTEM_PROGRAM_DIR, CommonFiles.USER_PROGRAM_DIR);
    }

    /**
     * Tries to resolve a {@link TaskContainer} file (a {@link ContentFile} which contains a task container) with the given name from one of the provided directory paths.
     * That means that the algorithm iterates over all of the directories and uses the first one which contains a task container file with the given name.
     * For example, this method is used to resolve program files from the {@code PROGRAM_DIRS} environment variable (see {@link #getProgramDirs(FileSystemModule)}).
     * Note that this method suppresses all exceptions that might occur.
     *
     * @param fsModule The {@link FileSystemModule} which manages the file systems that should be searched.
     * @param directories A list of paths to the directories that should be searched.
     * @param fileName The name of the task container file that should be found.
     * @return A task container file with the given name in one of the given directories.
     */
    public static ContentFile getTaskContainerFileFromDirs(FileSystemModule fsModule, Collection<String> directories, String fileName) {

        for (String directoryPath : directories) {
            String tcFilePath = PathUtils.resolve(directoryPath, fileName);

            try {
                File<?> tcFile = fsModule.getFile(tcFilePath);

                if (tcFile instanceof ContentFile && ((ContentFile) tcFile).getContent() instanceof TaskContainer) {
                    return (ContentFile) tcFile;
                }
            } catch (IllegalArgumentException | UnknownMountpointException | InvalidPathException e) {
                // Continue
            }
        }

        return null;
    }

    private TaskContainerUtils() {

    }

}
