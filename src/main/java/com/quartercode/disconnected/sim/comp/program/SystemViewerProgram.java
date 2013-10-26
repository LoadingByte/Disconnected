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

package com.quartercode.disconnected.sim.comp.program;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.graphics.component.TreeModel;
import com.quartercode.disconnected.graphics.component.TreeNode;
import com.quartercode.disconnected.graphics.session.Frame;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.sim.comp.program.Desktop.Window;
import com.quartercode.disconnected.sim.comp.program.Process.ProcessState;
import com.quartercode.disconnected.sim.run.Ticker;
import com.quartercode.disconnected.util.size.ByteUnit;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Event.Type;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TreeTable;

/**
 * The system viewer displays information about different system components, like the process tree.
 * 
 * @see OperatingSystem
 * @see Process
 */
public class SystemViewerProgram extends Program {

    /**
     * Creates a new empty system viewer program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected SystemViewerProgram() {

    }

    /**
     * Creates a new system viewer program and sets the name, the version and the vulnerabilities.
     * 
     * @param name The name the program has.
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public SystemViewerProgram(String name, Version version, List<Vulnerability> vulnerabilities) {

        super(name, version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(20, ByteUnit.KILOBYTE);
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) {

        return new ProgramExecutor(host) {

            private Window<SystemViewerFrame> mainWindow;
            private int                       mainWindowUpdateElapsed = 0;

            @Override
            public void update() {

                if (mainWindow == null) {
                    mainWindow = new Window<SystemViewerFrame>(new SystemViewerFrame(), getName(), getName());
                    openWindow(mainWindow);
                } else if (mainWindow.isClosed()) {
                    getHost().interrupt(true);
                } else {
                    mainWindowUpdateElapsed++;
                    if (mainWindowUpdateElapsed >= Ticker.DEFAULT_TICKS_PER_SECOND) {
                        mainWindowUpdateElapsed = 0;
                        updateMainWindow();
                    }
                }

                if (getHost().getState() == ProcessState.INTERRUPTED) {
                    if (!mainWindow.isClosed()) {
                        mainWindow.close();
                    }
                    getHost().stop(false);
                }
            }

            private void updateMainWindow() {

                TreeNode processRoot = mainWindow.getFrame().getRootProcessNode();
                List<TreeNode> addedNodes = updateProcessNodes(processRoot, getHost().getHost().getProcessManager().getRootProcess());
                for (TreeNode node : addedNodes) {
                    if (!node.isLeaf()) {
                        mainWindow.getFrame().getProcessTreeWidget().setRowExpanded(mainWindow.getFrame().getProcessTreeWidget().getRowFromNode(node), true);
                    }
                }
            }

            private List<TreeNode> updateProcessNodes(TreeNode parent, Process process) {

                List<TreeNode> addedNodes = new ArrayList<TreeNode>();

                AtomicBoolean added = new AtomicBoolean();
                TreeNode node = updateProcessNode(parent, process, added);
                if (added.get()) {
                    addedNodes.add(node);
                }
                if (node != null) {
                    for (Process childProcess : new ArrayList<Process>(process.getChildren())) {
                        addedNodes.addAll(updateProcessNodes(node, childProcess));
                    }
                }

                return addedNodes;
            }

            private TreeNode updateProcessNode(TreeNode parent, Process process, AtomicBoolean added) {

                added.set(false);
                for (final TreeNode node : parent.getChildren()) {
                    if (node.getData(1).equals(process.getPid())) {
                        for (final TreeNode child : node.getChildren()) {
                            boolean found = false;
                            for (Process childProcess : process.getChildren()) {
                                if (child.getData(1).equals(childProcess.getPid())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                Disconnected.getGraphicsManager().invoke(new Runnable() {

                                    @Override
                                    public void run() {

                                        node.removeChild(child);
                                    }
                                });
                            }
                        }

                        return node;
                    }
                }

                added.set(true);
                String session = process.getSession() == null ? "" : process.getSession().getUser().getName();
                return parent.addChild(process.getFile().getName(), process.getPid(), session, process.getState());
            }
        };
    }

    private class SystemViewerFrame extends Frame {

        private final TreeModel processTree;
        private final TreeTable processTreeWidget;

        private SystemViewerFrame() {

            processTree = new TreeModel("Process", "PID", "User", "Status");

            processTreeWidget = new TreeTable(processTree);
            processTreeWidget.setTheme("/table");
            processTreeWidget.setDefaultSelectionManager();

            ScrollPane scrollPane = new ScrollPane(processTreeWidget);
            scrollPane.setTheme("/scrollpane");

            add(scrollPane);
        }

        private TreeNode getRootProcessNode() {

            return processTree;
        }

        private TreeTable getProcessTreeWidget() {

            return processTreeWidget;
        }

        @Override
        protected void layout() {

            super.layout();

            setMinSize(500, 150);

            processTreeWidget.setPosition(getInnerX(), getInnerY());
            layoutTableColumns();
        }

        @Override
        protected boolean handleEvent(Event evt) {

            if (evt.getType() == Type.MOUSE_DRAGGED) {
                layoutTableColumns();
            }
            return super.handleEvent(evt);
        }

        private void layoutTableColumns() {

            setColumnWidth(processTreeWidget, 0, 0.5F);
            setColumnWidth(processTreeWidget, 1, 0.1F);
            setColumnWidth(processTreeWidget, 2, 0.2F);
            setColumnWidth(processTreeWidget, 3, 0.2F);
        }

        private void setColumnWidth(TreeTable table, int column, float width) {

            table.setColumnWidth(column, (int) (table.getWidth() * width));
        }

    }

}
