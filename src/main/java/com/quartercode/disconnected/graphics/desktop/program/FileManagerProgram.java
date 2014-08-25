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

package com.quartercode.disconnected.graphics.desktop.program;

import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.util.FeatureDefinitionReference;
import com.quartercode.disconnected.graphics.GraphicsState;
import com.quartercode.disconnected.graphics.desktop.DesktopProgramContext;
import com.quartercode.disconnected.graphics.desktop.DesktopProgramDescriptor;
import com.quartercode.disconnected.graphics.desktop.DesktopProgramWindow;
import com.quartercode.disconnected.graphics.desktop.DesktopWindowDefaultSizeMediator;
import com.quartercode.disconnected.graphics.desktop.popup.ConfirmPopup;
import com.quartercode.disconnected.graphics.desktop.popup.ConfirmPopup.Option;
import com.quartercode.disconnected.graphics.desktop.popup.MessagePopup;
import com.quartercode.disconnected.graphics.desktop.popup.TextInputPopup;
import com.quartercode.disconnected.util.ResourceBundles;
import com.quartercode.disconnected.util.ValueInjector.InjectValue;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.Directory;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.file.RootFile;
import com.quartercode.disconnected.world.comp.program.ProgramEvent.ProgramEventPredicate;
import com.quartercode.disconnected.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.world.comp.program.general.FileCreateProgram;
import com.quartercode.disconnected.world.comp.program.general.FileListProgram;
import com.quartercode.disconnected.world.comp.program.general.FileListProgram.SuccessEvent.FilePlaceholder;
import com.quartercode.disconnected.world.comp.program.general.FileRemoveProgram;
import com.quartercode.disconnected.world.event.ProgramLaunchCommandEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEvent.ProgramLaunchInfoResponseEvent;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;
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
public class FileManagerProgram extends DesktopProgramDescriptor {

    /**
     * Creates a new file manager desktop program descriptor.
     */
    public FileManagerProgram() {

        super(ResourceBundles.forProgram("file-manager"), "name");
    }

    @Override
    public DesktopProgramWindow create(GraphicsState state, DesktopProgramContext context) {

        return new FileManagerProgramWindow(state, this, context);
    }

    private static class FileManagerProgramWindow extends DesktopProgramWindow {

        @InjectValue ("bridge")
        private Bridge                 bridge;

        private final CurrentDirectory currentDirectory;

        private final Label            currentDirectoryLabel;
        private final Button           createFileButton;
        private final Button           createDirectoryButton;
        private final Button           removeFileButton;
        private final Table            fileListTable;
        private final SimpleTableModel fileListModel;

