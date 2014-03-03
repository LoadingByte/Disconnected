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

package com.quartercode.disconnected.world.comp.os;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.mocl.base.Feature;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.def.DefaultChildFeatureHolder;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.extra.def.ReferenceProperty;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory.CriteriumMatcher;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.hardware.Hardware;
import com.quartercode.disconnected.world.comp.program.Process;

/**
 * This class represents a kernel module which is used to access the available {@link FileSystem}s.
 * It is an essential part of the {@link OperatingSystem} and directly used by it.
 * 
 * @see FileSystem
 * @see File
 * @see OperatingSystem
 */
public class FileSystemModule extends DefaultChildFeatureHolder<OperatingSystem> {

    // ----- Properties -----

    /**
     * The {@link FileSystem}s which are associated with a known mountpoint and can be mounted.
     * These representation objects are called {@link KnownFileSystem}s.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<KnownFileSystem>>> KNOWN_FILE_SYSTEMS;

    static {

        KNOWN_FILE_SYSTEMS = new AbstractFeatureDefinition<ObjectProperty<Set<KnownFileSystem>>>("knownFileSytems") {

            @Override
            public ObjectProperty<Set<KnownFileSystem>> create(FeatureHolder holder) {

                return new ObjectProperty<Set<KnownFileSystem>>(getName(), holder, new HashSet<KnownFileSystem>());
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns a {@link Set} containing all available {@link FileSystem}s which are connected to the computer.
     * This uses different resources to collect the {@link FileSystem}s.
     */
    public static final FunctionDefinition<Set<FileSystem>>                        GET_AVAIABLE;

    /**
     * Returns a {@link Set} containing all {@link KnownFileSystem}s.
     * A {@link KnownFileSystem} can be mounted and associated with a set mountpoint (e.g. "system").
     * Only {@link KnownFileSystem}s can be mounted.
     */
    public static final FunctionDefinition<Set<KnownFileSystem>>                   GET_KNOWN;

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
    public static final FunctionDefinition<KnownFileSystem>                        GET_KNOWN_BY_FILESYSTEM;

    /**
     * Registers some {@link KnownFileSystem}s to the {@link OperatingSystem}.
     * The new {@link KnownFileSystem} can't be mounted already.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link KnownFileSystem}...</td>
     * <td>knownFileSystems</td>
     * <td>The {@link KnownFileSystem}s to register to the system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                   ADD_KNOWN;

    /**
     * Unregisters some {@link KnownFileSystem}s to the {@link OperatingSystem}.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link KnownFileSystem}...</td>
     * <td>knownFileSystems</td>
     * <td>The {@link KnownFileSystem}s to unregister from the system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                   REMOVE_KNOWN;

    /**
     * Returns a {@link Set} containing all mounted {@link KnownFileSystem}s.
     * Only the {@link File}s of currently mounted file systems can be accessed.
     * If you want to get all {@link KnownFileSystem}s, take a look at {@link #GET_KNOWN}.
     */
    public static final FunctionDefinition<Set<KnownFileSystem>>                   GET_MOUNTED;

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
    public static final FunctionDefinition<KnownFileSystem>                        GET_MOUNTED_BY_MOUNTPOINT;

