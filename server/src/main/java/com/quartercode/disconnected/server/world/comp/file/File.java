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

import static com.quartercode.classmod.extra.func.Priorities.LEVEL_7;
import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.ValueFactory;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.comp.user.Group;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;

/**
 * This class represents a file on a {@link FileSystem}.
 * Every file knows its name and can resolve its path.
 * There are different variants of a file: A {@link ContentFile} holds content, a {@link ParentFile} holds other files.
 * 
 * @param <P> The type of the parent {@link CFeatureHolder} which houses the file somehow.
 * @see ContentFile
 * @see ParentFile
 * @see FileSystem
 */
public abstract class File<P extends CFeatureHolder> extends WorldChildFeatureHolder<P> implements DerivableSize {

    /**
     * The default {@link FileRights} string for every new file.
     * Note that this object is not allowed to be modified.
     * If you wish to obtain an instance of the default file rights, please use the {@link FileRights#FileRights(FileRights)} copy constructor.
     */
    // TODO: Make the default {@link FileRights} dynamic
    public static final FileRights                           DEFAULT_FILE_RIGHTS = new FileRights("o:r,u:dw");

    // ----- Properties -----

    /**
     * The name of the file.
     */
    public static final PropertyDefinition<String>           NAME;

    /**
     * The {@link FileRights} object that stores which user groups are allowed to do which operations on the file.
     * See the {@link FileRights} class for more documentation on how it works.
     */
    public static final PropertyDefinition<FileRights>       RIGHTS;

    /**
     * The {@link User} who owns the file.
     * This is important for the {@link FileRights} system.
     */
    public static final PropertyDefinition<User>             OWNER;

    /**
     * The {@link Group} which partly owns the file.
     * This is important for the {@link FileRights} system.
     */
    public static final PropertyDefinition<Group>            GROUP;

