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

import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
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
    public static final PropertyDefinition<Object> CONTENT;

    static {

        CONTENT = ObjectProperty.createDefinition("content");
        CONTENT.addSetterExecutor("checkSize", ContentFile.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FileSystem fileSystem = invocation.getHolder().get(GET_FILE_SYSTEM).invoke();
                if (fileSystem != null && SizeUtil.getSize(arguments[0]) > fileSystem.get(FileSystem.GET_FREE).invoke()) {
                    throw new ExecutorInvocationException(new OutOfSpaceException(fileSystem, SizeUtil.getSize(arguments[0])));
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("content", ContentFile.class, SizeUtil.createGetSize(CONTENT));

    }

    /**
     * Creates a new content file.
     */
    public ContentFile() {

        setParentType(ParentFile.class);
    }

}
