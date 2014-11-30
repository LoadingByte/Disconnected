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

import java.io.Closeable;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is able to list classpath resources from multiple classpath entries.
 * For example, if two classpath entries (e.g. jars) contain the directory {@code /test1/test2}, this class returns both.
 */
public class ResourceLister implements Closeable {

    private static final Logger    LOGGER         = LoggerFactory.getLogger(ResourceLister.class);

    private final List<Path>       resourcePaths  = new ArrayList<>();
    private final List<FileSystem> jarFileSystems = new ArrayList<>();

    /**
     * Creates a new resource lister that lists all occurrences of the given classpath resource on the classpath as {@link Path}s.
     * It is possible that multiple paths are listed if multiple classpath entries (e.g. jars) contain the resource.
     * For example, if two jars contain the directory {@code /test1/test2}, this class lists both both.<br>
     * <br>
     * Note that this class uses a workaround to create path objects for files which are located inside a jar.
     * Because of that, each instance of this class should be closed after the paths have been used.
     * However, the paths might no longer be accessible after the lister has been closed.
     * 
     * @param resourcePath The path that defines the classpath resource whose occurrences should be listed.
     * @param throwAll Whether {@link IOException}s, which are thrown during the retrieval of one specific resource and would therefore also interrupt
     *        the retrieval of other resources, should be thrown.
     *        If this is {@code false}, the regarding exceptions are logged as errors.
     * @throws IOException Something goes wrong while retrieving the resource occurrences.
     *         If {@code throwAll} is {@code true} and an occurrence is located inside a jar, a jar reading exception is also possible.
     * @throws IllegalArgumentException The resource path doesn't start with "/" (this method cannot list relative resources).
     * @see #getResourcePaths()
     */
    public ResourceLister(String resourcePath, boolean throwAll) throws IOException {

        if (!resourcePath.startsWith("/")) {
            throw new IllegalArgumentException("Cannot retrieve relative resources, make sure your path starts with '/'");
        } else {
            // Retrieve all occurrences of the given path in the classpath
            Enumeration<URL> locations = ResourceLister.class.getClassLoader().getResources(StringUtils.stripStart(resourcePath, "/"));

            // Iterate over those occurrences
            while (locations.hasMoreElements()) {
                URL location = locations.nextElement();

                // Check whether the found location is inside a jar file
                if (location.getProtocol().equals("jar")) {
                    try {
                        // Resolve the path of the jar file and load the resources from there
                        Path jarFile = Paths.get(toURI( ((JarURLConnection) location.openConnection()).getJarFileURL()));

                        // Load the resources from the jar file
                        FileSystem jarFS = FileSystems.newFileSystem(jarFile, null);
                        resourcePaths.add(jarFS.getPath(resourcePath));
                        jarFileSystems.add(jarFS);
                    } catch (IOException e) {
                        if (throwAll) {
                            throw e;
                        } else {
                            LOGGER.error("Cannot read resource '{}' from jar file '{}'", resourcePath, location, e);
                        }
                    }
                } else {
                    // Directly load the resources from the main file system
                    resourcePaths.add(Paths.get(toURI(location)));
                }
            }
        }
    }

    /**
     * Returns all listed occurrences of the classpath resource.
     * 
     * @return All resource occurrences.
     */
    public List<Path> getResourcePaths() {

        return resourcePaths;
    }

    @Override
    public void close() throws IOException {

        for (FileSystem jarFileSystem : jarFileSystems) {
            jarFileSystem.close();
        }
    }

    private static URI toURI(URL url) {

        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Strange error: Cannot convert url '" + url + "' into uri", e);
        }
    }

}
