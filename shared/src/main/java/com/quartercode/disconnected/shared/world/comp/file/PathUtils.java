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

package com.quartercode.disconnected.shared.world.comp.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * This utility contains methods related to operating with file paths.
 */
public class PathUtils {

    /**
     * The path separator which separates different files in a path string ({@value #SEPARATOR}).
     */
    public static final String   SEPARATOR                    = "/";

    private static final Pattern SPLIT_AFTER_MOUNTPOINT_REGEX = Pattern.compile("(" + SEPARATOR + "+([^" + SEPARATOR + "]+))?(" + SEPARATOR + "*(.*))?");
    private static final Pattern SPLIT_BEFORE_NAME_REGEX      = Pattern.compile("((.*)" + SEPARATOR + ")?([^" + SEPARATOR + "]+)" + SEPARATOR + "*");

    /**
     * Normalizes the given path.
     * Here are some examples:
     *
     * <pre>
     * /user/bin/../homes/test/
     * =&gt; /user/homes/test/
     *
     * /user/./homes/test/
     * =&gt; /user/homes/test/
     * </pre>
     *
     * @param path The path that should be normalized.
     * @return The normalized path.
     */
    public static String normalize(String path) {

        return resolve(SEPARATOR, path);
    }

    /**
     * Creates an absolute path out of the given one.
     * The algorithm starts at the given start path and changes the path according to the "change" path.
     * The "change" path also can be absolute. This will ignore the start path.<br>
     * <br>
     * Here's an example:
     *
     * <pre>
     * Start:  /user/homes/test/
     * Change: ../test2/docs
     * Result: /user/home/test2/docs
     * </pre>
     *
     * Note that going further up than the root node (/) will just result in the root node:
     *
     * <pre>
     * Start:  /user/
     * Change: ../../
     * Result: /
     * </pre>
     *
     * @param start The absolute path the algorithm starts at.
     * @param path The "change" path which defines where the start path should change (see above).
     * @return The resolved absolute path.
     * @throws IllegalArgumentException The start path is not absolute (it does not start with {@link #SEPARATOR}).
     */
    public static String resolve(String start, String path) {

        if (!start.startsWith(SEPARATOR)) {
            throw new IllegalArgumentException("Start path must be absolute (it has to start with " + SEPARATOR + "): " + start);
        } else {
            List<String> current = new ArrayList<>();
            if (!path.startsWith(SEPARATOR)) {
                current.addAll(Arrays.asList(start.split(SEPARATOR)));
                if (current.size() > 0) {
                    // Remove first entry ([this]/...), it's empty
                    current.remove(0);
                }
            }

            for (String pathChange : path.split(SEPARATOR)) {
                if (!pathChange.equals(".") && !pathChange.isEmpty()) {
                    if (pathChange.equals("..")) {
                        if (!current.isEmpty()) {
                            current.remove(current.size() - 1);
                        }
                    } else {
                        current.add(pathChange);
                    }
                }
            }

            if (current.isEmpty()) {
                return SEPARATOR;
            } else {
                String resolvedPath = "";
                for (String part : current) {
                    resolvedPath += SEPARATOR + part;
                }
                return resolvedPath;
            }
        }
    }

    /**
     * Splits the path into an array that contains the different path segments which were separated by path separators ({@link #SEPARATOR}).
     * Examples:
     *
     * <pre>
     * Path: /system/etc/test
     * =&gt; [system, etc, test]
     *
     * Path: /system/etc/../test
     * =&gt; [system, etc, .., test]
     * </pre>
     *
     * Note that double separators are ignored and separators at the start or end of the path do not matter.
     * Moreover, any relative segments (e.g. "..") are not resolved.
     *
     * @param path The path that should be split into an array.
     * @return The split path array.
     */
    public static String[] split(String path) {

        return StringUtils.split(StringUtils.strip(path, SEPARATOR), SEPARATOR);
    }

