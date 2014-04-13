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
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory.CriteriumMatcher;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
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
     * The child {@link File}s the parent file contains.<br>
     * <br>
     * Exceptions that can occur when adding:
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
    public static final CollectionPropertyDefinition<File<ParentFile<?>>, List<File<ParentFile<?>>>> CHILDREN;

    static {

        CHILDREN = ObjectCollectionProperty.createDefinition("children", new ArrayList<File<ParentFile<?>>>(), true);
        CHILDREN.addAdderExecutor("checkSize", ParentFile.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FileSystem fileSystem = invocation.getHolder().get(GET_FILE_SYSTEM).invoke();
                if (fileSystem != null) {
                    int totalSize = 0;
                    totalSize += SizeUtil.getSize(arguments[0]);
                    if (totalSize > fileSystem.get(FileSystem.GET_FREE).invoke()) {
                        throw new ExecutorInvocationException(new OutOfSpaceException(fileSystem, totalSize));
                    }
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

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
    public static final FunctionDefinition<File<ParentFile<?>>>                                      GET_CHILD_BY_NAME;

    static {

        GET_CHILD_BY_NAME = FunctionDefinitionFactory.create("getChildByName", ParentFile.class, CollectionPropertyAccessorFactory.createGetSingle(CHILDREN, new CriteriumMatcher<File<ParentFile<?>>>() {

            @Override
            public boolean matches(File<ParentFile<?>> element, Object... arguments) throws ExecutorInvocationException {

                return element.get(NAME).get().equals(arguments[0]);
            }

        }), String.class);

        GET_SIZE.addExecutor("children", ParentFile.class, SizeUtil.createGetSize(CHILDREN));

    }

    /**
     * Creates a new parent file.
     */
    public ParentFile() {

    }

}
