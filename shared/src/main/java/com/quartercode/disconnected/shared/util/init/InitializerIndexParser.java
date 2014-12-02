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

package com.quartercode.disconnected.shared.util.init;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.shared.util.ClasspathScanningUtils;

/**
 * A utility class which reads class index files from the classpath and constructs instances of the mentioned {@link Initializer}s.
 * That way, initializers can be dynamically resolved without coupling them inside the code.
 * 
 * @see Initializer
 */
public class InitializerIndexParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializerIndexParser.class);

    /**
     * Parses all class index files which lie on the classpath under the given resource.
     * Note that the file is allowed to occur multiple times (e.g. in different jars).
     * The mentioned classes are then instantiated to the returned {@link Initializer} objects.<br>
     * <br>
     * See {@link ClasspathScanningUtils#getIndexedPackageClasses(String, boolean, boolean)} for information on the index file format.
     * 
     * @param indexResource The classpath resource path under which the class index files are located.
     * @return The parsed initializer list.
     * @throws IOException Thrown if the available class index file directories cannot be listed.
     */
    public static Collection<Initializer> parseIndex(String indexResource) throws IOException {

        Collection<Class<?>> classes = ClasspathScanningUtils.getIndexedPackageClasses(indexResource, true, false);
        Collection<Initializer> initializers = new ArrayList<>(classes.size());

        for (Class<?> initializerClass : classes) {
            if (!Initializer.class.isAssignableFrom(initializerClass)) {
                LOGGER.warn("Indexed initializer type '{}' does not implement the Initializer interface", initializerClass.getName());
            } else if (!initializerClass.isAnnotationPresent(InitializerSettings.class)) {
                LOGGER.warn("Indexed initializer type '{}' does not have the InitializerSettings annotation", initializerClass.getName());
            } else {
                try {
                    Object instance = initializerClass.newInstance();
                    initializers.add((Initializer) instance);
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.warn("Error while creating new instance of initializer type '{}'", initializerClass.getName(), e);
                }
            }
        }

        return initializers;
    }

    private InitializerIndexParser() {

    }

}
