/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.world.comp.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.disconnected.server.world.util.WorldNode;

/**
 * A {@link Config} object uses configuration entries which represent the lines in a configuration string.
 * These entries have different columns. Each column contains a single value or a list of values.
 *
 * <pre>
 * value1 | value2 | listentry1,listentry2 | value3
 * </pre>
 *
 * @param <S> The type of the subclass which extends this class (self-bound).
 * @see Config
 */
public abstract class ConfigEntry<S extends ConfigEntry<S>> extends WorldNode<Config<S>> implements DerivableSize {

    // This property doesn't need to be persistent since it is freshly set by subclasses each time a new instance is constructed
    private final List<String> columnNames;

    /**
     * Creates a new configuration entry.
     *
     * @param columnNames The names of all columns the configuration entry should have.
     *        See {@link #getColumnNames()} for more details.
     */
    protected ConfigEntry(List<String> columnNames) {

        Validate.notEmpty(columnNames, "Configuration entry column name list cannot be null or empty");
        this.columnNames = new ArrayList<>(columnNames);
    }

    /**
     * Returns the names of all columns the configuration entry has.
     * Each column of an entry contains a single value or a list.
     * For example, a column with the name {@code username} might contain some value like {@code myuser}.
     * The methods {@link #getColumnValue(String)} or {@link #setColumnValue(String, Object)} must work with any column name returned by this method.
     *
     * @return The names of all columns of the configuration entry.
     */
    public List<String> getColumnNames() {

        return Collections.unmodifiableList(columnNames);
    }

    /**
     * Returns the value which is stored in the column with the given name.
     * The underlying mechanism is similar to a key-value-pair.
     * The column name is the "key", the returned column value is the "value".
     * If the requested column contains a list, the returned string is a comma-separated list of the individual values.
     *
     * @param columnName The name of the column (the "key") whose current value should be returned.
     * @return The value which is currently stored in the given column.
     * @throws UnknownColumnException If the configuration entry doesn't have a column with the given name.
     *         Note that you can check for column existence by verifying that the column name is part of the {@link #getColumnNames()} list.
     */
    public abstract String getColumnValue(String columnName);

    /**
     * Sets the value which is stored in the column with the given name.
     * The underlying mechanism is similar to a key-value-pair.
     * The column name is the "key", the given column value is the "value".
     * If the new column value should be a list, the given value string must be a comma-separated list of the individual values.
     *
     * @param columnName The name of the column (the "key") whose value should be set to the given one.
     * @param columnValue The new value which should be stored in the given column.
     * @throws UnknownColumnException If the configuration entry doesn't have a column with the given name.
     *         Note that you can check for column existence by verifying that the column name is part of the {@link #getColumnNames()} list.
     */
    public abstract void setColumnValue(String columnName, String columnValue);

    @Override
    public long getSize() {

        long size = 0;

        for (String columnName : getColumnNames()) {
            size += SizeUtils.getSize(getColumnValue(columnName));
        }

        return size;
    }

}
