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
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory.CriteriumMatcher;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.world.comp.SizeUtil;

/**
 * This class represents a parent file.
 * Parent files contain and hold other {@link File}s.
 * 
 * @param <P> The type of the parent {@link FeatureHolder} which houses the parent file somehow.
 * @see File
 * @see FileSystem
 */
public class ParentFile<P extends FeatureHolder> extends File<P> {

    // ----- Properties -----

    /**
     * The child {@link File}s the parent file contains.
     */
    protected static final FeatureDefinition<ObjectProperty<List<File<ParentFile<?>>>>> CHILDREN;

    static {

        CHILDREN = new AbstractFeatureDefinition<ObjectProperty<List<File<ParentFile<?>>>>>("children") {

            @Override
            public ObjectProperty<List<File<ParentFile<?>>>> create(FeatureHolder holder) {

                return new ObjectProperty<List<File<ParentFile<?>>>>(getName(), holder, new ArrayList<File<ParentFile<?>>>());
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the child {@link File}s the parent file contains.
     */
    public static final FunctionDefinition<List<File<ParentFile<?>>>>                   GET_CHILDREN;

    /**
     * Returns the child {@link File} which has the given name.
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
     * <td>The name of the {@link File} to return.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<File<ParentFile<?>>>                         GET_CHILD_BY_NAME;

    /**
     * Adds child {@link File}s to the parent file.
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
     * <td>{@link File}...</td>
     * <td>files</td>
     * <td>The child {@link File}s to add to the parent file.</td>
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
     * <td>There is not enough space for the new child {@link File}s.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                        ADD_CHILDREN;

    /**
     * Removes child {@link File}s from the parent file.
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
     * <td>{@link File}...</td>
     * <td>files</td>
     * <td>The child {@link File}s to remove from the parent file.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                        REMOVE_CHILDREN;

    static {

        GET_CHILDREN = FunctionDefinitionFactory.create("getChildren", ParentFile.class, CollectionPropertyAccessorFactory.createGet(CHILDREN));
        GET_CHILD_BY_NAME = FunctionDefinitionFactory.create("getChildByName", ParentFile.class, CollectionPropertyAccessorFactory.createGetSingle(CHILDREN, new CriteriumMatcher<File<ParentFile<?>>>() {

            @Override
            public boolean matches(File<ParentFile<?>> element, Object... arguments) throws ExecutorInvokationException {

                return element.get(GET_NAME).invoke().equals(arguments[0]);
            }

        }), String.class);
        ADD_CHILDREN = FunctionDefinitionFactory.create("addChildren", ParentFile.class, CollectionPropertyAccessorFactory.createAdd(CHILDREN), File[].class);
        ADD_CHILDREN.addExecutor(ParentFile.class, "checkSize", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                FileSystem fileSystem = holder.get(GET_FILE_SYSTEM).invoke();
                if (fileSystem != null) {
                    int totalSize = 0;
                    for (Object file : arguments) {
                        totalSize += SizeUtil.getSize(file);
                    }
                    if (totalSize > fileSystem.get(FileSystem.GET_FREE).invoke()) {
                        throw new StopExecutionException(new OutOfSpaceException(fileSystem, totalSize));
                    }
                }

                return null;
            }

        });
        ADD_CHILDREN.addExecutor(ParentFile.class, "setParent", new FunctionExecutor<Void>() {

            @Override
            @SuppressWarnings ("unchecked")
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                for (Object file : arguments) {
                    ((File<FeatureHolder>) file).setParent(holder);
                }

                return null;
            }

        });
        REMOVE_CHILDREN = FunctionDefinitionFactory.create("removeChildren", ParentFile.class, CollectionPropertyAccessorFactory.createRemove(CHILDREN), File[].class);
        REMOVE_CHILDREN.addExecutor(ParentFile.class, "removeParent", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                for (Object file : arguments) {
                    ((File<?>) file).setParent(null);
                }

                return null;
            }

        });

        GET_SIZE.addExecutor(ParentFile.class, "children", SizeUtil.createGetSize(CHILDREN));

    }

    // ----- Functions End -----

    /**
     * Creates a new parent file.
     */
    public ParentFile() {

    }

}
