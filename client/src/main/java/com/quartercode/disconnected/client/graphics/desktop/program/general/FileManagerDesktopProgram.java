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

package com.quartercode.disconnected.client.graphics.desktop.program.general;

import static java.text.MessageFormat.format;
import lombok.RequiredArgsConstructor;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.desktop.DesktopProgramContext;
import com.quartercode.disconnected.client.graphics.desktop.DesktopProgramDescriptor;
import com.quartercode.disconnected.client.graphics.desktop.DesktopProgramWindow;
import com.quartercode.disconnected.client.graphics.desktop.DesktopProgramWindowSkeleton;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindowDefaultSizeMediator;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup;
import com.quartercode.disconnected.client.graphics.desktop.popup.ConfirmPopup.Option;
import com.quartercode.disconnected.client.graphics.desktop.popup.MessagePopup;
import com.quartercode.disconnected.client.graphics.desktop.popup.TextInputPopup;
import com.quartercode.disconnected.client.util.ResourceBundles;
import com.quartercode.disconnected.shared.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.comp.file.FileRights;
import com.quartercode.disconnected.shared.comp.file.PathUtils;
import com.quartercode.disconnected.shared.comp.program.GeneralProgramConstants;
import com.quartercode.disconnected.shared.event.program.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPClientAddErrorEvent;
import com.quartercode.disconnected.shared.event.program.general.FMPClientMissingRightEvent;
import com.quartercode.disconnected.shared.event.program.general.FMPClientUpdateViewCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldAddFileCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldChangeDirCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldRemoveFileCommand;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
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
 * The file manager desktop program allows the user to view and manipulate the files on all mounted file systems.
 */
public class FileManagerDesktopProgram extends DesktopProgramDescriptor {

    /**
     * Creates a new file manager desktop program descriptor.
     */
    public FileManagerDesktopProgram() {

        super(ResourceBundles.forProgram("file-manager"), "name");
    }

    @Override
    public DesktopProgramWindow create(GraphicsState state, DesktopProgramContext context) {

        return new FileManagerDesktopProgramWindow(state, this, context);
    }

    private static class FileManagerDesktopProgramWindow extends DesktopProgramWindowSkeleton {

        private Label            currentDirectoryLabel;
        private Button           createFileButton;
        private Button           createDirectoryButton;
        private Button           removeFileButton;
        private Table            fileListTable;
        private SimpleTableModel fileListModel;

        private FileManagerDesktopProgramWindow(GraphicsState state, DesktopProgramDescriptor descriptor, DesktopProgramContext context) {

            super(state, descriptor, context);
        }

        @Override
        protected void initializeGraphics() {

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
        }

        @Override
        protected void initializeInteractions() {

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
                                        bridge.send(new FMPWorldRemoveFileCommand(worldProcessId, fileName));
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

            final UpdateViewCommandHandler updateViewCommandHandler = new UpdateViewCommandHandler();
            final MissingReadRightEventHandler missingReadRightEventHandler = new MissingReadRightEventHandler();
            final AddErrorEventHandler addErrorCommandHandler = new AddErrorEventHandler();

            registerEventHandler(FMPClientUpdateViewCommand.class, updateViewCommandHandler);
            registerEventHandler(FMPClientMissingRightEvent.class, missingReadRightEventHandler);
            registerEventHandler(FMPClientAddErrorEvent.class, addErrorCommandHandler);

            addCloseListener(new Runnable() {

                @Override
                public void run() {

                    StandardHandlerModule handlerModule = bridge.getModule(StandardHandlerModule.class);
                    handlerModule.removeHandler(updateViewCommandHandler);
                    handlerModule.removeHandler(missingReadRightEventHandler);
                    handlerModule.removeHandler(addErrorCommandHandler);
                }

            });
        }

        @Override
        protected void doLaunchWorldProcess() {

            // Launch process
            bridge.send(new WorldProcessLaunchCommand(clientProcessId.getPid(), GeneralProgramConstants.COMLOC_FILE_MANAGER.toString()));

            // Set initial directory in order to receive an update view command
            changeDirectory(PathUtils.SEPARATOR);
        }

        private void changeDirectory(final String change) {

            // Send a request to set the new path
            bridge.send(new FMPWorldChangeDirCommand(worldProcessId, change));
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

        @Override
        protected void layout() {

            super.layout();

            setMinSize(500, 150);

            fileListTable.adjustSize();
            adjustColumnWidth(fileListTable, 0, 0.5F);
            adjustColumnWidth(fileListTable, 1, 0.25F);
            adjustColumnWidth(fileListTable, 2, 0.25F);
        }

        private void adjustColumnWidth(Table table, int column, float width) {

            table.setColumnWidth(column, (int) (table.getWidth() * width));
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
                            bridge.send(new FMPWorldAddFileCommand(worldProcessId, text, fileType));
                        }
                    }

                }), true);
            }

        }

        private class UpdateViewCommandHandler implements EventHandler<FMPClientUpdateViewCommand> {

            @Override
            public void handle(FMPClientUpdateViewCommand event) {

                updateView(event.getCurrentDir(), event.getFiles());
            }

        }

        private class MissingReadRightEventHandler implements EventHandler<FMPClientMissingRightEvent> {

            @Override
            public void handle(FMPClientMissingRightEvent event) {

                String messageKey = null;
                switch (event.getMissingRight()) {
                    case FileRights.READ:
                        messageKey = "fileList.missingReadRightPopup.message";
                    case FileRights.WRITE:
                        messageKey = "createFile.missingWriteRightPopup.message";
                    case FileRights.DELETE:
                        messageKey = "removeFile.missingDeleteRightPopup.message";
                }

                if (messageKey != null) {
                    String fileName = PathUtils.splitBeforeName(event.getFilePath())[1];
                    openPopup(new MessagePopup(getState(), format(getString(messageKey), fileName)), true);
                }
            }

        }

        private class AddErrorEventHandler implements EventHandler<FMPClientAddErrorEvent> {

            @Override
            public void handle(FMPClientAddErrorEvent event) {

                String fileName = PathUtils.splitBeforeName(event.getFilePath())[1];
                openPopup(new MessagePopup(getState(), format(getString("createFile." + event.getErrorType() + "Popup.message"), fileName)), true);
            }

        }

    }

}