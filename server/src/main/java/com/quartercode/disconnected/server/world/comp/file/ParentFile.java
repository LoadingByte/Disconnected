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
import static com.quartercode.classmod.extra.Priorities.LEVEL_6;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory.CriteriumMatcher;
import com.quartercode.disconnected.server.world.util.SizeUtils;

/**
 * This class represents a parent file.
 * Parent files contain and hold other {@link File}s.
 * 
 * @param <P> The type of the parent {@link CFeatureHolder} which houses the parent file somehow.
 * @see File
 * @see FileSystem
 */
public class ParentFile<P extends CFeatureHolder> extends File<P> {

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

        CHILDREN = create(new TypeLiteral<CollectionPropertyDefinition<File<ParentFile<?>>, List<File<ParentFile<?>>>>>() {}, "name", "children", "storage", new StandardStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));
        CHILDREN.addAdderExecutor("checkSize", ParentFile.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileSystem fileSystem = invocation.getCHolder().invoke(GET_FILE_SYSTEM);
                if (fileSystem != null) {
                    long fileSize = ((File<?>) arguments[0]).invoke(File.GET_SIZE);
                    if (fileSize > fileSystem.invoke(FileSystem.GET_FREE)) {
                        throw new OutOfSpaceException(fileSystem, fileSize);
                    }
                }

                return invocation.next(arguments);
            }

        }, LEVEL_6);

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

        GET_CHILD_BY_NAME = create(new TypeLiteral<FunctionDefinition<File<ParentFile<?>>>>() {}, "name", "getChildByName", "parameters", new Class[] { String.class });
        GET_CHILD_BY_NAME.addExecutor("default", ParentFile.class, CollectionPropertyAccessorFactory.createGetSingle(CHILDREN, new CriteriumMatcher<File<ParentFile<?>>>() {

            @Override
            public boolean matches(File<ParentFile<?>> element, Object... arguments) {

                return element.getObj(NAME).equals(arguments[0]);
            }

        }));

        GET_SIZE.addExecutor("children", ParentFile.class, SizeUtils.createGetSize(CHILDREN));

    }

}
