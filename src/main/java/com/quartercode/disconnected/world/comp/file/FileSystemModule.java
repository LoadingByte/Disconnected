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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.Validate;
import com.quartercode.classmod.base.Feature;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.DefaultChildFeatureHolder;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory.CriteriumMatcher;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.hardware.Hardware;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.OSModule;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.program.Process;

/**
 * This class represents an {@link OperatingSystem} module which is used to access the available {@link FileSystem}s.
 * It is an essential part of the {@link OperatingSystem} and is directly used by it.
 * 
 * @see FileSystem
 * @see File
 * @see OSModule
 * @see OperatingSystem
 */
public class FileSystemModule extends OSModule {

    // ----- Properties -----

    /**
     * The {@link FileSystem}s which are associated with a known mountpoint (e.g. "system") and can be mounted.
     * These representation objects are called {@link KnownFileSystem}s.<br>
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
    public static final CollectionPropertyDefinition<KnownFileSystem, List<KnownFileSystem>> KNOWN_FS;

    static {

        KNOWN_FS = ObjectCollectionProperty.createDefinition("knownFs", new ArrayList<KnownFileSystem>(), true);
        KNOWN_FS.addAdderExecutor("checkNotMounted", FileSystemModule.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                KnownFileSystem element = (KnownFileSystem) arguments[0];
                Validate.isTrue(!element.get(KnownFileSystem.MOUNTED).get(), "Can't register known file system while mounted");
                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    /**
     * Returns a {@link List} containing all available {@link FileSystem}s which are connected to the computer.
     * This uses different resources to collect the {@link FileSystem}s.
     */
    public static final FunctionDefinition<List<FileSystem>>                                 GET_AVAILABLE;

    /**
     * Returns the {@link KnownFileSystem} object representing the given {@link FileSystem}.
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
     * <td>The {@link FileSystem} the returned {@link KnownFileSystem} is representing.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<KnownFileSystem>                                  GET_KNOWN_BY_FILESYSTEM;

    /**
     * Returns a {@link List} containing all mounted {@link KnownFileSystem}s.
     * Only the {@link File}s of currently mounted file systems can be accessed.
     * If you want to get all {@link KnownFileSystem}s, take a look at {@link #GET_KNOWN}.
     */
    public static final FunctionDefinition<List<KnownFileSystem>>                            GET_MOUNTED;

    /**
     * Returns the mounted {@link KnownFileSystem} which is associated with the given mountpoint.
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
     * <td>The mountpoint of the returned mounted {@link KnownFileSystem}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<KnownFileSystem>                                  GET_MOUNTED_BY_MOUNTPOINT;

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
     * <td>The provided path is not absolute (it does not start with {@link File#SEPARATOR}).</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<File<?>>                                          GET_FILE;

    /**
     * Returns a {@link FileAction} for adding a file with the given parameters.
     * In order to actually add the file, the {@link FileAction#EXECUTE} method must be invoked.
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
     * <td>The provided path for new new file is not absolute (it does not start with {@link File#SEPARATOR}).</td>
     * </tr>
     * <tr>
     * <td>{@link IllegalStateException}</td>
     * <td>The file system for the path cannot be found or is not mounted.</td>
     * </tr>
     * </table>
     * 
     * @see FileAction#EXECUTE
     */
    public static final FunctionDefinition<FileAction>                                       CREATE_ADD_FILE;

