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

package com.quartercode.disconnected.sim.comp.file;

import java.util.Arrays;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang.Validate;

/**
 * File rights control the access to files by users.
 * You can set if a given user/group is allowed to read, write, execute or delete.
 */
@XmlJavaTypeAdapter (FileRights.FileRightsAdapter.class)
public class FileRights {

    /**
     * The file accessor represents the type of person who accesses a file in its context.
     */
    public static enum FileAccessor {

        /**
         * The owner of the file is the user who created it (the owner can be changed).
         */
        OWNER,
        /**
         * By default, this is the primary group of the creator of the file (the group can be changed).
         */
        GROUP,
        /**
         * Everyone else (not owner or group).
         */
        OTHERS;

    }

    /**
     * This enum stores all types of file rights which can be assigned.
     */
    public static enum FileRight {

        /**
         * The read-right determinates if a user is allowed to read the contents from a file or directory (letter 'r').
         */
        READ ('r'),
        /**
         * The write-right determinates if a user is allowed to write contents into files or create new files in a directory (letter 'w').
         */
        WRITE ('w'),
        /**
         * The delete-right determinates if a user is allowed to delete a file or a directory (letter 'e').
         * In the case of a directory, the user also needs the delete-right on every file or folder in it.
         */
        DELETE ('d'),
        /**
         * The execute-right determinates if a user is allowed to execute a file (letter 'x').
         * It cannot be applied to directories.
         */
        EXECUTE ('x'),
        /**
         * The execute-extended-right is equally to the setuid/setgid-flag on UNIX-systems.
         * It allows to execute files as the owner or the group of the file.
         */
        EXECUTE_EXTENDED ('s');

        private char letter;

        private FileRight(char letter) {

            this.letter = letter;
        }

        /**
         * Returns the letter which can be used as an acronym for the right.
         * 
         * @return The letter which can be used as an acronym for the right.
         */
        public char getLetter() {

            return letter;
        }

    }

    private FileRight[] ownerRights;
    private FileRight[] groupRights;
    private FileRight[] othersRights;

    /**
     * Creates a new empty file rights storage.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected FileRights() {

    }

    public FileRights(FileRight[] ownerRights, FileRight[] groupRights, FileRight[] othersRights) {

        Validate.isTrue(ownerRights.length == 4, "Owner right array must contain 4 elements");
        Validate.isTrue(groupRights.length == 4, "Group right array must contain 4 elements");
        Validate.isTrue(othersRights.length == 4, "Others right array must contain 4 elements");

        this.ownerRights = othersRights;
        this.groupRights = groupRights;
        this.othersRights = othersRights;
    }

    /**
     * Creates a new file rights storage using the given file right string.
     * This is using the typcial UNIX-format with some changes. Examples:
     * 
     * <pre>
     * rwdsr--xr--x
     * rwdxrwdx----
     * </pre>
     * 
     * You can split the string into 3 segments.
     * 
     * <pre>
     * rwds | r--x | r--x
     * rwdx | rwdx | ----
     * </pre>
     * 
     * The first segment defines the rights for the owner, the second for the group and the third for everyone else.
     * 
     * A "r" means that you can read from the file or directory.
     * A "w" means that you can write content into the file or create new files inside a directory.
     * A "d" means that you can delete the file or directory (if it's empty).
     * A "x" means that you can execute the file.
     * A "s" (replaces "x") meands that you can execute the file as the owner or the group of the file.
     * 
     * @param rights The right information to parse.
     */
    public FileRights(String rights) {

        Validate.isTrue(rights.length() == 4 * 3, "The right string must contain 4 * 3 = 12 characters");

        ownerRights = parseCharRights(rights.substring(0, 4).toCharArray());
        groupRights = parseCharRights(rights.substring(4, 8).toCharArray());
        othersRights = parseCharRights(rights.substring(8, 12).toCharArray());
    }

    private FileRight[] parseCharRights(char[] rights) {

        FileRight[] enumRights = new FileRight[4];
        for (int index = 0; index < rights.length; index++) {
            for (FileRight enumRight : FileRight.values()) {
                if (enumRight.getLetter() == rights[index]) {
                    enumRights[index] = enumRight;
                }
            }
        }
        return enumRights;
    }

    private FileRight[] getRightArray(FileAccessor accessor) {

        if (accessor == FileAccessor.OWNER) {
            return ownerRights;
        } else if (accessor == FileAccessor.GROUP) {
            return groupRights;
        } else if (accessor == FileAccessor.OTHERS) {
            return othersRights;
        } else {
            return null;
        }
    }

    /**
     * Returns if the given right is set for the given file accessor.
     * 
     * @param accessor The accessor who wants to access the file.
     * @param right The file right to test on.
     * @return If the given right is set for the given file accessor.
     */
    public boolean getRight(FileAccessor accessor, FileRight right) {

        for (FileRight setRight : getRightArray(accessor)) {
            if (setRight != null && setRight == right) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets or unsets the given right for the given file accessor.
     * If a right is set, the given file accessor use functions related to the right.
     * 
     * @param accessor The accessor who wants to access the file.
     * @param right The file right to set or unset.
     * @param set If the given right should be set (true) or unset (false).
     */
    public void setRight(FileAccessor accessor, FileRight right, boolean set) {

        int index = 0;
        if (right == FileRight.READ) {
            index = 0;
        } else if (right == FileRight.WRITE) {
            index = 1;
        } else if (right == FileRight.DELETE) {
            index = 2;
        } else if (right == FileRight.EXECUTE) {
            index = 3;
        } else if (right == FileRight.EXECUTE_EXTENDED) {
            index = 3;
        }

        getRightArray(accessor)[index] = set ? right : null;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(groupRights);
        result = prime * result + Arrays.hashCode(othersRights);
        result = prime * result + Arrays.hashCode(ownerRights);
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FileRights other = (FileRights) obj;
        if (!Arrays.equals(groupRights, other.groupRights)) {
            return false;
        }
        if (!Arrays.equals(othersRights, other.othersRights)) {
            return false;
        }
        if (!Arrays.equals(ownerRights, other.ownerRights)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return generateRightString(ownerRights) + generateRightString(groupRights) + generateRightString(othersRights);
    }

    private String generateRightString(FileRight[] rights) {

        String rightString = "";
        for (FileRight right : rights) {
            rightString += right.getLetter();
        }
        return rightString;
    }

    /**
     * This file rights adapter is for storing a file rights object as a simple string.
     * This is using the typcial UNIX-format with some changes. Examples:
     * 
     * <pre>
     * rwdsr--xr--x
     * rwdxrwdx----
     * </pre>
     * 
     * You can split the string into 3 segments.
     * 
     * <pre>
     * rwds | r--x | r--x
     * rwdx | rwdx | ----
     * </pre>
     * 
     * The first segment defines the rights for the owner, the second for the group and the third for everyone else.
     * 
     * A "r" means that you can read from the file or directory.
     * A "w" means that you can write content into the file or create new files inside a directory.
     * A "d" means that you can delete the file or directory (if it's empty).
     * A "x" means that you can execute the file.
     * A "s" (replaces "x") meands that you can execute the file as the owner or the group of the file.
     */
    public static class FileRightsAdapter extends XmlAdapter<String, FileRights> {

        /**
         * Creates a new file rights adapter.
         */
        public FileRightsAdapter() {

        }

        @Override
        public FileRights unmarshal(String v) {

            return new FileRights(v);
        }

        @Override
        public String marshal(FileRights v) {

            return v.toString();
        }

    }

}