    /**
     * Joins the given path segment array together and returns the resulting path string.
     * All segments are separated by the {@link #SEPARATOR} character.
     * Also, a leading separator is placed at the beginning.
     * Examples:
     *
     * <pre>
     * Path: [system, etc, test]
     * =&gt; /system/etc/test
     *
     * Path: [system, etc, .., test]
     * =&gt; /system/etc/../test
     * </pre>
     *
     * Any relative segments (e.g. "..") are not resolved.
     *
     * @param path The path segment array that should be joined into a path string.
     * @return The joined path.
     */
    public static String join(String[] path) {

        return join(path, true);
    }

    /**
     * Joins the given path segment array together and returns the resulting path string.
     * All segments are separated by the {@link #SEPARATOR} character.
     * Also, a leading separator might be placed at the beginning depending on the second argument.
     * Examples:
     *
     * <pre>
     * Path: [system, etc, test], Leading Separator: true
     * =&gt; /system/etc/test
     *
     * Path: [system, etc, test], Leading Separator: false
     * =&gt; system/etc/test
     *
     * Path: [system, etc, .., test], Leading Separator: true
     * =&gt; /system/etc/../test
     * </pre>
     *
     * Any relative segments (e.g. "..") are not resolved.
     *
     * @param path The path segment array that should be joined into a path string.
     * @param leadingSeparator Whether a leading path separator should be placed at the beginning of the path.
     * @return The joined path.
     */
    public static String join(String[] path, boolean leadingSeparator) {

        return (leadingSeparator ? SEPARATOR : "") + StringUtils.join(path, SEPARATOR);
    }

    /**
     * Splits the given global path into a mountpoint and a local file system path and returns the result.
     * The returned array always has two entries. [0] is the mountpoint and [1] is the local file system path.
     * The mountpoint of a path is the first path element.
     * Examples:
     *
     * <pre>
     * Path: /system/etc/test
     * =&gt; [system, etc/test]
     *
     * Path: /user
     * =&gt; [user, null]
     *
     * Path: home/user1/file
     * =&gt; [null, home/user1/file]
     *
     * Path: /
     * =&gt; [null, null]
     * </pre>
     *
     * @param path The path which should be split into a mountpoint and a local path.
     * @return The mountpoint and the local path of the given global path.
     *         May be {@code null} if an empty path (e.g. "" or "/") is provided.
     */
    public static String[] splitAfterMountpoint(String path) {

        Matcher matcher = SPLIT_AFTER_MOUNTPOINT_REGEX.matcher(path);

        if (matcher.find()) {
            String[] result = { StringUtils.defaultIfEmpty(matcher.group(2), null), StringUtils.defaultIfEmpty(matcher.group(4), null) };
            return result;
        }

        return null;
    }

    /**
     * Splits the given path into a directory path and a file name and returns the result.
     * The returned array always has two entries. [0] is the directory path and [1] is the file name.
     * The file name of a path is the last path element.
     * Examples:
     *
     * <pre>
     * Path: /system/etc/test
     * =&gt; [/system/etc, test]
     *
     * Path: /user
     * =&gt; [null, user]
     *
     * Path: home/user1/file
     * =&gt; [home/user1, file]
     *
     * Path: /
     * =&gt; [null, null]
     * </pre>
     *
     * Note that the file name will never contain a separator.
     *
     * @param path The path which should be split into a directory path and a file name.
     * @return The directory path and the file name of the given path.
     *         May be {@code null} if an empty path (e.g. "" or "/") is provided.
     */
    public static String[] splitBeforeName(String path) {

        Matcher matcher = SPLIT_BEFORE_NAME_REGEX.matcher(path);

        if (matcher.find()) {
            String dirPath = matcher.group(2);
            dirPath = StringUtils.isEmpty(dirPath) || StringUtils.containsOnly(dirPath, SEPARATOR) ? null : dirPath;
            return new String[] { dirPath, matcher.group(3) };
        }

        return new String[] { null, null };
    }

    private PathUtils() {

    }

}
