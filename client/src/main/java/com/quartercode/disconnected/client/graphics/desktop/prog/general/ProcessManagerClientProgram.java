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

package com.quartercode.disconnected.client.graphics.desktop.prog.general;

import static com.quartercode.disconnected.client.graphics.desktop.DesktopWindowUtils.setDefaultSize;
import static com.quartercode.disconnected.client.graphics.desktop.DesktopWindowUtils.setMinimumSize;
import static com.quartercode.disconnected.client.graphics.desktop.prog.util.ProgEventUtils.addEventHandler;
import static com.quartercode.disconnected.client.graphics.desktop.prog.util.ProgEventUtils.launchWorldProcess;
import static com.quartercode.disconnected.client.graphics.desktop.prog.util.ProgStateUtils.*;
import static java.text.MessageFormat.format;
import java.util.UUID;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.component.TreeModel;
import com.quartercode.disconnected.client.graphics.component.TreeNode;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup.Option;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramExecutor;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramStateContext;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramWindow;
import com.quartercode.disconnected.client.graphics.desktop.prog.util.GP_SBPWPU_ErrorEventPopupHandler;
import com.quartercode.disconnected.client.util.LocalizationSupplier;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_WP_InterruptProcessCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.util.ValueInjector.InjectValue;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserDetails;
import com.quartercode.disconnected.shared.world.comp.prog.UUIDSBPWorldProcessUserDetails;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessPlaceholder;
import com.quartercode.eventbridge.bridge.Bridge;
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
 */
public class ProcessManagerClientProgram implements ClientProgramExecutor {

    @InjectValue ("name")
    private String                           name;
    @InjectValue ("stateContext")
    private ClientProgramStateContext        stateContext;
    @InjectValue ("graphicsContext")
    private GraphicsState                    graphicsContext;
    @InjectValue ("l10nContext")
    private LocalizationSupplier             l10n;
    @InjectValue ("bridge")
    private Bridge                           bridge;

    private final SBPWorldProcessUserDetails clientProcessId = new UUIDSBPWorldProcessUserDetails(UUID.randomUUID());
    private final Mutable<WorldProcessId>    worldProcessId  = new MutableObject<>();

    @Override
    public void run() {

        interruptWorldProcessOnStop(bridge, stateContext, worldProcessId);
        stopOnWorldProcessInterrupt(bridge, stateContext, clientProcessId);
        nullFieldOnWorldProcessInterrupt(bridge, stateContext, clientProcessId, worldProcessId);

        new ProcessManagerClientProgramWindow().setVisible(true);

        launchWorldProcess(bridge, stateContext, clientProcessId, name, worldProcessId);
    }

    private class ProcessManagerClientProgramWindow extends ClientProgramWindow {

        private Button    interruptProcessButton;
        private TreeTable processListTable;
        private TreeModel processListModel;

        private ProcessManagerClientProgramWindow() {

            super(graphicsContext, l10n.get("name"));

            stopOnWindowClose(stateContext, this);

            setDefaultSize(this, new Dimension(700, 300));
            setMinimumSize(this, new Dimension(500, 150));

            initializeWidgets();
            initializeCallbacks();
            registerEventHandlers();
        }

        private void initializeWidgets() {

            interruptProcessButton = new Button(l10n.get("interruptProcess.text"));
            interruptProcessButton.setTheme("/button");

            String headerFileName = l10n.get("processList.header.fileName");
            String headerUser = l10n.get("processList.header.user");
            String headerPid = l10n.get("processList.header.pid");
            String headerState = l10n.get("processList.header.state");
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

        private void initializeCallbacks() {

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
                        String confirmMessage = format(l10n.get("interruptProcess.confirmPopup.message"), name, pid);
                        openPopup(new ConfirmPopup(getState(), confirmMessage, new Option[] { Option.NO, Option.YES }, new ConfirmPopup.Callback() {

                            @Override
                            public void onClose(Option selected) {

                                if (selected == Option.YES) {
                                    // Interrupt the process
                                    bridge.send(new PMP_WP_InterruptProcessCommand(worldProcessId.getValue(), pid));
                                }
                            }

                        }), true);
                    }
                }

            });
        }

        private void registerEventHandlers() {

            addEventHandler(bridge, stateContext, new UpdateViewCommandHandler(), PMP_SBPWPU_UpdateViewCommand.class, clientProcessId);
            addEventHandler(bridge, stateContext, new GP_SBPWPU_ErrorEventPopupHandler(this, l10n, "", "Popup.message", true), GP_SBPWPU_ErrorEvent.class, clientProcessId);
        }

        private void updateView(WorldProcessPlaceholder rootProcess) {

            // Add empty root process node if it doesn't exist yet
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
            node.setData(3, l10n.get("processList.processState." + process.getState().name().toLowerCase()));

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

            // Add nodes to represent the new children of the given process and update all existing nodes
            for (WorldProcessPlaceholder childProcess : process.getChildren()) {
                TreeNode childNode = null;
                for (TreeNode testChildNode : node.getChildren()) {
                    int childNodePid = (int) testChildNode.getData(2);
                    if (childNodePid == childProcess.getId().getPid()) {
                        childNode = testChildNode;
                        break;
                    }
                }

                // Add the node if it is new
                if (childNode == null) {
                    childNode = node.addChild();
                }

                // Update the (possibly new) node
                updateNode(childProcess, childNode);
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
