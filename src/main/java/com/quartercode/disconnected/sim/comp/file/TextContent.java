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

package com.quartercode.disconnected.sim.comp.file;

/**
 * Classes which implement text content can be serialized to and deserialized from simple text.
 * This can be useful for editing text files etc.
 */
public interface TextContent {

    /**
     * Returns the content of the object as a simple text string.
     * 
     * @return The object as a simple string.
     */
    public String getTextContent();

    /**
     * Changes the content of the object so it matches the given text string.
     * 
     * @param content The string which describes the new content of the object.
     */
    public void setTextContent(String content);

}
