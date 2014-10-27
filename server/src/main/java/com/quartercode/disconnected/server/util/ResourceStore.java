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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 * The resource store loads all kinds of resources before the game starts.
 * That has the advantage that you don't have to load files during the game what would slow it out.
 */
public class ResourceStore {

    private static final Map<String, Object> loadedResources = new HashMap<>();

    /**
     * Returns all resource objects which are currently loaded into the resource store.
     * 
     * @return All resource objects which are currently loaded into the resource store.
     */
    public static Map<String, Object> getAll() {

        return Collections.unmodifiableMap(loadedResources);
    }

    /**
     * Returns the loaded resource object which was stored under the given path.
     * 
     * @param path The path to use for the selection.
     * @return The loaded resource object which was stored under the given path.
     */
    public static Object get(String path) {

        return loadedResources.get(path);
    }

    /**
     * Loads resource objects recursively from the given path.
     * The path should be located in the classpath.<br>
     * <br>
     * Example:
     * If you load resources from /data, a file located under /data/test.png will be assigned to the path test.png.
     * A file located under /data/test/test.png will be assigned to the path test/test.png.
     * 
     * @param path The path to load the resources from.
     * @throws IOException Something goes wrong while reading from a jar file or resource.
     */
    public static void loadFromClasspath(String path) throws IOException {

        try (ResourceLister resourceLister = new ResourceLister(path)) {
            for (final Path resource : resourceLister.getResourcePaths()) {
                Files.walkFileTree(resource, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                        loadResource(resource.relativize(file).toString(), file);
                        return FileVisitResult.CONTINUE;
                    }

                });
            }
        }
    }

    private static void loadResource(String name, Path file) throws IOException {

        Object resourceObject = null;
        if (name.endsWith(".txt")) {
            try (Scanner scanner = new Scanner(Files.newBufferedReader(file, Charset.forName("UTF-8")))) {
                resourceObject = scanner.useDelimiter("\\Z").next();
            }
        } else if (name.endsWith(".png")) {
            try (InputStream inputStream = Files.newInputStream(file)) {
                resourceObject = ImageIO.read(inputStream);
            }
        }

        if (resourceObject == null) {
            throw new RuntimeException("Can't find parser for " + file);
        } else {
            loadedResources.put(name, resourceObject);
        }
    }

    private ResourceStore() {

    }

}
