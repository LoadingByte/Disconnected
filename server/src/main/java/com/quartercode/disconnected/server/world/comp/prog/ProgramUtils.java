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

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FSModule;
import com.quartercode.disconnected.server.world.comp.file.FSModule.KnownFS;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.comp.os.OS;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessPlaceholder;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessState;

/**
 * This utility class provides some utility methods for {@link Program}s and {@link Process}es.
 * 
 * @see Program
 * @see Process
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

    /**
     * Creates a new {@link WorldProcessPlaceholder} that represents the given world {@link Process}.
     * The method assumes that the {@link Process#SOURCE} file has been retrieved from a {@link FileSystem} that is known to the computer.
     * See {@link FSModule#KNOWN_FS} for more information on such known file systems.
     * 
     * @param process The world process that should be represented by the placeholder.
     * @param addChildren Whether the {@link WorldProcessPlaceholder#getChildren() child processes} should be added to the placeholder.
     *        If this argument is {@code true}, a tree with all child processes and their child processes and so on emerges.
     *        Therefore, the object becomes much bigger.
     * @return The new world process placeholder.
     */
    public static WorldProcessPlaceholder createProcessPlaceholder(Process<?> process, boolean addChildren) {

        WorldProcessId id = process.invoke(Process.GET_WORLD_PROCESS_ID);
        WorldProcessState state = process.getObj(Process.STATE);
        String user = process.invoke(Process.GET_USER).getObj(User.NAME);

        WorldProcessPlaceholder[] children = null;

        if (addChildren) {
            List<WorldProcessPlaceholder> childrenList = new ArrayList<>();

            for (Process<?> child : process.getColl(Process.CHILDREN)) {
                childrenList.add(createProcessPlaceholder(child, true));
            }

            children = childrenList.toArray(new WorldProcessPlaceholder[childrenList.size()]);
        }

        String sourcePath = "<unknown>";
        ContentFile sourceFile = process.getObj(Process.SOURCE);
        FileSystem sourceFileFs = sourceFile.invoke(File.GET_FS);
        // Check to avoid exceptions if the source file has been removed from its file system
        if (sourceFileFs != null) {
            FSModule fsModule = process.invoke(Process.GET_OS).getObj(OS.FS_MODULE);
            String localSourcePath = sourceFile.invoke(File.GET_PATH);
            String mountpoint = fsModule.invoke(FSModule.GET_KNOWN_BY_FS, sourceFileFs).getObj(KnownFS.MOUNTPOINT);
            sourcePath = PathUtils.resolve(PathUtils.SEPARATOR + mountpoint, localSourcePath);
        }

        String programName = getProgramName(process.getObj(Process.EXECUTOR).getClass());

        return new WorldProcessPlaceholder(id, state, user, children, sourcePath, programName);
    }

    private static String getProgramName(Class<?> programExecutor) {

        for (WorldProgram worldProgram : Registries.get(ServerRegistries.WORLD_PROGRAMS)) {
            if (worldProgram.getType() == programExecutor) {
                return worldProgram.getName();
            }
        }

        return null;
    }

    private ProgramUtils() {

    }

}
