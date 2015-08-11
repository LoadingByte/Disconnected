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

/**
 * This runtime exception occurs if a {@link ConfigEntry} is asked for a column it doesn't have.
 * More specifically, the {@link ConfigEntry#getColumnValue(String)} and {@link ConfigEntry#setColumnValue(String, String)} methods throw it if the provided column name is unknown.
 *
 * @see ConfigEntry
 */
public class UnknownColumnException extends RuntimeException {

    private static final long serialVersionUID = -6425868955593381985L;

    private final String      columnName;

    /**
     * Creates a new unknown column exception.
     *
     * @param columnName The name of the unknown column.
     */
    public UnknownColumnException(String columnName) {

        super("Unknown configuration entry column: " + columnName);

        this.columnName = columnName;
    }

    /**
     * Returns the name of the column the {@link ConfigEntry}, which threw the exception, doesn't have.
     *
     * @return The name of the unknown column.
     */
    public String getColumnName() {

        return columnName;
    }

}
