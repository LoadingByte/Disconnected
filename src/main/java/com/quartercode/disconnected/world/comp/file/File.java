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

package com.quartercode.disconnected.world.comp.file;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.SizeUtil;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;
import com.quartercode.disconnected.world.comp.os.Group;
import com.quartercode.disconnected.world.comp.os.User;

/**
 * This class represents a file on a {@link FileSystem}.
 * Every file knows its name and can resolve its path.
 * There are different variants of a file: A {@link ContentFile} holds content, a {@link ParentFile} holds other files.
 * 
 * @param <P> The type of the parent {@link FeatureHolder} which houses the file somehow.
 * @see ContentFile
 * @see ParentFile
 * @see FileSystem
 */
public class File<P extends FeatureHolder> extends WorldChildFeatureHolder<P> implements DerivableSize {

    private static final Logger                                          LOGGER              = Logger.getLogger(File.class.getName());

    /**
     * The path separator which seperates different files in a path string.
     */
    public static final String                                           SEPARATOR           = "/";

    /**
     * The default {@link FileRights} string for every new file.
     * 
     * @deprecated TODO: Make the default {@link FileRights} dynamic.
     */
    @Deprecated
    public static final String                                           DEFAULT_FILE_RIGHTS = "rwd-r---r---";

    // ----- Properties -----

    /**
     * The name of the file.
     */
    protected static final FeatureDefinition<ObjectProperty<String>>     NAME;

    /**
     * The {@link FileRights} object which stores the UNIX-like file right attributes.
     * For more documentation on how it works, see the {@link FileRights} class.
     */
    protected static final FeatureDefinition<ObjectProperty<FileRights>> RIGHTS;

    /**
     * The {@link User} who owns the file.
     * This is important for the {@link FileRights} system.
     */
    protected static final FeatureDefinition<ReferenceProperty<User>>    OWNER;

    /**
     * The {@link Group} which partly owns the file.
     * This is important for the {@link FileRights} system.
     */
    protected static final FeatureDefinition<ReferenceProperty<Group>>   GROUP;

