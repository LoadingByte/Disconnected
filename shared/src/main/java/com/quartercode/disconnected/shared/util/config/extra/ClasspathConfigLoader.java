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

package com.quartercode.disconnected.shared.util.config.extra;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.shared.util.ResourceLister;
import com.quartercode.disconnected.shared.util.config.ConfigService;

/**
 * A utility for loading xml configuration files from the classpath using a {@link ConfigService}.
 *
 * @see ConfigService
 */
public class ClasspathConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathConfigLoader.class);

    /**
     * Loads xml configuration files recursively from the given path.
     * The path should be located in the classpath.
     *
     * @param configService The {@link ConfigService} that is responsible for parsing the found files.
     * @param path The path to load the config files from.
     */
    public static void load(ConfigService configService, String path) {

        try (ResourceLister resourceLister = new ResourceLister(path, false)) {
            for (Path resource : resourceLister.getResourcePaths()) {
                loadResource(configService, resource);
            }
        } catch (IOException e) {
            LOGGER.error("Error while retrieving resource occurrences (resource path '{}')", path, e);
        }
    }

    private static void loadResource(final ConfigService configService, Path resource) throws IOException {

        try {
            Files.walkFileTree(resource, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    loadConfig(configService, file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {

                    LOGGER.error("Error while trying to process config dir/file '{}'", file, exc);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            LOGGER.error("Error while trying to load config files from '{}'", resource, e);
        }
    }

    private static void loadConfig(ConfigService configService, Path file) throws IOException {

        LOGGER.debug("Loading config from '{}'", file);

        try {
            Document config = new SAXBuilder().build(file.toUri().toURL());
            configService.parse(config);
        } catch (JDOMException e) {
            LOGGER.warn("Error while parsing config xml from '{}'", file, e);
        }
    }

    private ClasspathConfigLoader() {

    }

}
