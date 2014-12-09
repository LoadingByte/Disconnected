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

import static com.quartercode.classmod.extra.func.Priorities.LEVEL_6;
import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.classmod.factory.CollectionPropertyDefinitionFactory;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory.CriteriumMatcher;
import com.quartercode.disconnected.server.world.comp.os.OS;
import com.quartercode.disconnected.server.world.comp.os.OSModule;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;

/**
 * This class represents an {@link OS operating system} module which is used to access the available {@link FileSystem file systems}.
 * It is an essential part of the operating system and is directly used by it.
 * 
 * @see FileSystem
 * @see File
 * @see OSModule
 * @see OS
 */
public class FSModule extends OSModule {

    // ----- Properties -----

    /**
     * The {@link FileSystem} which are associated with a known mountpoint (e.g. "system") and can be mounted.
     * These representation objects are called {@link KnownFS known file systems}.<br>
     * <br>
     * Exceptions that can occur when adding:
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The new known file system that should be registered is already mounted.</td>
     * </tr>
     * </table>
     */
    public static final CollectionPropertyDefinition<KnownFS, List<KnownFS>> KNOWN_FS;

    static {

        KNOWN_FS = factory(CollectionPropertyDefinitionFactory.class).create("knownFs", new StandardStorage<>(), new CloneValueFactory<>(new ArrayList<>()));
        KNOWN_FS.addAdderExecutor("checkNotMounted", FSModule.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                KnownFS element = (KnownFS) arguments[0];
                Validate.isTrue(!element.getObj(KnownFS.MOUNTED), "Can't register known file system '%s' while it is mounted", element.getObj(KnownFS.MOUNTPOINT));
                return invocation.next(arguments);
            }

        }, LEVEL_6);

    }

    // ----- Functions -----

    /**
     * Returns the {@link KnownFS known file system} object representing the given {@link FileSystem}.
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
     * <td>{@link FileSystem}</td>
     * <td>fileSystem</td>
     * <td>The file system the returned known file system is representing.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<KnownFS>                          GET_KNOWN_BY_FS;

    /**
     * Returns a {@link List} containing all mounted {@link KnownFS known file systems}.
     * Only the {@link File}s of currently mounted file systems can be accessed.
     * If you want to get all {@link KnownFS known file systems}, take a look at {@link #KNOWN_FS}.
     */
    public static final FunctionDefinition<List<KnownFS>>                    GET_MOUNTED;

    /**
     * Returns the mounted {@link KnownFS known file system} which is associated with the given mountpoint.
     * Mountpoints are use to determine where you have to point for file path in order to access the {@link FileSystem}.
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
     * <td>mountpoint</td>
     * <td>The mountpoint of the returned mounted known file system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<KnownFS>                          GET_MOUNTED_BY_MOUNTPOINT;

    /**
     * Returns the {@link File} which is stored on a mounted {@link FileSystem} under the given path.
     * A path is a collection of {@link File}s seperated by a separator.
     * This will look up the {@link File} using a global os path.
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
     * <td>The path to search under.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The provided path is not absolute (it does not start with {@link PathUtils#SEPARATOR}).</td>
     * </tr>
     * <tr>
     * <td>{@link UnknownMountpointException}</td>
     * <td>The file system for the path cannot be found or is not mounted.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<File<?>>                          GET_FILE;

    /**
     * Returns a {@link FileAddAction} for adding a file with the given parameters.
     * In order to actually add the file, the {@link FileAddAction#EXECUTE} method must be invoked.
     * Note that that method might throw exceptions if the given file cannot be added.<br>
     * <br>
     * The returned action adds the given {@link File} to a mounted {@link FileSystem} under the given path.
     * If the path does not exist, this method creates directories to match it.<br>
     * <br>
     * The name of the file to add is changed to match the path.
     * Furthermore, newly created directories have the same right settings as the file to add.
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
     * <td>{@link File}&lt;{@link ParentFile}&lt;?&gt;&gt;</td>
     * <td>file</td>
     * <td>The file to add under the given path.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link String}</td>
     * <td>path</td>
     * <td>The path for the new file. The name of the file will be changed to the last entry.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The provided path for new new file is not absolute (it does not start with {@link PathUtils#SEPARATOR}).</td>
     * </tr>
     * <tr>
     * <td>{@link UnknownMountpointException}</td>
     * <td>The file system for the path cannot be found or is not mounted.</td>
     * </tr>
     * </table>
     * 
     * @see FileAddAction#EXECUTE
     */
    public static final FunctionDefinition<FileAddAction>                    CREATE_ADD_FILE;

    static {

        GET_KNOWN_BY_FS = factory(FunctionDefinitionFactory.class).create("getKnownByFS", new Class[] { FileSystem.class });
        GET_KNOWN_BY_FS.addExecutor("default", FSModule.class, CollectionPropertyAccessorFactory.createGetSingle(KNOWN_FS, new CriteriumMatcher<KnownFS>() {

            @Override
            public boolean matches(KnownFS element, Object... arguments) {

                return element.getObj(KnownFS.FILE_SYSTEM).equals(arguments[0]);
            }

        }));

        GET_MOUNTED = factory(FunctionDefinitionFactory.class).create("getMounted", new Class[0]);
        GET_MOUNTED.addExecutor("default", FSModule.class, CollectionPropertyAccessorFactory.createGet(KNOWN_FS, new CriteriumMatcher<KnownFS>() {

            @Override
            public boolean matches(KnownFS element, Object... arguments) {

                return element.getObj(KnownFS.MOUNTED);
            }

        }));
        GET_MOUNTED_BY_MOUNTPOINT = factory(FunctionDefinitionFactory.class).create("getMountedByMountpoint", new Class[] { String.class });
        GET_MOUNTED_BY_MOUNTPOINT.addExecutor("default", FSModule.class, CollectionPropertyAccessorFactory.createGetSingle(KNOWN_FS, new CriteriumMatcher<KnownFS>() {

            @Override
            public boolean matches(KnownFS element, Object... arguments) {

                return element.getObj(KnownFS.MOUNTED) && element.getObj(KnownFS.MOUNTPOINT).equals(arguments[0]);
            }

        }));

        GET_FILE = factory(FunctionDefinitionFactory.class).create("getFile", new Class[] { String.class });
        GET_FILE.addExecutor("default", FSModule.class, new FunctionExecutor<File<?>>() {

            @Override
            public File<?> invoke(FunctionInvocation<File<?>> invocation, Object... arguments) {

                String path = PathUtils.normalize((String) arguments[0]);
                String[] pathComponents = PathUtils.splitAfterMountpoint(path);
                Validate.isTrue(pathComponents[0] != null, "Must provide a path containing a mountpoint ('%s' is invalid)", path);

                KnownFS knownFs = invocation.getCHolder().invoke(GET_MOUNTED_BY_MOUNTPOINT, pathComponents[0]);
                if (knownFs == null) {
                    throw new UnknownMountpointException((FSModule) invocation.getCHolder(), pathComponents[0]);
                }

                FileSystem fileSystem = knownFs.getObj(KnownFS.FILE_SYSTEM);
                File<?> result = fileSystem.invoke(FileSystem.GET_FILE, pathComponents[1] == null ? "" : pathComponents[1]);

                invocation.next(arguments);
                return result;
            }

        });
        CREATE_ADD_FILE = factory(FunctionDefinitionFactory.class).create("createAddFile", new Class[] { File.class, String.class });
        CREATE_ADD_FILE.addExecutor("default", FSModule.class, new FunctionExecutor<FileAddAction>() {

            @Override
            public FileAddAction invoke(FunctionInvocation<FileAddAction> invocation, Object... arguments) {

                File<?> file = (File<?>) arguments[0];
                String path = PathUtils.normalize((String) arguments[1]);

                String[] pathComponents = PathUtils.splitAfterMountpoint(path);
                Validate.isTrue(pathComponents[0] != null && pathComponents[1] != null, "Must provide an absolute path ('%s' is invalid)", path);

                KnownFS knownFs = invocation.getCHolder().invoke(GET_MOUNTED_BY_MOUNTPOINT, pathComponents[0]);
                if (knownFs == null) {
                    throw new UnknownMountpointException((FSModule) invocation.getCHolder(), pathComponents[0]);
                }

                FileSystem fileSystem = knownFs.getObj(KnownFS.FILE_SYSTEM);
                FileAddAction action = fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, pathComponents[1]);

                invocation.next(arguments);
                return action;
            }

        });

        SET_RUNNING.addExecutor("mountSystemFs", FSModule.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                // Only invoke on bootstrap
                if ((Boolean) arguments[0]) {
                    for (KnownFS fileSystem : invocation.getCHolder().getColl(KNOWN_FS)) {
                        // TODO: Temp: Mount every available file system until an fs table is implemented
                        // if (fileSystem.getProp(KnownFileSystem.MOUNTPOINT).equals(CommonFiles.SYSTEM_MOUNTPOINT)) {
                        fileSystem.setObj(KnownFS.MOUNTED, true);
                        // break;
                        // }
                    }
                }

                return invocation.next(arguments);
            }

        });

    }

    /**
     * The known file system represents a {@link FileSystem} which can mounted into an operating system because it's mountpoint is known.
     * This is used in a data structure of the file system module to store the mountpoints of file systems.
     * A mountpoint is a string like "system". To get the root {@link File} of a mounted file systems with that mountpoint, you can use "/system".
     * 
     * @see FileSystem
     */
    public static class KnownFS extends WorldChildFeatureHolder<FSModule> {

        // ----- Properties -----

        /**
         * The {@link FileSystem} which is represented by the data structure object.
         */
        public static final PropertyDefinition<FileSystem> FILE_SYSTEM;

        /**
         * The mountpoint the represented {@link FileSystem} is using.
         * A mountpoint is a string like "system" which defines where you can find the file system.<br>
         * <br>
         * Exceptions that can occur when setting:
         * 
         * <table>
         * <tr>
         * <th>Exception</th>
         * <th>When?</th>
         * </tr>
         * <tr>
         * <td>{@link IllegalStateException}</td>
         * <td>The known file system is mounted while the mountpoint should be changed.</td>
         * </tr>
         * </table>
         */
        public static final PropertyDefinition<String>     MOUNTPOINT;

        /**
         * If the represented {@link FileSystem} is actually mounted to the set {@link #MOUNTPOINT}.<br>
         * <br>
         * Exceptions that can occur when setting:
         * 
         * <table>
         * <tr>
         * <th>Exception</th>
         * <th>When?</th>
         * </tr>
         * <tr>
         * <td>{@link IllegalStateException}</td>
         * <td>There is another known file system with the same mountpoint already mounted.</td>
         * </tr>
         * </table>
         */
        public static final PropertyDefinition<Boolean>    MOUNTED;

        static {

            FILE_SYSTEM = factory(PropertyDefinitionFactory.class).create("fileSystem", new ReferenceStorage<>());

            MOUNTPOINT = factory(PropertyDefinitionFactory.class).create("mountpoint", new StandardStorage<>());
            MOUNTPOINT.addSetterExecutor("checkNotMounted", KnownFS.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    CFeatureHolder holder = invocation.getCHolder();

                    Validate.validState(!holder.getObj(MOUNTED), "Can't change mountpoint of known file system '%s' while mounted", holder.getObj(MOUNTPOINT));

                    return invocation.next(arguments);
                }

            }, LEVEL_6);

            MOUNTED = factory(PropertyDefinitionFactory.class).create("mounted", new StandardStorage<>(), new ConstantValueFactory<>(false));
            MOUNTED.addSetterExecutor("checkMountpointFree", KnownFS.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    CFeatureHolder holder = invocation.getCHolder();
                    FSModule parent = ((KnownFS) holder).getParent();

                    if ((Boolean) arguments[0]) {
                        boolean mountpointFree = parent.invoke(FSModule.GET_MOUNTED_BY_MOUNTPOINT, holder.getObj(MOUNTPOINT)) == null;
                        Validate.validState(mountpointFree, "Other known file system with same mountpoint ('%s') already mounted", holder.getObj(MOUNTPOINT));
                    }

                    return invocation.next(arguments);
                }

            }, LEVEL_6);

        }

        /**
         * Creates a new known file system representation object.
         */
        public KnownFS() {

            setParentType(FSModule.class);
        }

    }

}
