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

package com.quartercode.disconnected.server.util;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * This utlity class lists classpath resources which can be found under a given path.
 */
public class ResourceLister {

    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    /**
     * Returns all classpath resources which can be found under the given path.
     * This can only list absoulte resources, so make sure that your path starts with "/".
     * 
     * @param path The parent classpath directory to search in. Has to start with "/".
     * @param directories If enabled, all directories will also be returned.
     * @return All classpath resources which can be found under the given path.
     * @throws IOException Something goes wrong while reading from the jar file (if it's a jar).
     * @throws IllegalStateException One of the classpath entries on this process doesn't exist.
     * @throws IllegalArgumentException The parts doesn't start with "/" (this can't list relative resources).
     */
    public static List<String> getResources(String path, boolean directories) throws IOException {

        if (StringUtils.strip(path, "/").isEmpty()) {
            throw new IllegalArgumentException("Can't list resources from classpath root ('/')");
        } else if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Can't list relative resources, make sure your path starts with '/'");
        } else {
            List<String> resources = new ArrayList<>();

            // Retrieve all occurrences of the given path in the classpath
            Enumeration<URL> locations = ResourceLister.class.getClassLoader().getResources(StringUtils.stripStart(path, "/"));

            // Iterate over those occurrences
            while (locations.hasMoreElements()) {
                URL location = locations.nextElement();

                // Check whether the found location is inside a jar file
                if (location.getProtocol().equals("jar")) {
                    // Resolve the path of the jar file and load the resources from there
                    Path jarFile = Paths.get(toURI( ((JarURLConnection) location.openConnection()).getJarFileURL()));

                    // Load the resources from the jar file
                    try (FileSystem jarFS = FileSystems.newFileSystem(jarFile, null)) {
                        return getResourcesFromDirectory(path, directories, null, jarFS.getPath(path));
                    }
                } else {
                    // Directly load the resources from the main file system
                    resources.addAll(getResourcesFromDirectory(path, directories, location.getFile(), Paths.get(toURI(location))));
                }
            }

            return resources;
        }
    }

    private static URI toURI(URL url) {

        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Strange error: Cannot convert url '" + url + "' into uri", e);
        }
    }

    private static List<String> getResourcesFromDirectory(String path, boolean directories, String filter, Path currentDirectory) throws IOException {

        List<String> resources = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(currentDirectory)) {
            for (Path file : directoryStream) {
                if (!Files.isDirectory(file) || directories) {
                    if (filter == null) {
                        resources.add(file.toString());
                    } else {
                        resources.add(path + file.toString().replace(filter, "").replace(PATH_SEPARATOR, "/"));
                    }
                }

                if (Files.isDirectory(file)) {
                    resources.addAll(getResourcesFromDirectory(path, directories, filter, file));
                }
            }
        }

        return resources;
    }

}