    static {

        GET_AVAIABLE = FunctionDefinitionFactory.create("getAvaiable", FileSystemModule.class, new FunctionExecutor<Set<FileSystem>>() {

            @Override
            public Set<FileSystem> invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                Set<FileSystem> available = new HashSet<FileSystem>();
                Computer computer = ((FileSystemModule) holder).getParent().get(Process.GET_ROOT).invoke().getParent().getParent();
                for (Hardware hardware : computer.get(Computer.GET_HARDWARE).invoke()) {
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

                return available;
            }

        });

        GET_KNOWN = FunctionDefinitionFactory.create("getKnown", FileSystemModule.class, CollectionPropertyAccessorFactory.createGet(KNOWN_FILE_SYSTEMS));
        GET_KNOWN_BY_FILESYSTEM = FunctionDefinitionFactory.create("getKnownByFilesystem", FileSystemModule.class, CollectionPropertyAccessorFactory.createGetSingle(KNOWN_FILE_SYSTEMS, new CriteriumMatcher<KnownFileSystem>() {

            @Override
            public boolean matches(KnownFileSystem element, Object... arguments) throws ExecutorInvokationException {

                return element.get(KnownFileSystem.GET_FILE_SYSTEM).invoke().equals(arguments[0]);
            }

        }), FileSystem.class);
        ADD_KNOWN = FunctionDefinitionFactory.create("addKnown", FileSystemModule.class, CollectionPropertyAccessorFactory.createAdd(KNOWN_FILE_SYSTEMS), KnownFileSystem[].class);
        ADD_KNOWN.addExecutor(FileSystemModule.class, "checkNotMounted", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                for (Object element : arguments) {
                    Validate.isTrue(! ((KnownFileSystem) element).get(KnownFileSystem.IS_MOUNTED).invoke(), "Can't register known file system while mounted");
                }
                return null;
            }

        });
        REMOVE_KNOWN = FunctionDefinitionFactory.create("removeKnown", FileSystemModule.class, CollectionPropertyAccessorFactory.createRemove(KNOWN_FILE_SYSTEMS), KnownFileSystem[].class);

        GET_MOUNTED = FunctionDefinitionFactory.create("getMounted", FileSystemModule.class, CollectionPropertyAccessorFactory.createGet(KNOWN_FILE_SYSTEMS, new CriteriumMatcher<KnownFileSystem>() {

            @Override
            public boolean matches(KnownFileSystem element, Object... arguments) throws ExecutorInvokationException {

                return element.get(KnownFileSystem.IS_MOUNTED).invoke();
            }

        }));
        GET_MOUNTED_BY_MOUNTPOINT = FunctionDefinitionFactory.create("getMountedByMountpoint", FileSystemModule.class, CollectionPropertyAccessorFactory.createGetSingle(KNOWN_FILE_SYSTEMS, new CriteriumMatcher<KnownFileSystem>() {

            @Override
            public boolean matches(KnownFileSystem element, Object... arguments) throws ExecutorInvokationException {

                return element.get(KnownFileSystem.IS_MOUNTED).invoke() && element.get(KnownFileSystem.GET_MOUNTPOINT).invoke().equals(arguments[0]);
            }

        }), String.class);

    }

    // ----- Functions End -----

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
        protected static final FeatureDefinition<ReferenceProperty<FileSystem>> FILE_SYSTEM;

        /**
         * The mountpoint the represented {@link FileSystem} is using.
         * A mountpoint is a string like "system" which defines where you can find the {@link FileSystem}.
         */
        protected static final FeatureDefinition<ObjectProperty<String>>        MOUNTPOINT;

        /**
         * If the represented {@link FileSystem} is actually mounted to the set {@link #MOUNTPOINT}.
         */
        protected static final FeatureDefinition<ObjectProperty<Boolean>>       MOUNTED;

        static {

            FILE_SYSTEM = new AbstractFeatureDefinition<ReferenceProperty<FileSystem>>("fileSystem") {

                @Override
                public ReferenceProperty<FileSystem> create(FeatureHolder holder) {

                    return new ReferenceProperty<FileSystem>(getName(), holder);
                }

            };

            MOUNTPOINT = new AbstractFeatureDefinition<ObjectProperty<String>>("mountpoint") {

                @Override
                public ObjectProperty<String> create(FeatureHolder holder) {

                    return new ObjectProperty<String>(getName(), holder);
                }

            };

            MOUNTED = new AbstractFeatureDefinition<ObjectProperty<Boolean>>("mounted") {

                @Override
                public ObjectProperty<Boolean> create(FeatureHolder holder) {

                    return new ObjectProperty<Boolean>(getName(), holder, false);
                }

            };

        }

        // ----- Properties End -----

        // ----- Functions -----

        /**
         * Returns the {@link FileSystem} which is represented by the data structure object.
         */
        public static final FunctionDefinition<FileSystem>                      GET_FILE_SYSTEM;

        /**
         * Changes the {@link FileSystem} which is represented by the data structure object.
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
         * <td>The new {@link FileSystem} the data structure object represents.</td>
         * </tr>
         * </table>
         */
        public static final FunctionDefinition<Void>                            SET_FILE_SYSTEM;

        /**
         * Returns the mountpoint the represented {@link FileSystem} is using.
         * A mountpoint is a string like "system" which defines where you can find the {@link FileSystem}.
         */
        public static final FunctionDefinition<String>                          GET_MOUNTPOINT;

        /**
         * Changes the mountpoint the represented {@link FileSystem} is using.
         * A mountpoint is a string like "system" which defines where you can find the {@link FileSystem}.
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
         * <td>The new mountpoint for the represented {@link FileSystem}.</td>
         * </tr>
         * </table>
         */
        public static final FunctionDefinition<Void>                            SET_MOUNTPOINT;

        /**
         * Returns true if the represented {@link FileSystem} is actually mounted to the set {@link #MOUNTPOINT}.
         */
        public static final FunctionDefinition<Boolean>                         IS_MOUNTED;

        /**
         * Changes if the represented {@link FileSystem} is actually mounted to the set {@link #MOUNTPOINT}.
         * That process is also called mounting/unmounting.
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
         * <td>{@link Boolean}</td>
         * <td>mounted</td>
         * <td>True if the represented {@link FileSystem} is mounted, false if not.</td>
         * </tr>
         * </table>
         */
        public static final FunctionDefinition<Void>                            SET_MOUNTED;

        static {

            GET_FILE_SYSTEM = FunctionDefinitionFactory.create("getFileSystem", KnownFileSystem.class, PropertyAccessorFactory.createGet(FILE_SYSTEM));
            SET_FILE_SYSTEM = FunctionDefinitionFactory.create("setFileSystem", KnownFileSystem.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(FILE_SYSTEM)), FileSystem.class);

            GET_MOUNTPOINT = FunctionDefinitionFactory.create("getMountpoint", KnownFileSystem.class, PropertyAccessorFactory.createGet(MOUNTPOINT));
            SET_MOUNTPOINT = FunctionDefinitionFactory.create("setMountpoint", KnownFileSystem.class, PropertyAccessorFactory.createSet(MOUNTPOINT), String.class);
            SET_MOUNTPOINT.addExecutor(KnownFileSystem.class, "checkNotMounted", new FunctionExecutor<Void>() {

                @Override
                @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
                public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                    Validate.isTrue(!holder.get(IS_MOUNTED).invoke(), "Can't change mountpoint of known file system while mounted");
                    return null;
                }

            });

            IS_MOUNTED = FunctionDefinitionFactory.create("isMounted", KnownFileSystem.class, PropertyAccessorFactory.createGet(MOUNTED));
            SET_MOUNTED = FunctionDefinitionFactory.create("setMounted", KnownFileSystem.class, PropertyAccessorFactory.createSet(MOUNTED), Boolean.class);
            SET_MOUNTED.addExecutor(KnownFileSystem.class, "checkMountpointNotTaken", new FunctionExecutor<Void>() {

                @Override
                @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
                public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                    FileSystemModule parent = ((KnownFileSystem) holder).getParent();
                    Validate.isTrue(parent.get(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT).invoke(holder.get(GET_MOUNTED).invoke()) == null, "Other known file system with same mountpoint already mounted");
                    return null;
                }

            });

        }

        // ----- Functions End -----

        /**
         * Creates a new known file system representation object.
         */
        public KnownFileSystem() {

        }

    }

}
