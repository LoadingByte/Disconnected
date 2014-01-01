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

import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.SizeUtil;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;

/**
 * This class represents a file system.
 * The system stores {@link File}s which can be accessed like regular file objects.
 * A file system can be virtual or physical.
 * 
 * @see File
 */
public class FileSystem extends DefaultFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The size of the file system, given in bytes.
     */
    protected static final FeatureDefinition<ObjectProperty<Long>>     SIZE;

    /**
     * The {@link RootFile} every other {@link File} branches of somehow.
     */
    protected static final FeatureDefinition<ObjectProperty<RootFile>> ROOT;

    static {

        SIZE = new AbstractFeatureDefinition<ObjectProperty<Long>>("size") {

            @Override
            public ObjectProperty<Long> create(FeatureHolder holder) {

                return new ObjectProperty<Long>(getName(), holder);
            }

        };

        ROOT = new AbstractFeatureDefinition<ObjectProperty<RootFile>>("root") {

            @Override
            public ObjectProperty<RootFile> create(FeatureHolder holder) {

                RootFile root = new RootFile();
                root.setParent((FileSystem) holder);
                return new ObjectProperty<RootFile>(getName(), holder, root);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Changes the size of the file system, given in bytes.
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
     * <td>{@link Long}</td>
     * <td>size</td>
     * <td>The new size of the file system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                       SET_SIZE;

    /**
     * Returns the {@link RootFile} every other {@link File} branches of somehow.
     */
    public static final FunctionDefinition<RootFile>                   GET_ROOT;

    /**
     * Returns the {@link File} which is stored under the given path.
     * A path is a collection of {@link File}s seperated by a seperator.
     * This will look up the {@link File} using a local file system path.
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
     */
    public static final FunctionDefinition<File<?>>                    GET_FILE;

    /**
     * Adds the given {@link File} to the file system.
     * If the given path doesn't exist, this creates directories to match it.
     * The name of the {@link File} and the parent object will be changed to match the path.
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
     * <td>{@link File}</td>
     * <td>file</td>
     * <td>The {@link File} to add to the file system.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link String}</td>
     * <td>path</td>
     * <td>The path for the new {@link File}. The name of the {@link File} will be changed to the last entry.</td>
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
     * <td>There is not enough space for the new {@link File}.</td>
     * </tr>
     * <tr>
     * <td>{@link IllegalStateException}</td>
     * <td>The given file path isn't valid.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                       ADD_FILE;

    /**
     * Returns the total amount of bytes which are occupied by {@link File}s on the file system.
     */
    public static final FunctionDefinition<Long>                       GET_FILLED;

    /**
     * Returns the total amount of bytes which are not occupied by {@link File}s on the file system.
     */
    public static final FunctionDefinition<Long>                       GET_FREE;

    static {

        GET_SIZE.addExecutor(FileSystem.class, "fileSystemSize", PropertyAccessorFactory.createGet(SIZE));
        SET_SIZE = FunctionDefinitionFactory.create("setSize", FileSystem.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(SIZE)), Long.class);

        GET_ROOT = FunctionDefinitionFactory.create("getRoot", FileSystem.class, PropertyAccessorFactory.createGet(ROOT));

        GET_FILE = FunctionDefinitionFactory.create("getFile", FileSystem.class, new FunctionExecutor<File<?>>() {

            @Override
            public File<?> invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                String[] parts = ((String) arguments[0]).split(File.SEPERATOR);
                File<?> current = holder.get(GET_ROOT).invoke();
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        if (current instanceof ParentFile) {
                            current = current.get(ParentFile.GET_CHILD_BY_NAME).invoke(part);
                            if (current == null) {
                                break;
                            }
                        }
                    }
                }

                return current;
            }

        }, String.class);

        ADD_FILE = FunctionDefinitionFactory.create("addFile", FileSystem.class, new FunctionExecutor<Void>() {

            @Override
            @SuppressWarnings ("unchecked")
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                String[] parts = ((String) arguments[1]).split(File.SEPERATOR);
                File<?> current = holder.get(GET_ROOT).invoke();
                File<FeatureHolder> file = (File<FeatureHolder>) arguments[0];
                for (int counter = 0; counter < parts.length; counter++) {
                    String part = parts[counter];
                    if (!part.isEmpty()) {
                        if (current.get(ParentFile.GET_CHILD_BY_NAME).invoke(part) == null) {
                            if (counter == parts.length - 1) {
                                file.get(File.SET_NAME).invoke(part);
                                current.get(ParentFile.ADD_CHILDREN).invoke(file);
                                return null;
                            } else {
                                Directory directory = new Directory();
                                directory.get(File.SET_NAME).invoke(part);
                                directory.get(File.SET_OWNER).invoke(file.get(File.GET_OWNER).invoke());
                                directory.get(File.SET_GROUP).invoke(file.get(File.GET_GROUP).invoke());
                                current.get(ParentFile.ADD_CHILDREN).invoke(directory);
                            }
                        }
                        current = current.get(ParentFile.GET_CHILD_BY_NAME).invoke(part);
                        if (! (current instanceof ParentFile)) {
                            throw new StopExecutionException(new IllegalStateException("File path '" + arguments[1] + "' isn't valid: A file along the way isn't a parent file"));
                        }
                    }
                }

                return null;
            }

        }, File.class, String.class);

        GET_FILLED = FunctionDefinitionFactory.create("getFilled", FileSystem.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return SizeUtil.getSize(holder.get(GET_ROOT).invoke());
            }

        });

        GET_FREE = FunctionDefinitionFactory.create("getFree", FileSystem.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return holder.get(GET_SIZE).invoke() - holder.get(GET_FILLED).invoke();
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new file system.
     */
    public FileSystem() {

    }

}
