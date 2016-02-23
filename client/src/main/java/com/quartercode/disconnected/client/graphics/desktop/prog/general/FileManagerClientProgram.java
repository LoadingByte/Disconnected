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
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup.Option;
import com.quartercode.disconnected.client.graphics.desktop.popup.TextInputPopup;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramExecutor;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramStateContext;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramWindow;
import com.quartercode.disconnected.client.graphics.desktop.prog.util.GP_SBPWPU_ErrorEventPopupHandler;
import com.quartercode.disconnected.client.util.ByteCountFormatter;
import com.quartercode.disconnected.client.util.LocalizationSupplier;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_AddFileCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_ChangeDirCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_RemoveFileCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.util.ValueInjector.InjectValue;
import com.quartercode.disconnected.shared.world.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.proc.SBPWorldProcessUserDetails;
import com.quartercode.disconnected.shared.world.comp.proc.UUIDSBPWorldProcessUserDetails;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessId;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Dimension;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ScrollPane.Fixed;
import de.matthiasmann.twl.Table;
import de.matthiasmann.twl.TableBase.Callback;
import de.matthiasmann.twl.model.SimpleTableModel;

/**
 * The file manager client program is used to list, create, and remove files.
 */
public class FileManagerClientProgram implements ClientProgramExecutor {

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

        final FileManagerClientProgramWindow window = new FileManagerClientProgramWindow();
        window.setVisible(true);

