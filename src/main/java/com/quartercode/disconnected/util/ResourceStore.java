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

package com.quartercode.disconnected.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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

    /**
     * The singleton instance of resource store.
     */
    public static final ResourceStore INSTANCE        = new ResourceStore();

    private final Map<String, Object> loadedResources = new HashMap<String, Object>();

    private ResourceStore() {

    }

    /**
     * Returns all resource objects which are currently loaded into the resource store.
     * 
     * @return All resource objects which are currently loaded into the resource store.
     */
    public Map<String, Object> getAll() {

        return Collections.unmodifiableMap(loadedResources);
    }

    /**
     * Returns the loaded resource object which was stored under the given path.
     * 
     * @param path The path to use for the selection.
     * @return The loaded resource object which was stored under the given path.
     */
    public Object get(String path) {

        return loadedResources.get(path);
    }

    /**
     * Loads resource objects recursively from the given path.
     * The path should be located in the classpath.
     * 
     * Example:
     * If you load resources from /data, a file located under /data/test.png will be assigned to the path test.png.
     * A file located under /data/test/test.png will be assigned to the path test/test.png.
     * 
     * @param path The path to load the resources from.
     * @throws IOException Something goes wrong while reading from a jar file or resource.
     */
    public void loadFromClasspath(String path) throws IOException {

        for (String resourcePath : ResourceLister.getResources(path, false)) {
            URL resource = ResourceStore.class.getResource(resourcePath);

            Object resourceObject = null;
            if (resourcePath.endsWith(".txt")) {
                resourceObject = new Scanner(new InputStreamReader(resource.openStream())).useDelimiter("\\Z").next();
            } else if (resourcePath.endsWith(".png")) {
                resourceObject = ImageIO.read(resource);
            }

            if (resourceObject == null) {
                throw new RuntimeException("Can't find parser for " + resourcePath);
            } else {
                String name = resourcePath.replace(path, "");
                if (name.startsWith("/")) {
                    name = name.substring(1);
                }
                loadedResources.put(name, resourceObject);
            }
        }
    }

}
