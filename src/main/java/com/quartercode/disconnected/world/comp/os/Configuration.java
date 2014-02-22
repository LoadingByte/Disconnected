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
import java.util.List;
import java.util.Map;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.SizeUtil;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;

/**
 * A configuration is used as content of a configuration file.
 * The configuration object could be represented as a string like
 * 
 * <pre>
 *           Column 1  Column 2  Column 3               Column 4
 * (Entry 1) value1    value2    listentry1,listentry2  value3
 * (Entry 2) value4    value5    listentry1             col4
 * (Entry 3) ...
 * </pre>
 * 
 * A configuration uses {@link ConfigurationEntry}s which contain different columns.
 * Every column contains a value or a list. If it contains a list, the list entries are seperated by commas.
 * 
 * @see ConfigurationEntry
 */
public class Configuration extends DefaultFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The {@link ConfigurationEntry}s the configuration object contains.
     * Such {@link ConfigurationEntry}s represents the lines in a configuration file.
     */
    protected static final FeatureDefinition<ObjectProperty<List<ConfigurationEntry>>> ENTRIES;

    static {

        ENTRIES = new AbstractFeatureDefinition<ObjectProperty<List<ConfigurationEntry>>>("entries") {

            @Override
            public ObjectProperty<List<ConfigurationEntry>> create(FeatureHolder holder) {

                return new ObjectProperty<List<ConfigurationEntry>>(getName(), holder, new ArrayList<ConfigurationEntry>());
            }
        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link ConfigurationEntry}s the configuration object contains.
     * Such {@link ConfigurationEntry}s represents the lines in a configuration file.
     */
    public static final FunctionDefinition<List<ConfigurationEntry>>                   GET_ENTRIES;

    /**
     * Adds some {@link ConfigurationEntry}s to the configuration object.
     * Such {@link ConfigurationEntry}s represents the lines in a configuration file.
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
     * <td>{@link ConfigurationEntry}...</td>
     * <td>entries</td>
     * <td>The {@link ConfigurationEntry}s to add to the configuration object.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                       ADD_ENTRIES;

    /**
     * Removes some {@link ConfigurationEntry}s from the configuration object.
     * Such {@link ConfigurationEntry}s represents the lines in a configuration file.
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
     * <td>{@link ConfigurationEntry}...</td>
     * <td>entries</td>
     * <td>The {@link ConfigurationEntry}s to remove from the configuration object.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                       REMOVE_ENTRIES;

    static {

        GET_ENTRIES = FunctionDefinitionFactory.create("getEntires", Configuration.class, CollectionPropertyAccessorFactory.createGet(ENTRIES));
        ADD_ENTRIES = FunctionDefinitionFactory.create("addEntries", Configuration.class, CollectionPropertyAccessorFactory.createAdd(ENTRIES), ConfigurationEntry[].class);
        REMOVE_ENTRIES = FunctionDefinitionFactory.create("removeEntires", Configuration.class, CollectionPropertyAccessorFactory.createRemove(ENTRIES), ConfigurationEntry[].class);

    }

    // ----- Functions End -----

    /**
     * Creates a new configuration object.
     */
    public Configuration() {

    }

    /**
     * A {@link Configuration} object uses configuration entries which represent the lines in a configuration string.
     * These entries have different columns. Every column contains a value or a list.
     * 
     * <pre>
     * value1 | value2 | listentry1,listentry2 | value3
     * </pre>
     * 
     * @see Configuration
     */
    public static class ConfigurationEntry extends WorldChildFeatureHolder<Configuration> implements DerivableSize {

        // ----- Functions -----

        /**
         * Returns the column names of the entry along with their current values.
         * The values should be {@link String}s or {@link List}s.
         * Every returned column map of every invoked {@link FunctionExecutor} will be used.
         * This method should be implemented by different subclasses for different purposes.
         */
        public static final FunctionDefinition<Map<String, Object>> GET_COLUMNS;

        /**
         * Changes the column values to the ones set in the given value map.
         * The values are be {@link String}s or {@link List}s.
         * This method should be implemented by different subclasses for different purposes.
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
         * <td>{@link Map}&lt;{@link String}, {@link Object}&gt;</td>
         * <td>columns</td>
         * <td>The new column values ({@link String}s or {@link List}s) assigned to their names.</td>
         * </tr>
         * </table>
         */
        public static final FunctionDefinition<Void>                SET_COLUMNS;

        static {

            GET_COLUMNS = FunctionDefinitionFactory.create("getColumns");
            SET_COLUMNS = FunctionDefinitionFactory.create("setColumns", Map.class);

            GET_SIZE.addExecutor(ConfigurationEntry.class, "columns", new FunctionExecutor<Long>() {

                @Override
                public Long invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                    return SizeUtil.getSize(holder.get(GET_COLUMNS).invoke());
                }

            });

        }

        // ----- Functions End -----

        /**
         * Creates a new configuration entry.
         */
        public ConfigurationEntry() {

        }

    }

}