        private FileManagerProgramWindow(GraphicsState state, DesktopProgramDescriptor descriptor, DesktopProgramContext context) {

            super(state, descriptor, context);

            new DesktopWindowDefaultSizeMediator(this, new Dimension(700, 300));

            context.injectValues(this);

            currentDirectoryLabel = new Label();
            currentDirectoryLabel.setTheme("/label");

            createFileButton = new Button(getString("createFile.text"));
            createFileButton.setTheme("/button");
            createFileButton.addCallback(new CreateFileCallback(ContentFile.class));

            createDirectoryButton = new Button(getString("createDirectory.text"));
            createDirectoryButton.setTheme("/button");
            createDirectoryButton.addCallback(new CreateFileCallback(Directory.class));

            removeFileButton = new Button(getString("removeFile.text"));
            removeFileButton.setTheme("/button");
            removeFileButton.addCallback(new RemoveFileCallback());

            String headerName = getString("fileList.header.name");
            String headerType = getString("fileList.header.type");
            String headerSize = getString("fileList.header.size");
            fileListModel = new SimpleTableModel(new String[] { headerName, headerType, headerSize });

            fileListTable = new Table(fileListModel);
            fileListTable.setTheme("/table");
            fileListTable.setDefaultSelectionManager();
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
                                currentDirectory.useParent();
                            } else {
                                currentDirectory.useChild(name);
                            }
                            update();
                        }
                    }
                }

                @Override
                public void columnHeaderClicked(int column) {

                }

            });

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

            currentDirectory = new CurrentDirectory(bridge, File.SEPARATOR);
            update();
        }

        private void update() {

            // Change path label
            String path = currentDirectory.getPath();
            currentDirectoryLabel.setText(format(getString("currentDirectoryLabel.text"), path));

            createFileButton.setEnabled(currentDirectory.canCreateOrRemoveFiles());
            createDirectoryButton.setEnabled(currentDirectory.canCreateOrRemoveFiles());
            removeFileButton.setEnabled(currentDirectory.canCreateOrRemoveFiles());

            // Clear file list
            while (fileListModel.getNumRows() > 0) {
                fileListModel.deleteRow(0);
            }

            currentDirectory.getChildren(new CurrentDirectory.GetChildrenCallback() {

                @Override
                public void success(List<FileListEntry> children) {

                    for (FileListEntry child : children) {
                        String type = getString("fileList.file.type." + child.getTypeKey());

                        if (child.getSize() < 0) {
                            fileListModel.addRow(child.getName(), type);
                        } else {
                            String size = child.getSize() + " B";
                            fileListModel.addRow(child.getName(), type, size);
                        }
                    }
                }

                @Override
                public void invalidPath() {

                    // Just go back to the parent dir
                    currentDirectory.useParent();
                    update();
                }

                @Override
                public void missingRights() {

                    // TODO: Temp: Just go back to the parent dir
                    currentDirectory.useParent();
                    update();
                }

            });
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

        private class CreateFileCallback implements Runnable {

            private final Class<? extends File<?>> fileType;
            private final String                   keyBase;

            private CreateFileCallback(Class<? extends File<?>> fileType) {

                Validate.isTrue(fileType == ContentFile.class || fileType == Directory.class, "Cannot create file of type '%s'", fileType.getName());

                this.fileType = fileType;
                keyBase = "create" + (fileType == Directory.class ? "Directory" : "File");
            }

            @Override
            public void run() {

                String inputMessage = getString(keyBase + ".fileNameInputPopup.message");
                openPopup(new TextInputPopup(getState(), inputMessage, null, true, new TextInputPopup.Callback() {

                    @Override
                    public void onClose(boolean cancelled, String text) {

                        if (!cancelled && text != null) {
                            currentDirectory.createFile(fileType, text, new InternalCreateFileCallback());
                        }
                    }

                }), true);
            }

            private class InternalCreateFileCallback implements CurrentDirectory.CreateFileCallback {

                @Override
                public void success() {

                    update();
                }

                @Override
                public void missingRights() {

                    openPopup(new MessagePopup(getState(), getString(keyBase + ".missingRightsPopup.message")), true);
                }

                @Override
                public void invalidPath(String path) {

                    String message = format(getString(keyBase + ".invalidPathPopup.message"), path);
                    openPopup(new MessagePopup(getState(), message), true);
                }

                @Override
                public void occupiedPath(String path) {

                    String fileName = path.contains(File.SEPARATOR) ? path.substring(path.lastIndexOf(File.SEPARATOR) + 1) : path;
                    String message = format(getString(keyBase + ".occupiedPathPopup.message"), fileName);
                    openPopup(new MessagePopup(getState(), message), true);
                }

                @Override
                public void outOfSpace() {

                    openPopup(new MessagePopup(getState(), getString(keyBase + ".outOfSpacePopup.message")), true);
                }

            }

        }

        private class RemoveFileCallback implements Runnable {

            @Override
            public void run() {

                int[] selections = fileListTable.getSelectionManager().getSelectionModel().getSelection();
                for (int selection : selections) {
                    final String name = (String) fileListModel.getCell(selection, 0);

                    if (!name.equals("..")) {
                        String confirmMessage = format(getString("removeFile.confirmPopup.message"), name);
                        openPopup(new ConfirmPopup(getState(), confirmMessage, new Option[] { Option.NO, Option.YES }, new ConfirmPopup.Callback() {

                            @Override
                            public void onClose(Option selected) {

                                if (selected == Option.YES) {
                                    currentDirectory.removeFile(name, new InternalRemoveFileCallback());
                                }
                            }

                        }), true);

                    }
                }
            }

            private class InternalRemoveFileCallback implements CurrentDirectory.RemoveFileCallback {

                @Override
                public void success() {

                    update();
                }

                @Override
                public void missingRights() {

                    openPopup(new MessagePopup(getState(), getString("removeFile.missingRightsPopup.message")), true);
                }

            }

        }

    }

    /*
     * This class encapsulates the whole logic that is required for differentiating between directories and file systems.
     * The user doesn't notice the difference.
     */
    private static class CurrentDirectory {

        private static final Map<Class<? extends File<?>>, String> FILE_TYPE_KEYS = new HashMap<>();

        static {

            FILE_TYPE_KEYS.put(RootFile.class, "fileSystem");
            FILE_TYPE_KEYS.put(ContentFile.class, "content");
            FILE_TYPE_KEYS.put(Directory.class, "directory");

        }

        private final Bridge                                       bridge;
        private String                                             path;

        private CurrentDirectory(Bridge bridge, String startPath) {

            this.bridge = bridge;
            setPath(FileUtils.normalizePath(startPath));
        }

        private String getPath() {

            return path;
        }

        private void getChildren(final GetChildrenCallback callback) {

            ProgramLaunchInfoRequestEvent infoRequest = new ProgramLaunchInfoRequestEvent();
            bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(infoRequest, new EventHandler<ProgramLaunchInfoResponseEvent>() {

                @Override
                public void handle(ProgramLaunchInfoResponseEvent event) {

                    // Add FileListProgram handlers
                    EventPredicate<FileListProgram.FileListProgramEvent> predicate = new ProgramEventPredicate<>(event.getComputerId(), event.getPid());
                    bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FileListProgram.FileListProgramEvent>() {

                        @Override
                        public void handle(FileListProgram.FileListProgramEvent event) {

                            if (event instanceof FileListProgram.SuccessEvent) {
                                List<FileListEntry> children = new ArrayList<>();

                                if (!isRoot()) {
                                    // Add reference to parent file
                                    children.add(new FileListEntry("..", "parent", -1));
                                }

                                children.addAll(convertFilePlaceholdersToEntries( ((FileListProgram.SuccessEvent) event).getFiles()));
                                callback.success(children);
                            } else if (event instanceof FileListProgram.InvalidPathEvent) {
                                callback.invalidPath();
                            } else if (event instanceof FileListProgram.MissingRightsEvent) {
                                callback.missingRights();
                            }
                        }

                    }, predicate);

                    // Launch FileListProgram
                    String fileListProgramPath = ProgramUtils.getCommonLocation(FileListProgram.class);
                    Map<FeatureDefinitionReference<?>, Object> executorProperties = new HashMap<>();
                    executorProperties.put(new FeatureDefinitionReference<>(FileListProgram.class, FileListProgram.PATH), path);
                    bridge.send(new ProgramLaunchCommandEvent(event.getPid(), fileListProgramPath, executorProperties));
                }

            });
        }

        private List<FileListEntry> convertFilePlaceholdersToEntries(List<FilePlaceholder> files) {

            List<FileListEntry> entries = new ArrayList<>();

            for (FilePlaceholder file : files) {
                String typeKey = FILE_TYPE_KEYS.containsKey(file.getType()) ? FILE_TYPE_KEYS.get(file.getType()) : "unknown";
                entries.add(new FileListEntry(file.getName(), typeKey, file.getSize()));
            }

            return entries;
        }

        private boolean canCreateOrRemoveFiles() {

            return !isRoot();
        }

        private void createFile(final Class<? extends File<?>> type, final String name, final CreateFileCallback callback) {

            Validate.isTrue(canCreateOrRemoveFiles(), "Unnable to create files in dir '%s'", getPath());

            ProgramLaunchInfoRequestEvent infoRequest = new ProgramLaunchInfoRequestEvent();
            bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(infoRequest, new EventHandler<ProgramLaunchInfoResponseEvent>() {

                @Override
                public void handle(ProgramLaunchInfoResponseEvent event) {

                    // Add FileCreateProgram handlers
                    EventPredicate<FileCreateProgram.FileCreateProgramEvent> predicate = new ProgramEventPredicate<>(event.getComputerId(), event.getPid());
                    bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FileCreateProgram.FileCreateProgramEvent>() {

                        @Override
                        public void handle(FileCreateProgram.FileCreateProgramEvent event) {

                            if (event instanceof FileCreateProgram.SuccessEvent) {
                                callback.success();
                            } else if (event instanceof FileCreateProgram.InvalidPathEvent) {
                                callback.invalidPath( ((FileCreateProgram.InvalidPathEvent) event).getPath());
                            } else if (event instanceof FileCreateProgram.OccupiedPathEvent) {
                                callback.occupiedPath( ((FileCreateProgram.OccupiedPathEvent) event).getPath());
                            } else if (event instanceof FileCreateProgram.OutOfSpaceEvent) {
                                callback.outOfSpace();
                            } else if (event instanceof FileCreateProgram.MissingRightsEvent) {
                                callback.missingRights();
                            }
                        }

                    }, predicate);

                    // Launch FileCreateProgram
                    String fileCreateProgramPath = ProgramUtils.getCommonLocation(FileCreateProgram.class);
                    Map<FeatureDefinitionReference<?>, Object> executorProperties = new HashMap<>();
                    executorProperties.put(new FeatureDefinitionReference<>(FileCreateProgram.class, FileCreateProgram.PATH), FileUtils.resolvePath(getPath(), name));
                    executorProperties.put(new FeatureDefinitionReference<>(FileCreateProgram.class, FileCreateProgram.FILE_TYPE), type);
                    bridge.send(new ProgramLaunchCommandEvent(event.getPid(), fileCreateProgramPath, executorProperties));
                }

            });
        }

        private void removeFile(final String name, final RemoveFileCallback callback) {

            Validate.isTrue(canCreateOrRemoveFiles(), "Unnable to remove files in dir '%s'", getPath());

            ProgramLaunchInfoRequestEvent infoRequest = new ProgramLaunchInfoRequestEvent();
            bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(infoRequest, new EventHandler<ProgramLaunchInfoResponseEvent>() {

                @Override
                public void handle(ProgramLaunchInfoResponseEvent event) {

                    // Add FileRemoveProgram handlers
                    EventPredicate<FileRemoveProgram.FileRemoveProgramEvent> predicate = new ProgramEventPredicate<>(event.getComputerId(), event.getPid());
                    bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FileRemoveProgram.FileRemoveProgramEvent>() {

                        @Override
                        public void handle(FileRemoveProgram.FileRemoveProgramEvent event) {

                            if (event instanceof FileRemoveProgram.SuccessEvent) {
                                callback.success();
                            } else if (event instanceof FileRemoveProgram.MissingRightsEvent) {
                                callback.missingRights();
                            }
                        }

                    }, predicate);

                    // Launch FileRemoveProgram
                    String fileCreateProgramPath = ProgramUtils.getCommonLocation(FileRemoveProgram.class);
                    Map<FeatureDefinitionReference<?>, Object> executorProperties = new HashMap<>();
                    executorProperties.put(new FeatureDefinitionReference<>(FileRemoveProgram.class, FileRemoveProgram.PATH), FileUtils.resolvePath(getPath(), name));
                    bridge.send(new ProgramLaunchCommandEvent(event.getPid(), fileCreateProgramPath, executorProperties));
                }

            });
        }

        private void setPath(String path) {

            this.path = FileUtils.normalizePath(path);
        }

        private void useParent() {

            if (!isRoot()) {
                setPath(path.substring(0, path.lastIndexOf(File.SEPARATOR) + 1));
            }
        }

        private void useChild(String name) {

            setPath(path + (isRoot() ? "" : File.SEPARATOR) + name);
        }

        private boolean isRoot() {

            return path.equals(File.SEPARATOR);
        }

        private static interface GetChildrenCallback {

            public void success(List<FileListEntry> children);

            public void invalidPath();

            public void missingRights();

        }

        private static interface CreateFileCallback {

            public void success();

            public void missingRights();

            public void invalidPath(String path);

            public void occupiedPath(String path);

            public void outOfSpace();

        }

        private static interface RemoveFileCallback {

            public void success();

            public void missingRights();

        }

    }

    private static class FileListEntry {

        private final String name;
        private final String typeKey;
        private final long   size;

        private FileListEntry(String name, String typeKey, long size) {

            this.name = name;
            this.typeKey = typeKey;
            this.size = size;
        }

        private String getName() {

            return name;
        }

        private String getTypeKey() {

            return typeKey;
        }

        private long getSize() {

            return size;
        }

    }

}