    static {

        GET_AVAILABLE = FunctionDefinitionFactory.create("getAvailable", FileSystemModule.class, new FunctionExecutor<List<FileSystem>>() {

            @Override
            public List<FileSystem> invoke(FunctionInvocation<List<FileSystem>> invocation, Object... arguments) {

                List<FileSystem> available = new ArrayList<FileSystem>();
                Computer computer = ((FileSystemModule) invocation.getHolder()).getParent().get(Process.GET_OPERATING_SYSTEM).invoke().getParent();
                for (Hardware hardware : computer.get(Computer.HARDWARE).get()) {
                    for (Feature feature : hardware) {
                        if (feature instanceof Iterable) {
                            for (Object child : (Iterable<?>) feature) {
                                if (child instanceof FileSystem) {
                                    available.add((FileSystem) child);
                                }
                            }
                        }
                    }
                }

                invocation.next(arguments);
                return available;
            }

        });

        GET_KNOWN_BY_FILESYSTEM = FunctionDefinitionFactory.create("getKnownByFilesystem", FileSystemModule.class, CollectionPropertyAccessorFactory.createGetSingle(KNOWN_FS, new CriteriumMatcher<KnownFileSystem>() {

            @Override
            public boolean matches(KnownFileSystem element, Object... arguments) {

                return element.get(KnownFileSystem.FILE_SYSTEM).get().equals(arguments[0]);
            }

        }), FileSystem.class);

        GET_MOUNTED = FunctionDefinitionFactory.create("getMounted", FileSystemModule.class, CollectionPropertyAccessorFactory.createGet(KNOWN_FS, new CriteriumMatcher<KnownFileSystem>() {

            @Override
            public boolean matches(KnownFileSystem element, Object... arguments) {

                return element.get(KnownFileSystem.MOUNTED).get();
            }

        }));
        GET_MOUNTED_BY_MOUNTPOINT = FunctionDefinitionFactory.create("getMountedByMountpoint", FileSystemModule.class, CollectionPropertyAccessorFactory.createGetSingle(KNOWN_FS, new CriteriumMatcher<KnownFileSystem>() {

            @Override
            public boolean matches(KnownFileSystem element, Object... arguments) {

                return element.get(KnownFileSystem.MOUNTED).get() && element.get(KnownFileSystem.MOUNTPOINT).get().equals(arguments[0]);
            }

        }), String.class);

        GET_FILE = FunctionDefinitionFactory.create("getFile", FileSystemModule.class, new FunctionExecutor<File<?>>() {

            @Override
            public File<?> invoke(FunctionInvocation<File<?>> invocation, Object... arguments) {

                String path = (String) arguments[0];
                String[] pathComponents = FileUtils.getComponents(path);
                Validate.isTrue(pathComponents[0] != null && pathComponents[1] != null, "Must provide an absolute path");

                FileSystem fileSystem = invocation.getHolder().get(GET_MOUNTED_BY_MOUNTPOINT).invoke(pathComponents[0]).get(KnownFileSystem.FILE_SYSTEM).get();
                File<?> result = null;
                if (fileSystem != null) {
                    result = fileSystem.get(FileSystem.GET_FILE).invoke(pathComponents[1]);
                }

                invocation.next(arguments);
                return result;
            }

        }, String.class);
        CREATE_ADD_FILE = FunctionDefinitionFactory.create("createAddFile", FileSystemModule.class, new FunctionExecutor<FileAction>() {

            @Override
            public FileAction invoke(FunctionInvocation<FileAction> invocation, Object... arguments) {

                File<?> file = (File<?>) arguments[0];
                String path = (String) arguments[1];
                FileAction action = null;

                String[] pathComponents = FileUtils.getComponents(path);
                Validate.isTrue(pathComponents[0] != null && pathComponents[1] != null, "Must provide an absolute path");

                FileSystem fileSystem = invocation.getHolder().get(GET_MOUNTED_BY_MOUNTPOINT).invoke(pathComponents[0]).get(KnownFileSystem.FILE_SYSTEM).get();
                if (fileSystem != null) {
                    action = fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, pathComponents[1]);
                } else {
                    throw new IllegalStateException("No mounted file system with mountpoint '" + pathComponents[0] + "'");
                }

                invocation.next(arguments);
                return action;
            }

        }, File.class, String.class);

        SET_RUNNING.addExecutor("mountSystemFs", FileSystemModule.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                // Only invoke on bootstrap
                if ((Boolean) arguments[0]) {
                    for (KnownFileSystem fileSystem : invocation.getHolder().get(KNOWN_FS).get()) {
                        if (fileSystem.get(KnownFileSystem.MOUNTPOINT).get().equals(CommonFiles.SYSTEM_MOUNTPOINT)) {
                            fileSystem.get(KnownFileSystem.MOUNTED).set(true);
                            break;
                        }
                    }
                }

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new file system module.
     */
    public FileSystemModule() {

    }

    /**
     * The known file system represents a {@link FileSystem} which can mounted into an operating system because it's mountpoint is known.
     * This is used in a datastructure of the {@link FileSystemModule} to store the mountpoints of {@link FileSystem}s.
     * A mountpoint is a string like "system". To get the root {@link File} of a mounted {@link FileSystem} with that mountpoint, you can use "/system".
     * 
     * @see FileSystem
     */
    public static class KnownFileSystem extends DefaultChildFeatureHolder<FileSystemModule> {

        // ----- Properties -----

        /**
         * The {@link FileSystem} which is represented by the data structure object.
         */
        public static final PropertyDefinition<FileSystem> FILE_SYSTEM;

        /**
         * The mountpoint the represented {@link FileSystem} is using.
         * A mountpoint is a string like "system" which defines where you can find the {@link FileSystem}.<br>
         * <br>
         * Exceptions that can occur when setting:
         * 
         * <table>
         * <tr>
         * <th>Exception</th>
         * <th>When?</th>
         * </tr>
         * <tr>
         * <td>{@link IllegalArgumentException}</td>
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

            FILE_SYSTEM = ReferenceProperty.createDefinition("fileSystem");

            MOUNTPOINT = ObjectProperty.createDefinition("mountpoint");
            MOUNTPOINT.addSetterExecutor("checkNotMounted", KnownFileSystem.class, new FunctionExecutor<Void>() {

                @Override
                @Prioritized (Prioritized.LEVEL_6)
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    Validate.isTrue(!invocation.getHolder().get(MOUNTED).get(), "Can't change mountpoint of known file system while mounted");
                    return invocation.next(arguments);
                }

            });

            MOUNTED = ObjectProperty.createDefinition("mounted", false, false);
            MOUNTED.addSetterExecutor("checkMountpointNotTaken", KnownFileSystem.class, new FunctionExecutor<Void>() {

                @Override
                @Prioritized (Prioritized.LEVEL_6)
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    FeatureHolder holder = invocation.getHolder();
                    FileSystemModule parent = ((KnownFileSystem) holder).getParent();
                    if ((Boolean) arguments[0] && parent.get(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT).invoke(holder.get(MOUNTPOINT).get()) != null) {
                        throw new IllegalStateException("Other known file system with same mountpoint already mounted");
                    }

                    return invocation.next(arguments);
                }

            });

        }

        /**
         * Creates a new known file system representation object.
         */
        public KnownFileSystem() {

            setParentType(FileSystemModule.class);
        }

    }

}
