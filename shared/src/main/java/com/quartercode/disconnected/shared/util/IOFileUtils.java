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

package com.quartercode.disconnected.shared.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.lang3.Validate;

/**
 * This utility class contains constants and utility methods for dealing with real files and {@link Path}s.
 * It is not responsible for manipulating simulated files on the server.
 */
public class IOFileUtils {

    /**
     * Copies the given source directory and all its content to the given target directory recursively.
     * 
     * @param sourceDir The directory that contains the files which should be copied.
     * @param targetDir The directory into which the source files should be copied.
     * @throws IOException Something goes wrong while copying a file or directory.
     */
    public static void copyDirectory(final Path sourceDir, final Path targetDir) throws IOException {

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Path targetFile = targetDir.resolve(sourceDir.relativize(file).toString());
                Files.createDirectories(targetFile.getParent());
                Files.copy(file, targetFile);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    /**
     * Deletes the given directory and all its content recursively.
     * 
     * @param dir The directory that should be deleted.
     * @throws IOException Something goes wrong while deleting a file or directory.
     */
    public static void deleteDirectory(Path dir) throws IOException {

        Validate.isTrue(Files.isDirectory(dir), "deleteDirectory() requires a directory path");

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    private IOFileUtils() {

    }

}
