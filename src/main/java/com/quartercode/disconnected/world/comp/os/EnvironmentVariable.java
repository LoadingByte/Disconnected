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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.ValueSupplierDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.util.NullPreventer;
import com.quartercode.disconnected.world.comp.os.Configuration.ConfigurationEntry;

/**
 * Environment variables are used by different programs.
 * They are like global variables on the level of the os.
 * Every environment variable has a name and an associated value string.
 */
public class EnvironmentVariable extends ConfigurationEntry {

    /**
     * The separator which is used to separate the different entries of a value list.
     */
    public static final String                           LIST_SEPARATOR = ":";

    // ----- Properties -----

    /**
     * The name of the environment variable (the "key").
     * It is final and can't be changed.
     */
    public static final PropertyDefinition<String>       NAME;

    /**
     * The value {@link String} which is assigned to the environment variable.
     * It is modifiable can be changed.
     */
    public static final PropertyDefinition<String>       VALUE;

    static {

        NAME = ObjectProperty.createDefinition("name");
        VALUE = ObjectProperty.createDefinition("value");

    }

    // ----- Functions -----

    /**
     * Returns the value you can retrieve with {@link #GET_VALUE} as a {@link List}.
     * Such a list is a collection of entries separated by the character {@value #LIST_SEPARATOR}.
     * Example:
     * 
     * <pre>
     * subvalue1:subvalue2:subvalue3
     * =&gt; Returns [subvalue1, subvalue2, subvalue3]
     * </pre>
     * 
     * Modifications to the returned {@link List} object do not apply to the variable.
     */
    public static final FunctionDefinition<List<String>> GET_VALUE_LIST;

    /**
     * Changes the value of the environment variable to the given {@link List}.
     * Such a list is a collection of entries separated by the character {@value #LIST_SEPARATOR}.
     * Example:
     * 
     * <pre>
     * [subvalue1, subvalue2, subvalue3]
     * =&gt; Sets subvalue1:subvalue2:subvalue3
     * </pre>
     * 
     * Modifications to the input {@link List} object do not apply to the variable.
     * You can get the currently set value list using {@link #GET_VALUE_LIST}.
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
     * <td>{@link List}&lt;{@link String}&gt;</td>
     * <td>list</td>
     * <td>The list containing the new subvalues.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>         SET_VALUE_LIST;

    static {

        GET_VALUE_LIST = FunctionDefinitionFactory.create("getValueList", EnvironmentVariable.class, new FunctionExecutor<List<String>>() {

            @Override
            public List<String> invoke(FunctionInvocation<List<String>> invocation, Object... arguments) {

                String value = invocation.getHolder().get(VALUE).get();
                List<String> valueList = new ArrayList<String>();

                if (value != null && !value.isEmpty()) {
                    // Split at separator and return
                    // If there's no separator, the whole value is handled as one entry
                    valueList.addAll(Arrays.asList(value.split(LIST_SEPARATOR)));
                }

                invocation.next(arguments);
                return valueList;
            }

        });
        SET_VALUE_LIST = FunctionDefinitionFactory.create("setValueList", EnvironmentVariable.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                // Trust the user of the method
                @SuppressWarnings ("unchecked")
                List<String> list = (List<String>) arguments[0];

                String value = "";
                for (String listValue : list) {
                    value += ":" + listValue;
                }
                invocation.getHolder().get(VALUE).set(value.substring(1));

                return invocation.next(arguments);
            }

        }, List.class);

        GET_COLUMNS.addExecutor("name", EnvironmentVariable.class, new FunctionExecutor<Map<ValueSupplierDefinition<?, ?>, Class<?>>>() {

            @Override
            public Map<ValueSupplierDefinition<?, ?>, Class<?>> invoke(FunctionInvocation<Map<ValueSupplierDefinition<?, ?>, Class<?>>> invocation, Object... arguments) {

                Map<ValueSupplierDefinition<?, ?>, Class<?>> columns = NullPreventer.prevent(invocation.next(arguments));
                columns.put(NAME, String.class);
                return columns;
            }

        });
        GET_COLUMNS.addExecutor("value", EnvironmentVariable.class, new FunctionExecutor<Map<ValueSupplierDefinition<?, ?>, Class<?>>>() {

            @Override
            public Map<ValueSupplierDefinition<?, ?>, Class<?>> invoke(FunctionInvocation<Map<ValueSupplierDefinition<?, ?>, Class<?>>> invocation, Object... arguments) {

                Map<ValueSupplierDefinition<?, ?>, Class<?>> columns = NullPreventer.prevent(invocation.next(arguments));
                columns.put(VALUE, String.class);
                return columns;
            }

        });

    }

    /**
     * Creates a new environment variable.
     */
    public EnvironmentVariable() {

    }

}