        launchWorldProcess(bridge, stateContext, clientProcessId, name, worldProcessId, new Runnable() {

            @Override
            public void run() {

                window.afterLaunch();
            }

        });
    }

    private class FileManagerClientProgramWindow extends ClientProgramWindow {

        private Label            currentDirectoryLabel;
        private Button           createFileButton;
        private Button           createDirectoryButton;
        private Button           removeFileButton;
        private Table            fileListTable;
        private SimpleTableModel fileListModel;

        private FileManagerClientProgramWindow() {

            super(graphicsContext, l10n.get("name"));

            stopOnWindowClose(stateContext, this);

            setDefaultSize(this, new Dimension(700, 300));
            setMinimumSize(this, new Dimension(500, 150));

            initializeWidgets();
            initializeCallbacks();
            registerEventHandlers();
        }

        private void initializeWidgets() {

            currentDirectoryLabel = new Label();
            currentDirectoryLabel.setTheme("/label");

            createFileButton = new Button(l10n.get("createFile.text"));
            createFileButton.setTheme("/button");

            createDirectoryButton = new Button(l10n.get("createDirectory.text"));
            createDirectoryButton.setTheme("/button");

            removeFileButton = new Button(l10n.get("removeFile.text"));
            removeFileButton.setTheme("/button");

            String headerName = l10n.get("fileList.header.name");
            String headerType = l10n.get("fileList.header.type");
            String headerOwner = l10n.get("fileList.header.owner");
            String headerGroup = l10n.get("fileList.header.group");
            String headerRights = l10n.get("fileList.header.rights");
            String headerSize = l10n.get("fileList.header.size");
            fileListModel = new SimpleTableModel(new String[] { headerName, headerType, headerOwner, headerGroup, headerRights, headerSize });

            fileListTable = new Table(fileListModel);
            fileListTable.setTheme("/table");
            fileListTable.setDefaultSelectionManager();

            ScrollPane scrollPane = new ScrollPane(fileListTable);
            scrollPane.setTheme("/scrollpane");
            scrollPane.setFixed(Fixed.HORIZONTAL);

            DialogLayout layout = new DialogLayout();
            layout.setTheme("");
            layout.setDefaultGap(new Dimension(5, 5));
            Group hButtons = layout.createSequentialGroup(createFileButton, createDirectoryButton, removeFileButton).addGap();
            Group vButtons = layout.createParallelGroup(createFileButton, createDirectoryButton, removeFileButton);
            layout.setHorizontalGroup(layout.createParallelGroup(currentDirectoryLabel).addGroup(hButtons).addWidget(scrollPane));
            layout.setVerticalGroup(layout.createSequentialGroup(currentDirectoryLabel).addGroup(vButtons).addWidget(scrollPane));
            add(layout);

            fileListTable.adjustSize();
            adjustColumnWidth(fileListTable, 0, 0.23F);
            adjustColumnWidth(fileListTable, 1, 0.15F);
            adjustColumnWidth(fileListTable, 2, 0.15F);
            adjustColumnWidth(fileListTable, 3, 0.15F);
            adjustColumnWidth(fileListTable, 4, 0.17F);
            adjustColumnWidth(fileListTable, 5, 0.15F);
        }

        private void adjustColumnWidth(Table table, int column, float width) {

            table.setColumnWidth(column, (int) (table.getWidth() * width));
        }

        private void initializeCallbacks() {

            fileListTable.addCallback(new Callback() {

                @Override
                public void mouseRightClick(int row, int column, de.matthiasmann.twl.Event event) {

                }

                @Override
                public void mouseDoubleClicked(int row, int column) {

                    if (fileListModel.getNumRows() > 0) {
                        // Change the current directory to the clicked directory
                        String name = (String) fileListModel.getCell(row, 0);
                        if (name != null) {
                            if (name.equals("..")) {
                                changeDirectory("..");
                            } else {
                                changeDirectory(name);
                            }
                        }
                    }
                }

                @Override
                public void columnHeaderClicked(int column) {

                }

            });

            createFileButton.addCallback(new CreateFileCallback("contentFile", "createFile"));
            createDirectoryButton.addCallback(new CreateFileCallback("directory", "createDirectory"));

            removeFileButton.addCallback(new Runnable() {

                @Override
                public void run() {

                    // Retrieve the currently selected file name
                    int[] selections = fileListTable.getSelectionManager().getSelectionModel().getSelection();
                    for (int selection : selections) {
                        final String fileName = (String) fileListModel.getCell(selection, 0);

                        // If the selected file is not a reference to the parent dir
                        if (!fileName.equals("..")) {
                            // Ask for confirmation
                            String confirmMessage = format(l10n.get("removeFile.confirmPopup.message"), fileName);
                            openPopup(new ConfirmPopup(getState(), confirmMessage, new Option[] { Option.NO, Option.YES }, new ConfirmPopup.Callback() {

                                @Override
                                public void onClose(Option selected) {

                                    if (selected == Option.YES) {
                                        // Remove the file
                                        bridge.send(new FMP_WP_RemoveFileCommand(worldProcessId.getValue(), fileName));
                                    }
                                }

                            }), true);
                        }
                    }
                }

            });
        }

        private void registerEventHandlers() {

            addEventHandler(bridge, stateContext, new UpdateViewCommandHandler(), FMP_SBPWPU_UpdateViewCommand.class, clientProcessId);
            addEventHandler(bridge, stateContext, new GP_SBPWPU_ErrorEventPopupHandler(this, l10n, "", "Popup.message", true), GP_SBPWPU_ErrorEvent.class, clientProcessId);
        }

        private void afterLaunch() {

            // Set initial directory in order to receive an update view command
            changeDirectory(PathUtils.SEPARATOR);
        }

        private void changeDirectory(final String change) {

            // Send a request to set the new path
            bridge.send(new FMP_WP_ChangeDirCommand(worldProcessId.getValue(), change));
        }

        private void updateView(String currentDir, FilePlaceholder[] files) {

            // Change path label
            currentDirectoryLabel.setText(format(l10n.get("currentDirectoryLabel.text"), currentDir));

            // Set whether the file modification buttons are enabled
            boolean absoluteRoot = currentDir.equals(PathUtils.SEPARATOR);
            createFileButton.setEnabled(!absoluteRoot);
            createDirectoryButton.setEnabled(!absoluteRoot);
            removeFileButton.setEnabled(!absoluteRoot);

            // Clear old file list
            while (fileListModel.getNumRows() > 0) {
                fileListModel.deleteRow(0);
            }

            // Add reference to parent file
            if (!absoluteRoot) {
                fileListModel.addRow("..", l10n.get("fileList.fileType.parentFile"));
            }

            // Add new entries to file list
            for (FilePlaceholder file : files) {
                String type = l10n.get("fileList.fileType." + file.getType());

                String size = ByteCountFormatter.format(file.getSize());
                String rights = file.getRights().exportRightsAsString();
                fileListModel.addRow(file.getName(), type, file.getOwner(), file.getGroup(), rights, size);
            }
        }

        @RequiredArgsConstructor
        private class CreateFileCallback implements Runnable {

            private final String fileType;
            private final String keyBase;

            @Override
            public void run() {

                String inputMessage = l10n.get(keyBase + ".fileNameInputPopup.message");
                openPopup(new TextInputPopup(getState(), inputMessage, null, true, new TextInputPopup.Callback() {

                    @Override
                    public void onClose(boolean cancelled, String text) {

                        if (!cancelled && text != null) {
                            // Add the new file
                            bridge.send(new FMP_WP_AddFileCommand(worldProcessId.getValue(), text, fileType));
                        }
                    }

                }), true);
            }

        }

        private class UpdateViewCommandHandler implements EventHandler<FMP_SBPWPU_UpdateViewCommand> {

            @Override
            public void handle(FMP_SBPWPU_UpdateViewCommand event) {

                updateView(event.getCurrentDir(), event.getFiles());
            }

        }

    }

}
