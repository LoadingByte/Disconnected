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
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.util.SizeUtils;

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
     * The content of the content file.<br>
     * <br>
     * Exceptions that can occur when setting:
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link OutOfSpaceException}</td>
     * <td>There is not enough space on the current file system for the new file content.</td>
     * </tr>
     * </table>
     */
    public static final PropertyDefinition<Object> CONTENT;

    static {

        CONTENT = factory(PropertyDefinitionFactory.class).create("content", new StandardStorage<>());
        CONTENT.addSetterExecutor("checkSize", ContentFile.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                long size = SizeUtils.getSize(arguments[0]);

                FileSystem fileSystem = invocation.getCHolder().invoke(GET_FS);
                if (fileSystem != null && size > fileSystem.invoke(FileSystem.GET_FREE)) {
                    throw new OutOfSpaceException(fileSystem, size);
                }

                return invocation.next(arguments);
            }

        }, LEVEL_6);

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("content", ContentFile.class, SizeUtils.createGetSize(CONTENT));

    }

    /**
     * Creates a new content file.
     */
    public ContentFile() {

        setParentType(ParentFile.class);
    }

}
