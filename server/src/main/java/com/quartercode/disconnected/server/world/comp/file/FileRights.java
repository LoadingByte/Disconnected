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

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.Arrays;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.disconnected.server.util.NullPreventer;
import com.quartercode.disconnected.server.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.server.world.general.StringRepresentable;

/**
 * File rights control the access to files by users.
 * You can set if a given user/group is allowed to read, write, execute or delete.
 */
public class FileRights extends WorldChildFeatureHolder<File<?>> implements StringRepresentable {

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
         * The read-right determines if a user is allowed to read the contents from a file or directory (letter 'r').
         */
        READ ('r'),
        /**
         * The write-right determines if a user is allowed to write contents into files or create new files in a directory (letter 'w').
         */
        WRITE ('w'),
        /**
         * The delete-right determines if a user is allowed to delete a file or a directory (letter 'e').
         * In the case of a directory, the user also needs the delete-right on every file or folder in it.
         */
        DELETE ('d'),
        /**
         * The execute-right determines if a user is allowed to execute a file (letter 'x').
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

    // ----- Properties -----

    /**
     * The boolean array which stores the rights.
     * The first four booleans represent the owner rights in the order read, write, delete, execute.
     * The booleans from index 4 to 7 represent the group rights, the ones from 8 to 11 the others' rights.
     */
    protected static final PropertyDefinition<Boolean[]> RIGHTS;

    static {

        RIGHTS = create(new TypeLiteral<PropertyDefinition<Boolean[]>>() {}, "name", "rights", "storage", new StandardStorage<>(), "initialValue", new CloneValueFactory<>(new Boolean[3 * 4]));

    }

    // ----- Functions -----

    /**
     * Returns if the given {@link FileRight} is set for the given {@link FileAccessor}.
     * If a {@link FileRight} is set, the given {@link FileAccessor} can use functions related to the right.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link FileAccessor}</td>
     * <td>accessor</td>
     * <td>The accessor who wants to access the file.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link FileRight}</td>
     * <td>rights</td>
     * <td>The file right to test.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Boolean>      GET;

    /**
     * Sets or unsets the given {@link FileRight} for the given {@link FileAccessor}.
     * If a {@link FileRight} is set, the given {@link FileAccessor} can use functions related to the right.
     * If you use null for the {@link FileAccessor}, you will change the right for every accessor type.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link FileAccessor}</td>
     * <td>accessor</td>
     * <td>The accessor who wants to access the file; null for all.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link FileRight}</td>
     * <td>rights</td>
     * <td>The file right to set or unset.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link Boolean}</td>
     * <td>set</td>
     * <td>If the given right should be set (true) or unset (false).</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>         SET;

    /**
     * Changes the stored rights to the ones stored in the given file rights object.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link FileRights}</td>
     * <td>rights</td>
     * <td>The file rights object to get the rights from.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>         FROM_OBJECT;

    /**
     * Returns the stored file rights as a string.
     * The string is using the typcial UNIX-format with some changes. Examples:
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
    public static final FunctionDefinition<String>       TO_STRING   = StringRepresentable.TO_STRING;

    /**
     * Changes the rights to the ones stored in the given rights string.
     * The string is using the typcial UNIX-format with some changes. Examples:
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
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link String}</td>
     * <td>rights</td>
     * <td>The right information to parse.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>         FROM_STRING = StringRepresentable.FROM_STRING;

    static {

        GET = create(new TypeLiteral<FunctionDefinition<Boolean>>() {}, "name", "get", "parameters", new Class[] { FileAccessor.class, FileRight.class });
        GET.addExecutor("default", FileRights.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                boolean result = false;
                if (arguments[0] != null) {
                    // The requested array item might be null
                    Boolean rawResult = invocation.getHolder().get(RIGHTS).get()[ ((FileAccessor) arguments[0]).ordinal() * 4 + ((FileRight) arguments[1]).ordinal()];
                    result = NullPreventer.prevent(rawResult);
                }

                invocation.next(arguments);
                return result;
            }

        });

        SET = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "set", "parameters", new Class[] { FileAccessor.class, FileRight.class, Boolean.class });
        SET.addExecutor("default", FileRights.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                if (arguments[0] == null) {
                    holder.get(SET).invoke(FileAccessor.OWNER, arguments[1], arguments[2]);
                    holder.get(SET).invoke(FileAccessor.GROUP, arguments[1], arguments[2]);
                    holder.get(SET).invoke(FileAccessor.OTHERS, arguments[1], arguments[2]);
                } else {
                    holder.get(RIGHTS).get()[ ((FileAccessor) arguments[0]).ordinal() * 4 + ((FileRight) arguments[1]).ordinal()] = (Boolean) arguments[2];
                }

                return invocation.next(arguments);
            }

        });

        FROM_OBJECT = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "fromObject", "parameters", new Class[] { FileRights.class });
        FROM_OBJECT.addExecutor("default", FileRights.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                invocation.getHolder().get(RIGHTS).set(Arrays.copyOf( ((FileRights) arguments[0]).get(RIGHTS).get(), 3 * 4));

                return invocation.next(arguments);
            }

        });

        TO_STRING.addExecutor("default", FileRights.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                String rights = "";
                for (int index = 0; index < 3 * 4; index++) {
                    if (invocation.getHolder().get(RIGHTS).get()[index]) {
                        FileRight right = null;
                        if (index == 0 || index == 4 || index == 8) {
                            right = FileRight.READ;
                        } else if (index == 1 || index == 5 || index == 9) {
                            right = FileRight.WRITE;
                        } else if (index == 2 || index == 6 || index == 10) {
                            right = FileRight.DELETE;
                        } else if (index == 3 || index == 7 || index == 11) {
                            right = FileRight.EXECUTE;
                        }
                        rights += right.getLetter();
                    } else {
                        rights += '-';
                    }
                }

                invocation.next(arguments);
                return rights;
            }

        });
        FROM_STRING.addExecutor("default", FileRights.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                char[] rights = ((String) arguments[0]).toCharArray();
                for (int index = 0; index < 3 * 4; index++) {
                    invocation.getHolder().get(RIGHTS).get()[index] = rights[index] != '-';
                }

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new file rights storage.
     * You can fill it using {@link #SET}.
     */
    public FileRights() {

        setParentType(File.class);
    }

}
