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
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.sim.comp.program.Process;

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
        OWNER ('u'),
        /**
         * By default, this is the primary group of the creator of the file (the group can be changed).
         */
        GROUP ('g'),
        /**
         * Everyone else (not owner or group).
         */
        OTHERS ('o');

        /**
         * Returns the file accessor which has the given letter assigned.
         * 
         * @param letter The letter which is assigned to the returned accessor.
         * @return The file accessor which has the given letter assigned.
         */
        public static FileAccessor valueOf(char letter) {

            for (FileAccessor accessor : values()) {
                if (accessor.getLetter() == letter) {
                    return accessor;
                }
            }

            return null;
        }

        private char letter;

        private FileAccessor(char letter) {

            this.letter = letter;
        }

        /**
         * Returns the letter which can be used as an acronym for the accessor.
         * 
         * @return The letter which can be used as an acronym for the accessor.
         */
        public char getLetter() {

            return letter;
        }

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
        EXECUTE ('x');

        /**
         * Returns the file right which has the given letter assigned.
         * 
         * @param letter The letter which is assigned to the returned right.
         * @return The file right which has the given letter assigned.
         */
        public static FileRight valueOf(char letter) {

            for (FileRight right : values()) {
                if (right.getLetter() == letter) {
                    return right;
                }
            }

            return null;
        }

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

    /**
     * Returns if the given user has the given right on the given file.
     * 
     * @param user The user who may have the given right on the given file.
     * @param file The file the given user may have access to.
     * @param right The right the given user may have.
     * @return True if the given user has the given right on the given file.
     */
    public static boolean hasRight(User user, File file, FileRight right) {

        if (user.isSuperuser()) {
            return true;
        } else if (checkRight(file, FileAccessor.OWNER, right) && file.getOwner().equals(user)) {
            return true;
        } else if (checkRight(file, FileAccessor.GROUP, right) && user.getGroups().contains(file.getGroup())) {
            return true;
        } else if (checkRight(file, FileAccessor.OTHERS, right)) {
            return true;
        }

        return false;
    }

    private static boolean checkRight(File file, FileAccessor accessor, FileRight right) {

        if (file.getRights().getRight(accessor, right)) {
            if (right == FileRight.DELETE && file.getType() == FileType.DIRECTORY) {
                for (File child : file.getChildFiles()) {
                    if (!checkRight(child, accessor, right)) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Throws a {@link NoFileRightException} if the given process hasn't the given right on the given file.
     * 
     * @param process The process which may have the given right on the given file.
     * @param file The file the given process may have access to.
     * @param right The right the given process may have.
     * @throws NoFileRightException The given process hasn't the given right on the given file.
     */
    public static void checkRight(Process process, File file, FileRight right) throws NoFileRightException {

        if (!hasRight(process.getSession().getUser(), file, right)) {
            throw new NoFileRightException(process, file, right);
        }
    }

    /**
     * Returns if the given user can change the right attributes of the given file.
     * Every process has to check this for itself!
     * 
     * @param user The user who may can change the right attributes.
     * @param file The file the given user may have access to.
     * @return True if the given user can change the right attributes of the given file.
     */
    public static boolean canChangeRights(User user, File file) {

        return file.getOwner().equals(user) || user.isSuperuser();
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

    /**
     * Creates a new file rights storage using the given file right arrays.
     * Every array sets the rights for the self-explantory group of users.
     * The arrays hold {@link FileRight} values. Examples for the format:
     * 
     * <pre>
     * Right string:
     * rwdxr--xr--x
     * 
     * Arrays:
     * ownerRights[READ, WRITE, DELETE, EXECUTE]
     * groupRights[READ, null, null, EXECUTE]
     * othersRights[READ, null, null, EXECUTE]
     * </pre>
     * 
     * @param ownerRights The array which defines the rights the file owner has.
     * @param groupRights The array which defines the rights the file group has.
     * @param othersRights The array which defines the rights everyone else has.
     */
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
     * rwdxr--xr--x
     * rwdxrwdx----
     * </pre>
     * 
     * You can split the string into 3 segments.
     * 
     * <pre>
     * rwdx | r--x | r--x
     * rwdx | rwdx | ----
     * </pre>
     * 
     * The first segment defines the rights for the owner, the second for the group and the third for everyone else.
     * 
     * A "r" means that you can read from the file or directory.
     * A "w" means that you can write content into the file or create new files inside a directory.
     * A "d" means that you can delete the file or directory (if it's empty).
     * A "x" means that you can execute the file.
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
            enumRights[index] = FileRight.valueOf(rights[index]);
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

        if (accessor != null) {
            return getRightArray(accessor)[right.ordinal()] == right;
        } else {
            return false;
        }
    }

    /**
     * Sets or unsets the given right for the given file accessor.
     * If a right is set, the given file accessor use functions related to the right.
     * If you use null for the file accessor, you will change the right for every accessor type.
     * 
     * @param accessor The accessor who wants to access the file; null for all.
     * @param right The file right to set or unset.
     * @param set If the given right should be set (true) or unset (false).
     */
    public void setRight(FileAccessor accessor, FileRight right, boolean set) {

        if (accessor == null) {
            ownerRights[right.ordinal()] = set ? right : null;
            groupRights[right.ordinal()] = set ? right : null;
            othersRights[right.ordinal()] = set ? right : null;
        } else {
            getRightArray(accessor)[right.ordinal()] = set ? right : null;
        }
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
            if (right == null) {
                rightString += '-';
            } else {
                rightString += right.getLetter();
            }
        }
        return rightString;
    }

    /**
     * This file rights adapter is for storing a file rights object as a simple string.
     * This is using the typcial UNIX-format with some changes. Examples:
     * 
     * <pre>
     * rwdxr--xr--x
     * rwdxrwdx----
     * </pre>
     * 
     * You can split the string into 3 segments.
     * 
     * <pre>
     * rwdx | r--x | r--x
     * rwdx | rwdx | ----
     * </pre>
     * 
     * The first segment defines the rights for the owner, the second for the group and the third for everyone else.
     * 
     * A "r" means that you can read from the file or directory.
     * A "w" means that you can write content into the file or create new files inside a directory.
     * A "d" means that you can delete the file or directory (if it's empty).
     * A "x" means that you can execute the file.
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

            if (v != null) {
                return v.toString();
            } else {
                return null;
            }
        }

    }

}
