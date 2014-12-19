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
import lombok.RequiredArgsConstructor;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.desktop.ClientProgramContext;
import com.quartercode.disconnected.client.graphics.desktop.ClientProgramDescriptor;
import com.quartercode.disconnected.client.graphics.desktop.ClientProgramWindow;
import com.quartercode.disconnected.client.graphics.desktop.ClientProgramWindowSkeleton;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindowDefaultSizeMediator;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindowSizeLimitsMediator;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup.Option;
import com.quartercode.disconnected.client.graphics.desktop.popup.TextInputPopup;
import com.quartercode.disconnected.client.graphics.desktop.prog.util.GP_SBPWPU_ErrorEventPopupHandler;
import com.quartercode.disconnected.client.util.ResourceBundles;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_AddFileCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_ChangeDirCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_RemoveFileCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.world.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
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
 * The file manager client program allows the user to view and manipulate the files on all mounted file systems.
 * 
 * @see FileManagerClientProgramWindow
 */
public class FileManagerClientProgram extends ClientProgramDescriptor {

    /**
     * Creates a new file manager client program descriptor.
     */
    public FileManagerClientProgram() {

        super(ResourceBundles.forProgram("fileManager"), "name");
    }

    @Override
    public ClientProgramWindow create(GraphicsState state, ClientProgramContext context) {

        return new FileManagerClientProgramWindow(state, this, context);
    }

    /**
     * The internal {@link ClientProgramWindow} used by the {@link FileManagerClientProgram}.
     */
    public static class FileManagerClientProgramWindow extends ClientProgramWindowSkeleton {

        private Label            currentDirectoryLabel;
        private Button           createFileButton;
        private Button           createDirectoryButton;
        private Button           removeFileButton;
        private Table            fileListTable;
        private SimpleTableModel fileListModel;

        private FileManagerClientProgramWindow(GraphicsState state, ClientProgramDescriptor descriptor, ClientProgramContext context) {

            super(state, descriptor, context);
        }

        @Override
        protected void initializeGraphics() {

            new DesktopWindowSizeLimitsMediator(this, new Dimension(500, 150), null);
            new DesktopWindowDefaultSizeMediator(this, new Dimension(700, 300));

            currentDirectoryLabel = new Label();
            currentDirectoryLabel.setTheme("/label");

            createFileButton = new Button(getString("createFile.text"));
            createFileButton.setTheme("/button");

            createDirectoryButton = new Button(getString("createDirectory.text"));
            createDirectoryButton.setTheme("/button");

            removeFileButton = new Button(getString("removeFile.text"));
            removeFileButton.setTheme("/button");

            String headerName = getString("fileList.header.name");
            String headerType = getString("fileList.header.type");
            String headerSize = getString("fileList.header.size");
            fileListModel = new SimpleTableModel(new String[] { headerName, headerType, headerSize });

            fileListTable = new Table(fileListModel);
            fileListTable.setTheme("/table");
            fileListTable.setDefaultSelectionManager();

            ScrollPane scrollPane = new ScrollPane(fileListTable);
            scrollPane.setTheme("/scrollpane");
            scrollPane.setFixed(Fixed.HORIZONTAL);

            DialogLayout layout = new DialogLayout();
            layout.setTheme("");
            layout.setDefaultGap(new Dimension(5, 5));
            Group hButtons = layout.createSequentialGroup(createFileButton, createDirectoryButton, removeFileButton);
            Group vButtons = layout.createParallelGroup(createFileButton, createDirectoryButton, removeFileButton);
            layout.setHorizontalGroup(layout.createParallelGroup(currentDirectoryLabel).addGroup(hButtons).addWidget(scrollPane));
            layout.setVerticalGroup(layout.createSequentialGroup(currentDirectoryLabel).addGroup(vButtons).addWidget(scrollPane));
            add(layout);

            fileListTable.adjustSize();
            adjustColumnWidth(fileListTable, 0, 0.4F);
            adjustColumnWidth(fileListTable, 1, 0.3F);
            adjustColumnWidth(fileListTable, 2, 0.3F);
        }

        private void adjustColumnWidth(Table table, int column, float width) {

            table.setColumnWidth(column, (int) (table.getWidth() * width));
        }

        @Override
        protected void initializeInteractions() {

            super.initializeInteractions();

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
                            String confirmMessage = format(getString("removeFile.confirmPopup.message"), fileName);
                            openPopup(new ConfirmPopup(getState(), confirmMessage, new Option[] { Option.NO, Option.YES }, new ConfirmPopup.Callback() {

                                @Override
                                public void onClose(Option selected) {

                                    if (selected == Option.YES) {
                                        // Remove the file
                                        bridge.send(new FMP_WP_RemoveFileCommand(worldProcessId, fileName));
                                    }
                                }

                            }), true);

                        }
                    }
                }

            });
        }

        @Override
        protected void registerEventHandlers() {

            registerEventHandler(FMP_SBPWPU_UpdateViewCommand.class, new UpdateViewCommandHandler());
            registerEventHandler(GP_SBPWPU_ErrorEvent.class, new GP_SBPWPU_ErrorEventPopupHandler(this, "", "Popup.message", true));
        }

        @Override
        protected void doLaunchWorldProcess() {

            // Launch process
            bridge.send(new WorldProcessLaunchCommand(clientProcessDetails, "fileManager"));
        }

        @Override
        protected void executeActionsRequiringWorldProcessId() {

            // Set initial directory in order to receive an update view command
            changeDirectory(PathUtils.SEPARATOR);
        }

        private void changeDirectory(final String change) {

            // Send a request to set the new path
            bridge.send(new FMP_WP_ChangeDirCommand(worldProcessId, change));
        }

        private void updateView(String currentDir, FilePlaceholder[] files) {

            // Change path label
            currentDirectoryLabel.setText(format(getString("currentDirectoryLabel.text"), currentDir));

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
                fileListModel.addRow("..", getString("fileList.file.type.parentFile"));
            }

            // Add new entries to file list
            for (FilePlaceholder file : files) {
                String type = getString("fileList.file.type." + file.getType());

                String size = file.getSize() + " B";
                fileListModel.addRow(file.getName(), type, size);
            }
        }

        @RequiredArgsConstructor
        private class CreateFileCallback implements Runnable {

            private final String fileType;
            private final String keyBase;

            @Override
            public void run() {

                String inputMessage = getString(keyBase + ".fileNameInputPopup.message");
                openPopup(new TextInputPopup(getState(), inputMessage, null, true, new TextInputPopup.Callback() {

                    @Override
                    public void onClose(boolean cancelled, String text) {

                        if (!cancelled && text != null) {
                            // Add the new file
                            bridge.send(new FMP_WP_AddFileCommand(worldProcessId, text, fileType));
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
