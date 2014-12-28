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

package com.quartercode.disconnected.client.graphics.desktop.prog.general;

import static java.text.MessageFormat.format;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.component.TreeModel;
import com.quartercode.disconnected.client.graphics.component.TreeNode;
import com.quartercode.disconnected.client.graphics.desktop.ClientProgramContext;
import com.quartercode.disconnected.client.graphics.desktop.ClientProgramDescriptor;
import com.quartercode.disconnected.client.graphics.desktop.ClientProgramWindow;
import com.quartercode.disconnected.client.graphics.desktop.ClientProgramWindowSkeleton;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindowDefaultSizeMediator;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindowSizeLimitsMediator;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup.Option;
import com.quartercode.disconnected.client.graphics.desktop.prog.util.GP_SBPWPU_ErrorEventPopupHandler;
import com.quartercode.disconnected.client.util.ResourceBundles;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_WP_InterruptProcessCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessPlaceholder;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Dimension;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ScrollPane.Fixed;
import de.matthiasmann.twl.TreeTable;

/**
 * The process manager client program is used to list and interrupt processes.
 * 
 * @see ProcessManagerClientProgramWindow
 */
public class ProcessManagerClientProgram extends ClientProgramDescriptor {

    /**
     * Creates a new process manager client program descriptor.
     */
    public ProcessManagerClientProgram() {

        super(ResourceBundles.forProgram("processManager"), "name");
    }

    @Override
    public ClientProgramWindow create(GraphicsState state, ClientProgramContext context) {

        return new ProcessManagerClientProgramWindow(state, this, context);
    }

    /**
     * The internal {@link ClientProgramWindow} used by the {@link ProcessManagerClientProgram}.
     */
    public static class ProcessManagerClientProgramWindow extends ClientProgramWindowSkeleton {

        private Button    interruptProcessButton;
        private TreeTable processListTable;
        private TreeModel processListModel;

        private ProcessManagerClientProgramWindow(GraphicsState state, ClientProgramDescriptor descriptor, ClientProgramContext context) {

            super(state, descriptor, context);
        }

        @Override
        protected void initializeGraphics() {

            new DesktopWindowSizeLimitsMediator(this, new Dimension(500, 150), null);
            new DesktopWindowDefaultSizeMediator(this, new Dimension(700, 300));

            interruptProcessButton = new Button(getString("interruptProcess.text"));
            interruptProcessButton.setTheme("/button");

            Button btn = new Button("TestBTN");
            btn.setTheme("/button");

            String headerFileName = getString("processList.header.fileName");
            String headerUser = getString("processList.header.user");
            String headerPid = getString("processList.header.pid");
            String headerState = getString("processList.header.state");
            processListModel = new TreeModel(headerFileName, headerUser, headerPid, headerState);

            processListTable = new TreeTable(processListModel);
            processListTable.setTheme("/table");
            processListTable.setDefaultSelectionManager();

            ScrollPane scrollPane = new ScrollPane(processListTable);
            scrollPane.setTheme("/scrollpane");
            scrollPane.setFixed(Fixed.HORIZONTAL);

            DialogLayout layout = new DialogLayout();
            layout.setTheme("");
            layout.setDefaultGap(new Dimension(5, 5));
            Group hButtons = layout.createSequentialGroup(interruptProcessButton).addGap();
            layout.setHorizontalGroup(layout.createParallelGroup(hButtons).addWidget(scrollPane));
            layout.setVerticalGroup(layout.createSequentialGroup(interruptProcessButton).addWidget(scrollPane));
            add(layout);

            processListTable.adjustSize();
            adjustColumnWidth(processListTable, 0, 0.4F);
            adjustColumnWidth(processListTable, 1, 0.2F);
            adjustColumnWidth(processListTable, 2, 0.2F);
            adjustColumnWidth(processListTable, 3, 0.2F);
        }

        private void adjustColumnWidth(TreeTable table, int column, float width) {

            table.setColumnWidth(column, (int) (table.getWidth() * width));
        }

        @Override
        protected void initializeInteractions() {

            super.initializeInteractions();

            interruptProcessButton.addCallback(new Runnable() {

                @Override
                public void run() {

                    // Retrieve the currently selected process
                    int[] selections = processListTable.getSelectionManager().getSelectionModel().getSelection();
                    for (int selection : selections) {
                        TreeNode node = (TreeNode) processListTable.getNodeFromRow(selection);
                        String name = (String) node.getData(0);
                        final int pid = (int) node.getData(2);

                        // Ask for confirmation
                        String confirmMessage = format(getString("interruptProcess.confirmPopup.message"), name, pid);
                        openPopup(new ConfirmPopup(getState(), confirmMessage, new Option[] { Option.NO, Option.YES }, new ConfirmPopup.Callback() {

                            @Override
                            public void onClose(Option selected) {

                                if (selected == Option.YES) {
                                    // Interrupt the process
                                    bridge.send(new PMP_WP_InterruptProcessCommand(worldProcessId, pid));
                                }
                            }

                        }), true);
                    }
                }

            });
        }

        @Override
        protected void registerEventHandlers() {

            super.registerEventHandlers();

            registerEventHandler(PMP_SBPWPU_UpdateViewCommand.class, new UpdateViewCommandHandler());
            registerEventHandler(GP_SBPWPU_ErrorEvent.class, new GP_SBPWPU_ErrorEventPopupHandler(this, "", "Popup.message", true));
        }

        @Override
        protected void doLaunchWorldProcess() {

            // Launch process
            bridge.send(new WorldProcessLaunchCommand(clientProcessDetails, "processManager"));
        }

        private void updateView(WorldProcessPlaceholder rootProcess) {

            // Add empty root process node if it doesn't exist
            if (processListModel.getNumChildren() == 0) {
                processListModel.addChild();
            }

            updateNode(rootProcess, processListModel.getChild(0));
        }

        private void updateNode(WorldProcessPlaceholder process, TreeNode node) {

            // Update the data of the current node
            node.setData(0, PathUtils.splitBeforeName(process.getSourcePath())[1]);
            node.setData(1, process.getUser());
            node.setData(2, process.getId().getPid());
            node.setData(3, getString("processList.processState." + process.getState().name().toLowerCase()));

            // Remove nodes that are no longer children of the given process
            for (TreeNode childNode : node.getChildren()) {
                int childNodePid = (int) childNode.getData(2);

                boolean contains = false;
                for (WorldProcessPlaceholder childProcess : process.getChildren()) {
                    if (childProcess.getId().getPid() == childNodePid) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    node.removeChild(childNode);
                }
            }

            // Add nodes to represent the new children of the given process
            for (WorldProcessPlaceholder childProcess : process.getChildren()) {
                boolean contains = false;
                for (TreeNode childNode : node.getChildren()) {
                    int childNodePid = (int) childNode.getData(2);
                    if (childNodePid == childProcess.getId().getPid()) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    TreeNode newChildNode = node.addChild();
                    updateNode(childProcess, newChildNode);
                }
            }
        }

        private class UpdateViewCommandHandler implements EventHandler<PMP_SBPWPU_UpdateViewCommand> {

            @Override
            public void handle(PMP_SBPWPU_UpdateViewCommand event) {

                updateView(event.getRootProcess());
            }

        }

    }

}
