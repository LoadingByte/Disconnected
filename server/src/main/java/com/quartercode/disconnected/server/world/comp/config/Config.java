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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * A configuration object represents the content of a configuration file.
 * Any configuration object could be represented as a string like:
 *
 * <pre>
 *           (Column 1)  (Column 2)  (Column 3)               (Column 4)
 * (Entry 1)  value1      value2      listentry1,listentry2    value3
 * (Entry 2)  value4      value5      listentry1               col4
 * (Entry 3) ...
 * </pre>
 *
 * A configuration uses multiple {@link ConfigEntry configuration entries} <b>of the same type</b> which represent the lines.
 * Each entry represents one line and contains contains different columns.
 * Each column contains a single value or a list of values. If it contains a list, the individual values are separated by commas.
 *
 * @param <E> The type of the configuration entries that can be part of this configuration.
 * @see ConfigEntry
 */
public class Config<E extends ConfigEntry<E>> extends WorldNode<Node<?>> implements DerivableSize {

    @XmlElementWrapper
    @XmlAnyElement (lax = true)
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<E> entries = new ArrayList<>();

    /**
     * Returns the {@link ConfigEntry configuration entries} the configuration object contains.
     * Such configuration entries represent the lines in a configuration file.
     *
     * @return The configuration the configuration contains.
     */
    public List<E> getEntries() {

        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns the {@link #getEntries() configuration entries} which provide the given {@link ConfigEntry#getColumnValue(String) value} in the column with the given name.
     * Of course, the column names are dependent on the different types of config entries.
     * For example, you are able to retrieve all users with the primary group {@code "myGroup"} from a user config with the given call:
     *
     * <pre>
     * userConfig.getEntriesByColumn("primaryGroup", "myGroup");
     * </pre>
     *
     * If you only want to retrieve one entry (e.g. the user with a specific name) instead of a list with multiple entries, try {@link #getEntryByColumn(String, String)}.
     * Of course, this method is just a convenience method for very simple cases.
     * If your query is more complex (e.g. all users which are part of a specific group), you still have to loop through all entries and check each one individually.
     *
     * @param column The name of the {@link ConfigEntry#getColumnNames() column} which should be checked.
     * @param value The {@link ConfigEntry#getColumnValue(String) column value} all returned entries must have in the given column.
     * @return All config entries which provide the given value in the column with the given name.
     */
    public List<E> getEntriesByColumn(String column, String value) {

        List<E> result = new ArrayList<>();

        for (E entry : entries) {
            if (entry.getColumnNames().contains(column) && entry.getColumnValue(column).endsWith(value)) {
                result.add(entry);
            }
        }

        return result;
    }

    /**
     * Returns the first {@link #getEntries() configuration entry} which provides the given {@link ConfigEntry#getColumnValue(String) value} in the column with the given name.
     * Of course, the column names are dependent on the different types of config entries.
     * For example, you are able to retrieve the user with the name {@code "root"} from a user config with the given call:
     *
     * <pre>
     * userConfig.getEntryByColumn("name", "root");
     * </pre>
     *
     * If you want to retrieve multiple entries (e.g. all users with a specific primary group) instead of just one, try {@link #getEntriesByColumn(String, String)}.
     * Of course, this method is just a convenience method for very simple cases.
     * If your query is more complex (e.g. all users which are part of a specific group), you still have to loop through all entries and check each one individually.
     *
     * @param column The name of the {@link ConfigEntry#getColumnNames() column} which should be checked.
     * @param value The {@link ConfigEntry#getColumnValue(String) column value} the returned entry must have in the given column.
     * @return The first config entries which provides the given value in the column with the given name.
     *         If no config entry can be found, {@code null} is returned.
     */
    public E getEntryByColumn(String column, String value) {

        List<E> entriesByColumn = getEntriesByColumn(column, value);
        return entriesByColumn.isEmpty() ? null : entriesByColumn.get(0);
    }

    /**
     * Adds the given {@link ConfigEntry} to the configuration object.
     *
     * @param entry The configuration entry to add to the configuration.
     */
    public void addEntry(E entry) {

        Validate.notNull(entry, "Cannot add null entry to config");
        entries.add(entry);
    }

    /**
     * Removes the given {@link ConfigEntry} from the configuration object.
     *
     * @param entry The configuration entry to remove from the configuration.
     */
    public void removeEntry(E entry) {

        entries.remove(entry);
    }

    @Override
    public long getSize() {

        return SizeUtils.getSize(entries);
    }

}
