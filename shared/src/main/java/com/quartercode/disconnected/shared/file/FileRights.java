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

package com.quartercode.disconnected.shared.file;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A storage object for file rights which control the access to files by users.
 * Such file rights can be granted to specific accessor types, which are basically types of users.
 * For example, by granting the read right to the owner accessor, the owner of a file is allowed to read its content.<br>
 * <br>
 * Note that accessor types and right types are represented by single characters.
 * By default, the following file accessors can be used:
 * 
 * <ul>
 * <li>{@link #OWNER} -&gt; {@value #OWNER}</li>
 * <li>{@link #GROUP} -&gt; {@value #GROUP}</li>
 * <li>{@link #OTHERS} -&gt; {@value #OTHERS}</li>
 * </ul>
 * 
 * Moreover, the following default file rights are provided:
 * 
 * <ul>
 * <li>{@link #READ} -&gt; {@value #READ}</li>
 * <li>{@link #WRITE} -&gt; {@value #WRITE}</li>
 * <li>{@link #DELETE} -&gt; {@value #DELETE}</li>
 * <li>{@link #EXECUTE} -&gt; {@value #EXECUTE}</li>
 * </ul>
 */
public class FileRights implements Serializable {

    // ----- Default file accessors -----

    /**
     * A file accessor that grants rights to the owning user of a file (letter {@code 'u'}).
     * By default, this is the user who created the file.
     * Note that the owner of a file can be changed.
     */
    public static final char                  OWNER           = 'u';

    /**
     * A file accessor that grants rights to all members of the group of a file (letter {@code 'g'}).
     * By default, the group is the primary group of the user who created the file.
     * Note that the group of a file can be changed.
     */
    public static final char                  GROUP           = 'g';

    /**
     * A file accessor that grants rights to everyone (letter {@code 'o'})..
     */
    public static final char                  OTHERS          = 'o';

    // ----- Default file rights -----

    /**
     * A file right that determines whether a user is allowed to read the content of a file or to retrieve the files inside a directory (letter {@code 'r'}).
     */
    public static final char                  READ            = 'r';

    /**
     * A file right that determines whether a user is allowed to write new content into a file or to create new files inside a directory (letter {@code 'w'}).
     */
    public static final char                  WRITE           = 'w';

    /**
     * A file right that determines whether a user is allowed to delete a file or a directory (letter {@code 'd'}).
     * In the case of a directory, the user also needs the delete right on each file or folder inside it.
     */
    public static final char                  DELETE          = 'd';

    /**
     * A file right that determines whether a user is allowed to execute a file (letter {@code 'x'}).
     * It has no effect on directories.
     */
    public static final char                  EXECUTE         = 'x';

    // ----- Object -----

    private static final SortedSet<Character> EMPTY_RIGHT_SET = Collections.unmodifiableSortedSet(new TreeSet<Character>());

    private static void validateCharacter(char c) {

        Validate.isTrue(c != ':', "Character used for file rights cannot be ':'");
        Validate.isTrue(c != ',', "Character used for file rights cannot be ','");
    }

    private static SortedMap<Character, SortedSet<Character>> fromString(String string) {

        SortedMap<Character, SortedSet<Character>> rights = new TreeMap<>();

        for (String accessorGroup : StringUtils.split(string, ',')) {
            boolean isValid = accessorGroup.indexOf(':') == 1 && StringUtils.countMatches(accessorGroup, ":") == 1;
            Validate.isTrue(isValid, "Each accessor group separated by a ',' must contain exactly one ':' at the second position; wrong format: '%s'", string);

            char accessor = accessorGroup.charAt(0);
            validateCharacter(accessor);
            rights.put(accessor, new TreeSet<Character>());

            for (char right : accessorGroup.substring(2).toCharArray()) {
                validateCharacter(right);
                rights.get(accessor).add(right);
            }
        }

        return rights;
    }

    private static String toString(SortedMap<Character, SortedSet<Character>> rights) {

        StringBuilder string = new StringBuilder();

        for (Entry<Character, SortedSet<Character>> entry : rights.entrySet()) {
            string.append(",").append(entry.getKey()).append(":");

            for (char right : entry.getValue()) {
                string.append(right);
            }
        }

        return string.length() == 0 ? "" : string.substring(1);
    }

    @XmlJavaTypeAdapter (FileRightsAdapter.class)
    private final SortedMap<Character, SortedSet<Character>> rights;

    /**
     * Creates a new empty file rights object with no rights being set.
     */
    public FileRights() {

        rights = new TreeMap<>();
    }

    /**
     * Creates a new file rights object and fills it with the rights stored in the given file rights object.
     * Modifications on one of the two objects won't change the other object.
     * 
     * @param original The file rights object whose rights should be copied into the new object.
     */
    public FileRights(FileRights original) {

        rights = new TreeMap<>();

        for (Entry<Character, SortedSet<Character>> entry : original.rights.entrySet()) {
            rights.put(entry.getKey(), new TreeSet<>(entry.getValue()));
        }
    }

    /**
     * Creates a new file rights object and fills it with the rights stored by the given string representation.
     * The format is the same one used by {@link #toString()}. Check that method for more documentation.
     * 
     * @param string The string representation that contains the file rights for the new object.
     */
    public FileRights(String string) {

        rights = fromString(string);
    }

    /**
     * Returns whether the given file right (second character) is granted to the given file accessor type (first character).
     * If the file right is set, the given accessor is allowed to use functions related to the right.
     * 
     * @param accessor A character that describes the type of user that wants to access a file.
     *        Default accessors are {@link #OWNER}, {@link #GROUP} and {@link #OTHERS}.
     * @param right A character that describes the right required for certain operations on a file.
     *        Default rights are {@link #READ}, {@link #WRITE}, {@link #DELETE} and {@link #EXECUTE}.
     * @return Whether the given file right is granted to the given file accessor type.
     */
    public boolean isRightSet(char accessor, char right) {

        validateCharacter(accessor);
        validateCharacter(right);

        return rights.containsKey(accessor) && rights.get(accessor).contains(right);
    }

    /**
     * Returns an unmodifiable set that contains all rights granted to the given file accessor type.
     * If a file right is set, the given accessor is allowed to use functions related to that right.
     * 
     * @param accessor A character that describes the type of user that wants to access a file.
     *        Default accessors are {@link #OWNER}, {@link #GROUP} and {@link #OTHERS}.
     * @return All rights that are granted to the given file accessor type.
     */
    public SortedSet<Character> getAllSetRights(char accessor) {

        validateCharacter(accessor);

        if (rights.containsKey(accessor)) {
            return Collections.unmodifiableSortedSet(rights.get(accessor));
        } else {
            return EMPTY_RIGHT_SET;
        }
    }

    /**
     * Sets whether the given file right (second character) is granted to the given file accessor type (first character).
     * If the file right is set, the given accessor is allowed to use functions related to the right.
     * 
     * @param accessor A character that describes the type of file-accessing user the right is granted or not granted to.
     *        Default accessors are {@link #OWNER}, {@link #GROUP} and {@link #OTHERS}.
     * @param right A character that describes the right, which is required for certain operations on a file, that should be granted or not granted.
     *        Default rights are {@link #READ}, {@link #WRITE}, {@link #DELETE} and {@link #EXECUTE}.
     * @param set Whether the given file right should be granted to the given file accessor type.
     */
    public void setRight(char accessor, char right, boolean set) {

        validateCharacter(accessor);
        validateCharacter(right);

        if (set) {
            if (!rights.containsKey(accessor)) {
                rights.put(accessor, new TreeSet<Character>());
            }

            rights.get(accessor).add(right);
        } else if (rights.containsKey(accessor)) {
            rights.get(accessor).remove(right);

            if (rights.get(accessor).isEmpty()) {
                rights.remove(accessor);
            }
        }
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Returns a string representation of the stored file rights.
     * Examples:
     * 
     * <pre>
     * o:rx,u:dw
     * =&gt; File accessor 'o' (everyone) has the rights 'r' and 'x'.
     * =&gt; File accessor 'u' (owner) has the explicit rights 'd' and 'w'.
     *    Note that 'u' also has the implicit rights 'r' and 'x' because those two rights are granted to everyone.
     * 
     * g:drwx,u:drwx
     * =&gt; File accessors 'g' (group) and 'u' (owner) have the rights 'd', 'r', 'w', and 'x'.
     * </pre>
     * 
     * @return A string representation of the file rights.
     */
    @Override
    public String toString() {

        return toString(rights);
    }

    static class FileRightsAdapter extends XmlAdapter<String, SortedMap<Character, SortedSet<Character>>> {

        @Override
        public String marshal(SortedMap<Character, SortedSet<Character>> v) {

            return FileRights.toString(v);
        }

        @Override
        public SortedMap<Character, SortedSet<Character>> unmarshal(String v) throws Exception {

            return FileRights.fromString(v);
        }

    }

}