    static {

        NAME = factory(PropertyDefinitionFactory.class).create("name", new StandardStorage<>());
        NAME.addSetterExecutor("trim", File.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                String name = (String) arguments[0];
                name = name.trim();

                return invocation.next(name);
            }

        }, LEVEL_7);

        RIGHTS = factory(PropertyDefinitionFactory.class).create("rights", new StandardStorage<>(), new ValueFactory<FileRights>() {

            @Override
            public FileRights get() {

                return new FileRights(DEFAULT_FILE_RIGHTS);
            }

        });

        OWNER = factory(PropertyDefinitionFactory.class).create("owner", new ReferenceStorage<>());
        GROUP = factory(PropertyDefinitionFactory.class).create("group", new ReferenceStorage<>());

    }

    // ----- Functions -----

    /**
     * Returns the local the path of the file.
     * The local path can be used to look up the file a on its {@link FileSystem}.<br>
     * A path is a collection of files seperated by a separator.
     */
    public static final FunctionDefinition<String>           GET_PATH;

    /**
     * Returns a {@link FileMoveAction} that moves the file to a new path on the same {@link FileSystem}.
     * In order to actually move the file, the {@link FileMoveAction#EXECUTE} method must be invoked.
     * Note that that method might throw exceptions if the file cannot be moved.<br>
     * <br>
     * If the new path does not exist, this method creates directories to match it.
     * Newly created directories have the same right settings as the file to move.
     * Furthermore, the name of the file to move is changed to match the new path.
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
     * @see FileMoveAction#EXECUTE
     */
    public static final FunctionDefinition<FileMoveAction>   CREATE_MOVE;

    /**
     * Returns a {@link FileMoveAction} that moves the file to a new path on the given {@link FileSystem}.
     * In order to actually move the file, the {@link FileMoveAction#EXECUTE} method must be invoked.
     * Note that that method might throw exceptions if the file cannot be moved.<br>
     * <br>
     * If the new path does not exist, this method creates directories to match it.
     * Newly created directories have the same right settings as the file to move.
     * Furthermore, the name of the file to move is changed to match the new path.
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
     * <tr>
     * <td>1</td>
     * <td>{@link FileSystem}</td>
     * <td>fileSystem</td>
     * <td>The new file system the file will be moved to. The path is located on this file system.</td>
     * </tr>
     * </table>
     * 
     * @see FileMoveAction#EXECUTE
     */
    public static final FunctionDefinition<FileMoveAction>   CREATE_MOVE_TO_OTHER_FS;

    /**
     * Returns an {@link FileRemoveAction} for removing the file.
     * If the file is a {@link ParentFile}, all child files are also going to be removed.
     * In order to actually remove the file from its current file system, the {@link FileRemoveAction#EXECUTE} method must be invoked.
     * Note that that method might throw exceptions if the given file cannot be added.
     * 
     * @see FileRemoveAction#EXECUTE
     */
    public static final FunctionDefinition<FileRemoveAction> CREATE_REMOVE;

    /**
     * Returns the {@link FileSystem} which is hosting the file.
     */
    public static final FunctionDefinition<FileSystem>       GET_FILE_SYSTEM;

    /**
     * Returns whether the given {@link User} has access to the given file right on the file.
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
     * <td>user</td>
     * <td>The user whose access to the given file right should be checked.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link Character}</td>
     * <td>right</td>
     * <td>The file right character that describes the right which should be checked.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Boolean>          HAS_RIGHT;

    /**
     * Returns whether the given {@link User} is allowed to change the {@link FileRights} attributes of the file.
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
     * <td>user</td>
     * <td>The user whose ability to change the file rights should be checked.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Boolean>          CAN_CHANGE_RIGHTS;

    static {

        GET_PATH = factory(FunctionDefinitionFactory.class).create("getPath", new Class[0]);
        GET_PATH.addExecutor("default", File.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                File<?> holder = (File<?>) invocation.getCHolder();
                String path = null;
                // Check for removed files
                if (holder.getParent() != null) {
                    String parentPath = holder.getParent().invoke(GET_PATH);
                    path = parentPath + (parentPath.isEmpty() ? "" : PathUtils.SEPARATOR) + holder.getObj(NAME);
                }

                invocation.next(arguments);
                return path;
            }

        });

        CREATE_MOVE = factory(FunctionDefinitionFactory.class).create("createMove", new Class[] { String.class });
        CREATE_MOVE.addExecutor("default", File.class, new FunctionExecutor<FileMoveAction>() {

            @Override
            public FileMoveAction invoke(FunctionInvocation<FileMoveAction> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                String path = (String) arguments[0];
                FileSystem fileSystem = holder.invoke(GET_FILE_SYSTEM);
                FileMoveAction action = holder.invoke(CREATE_MOVE_TO_OTHER_FS, path, fileSystem);

                invocation.next(arguments);
                return action;
            }

        });

        CREATE_MOVE_TO_OTHER_FS = factory(FunctionDefinitionFactory.class).create("createMoveToOtherFs", new Class[] { String.class, FileSystem.class });
        CREATE_MOVE_TO_OTHER_FS.addExecutor("default", File.class, new FunctionExecutor<FileMoveAction>() {

            @SuppressWarnings ("unchecked")
            @Override
            public FileMoveAction invoke(FunctionInvocation<FileMoveAction> invocation, Object... arguments) {

                FileMoveAction action = new FileMoveAction();
                action.setObj(FileMoveAction.FILE_SYSTEM, (FileSystem) arguments[1]);
                action.setObj(FileMoveAction.FILE, (File<ParentFile<?>>) invocation.getCHolder());
                action.setObj(FileMoveAction.PATH, (String) arguments[0]);

                invocation.next(arguments);
                return action;
            }

        });

        CREATE_REMOVE = factory(FunctionDefinitionFactory.class).create("createRemove", new Class[0]);
        CREATE_REMOVE.addExecutor("default", File.class, new FunctionExecutor<FileRemoveAction>() {

            @Override
            @SuppressWarnings ("unchecked")
            public FileRemoveAction invoke(FunctionInvocation<FileRemoveAction> invocation, Object... arguments) {

                FileRemoveAction action = new FileRemoveAction();
                action.setObj(FileAddAction.FILE, (File<ParentFile<?>>) invocation.getCHolder());

                invocation.next(arguments);
                return action;
            }

        });

        GET_FILE_SYSTEM = factory(FunctionDefinitionFactory.class).create("getFileSystem", new Class[0]);
        GET_FILE_SYSTEM.addExecutor("default", File.class, new FunctionExecutor<FileSystem>() {

            @Override
            public FileSystem invoke(FunctionInvocation<FileSystem> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                FileSystem fileSystem = null;
                if (holder instanceof RootFile) {
                    fileSystem = ((RootFile) holder).getParent();
                } else if (holder instanceof File && ((File<?>) holder).getParent() != null) {
                    fileSystem = ((File<?>) holder).getParent().invoke(GET_FILE_SYSTEM);
                }

                invocation.next(arguments);
                return fileSystem;
            }

        });

        HAS_RIGHT = factory(FunctionDefinitionFactory.class).create("hasRight", new Class[] { User.class, Character.class });
        HAS_RIGHT.addExecutor("default", File.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                User user = (User) arguments[0];
                char right = (char) arguments[1];

                boolean result;
                if (user == null || user.invoke(User.IS_SUPERUSER)) {
                    result = true;
                } else if (holder instanceof RootFile) {
                    // Only superusers (filtered out by the previous check) can add files to the root file
                    result = false;
                } else if (checkRight(holder, FileRights.OWNER, right) && holder.getObj(File.OWNER).equals(user)) {
                    result = true;
                } else if (checkRight(holder, FileRights.GROUP, right) && user.getColl(User.GROUPS).contains(holder.getObj(File.GROUP))) {
                    result = true;
                } else if (checkRight(holder, FileRights.OTHERS, right)) {
                    result = true;
                } else {
                    result = false;
                }

                invocation.next(arguments);
                return result;
            }

            private boolean checkRight(CFeatureHolder file, char accessor, char right) {

                return file.getObj(File.RIGHTS).isRightSet(accessor, right);
            }

        });

        CAN_CHANGE_RIGHTS = factory(FunctionDefinitionFactory.class).create("canChangeRights", new Class[] { User.class });
        CAN_CHANGE_RIGHTS.addExecutor("default", File.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                User user = (User) arguments[0];

                boolean result = holder.getObj(File.OWNER).equals(user) || user.invoke(User.IS_SUPERUSER);

                invocation.next(arguments);
                return result;
            }

        });

        GET_SIZE.addExecutor("name", File.class, SizeUtils.createGetSize(NAME));

    }

}
