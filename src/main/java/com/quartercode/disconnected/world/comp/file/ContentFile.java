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

import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.SizeUtil;

/**
 * This class represents a content file.
 * Content files can contain and store objects.
 * 
 * @see File
 * @see FileSystem
 */
public class ContentFile extends File<ParentFile<?>> {

    // ----- Properties -----

    /**
     * The content of the content file.
     */
    protected static final FeatureDefinition<ObjectProperty<Object>> CONTENT;

    static {

        CONTENT = ObjectProperty.createDefinition("content");

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the content of the content file.
     */
    public static final FunctionDefinition<Object>                   GET_CONTENT;

    /**
     * Changes the content of the content file.
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
     * <td>{@link Object}</td>
     * <td>content</td>
     * <td>The new content of the content file.</td>
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
     * <td>There is not enough space for the new file content.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                     SET_CONTENT;

    static {

        GET_CONTENT = FunctionDefinitionFactory.create("getContent", ContentFile.class, PropertyAccessorFactory.createGet(CONTENT));
        SET_CONTENT = FunctionDefinitionFactory.create("setContent", ContentFile.class, PropertyAccessorFactory.createSet(CONTENT), Object.class);
        SET_CONTENT.addExecutor(ContentFile.class, "checkSize", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FileSystem fileSystem = invocation.getHolder().get(GET_FILE_SYSTEM).invoke();
                if (fileSystem != null && SizeUtil.getSize(arguments[0]) > fileSystem.get(FileSystem.GET_FREE).invoke()) {
                    throw new ExecutorInvocationException(new OutOfSpaceException(fileSystem, SizeUtil.getSize(arguments[0])));
                }

                return invocation.next(arguments);
            }

        });

        GET_SIZE.addExecutor(ContentFile.class, "content", SizeUtil.createGetSize(CONTENT));

    }

    // ----- Functions End -----

    /**
     * Creates a new content file.
     */
    public ContentFile() {

    }

}