    static {

        NAME = ObjectProperty.createDefinition("name");
        RIGHTS = new AbstractFeatureDefinition<ObjectProperty<FileRights>>("rights") {

            @Override
            public ObjectProperty<FileRights> create(FeatureHolder holder) {

                FileRights rights = new FileRights();
                try {
                    rights.get(FileRights.FROM_STRING).invoke(DEFAULT_FILE_RIGHTS);
                } catch (ExecutorInvocationException e) {
                    LOGGER.log(Level.SEVERE, "Unexpected exception during creation of default file rights object", e);
                }
                return new ObjectProperty<FileRights>(getName(), holder, rights);
            }

        };
        OWNER = ReferenceProperty.createDefinition("owner");
        GROUP = ReferenceProperty.createDefinition("group");

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the name of the file.
     */
    public static final FunctionDefinition<String>                       GET_NAME;

    /**
     * Changes the name of the file.
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
     * <td>name</td>
     * <td>The new name of the file.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                         SET_NAME;

    /**
     * Returns the {@link FileRights} object which stores the UNIX-like file right attributes.
     * For more documentation on how it works, see the {@link FileRights} class.
     */
    public static final FunctionDefinition<FileRights>                   GET_RIGHTS;

    /**
     * Returns the {@link User} who owns the file.
     * This is important for the {@link FileRights} system.
     */
    public static final FunctionDefinition<User>                         GET_OWNER;

    /**
     * Changes the {@link User} who owns the file.
     * This is important for the {@link FileRights} system.
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
     * <td>{@link User}</td>
     * <td>owner</td>
     * <td>The new {@link User} who owns the file.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                         SET_OWNER;

    /**
     * Returns the {@link Group} which partly owns the file.
     * This is important for the {@link FileRights} system.
     */
    public static final FunctionDefinition<Group>                        GET_GROUP;

    /**
     * Changes the {@link Group} which partly owns the file.
     * This is important for the {@link FileRights} system.
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
     * <td>{@link Group}</td>
     * <td>group</td>
     * <td>The new {@link Group} which partly owns the file.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                         SET_GROUP;

    /**
     * Returns the local the path of the file.
     * A path is a collection of files seperated by a separator.
     * The local path can be used to look up the file a on its {@link FileSystem}.
     */
    public static final FunctionDefinition<String>                       GET_PATH;

    /**
     * Moves the file to the given local path.
     * A path is a collection of files seperated by a separator.
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
     * <td>path</td>
     * <td>The path the file will be moved to.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link OutOfSpaceException}</td>
     * <td>There is not enough space for the moved file on the target {@link FileSystem}.</td>
     * </tr>
     * <tr>
     * <td>{@link IllegalStateException}</td>
     * <td>The given file path isn't valid.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                         SET_PATH;

    /**
     * Removes the file from the {@link FileSystem}.
     * If this file is a {@link ParentFile}, all child files will also be removed.
     */
    public static final FunctionDefinition<Void>                         REMOVE;

    /**
     * Returns the {@link FileSystem} which is hosting the file.
     */
    public static final FunctionDefinition<FileSystem>                   GET_FILE_SYSTEM;

    static {

        GET_NAME = FunctionDefinitionFactory.create("getName", File.class, PropertyAccessorFactory.createGet(NAME));
        SET_NAME = FunctionDefinitionFactory.create("setName", File.class, PropertyAccessorFactory.createSet(NAME), String.class);

        GET_RIGHTS = FunctionDefinitionFactory.create("getRights", File.class, PropertyAccessorFactory.createGet(RIGHTS));

        GET_OWNER = FunctionDefinitionFactory.create("getOwner", File.class, PropertyAccessorFactory.createGet(OWNER));
        SET_OWNER = FunctionDefinitionFactory.create("setOwner", File.class, PropertyAccessorFactory.createSet(OWNER), User.class);

        GET_GROUP = FunctionDefinitionFactory.create("getGroup", File.class, PropertyAccessorFactory.createGet(GROUP));
        SET_GROUP = FunctionDefinitionFactory.create("setGroup", File.class, PropertyAccessorFactory.createSet(GROUP), Group.class);

        GET_PATH = FunctionDefinitionFactory.create("getPath", File.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                String path = null;
                // Check for removed files
                if ( ((File<?>) holder).getParent() != null) {
                    path = ((File<?>) holder).getParent().get(GET_PATH).invoke() + File.SEPARATOR + holder.get(GET_NAME).invoke();
                }

                invocation.next(arguments);
                return path;
            }

        });
        SET_PATH = FunctionDefinitionFactory.create("setPath", File.class, new FunctionExecutor<Void>() {

            @Override
            @SuppressWarnings ("unchecked")
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                FileSystem fileSystem = holder.get(GET_FILE_SYSTEM).invoke();
                if (fileSystem != null) {
                    ParentFile<?> oldParent = (ParentFile<?>) ((File<?>) holder).getParent();
                    String path = FileUtils.resolvePath(holder.get(GET_PATH).invoke(), (String) arguments[0]);
                    fileSystem.get(FileSystem.ADD_FILE).invoke(holder, path);
                    FeatureHolder parent = ((File<?>) holder).getParent();
                    oldParent.get(ParentFile.REMOVE_CHILDREN).invoke(holder);
                    ((File<FeatureHolder>) holder).setParent(parent);
                }

                return invocation.next(arguments);
            }

        }, String.class);

        REMOVE = FunctionDefinitionFactory.create("remove", File.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                if ( ((File<?>) holder).getParent() instanceof ParentFile) {
                    ((File<?>) holder).getParent().get(ParentFile.REMOVE_CHILDREN).invoke(holder);
                }

                return invocation.next(arguments);
            }

        });

        GET_FILE_SYSTEM = FunctionDefinitionFactory.create("getFileSystem", File.class, new FunctionExecutor<FileSystem>() {

            @Override
            public FileSystem invoke(FunctionInvocation<FileSystem> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                FileSystem fileSystem = null;
                if (holder instanceof RootFile) {
                    fileSystem = ((RootFile) holder).getParent();
                } else if (holder instanceof File && ((File<?>) holder).getParent() != null) {
                    fileSystem = ((File<?>) holder).getParent().get(GET_FILE_SYSTEM).invoke();
                }

                invocation.next(arguments);
                return fileSystem;
            }

        });

        GET_SIZE.addExecutor(File.class, "name", SizeUtil.createGetSize(NAME));
        GET_SIZE.addExecutor(File.class, "rights", SizeUtil.createGetSize(RIGHTS));

    }

    // ----- Functions End -----

    /**
     * Creates a new file.
     */
    public File() {

    }

}
