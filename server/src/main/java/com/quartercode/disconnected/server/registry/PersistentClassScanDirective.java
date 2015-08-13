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

package com.quartercode.disconnected.server.registry;

import javax.xml.bind.JAXBContext;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * A data object that describes a location which should be scanned for persistent world classes.
 * Such classes are marshalled or unmarshalled during the serialization or deserialization of a world object.
 * Therefore, they must be known to the operating {@link JAXBContext}.
 *
 * @see ServerRegistries#PERSISTENT_CLASS_SCAN_DIRECTIVES
 */
public class PersistentClassScanDirective extends RegistryObject {

    private final String     packageName;
    private final ScanMethod method;

    public PersistentClassScanDirective(String packageName, ScanMethod method) {

        this.packageName = packageName;
        this.method = method;
    }

    /**
     * Returns the package which should be scanned for persistent world classes.
     * Depending on the {@link #getMethod() scanning method}, the subpackages of this package might also be scanned.
     *
     * @return The package for scanning, e.g. {@code com.quartercode.disconnected.world}.
     */
    public String getPackageName() {

        return packageName;
    }

    /**
     * Returns the {@link ScanMethod} which defines how the given {@link #getPackageName() package} is scanned for persistent world classes.
     *
     * @return The scanning method.
     */
    public ScanMethod getMethod() {

        return method;
    }

    /**
     * A scan method defines how the a certain package is scanned for persistent world classes.
     * Depending on the method, the subpackages of an input package might also be scanned.
     *
     * @see PersistentClassScanDirective
     */
    public static enum ScanMethod {

        /**
         * Scans the provided package and all its subpackages (and all their subpackages ...) for classes which have the {@link XmlPersistent} annotation.
         */
        RECURSIVE_ANNOTATION_SEARCH ("recursiveAnnotationSearch"),
        /**
         * Reads the persistent classes from {@code jaxb.index} files in the provided package.
         * Note that this method does not recursively scan subpackages of the provided packages.
         */
        JAXB_INDEX_LOOKUP ("jaxbIndexLookup");

        /**
         * Returns the scan method which has the given camel-cased name.
         *
         * @param name The camel-cased name the returned scan method must have.
         * @return The scan method with the given name, or {@code null} if no such scan method is found.
         * @see #getName()
         */
        public static ScanMethod valueOfCamelCase(String name) {

            for (ScanMethod scanMethod : values()) {
                if (scanMethod.getName().equals(name)) {
                    return scanMethod;
                }
            }

            return null;
        }

        private final String name;

        private ScanMethod(String name) {

            this.name = name;
        }

        /**
         * Returns the name of the scan method.
         * It is just the name of the constant in camel case.
         *
         * @return The scan method name.
         */
        public String getName() {

            return name;
        }

    }

}
