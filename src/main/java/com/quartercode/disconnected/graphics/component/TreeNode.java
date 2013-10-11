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

package com.quartercode.disconnected.graphics.component;

import de.matthiasmann.twl.model.TreeTableNode;

/**
 * The tree node is a central access point for accessing a tree with one interface.
 * It describes common methods for tree modification.
 * 
 * @see TreeModel
 */
public interface TreeNode extends TreeTableNode {

    /**
     * Returns all child nodes this node holds.
     * 
     * @return All child nodes this node holds.
     */
    public TreeNode[] getChildren();

    /**
     * Adds a new child node to this node using the given data array for the columns of the table.
     * 
     * @param data The data to use for the columns of the table.
     * @return The created child node object.
     */
    public TreeNode addChild(Object... data);

    /**
     * Removes a given child node from this node.
     * 
     * @param child The child node to remove from this node.
     */
    public void removeChild(TreeNode child);

    /**
     * Removes all child nodes from this node.
     */
    public void removeAllChildren();

}
