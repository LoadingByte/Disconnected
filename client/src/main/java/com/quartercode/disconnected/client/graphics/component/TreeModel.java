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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.Validate;
import de.matthiasmann.twl.model.AbstractTreeTableModel;
import de.matthiasmann.twl.model.AbstractTreeTableNode;
import de.matthiasmann.twl.model.TreeTableNode;

/**
 * This tree model is an implementation of twl's abstract tree table model using the {@link TreeNode}-interface.
 * The tree allows adding and removing child nodes.
 * Although the tree model class implements the {@link TreeNode} interface, it isn't able to store data since it is only a 'virtual' root node.
 * The visible root nodes are added to this 'virtual' one.
 * 
 * @see TreeNode
 * @see TreeNodeImpl
 */
public class TreeModel extends AbstractTreeTableModel implements TreeNode {

    private final String[] columns;

    /**
     * Creates a new tree model with the given columns.
     * 
     * @param columns The column names to use for the table.
     */
    public TreeModel(String... columns) {

        this.columns = columns;
    }

    /**
     * Returns the column names which are used for the table.
     * 
     * @return The column names which are used for the table.
     */
    public String[] getColumns() {

        return columns;
    }

    @Override
    public int getNumColumns() {

        return columns.length;
    }

    @Override
    public String getColumnHeaderText(int column) {

        return columns[column];
    }

    @Override
    public Object getData(int column) {

        throw new UnsupportedOperationException("Cannot retrieve data from tree model root node (it is a 'virtual' root node)");
    }

    @Override
    public void setData(int column, Object data) {

        throw new UnsupportedOperationException("Cannot write data into tree model root node (it is a 'virtual' root node)");
    }

    @Override
    public List<TreeNode> getChildren() {

        List<TreeNode> children = new ArrayList<>(getNumChildren());
        for (int index = 0; index < getNumChildren(); index++) {
            children.add(getChild(index));
        }
        return children;
    }

    @Override
    public TreeNode getChild(int index) {

        return (TreeNode) super.getChild(index);
    }

    @Override
    public boolean hasChild(TreeNode child) {

        return getChildIndex(child) >= 0;
    }

    @Override
    public TreeNode addChild(Object... data) {

        TreeNode child = new TreeNodeImpl(this, data);
        insertChild(child, getNumChildren());
        return child;
    }

    @Override
    public void removeChild(TreeNode child) {

        removeChild(getChildIndex(child));
    }

    @Override
    public void clear() {

        removeAllChildren();
    }

    /**
     * Tree nodes are used by the tree model to store the tree structure.
     * Every node can hold a data array to use for the columns of the table.
     * 
     * @see TreeNode
     * @see TreeModel
     */
    public static class TreeNodeImpl extends AbstractTreeTableNode implements TreeNode {

        private final Object[] data;

        /**
         * Creates a new node for a tree with the given parent node and data array to use for the columns of the table.
         * 
         * @param parent The parent node which holds the new node.
         * @param data The data to use for the columns of the table.
         */
        protected TreeNodeImpl(TreeTableNode parent, Object... data) {

            super(parent);

            int columns = getTreeTableModel().getNumColumns();
            Validate.inclusiveBetween(0, columns, data.length, "Cannot create tree node with %d entries while the table has %d columns", data.length, columns);

            this.data = Arrays.copyOf(data, columns);
            setLeaf(true);
        }

        @Override
        public Object getData(int column) {

            int columns = getTreeTableModel().getNumColumns();
            Validate.inclusiveBetween(0, columns - 1, column, "Cannot retrieve data from unknown column %d (table has %d columns)", column, columns);

            Object result = data[column];
            return result == null ? "" : result;
        }

        @Override
        public void setData(int column, Object data) {

            int columns = getTreeTableModel().getNumColumns();
            Validate.inclusiveBetween(0, columns - 1, column, "Cannot write data into unknown column %d (table has %d columns)", column, columns);

            this.data[column] = data;
        }

        @Override
        public List<TreeNode> getChildren() {

            List<TreeNode> children = new ArrayList<>(getNumChildren());
            for (int index = 0; index < getNumChildren(); index++) {
                children.add(getChild(index));
            }
            return children;
        }

        @Override
        public TreeNode getChild(int index) {

            return (TreeNode) super.getChild(index);
        }

        @Override
        public boolean hasChild(TreeNode child) {

            return getChildIndex(child) >= 0;
        }

        @Override
        public TreeNode addChild(Object... data) {

            TreeNode child = new TreeNodeImpl(this, data);
            insertChild(child, getNumChildren());
            setLeaf(false);
            return child;
        }

        @Override
        public void removeChild(TreeNode child) {

            if (hasChild(child)) {
                removeChild(getChildIndex(child));

                if (getNumChildren() == 0) {
                    setLeaf(true);
                }
            }
        }

        @Override
        public void clear() {

            removeAllChildren();
        }

    }

}
