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

package com.quartercode.disconnected.client.graphics.component;

import java.util.List;
import de.matthiasmann.twl.model.TreeTableNode;

/**
 * The tree node is a central access point for accessing a tree with one interface.
 * It describes common methods for tree modification.
 *
 * @see TreeModel
 */
public interface TreeNode extends TreeTableNode {

    /**
     * Returns the data object which is stored inside the given table column of the node (row).
     *
     * @param column The index of the column whose data object should be returned.
     *        Note that the column indices start at {@code 0}. For example, the third column has the index {@code 2}.
     * @return The data object which is stored inside the given column.
     * @throws IllegalArgumentException Thrown if the given column doesn't exist.
     */
    @Override
    public Object getData(int column);

    /**
     * Changes the data object which is stored inside the given table column of the node (row).
     *
     * @param column The index of the column whose data object should be returned.
     *        Note that the column indices start at {@code 0}. For example, the third column has the index {@code 2}.
     * @param data The new data object which should be stored inside the given column.
     * @throws IllegalArgumentException Thrown if the given column doesn't exist.
     */
    public void setData(int column, Object data);

    /**
     * Returns all direct child nodes this node holds.
     *
     * @return All direct child nodes.
     */
    public List<TreeNode> getChildren();

    /**
     * Returns the direct child node this node holds at the given index.
     * A call to this method is equivalent to calling <code>{@link #getChildren()}[index]</code>.
     *
     * @param index The index of the direct child node for retrieval.
     */
    @Override
    public TreeNode getChild(int index);

    /**
     * Returns if the given node is a direct child of this node.
     *
     * @param child The node to check.
     * @return True if the given node is a direct child node.
     */
    public boolean hasChild(TreeNode child);

    /**
     * Adds a new child node to this node using the given data array for the columns of the table.
     *
     * @param data The data to use for the columns of the table.
     * @return The created child node object.
     * @throws IllegalArgumentException Thrown if the amount of data objects does not match the amount of available columns.
     */
    public TreeNode addChild(Object... data);

    /**
     * Removes a given child node from this node.
     *
     * @param child The child node for removal.
     */
    public void removeChild(TreeNode child);

    /**
     * Removes all child nodes from this node.
     */
    public void clear();

}
