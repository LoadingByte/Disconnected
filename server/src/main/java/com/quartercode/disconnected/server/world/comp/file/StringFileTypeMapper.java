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

package com.quartercode.disconnected.server.world.comp.file;

import java.util.HashMap;
import java.util.Map;

/**
 * This utility class is used to map {@link File} types (class objects) to string representations.
 * For example, the {@link Directory} class is mapped to the string {@code "directory"}.
 * These mappings can then be retrieved in both directions.
 * Moreover, mappings can be added and removed dynamically.<br>
 * <br>
 * By default, the following mappings are available (they must be added using the default server data class):
 * 
 * <table>
 * <tr>
 * <th>Class Type</th>
 * <td>{@link RootFile}</td>
 * <td>{@link Directory}</td>
 * <td>{@link ContentFile}</td>
 * </tr>
 * <tr>
 * <th>String Type</th>
 * <td>rootFile</td>
 * <td>directory</td>
 * <td>contentFile</td>
 * </tr>
 * </table>
 * 
 * @see #classToString(Class)
 * @see #stringToClass(String)
 */
public class StringFileTypeMapper {

    private static final Map<Class<? extends File<?>>, String> CLASSES_TO_STRINGS = new HashMap<>();
    private static final Map<String, Class<? extends File<?>>> STRINGS_TO_CLASSES = new HashMap<>();

    /**
     * Returns the string representation of the given {@link File} class.
     * The class parameter is not generic in order to prevent unnecessary unchecked casts.
     * 
     * @param classType The class object of the file type whose string representation should be returned.
     * @return The string representation of the given file class.
     */
    public static String classToString(Class<?> classType) {

        return CLASSES_TO_STRINGS.get(classType);
    }

    /**
     * Returns the file class object of the given string representation.
     * 
     * @param stringType The string representation whose file class should be returned.
     * @return The class object of the file type represented by the given string.
     */
    public static Class<? extends File<?>> stringToClass(String stringType) {

        return STRINGS_TO_CLASSES.get(stringType);
    }

    /**
     * Adds the given mapping in order to make it available through the accessor methods.
     * Note that all mappings with the same class or string are removed.
     * 
     * @param classType The file class object that should be mapped to the given string representation.
     * @param stringType The string representation that should be mapped to the given file class object.
     */
    public static void addMapping(Class<? extends File<?>> classType, String stringType) {

        // Clear any old mappings with the same class or name
        removeMapping(classType);
        removeMapping(stringType);

        CLASSES_TO_STRINGS.put(classType, stringType);
        STRINGS_TO_CLASSES.put(stringType, classType);
    }

    /**
     * Removes the mapping that maps the given file class object to some string representation.
     * 
     * @param classType The file class whose mapping should be removed.
     */
    public static void removeMapping(Class<? extends File<?>> classType) {

        STRINGS_TO_CLASSES.remove(classToString(classType));
        CLASSES_TO_STRINGS.remove(classType);
    }

    /**
     * Removes the mapping that maps the given string representation to some file class object.
     * 
     * @param stringType The string representation whose mapping should be removed.
     */
    public static void removeMapping(String stringType) {

        CLASSES_TO_STRINGS.remove(stringToClass(stringType));
        STRINGS_TO_CLASSES.remove(stringType);
    }

    /**
     * Removes all added mappings.
     * Note that this method also removes the default mappings.
     */
    public static void clearMappings() {

        CLASSES_TO_STRINGS.clear();
        STRINGS_TO_CLASSES.clear();
    }

    private StringFileTypeMapper() {

    }

}
