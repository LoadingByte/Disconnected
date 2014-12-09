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

package com.quartercode.disconnected.server.world.comp.os.config;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.Map;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.ValueSupplierDefinition;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.disconnected.server.util.NullPreventer;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;

/**
 * A {@link Config} object uses configuration entries which represent the lines in a configuration string.
 * These entries have different columns. Every column contains a value or a list.
 * 
 * <pre>
 * value1 | value2 | listentry1,listentry2 | value3
 * </pre>
 * 
 * @see Config
 */
public class ConfigEntry extends WorldChildFeatureHolder<Config> implements DerivableSize {

    // ----- Functions -----

    /**
     * Returns the definitions of the features which define the different columns of the entry, along with the types of the columns.
     * Each definition needs to be a {@link ValueSupplierDefinition}, so the value of the defined feature can be retrieved.
     * Moreover, each definition must define a property or a collection property because the column values must be changeable.
     */
    public static final FunctionDefinition<Map<ValueSupplierDefinition<?, ?>, Class<?>>> GET_COLUMNS;

    static {

        GET_COLUMNS = factory(FunctionDefinitionFactory.class).create("getColumns", new Class[0]);

        GET_SIZE.addExecutor("columns", ConfigEntry.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                long size = 0;
                for (ValueSupplierDefinition<?, ?> column : holder.invoke(GET_COLUMNS).keySet()) {
                    // Cannot use the convenient method because the value supplier has generic wildcard parameters
                    Object columnValue = holder.get(column).get();
                    size += SizeUtils.getSize(columnValue);
                }

                return size + NullPreventer.prevent(invocation.next(arguments));
            }

        });

    }

    /**
     * Creates a new configuration entry.
     */
    public ConfigEntry() {

        setParentType(Config.class);
    }

}
