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

package com.quartercode.disconnected.shared.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The temp file manager provides a facility for creating and storing temporary files that are <b>not deleted while the application is running</b>.
 * It should be preferred over the standard temporary files provided by the jdk.
 * The temporary directory for the current process can be retrieved with {@link #getTempDir()}.<br>
 * <br>
 * Internally, this class provides a temporary directory whose name is the pid of the current process.
 * When the manager is initialized, it looks for other old temporary directories whose pid is no longer assigned to a java process.
 * Such directories were used by old processes and can therefore be deleted.
 * That way, old temporary directories are purged reliably.
 */
public class TempFileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileManager.class);

    private static Path         parentDir;
    private static Path         tempDir;

    /**
     * Initializes the temp file manager inside the given parent directory.
     * All temporary directories, which can be used by a process, are stored inside that parent directory.
     * It should be constant for each application process.<br>
     * <br>
     * Note that this method also purges old temporary directories.
     * See {@link TempFileManager} for more details on that.
     * 
     * @param parentDir The directory that holds all temporary directories.
     * @throws IOException Something goes wrong during the initialization.
     */
    public static void initialize(Path parentDir) throws IOException {

        if (TempFileManager.parentDir != null) {
            throw new IllegalStateException("Temp file manager cannot be initialized twice");
        }

        TempFileManager.parentDir = parentDir;
        Files.createDirectories(parentDir);

        // Clean up all temp dirs used by older processes which are no longer running
        cleanUp();

        // Create a new temp dir for the current process
        createTempDir();
    }

    private static void cleanUp() throws IOException {

        List<String> javaPids = getJavaPids();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parentDir)) {
            for (Path otherTempDir : directoryStream) {
                try {
                    if (!Files.isDirectory(otherTempDir)) {
                        Files.delete(otherTempDir);
                    } else {
                        String otherTempDirPid = otherTempDir.getFileName().toString();

                        if (!javaPids.contains(otherTempDirPid)) {
                            LOGGER.debug("Deleting old temp dir from '{}'", otherTempDir);
                            IOFileUtils.deleteDirectory(otherTempDir);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.warn("Error while trying to delete old temp dir from '{}'", otherTempDir, e);
                }
            }
        }
    }

    private static List<String> getJavaPids() throws IOException {

        if (!SystemUtils.IS_OS_UNIX && !SystemUtils.IS_OS_WINDOWS) {
            throw new IllegalStateException("TempFileManager does only support Unix and Windows");
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        if (SystemUtils.IS_OS_UNIX) {
            processBuilder.command("ps", "-C", "java", "-o", "pid=");
        } else {
            processBuilder.command("tasklist.exe", "/fo", "csv", "/nh");
        }

        List<String> javaPids = new ArrayList<>();

        Process process = processBuilder.start();
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            scanner.useDelimiter("\\n");
            while (scanner.hasNext()) {
                if (SystemUtils.IS_OS_UNIX) {
                    javaPids.add(scanner.next().trim());
                } else {
                    String[] tokens = scanner.next().split(",");
                    if (tokens.length == 5 && tokens[0].equals("java.exe")) {
                        javaPids.add(StringUtils.strip(tokens[1], "\""));
                    }
                }
            }
        }

        return javaPids;
    }

    private static void createTempDir() throws IOException {

        String pid = StringUtils.substringBefore(ManagementFactory.getRuntimeMXBean().getName(), "@");

        tempDir = parentDir.resolve(pid);
        LOGGER.debug("Creating new temp dir under '{}'", tempDir);
        Files.createDirectory(tempDir);
    }

    /**
     * Returns the temporary directory which can be used by the current application process to store temporary files.
     * It is recommended to use the {@link Path#resolve(String)} method for resolving the path of a new temporary file:
     * 
     * <pre>
     * TempFileManager.getTempDir().resolve(&quot;someNewTempFile&quot;);
     * </pre>
     * 
     * @return The temporary directory where temporary files can be stored.
     */
    public static Path getTempDir() {

        return tempDir;
    }

    private TempFileManager() {

    }

}
