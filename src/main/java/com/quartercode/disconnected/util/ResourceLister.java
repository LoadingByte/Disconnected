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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This utlity class lists classpath resources which can be found under a given path.
 */
public class ResourceLister {

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

        if (path.startsWith("/")) {
            List<String> resources = new ArrayList<String>();

            for (String entry : System.getProperty("java.class.path", ".").split(System.getProperty("path.separator"))) {
                File file = new File(entry);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        resources.addAll(getResourcesFromDirectory(file, file, path, directories));
                    } else {
                        resources.addAll(getResourcesFromJar(file, path, directories));
                    }
                } else {
                    throw new IllegalStateException("Classpath entry \"" + entry + "\" doesn't exist");
                }
            }

            return resources;
        } else {
            throw new IllegalArgumentException("Can't list relative resources, make sure your path starts with \"/\"");
        }
    }

    private static List<String> getResourcesFromDirectory(File classpathEntry, File directory, String path, boolean directories) {

        List<String> resources = new ArrayList<String>();

        for (File file : directory.listFiles()) {
            if (!file.isDirectory() || directories) {
                String fileName = file.getAbsolutePath().replace(classpathEntry.getAbsolutePath(), "").replaceAll("\\\\", "/");
                if (fileName.startsWith(path)) {
                    resources.add(fileName);
                }
            }

            if (file.isDirectory()) {
                resources.addAll(getResourcesFromDirectory(classpathEntry, file, path, directories));
            }
        }

        return resources;
    }

    private static List<String> getResourcesFromJar(File file, String path, boolean directories) throws IOException {

        List<String> resources = new ArrayList<String>();

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();

                if (!entry.isDirectory() || directories) {
                    String fileName = "/" + entry.getName();
                    if (fileName.startsWith(path)) {
                        resources.add(fileName);
                    }
                }
            }
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }

        return resources;
    }

}
