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

package com.quartercode.disconnected.server.world.comp.proc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.world.comp.config.Config;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileException;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.comp.os.config.EnvVariable;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskContainer;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessId;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessPlaceholder;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessState;

/**
 * This utility class provides some utility methods related to generic {@link Process}es.
 *
 * @see Process
 */
public class ProcessUtils {

    /**
     * Creates a new {@link WorldProcessPlaceholder} that represents the given world {@link Process}.
     * The method assumes that the {@link Process#SOURCE} file has been retrieved from a {@link FileSystem} that is known to the computer.
     * See {@link FileSystemModule#KNOWN_FS} for more information on such known file systems.
     *
     * @param process The world process that should be represented by the placeholder.
     * @param addChildren Whether the {@link WorldProcessPlaceholder#getChildren() child processes} should be added to the placeholder.
     *        If this argument is {@code true}, a tree with all child processes and their child processes and so on emerges.
     *        Therefore, the object becomes much bigger.
     * @return The new world process placeholder.
     */
    public static WorldProcessPlaceholder createProcessPlaceholder(Process process, boolean addChildren) {

        WorldProcessId id = process.getWorldProcessId();
        WorldProcessState state = process.getState();
        String user = process.getUser().getName();

        WorldProcessPlaceholder[] children = null;

        if (addChildren) {
            List<WorldProcessPlaceholder> childrenList = new ArrayList<>();

            for (Process child : process.getChildProcesses()) {
                childrenList.add(createProcessPlaceholder(child, true));
            }

            children = childrenList.toArray(new WorldProcessPlaceholder[childrenList.size()]);
        }

        String sourcePath = "<unknown>";
        ContentFile sourceFile = process.getSource();
        FileSystem sourceFileFs = sourceFile.getFileSystem();
        // Check to avoid exceptions if the source file has been removed from its file system
        if (sourceFileFs != null) {
            FileSystemModule fsModule = process.getOs().getFsModule();
            String localSourcePath = sourceFile.getPath();
            String mountpoint = fsModule.getKnownFsByFs(sourceFileFs).getMountpoint();
            sourcePath = PathUtils.resolve(PathUtils.SEPARATOR + mountpoint, localSourcePath);
        }

        String programName = getProgramName(process.getExecutor().getClass());

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

    private ProcessUtils() {

    }

}
