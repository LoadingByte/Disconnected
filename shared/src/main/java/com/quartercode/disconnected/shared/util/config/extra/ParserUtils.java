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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for parsing strings that occur in XML configuration files.
 */
public class ParserUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserUtils.class);

    /**
     * Parses the given positive number string and returns the result (as a long).
     * This method outputs a logger warning and returns the given old number if the string does not represent a positive number.
     * 
     * @param config The XML configuration {@link Document} that contains the string for parsing.
     * @param usage A string that is used to specify the logger warning.
     *        The context is "Cannot use ... as {}", where {} is the usage string.
     *        An example for this might be "program size".
     *        Note that additional parameters should be supplied for making finding the error easier: "program size for {programName}".
     * @param string The number string that should be parsed.
     * @param oldNumber The old number that will be returned if there's an error during parsing.
     * @return The parsed number, or {@code oldNumber} if an error occurred.
     */
    public static long parsePositiveNumber(Document config, String usage, String string, long oldNumber) {

        if (!StringUtils.isNumeric(string)) {
            LOGGER.warn("Config: Cannot use non-numeric string '{}' as {} (in '{}')", string, usage, config.getBaseURI());
        } else {
            return Long.parseLong(string);
        }

        return oldNumber;
    }

    /**
     * Parses the given decimal number string and returns the result (as a double).
     * This method outputs a logger warning and returns the given old number if the string does not represent a positive or negative decimal number.
     * 
     * @param config The XML configuration {@link Document} that contains the string for parsing.
     * @param usage A string that is used to specify the logger warning.
     *        The context is "Cannot use ... as {}", where {} is the usage string.
     *        An example for this might be "program selection probability".
     *        Note that additional parameters should be supplied for making finding the error easier: "program selection probability for {programName}".
     * @param string The decimal number string that should be parsed.
     * @param oldNumber The old decimal number that will be returned if there's an error during parsing.
     * @return The parsed decimal number, or {@code oldNumber} if an error occurred.
     */
    public static double parseDecimalNumber(Document config, String usage, String string, double oldNumber) {

        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            LOGGER.warn("Config: Cannot use non-decimal string '{}' as {} (in '{}')", string, usage, config.getBaseURI());
        }

        return oldNumber;
    }

    /**
     * Parses the given fully qualified java {@link Class} name string and returns the result.
     * This method outputs a logger warning and returns the given old class if the string does not represent a valid class.
     * 
     * @param config The XML configuration {@link Document} that contains the string for parsing.
     * @param usage A string that is used to specify the logger warning.
     *        The context is "Cannot use ... as {}", where {} is the usage string.
     *        An example for this might be "program executor class".
     *        Note that additional parameters should be supplied for making finding the error easier: "program executor class for {programName}".
     * @param superclass The superclass the parsed class must extend somehow.
     *        If the parsed class does not do that, a logger warning will be printed.
     *        This parameter may be {@code null} to disable the check.
     * @param string The fully qualified java class name string that should be parsed.
     * @param oldClass The old class that will be returned if there's an error during parsing.
     * @return The parsed class, or {@code oldClass} if an error occurred.
     * @see #parseGenericClass(Document, String, Class, String, Class)
     */
    public static Class<?> parseClass(Document config, String usage, Class<?> superclass, String string, Class<?> oldClass) {

        try {
            Class<?> newClass = Class.forName(string);

            if (superclass != null && !superclass.isAssignableFrom(newClass)) {
                LOGGER.warn("Config: Cannot use class '{}' as {} because it does not extend '{}' (in '{}')", newClass.getName(), usage, superclass.getName(), config.getBaseURI());
            } else {
                return newClass;
            }
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Config: Cannot use unknown class '{}' as {} (in '{}')", string, usage, config.getBaseURI());
        }

        return oldClass;
    }

    /**
     * Parses the given fully qualified java {@link Class} name string and returns the result as a <b>class object with the correct generic parameter</b>.
     * This method outputs a logger warning and returns the given old class if the string does not represent a valid class.
     * 
     * @param config The XML configuration {@link Document} that contains the string for parsing.
     * @param usage A string that is used to specify the logger warning.
     *        The context is "Cannot use ... as {}", where {} is the usage string.
     *        An example for this might be "program executor class".
     *        Note that additional parameters should be supplied for making finding the error easier: "program executor class for {programName}".
     * @param superclass The superclass the parsed class must extend somehow.
     *        If the parsed class does not do that, a logger warning will be printed.
     *        This parameter may be {@code null} to disable the check.
     * @param string The fully qualified java class name string that should be parsed.
     * @param oldClass The old class that will be returned if there's an error during parsing.
     * @return The parsed class, or {@code oldClass} if an error occurred.
     * @see #parseClass(Document, String, Class, String, Class)
     */
    @SuppressWarnings ("unchecked")
    public static <T> Class<T> parseGenericClass(Document config, String usage, Class<? super T> superclass, String string, Class<T> oldClass) {

        Class<?> newClass = parseClass(config, usage, superclass, string, oldClass);
        return newClass != null ? (Class<T>) newClass : oldClass;
    }

    /**
     * Parses the given {@link URL} string and returns the result.
     * This method outputs a logger warning and returns the given old URL if the string does not represent a valid URL.
     * 
     * @param config The XML configuration {@link Document} that contains the string for parsing.
     * @param usage A string that is used to specify the logger warning.
     *        The context is "Cannot use ... as {}", where {} is the usage string.
     *        An example for this might be "theme URL".
     *        Note that additional parameters should be supplied for making finding the error easier: "theme URL for {programName}".
     * @param string The URL string that should be parsed.
     * @param oldURL The old URL that will be returned if there's an error during parsing.
     * @return The parsed URL, or {@code oldURL} if an error occurred.
     */
    public static URL parseURL(Document config, String usage, String string, URL oldURL) {

        if (string.startsWith("/")) {
            URL newURL = ParserUtils.class.getResource(string);

            if (newURL == null) {
                LOGGER.warn("Config: Unable to find url resource '{}' for usage as {} (in '{}')", string, usage, config.getBaseURI());
            } else {
                return newURL;
            }
        } else {
            Path file = Paths.get(string);

            if (!Files.exists(file)) {
                LOGGER.warn("Config: Unable to find url file '{}' for usage as {} (in '{}')", string, usage, config.getBaseURI());
            } else {
                try {
                    return file.toUri().toURL();
                } catch (MalformedURLException e) {
                    LOGGER.error("Strange error while converting file '{}' to url", file, e);
                }
            }
        }

        return oldURL;
    }

    /**
     * Parses the given string, which must contain a fully qualified java {@link Class} name string and a static field name, and returns the result.
     * The format for the string is {@code class.field} (e.g. {@code com.quartercode.disconnected.Something.CONSTANT_3}).
     * Note that inner classes can also be accessed (e.g. {@code com.quartercode.disconnected.Outer$Inner.CONSTANT_15}).
     * This method outputs a logger warning and returns the given old constant value class if the string does not represent a valid static field.
     * 
     * @param config The XML configuration {@link Document} that contains the string for parsing.
     * @param usage A string that is used to specify the logger warning.
     *        The context is "Cannot use ... as {}", where {} is the usage string.
     *        An example for this might be "mapping value".
     *        Note that additional parameters should be supplied for making finding the error easier: "mapping value for {mappingKey}".
     * @param valueSuperclass The superclass the parsed constant value must extend somehow.
     *        If the parsed value does not do that, a logger warning will be printed.
     *        This parameter may be {@code null} to disable the check.
     * @param string The fully qualified java class name string followed by a dot and the static field name that should be parsed.
     * @param oldValue The old constant value that will be returned if there's an error during parsing.
     * @return The parsed constant value, or {@code oldValue} if an error occurred.
     */
    public static Object parseConstant(Document config, String usage, Class<?> valueSuperclass, String string, Class<?> oldValue) {

        String className = StringUtils.substringBeforeLast(string, ".");
        String fieldName = StringUtils.substringAfterLast(string, ".");

        try {
            Class<?> type = Class.forName(className);
            Object value = type.getField(fieldName).get(null);

            if (valueSuperclass != null && !valueSuperclass.isInstance(value)) {
                LOGGER.warn("Config: Cannot use constant value '{}' as {} because it does not extend '{}' (in '{}')", value, usage, valueSuperclass.getName(), config.getBaseURI());
            } else {
                return value;
            }
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Config: Cannot use constant '{}' from unknown class '{}' as {} (in '{}')", fieldName, className, usage, config.getBaseURI());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.warn("Config: Cannot use unknown constant '{}' as {} (in '{}')", string, usage, config.getBaseURI());
        }

        return oldValue;
    }

    private ParserUtils() {

    }

}
