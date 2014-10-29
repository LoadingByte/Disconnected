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

package com.quartercode.disconnected.server.world.comp.program.general;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileUtils;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
import com.quartercode.disconnected.server.world.comp.file.StringFileTypeMapper;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.shared.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.comp.file.FileRights;
import com.quartercode.disconnected.shared.comp.file.PathUtils;
import com.quartercode.disconnected.shared.comp.program.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.event.program.general.FMPWPUUpdateViewCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldAddFileCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldChangeDirCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldRemoveFileCommand;
import com.quartercode.disconnected.shared.event.program.generic.GPWPUErrorEvent;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * The file manager program is used to list, create, and remove {@link File}s.
 * 
 * @see ProgramExecutor
 */
public class FileManagerProgram extends ProgramExecutor {

    /**
     * This list contains all file types the program is allowed to create.
     * It can be modified in order to add new file types.
     */
    public static final List<String>               ALLOWED_FILE_TYPES = new ArrayList<>();

    static {

        ALLOWED_FILE_TYPES.add("contentFile");
        ALLOWED_FILE_TYPES.add("directory");

    }

    // ----- Properties -----

    /**
     * The current path of the file manager.
     * All operations are done in the directory represented by this path.
     */
    public static final PropertyDefinition<String> CURRENT_DIR;

    static {

        CURRENT_DIR = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "currentPath", "storage", new StandardStorage<>(), "initialValue", new ConstantValueFactory<>(PathUtils.SEPARATOR));

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("registerChangeDirCommandHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getCHolder();
                ChangeDirCommandHandler handler = new ChangeDirCommandHandler();
                handler.holder = holder;
                ProgramUtils.registerSBPAwareEventHandler(holder, FMPWorldChangeDirCommand.class, handler, true);

                return invocation.next(arguments);
            }

        });
        RUN.addExecutor("registerAddFileCommandHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getCHolder();
                AddFileCommandHandler handler = new AddFileCommandHandler();
                handler.holder = holder;
                ProgramUtils.registerSBPAwareEventHandler(holder, FMPWorldAddFileCommand.class, handler, true);

                return invocation.next(arguments);
            }

        });
        RUN.addExecutor("registerRemoveFileCommandHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getCHolder();
                RemoveFileCommandHandler handler = new RemoveFileCommandHandler();
                handler.holder = holder;
                ProgramUtils.registerSBPAwareEventHandler(holder, FMPWorldRemoveFileCommand.class, handler, true);

                return invocation.next(arguments);
            }

        });

    }

    private static class ChangeDirCommandHandler implements SBPAwareEventHandler<FMPWorldChangeDirCommand> {

        private FileManagerProgram holder;

        @Override
        public void handle(FMPWorldChangeDirCommand event, SBPIdentity sender) {

            String change = event.getChange();
            Validate.notBlank(change, "Path change cannot be blank");

            changeCurrentDir(change);
        }

        private void changeCurrentDir(String change) {

            // Verify that the currently set dir is still valid before updating.
            // If it is, the change update is applied to the valid current dir.
            // If it isn't, the current is set to the next valid dir and the requested change is ignored.
            if (verifyAndUpdateCurrentDir(holder.getObj(CURRENT_DIR))) {
                String newDirPath = PathUtils.resolve(holder.getObj(CURRENT_DIR), change);

                int validationResult = verifyDir(newDirPath);

                if (validationResult == 0) {
                    // New dir is valid -> update current dir and send change to sbp later on
                    holder.setObj(CURRENT_DIR, newDirPath);
                } else if (validationResult == 3) {
                    // Missing read right on new dir -> do not update current dir and send error message to sbp
                    SBPWorldProcessUserId wpuId = holder.getParent().getObj(Process.WORLD_PROCESS_USER);
                    holder.getBridge().send(new GPWPUErrorEvent(wpuId, "fileList.missingReadRight", new String[] { newDirPath }));
                    return;
                } else {
                    // New dir does not exist or is not a directory -> do not update current dir
                    return;
                }

                holder.setObj(CURRENT_DIR, newDirPath);
            }

            // Send the updated dir to the sbp
            sendUpdateView(holder.getParent(), holder.getBridge(), holder.getObj(CURRENT_DIR));
        }

        /*
         * This method verifies that the given dir is valid before.
         * If it isn't, this method jumps up one file in the file hierarchy and checks again.
         * When it finally reaches a valid directory, it stops and sets that directory as the new current dir.
         * Note that this method only returns true if the first input dir is already valid.
         */
        private boolean verifyAndUpdateCurrentDir(String currentDir) {

            if (verifyDir(currentDir) != 0) {
                verifyAndUpdateCurrentDir(PathUtils.resolve(currentDir, ".."));
                return false;
            } else {
                holder.setObj(CURRENT_DIR, currentDir);
                return true;
            }
        }

        /*
         * Return codes:
         * 0 -> Valid dir
         * 1 -> Unknown mountpoint
         * 2 -> File does not exist or isn't a dir
         * 3 -> Missing read right
         */
        private int verifyDir(String path) {

            if (!path.equals(PathUtils.SEPARATOR)) {
                try {
                    Process<?> process = holder.getParent();
                    FileSystemModule fsModule = process.invoke(Process.GET_OPERATING_SYSTEM).getObj(OperatingSystem.FS_MODULE);
                    File<?> dir = fsModule.invoke(FileSystemModule.GET_FILE, path);

                    if (! (dir instanceof ParentFile)) {
                        // File does not exist or is not a directory
                        return 2;
                    }

                    User sessionUser = process.invoke(Process.GET_USER);
                    if (!FileUtils.hasRight(sessionUser, dir, FileRights.READ)) {
                        // Missing read right on directory
                        return 3;
                    }
                } catch (UnknownMountpointException e) {
                    // No file system with referenced mountpoint is mounted
                    return 1;
                }
            }

            return 0;
        }

    }

    private static class AddFileCommandHandler implements SBPAwareEventHandler<FMPWorldAddFileCommand> {

        private FileManagerProgram holder;

        @Override
        public void handle(FMPWorldAddFileCommand event, SBPIdentity sender) {

            if (holder.getObj(CURRENT_DIR).equals(PathUtils.SEPARATOR)) {
                throw new IllegalStateException("Cannot create a file when the current path is set to the absolute root");
            }

            String fileName = event.getFileName();
            Validate.notBlank(fileName, "File name cannot be blank");

            if (PathUtils.split(fileName).length != 1) {
                SBPWorldProcessUserId wpuId = holder.getParent().getObj(Process.WORLD_PROCESS_USER);
                holder.getBridge().send(new GPWPUErrorEvent(wpuId, "createFile.invalidFileName", new String[] { fileName }));
                return;
            }

            String fileType = event.getFileType();
            Validate.notBlank(fileName, "File type cannot be blank");
            Validate.isTrue(ALLOWED_FILE_TYPES.contains(fileType), "File type ('%s') must be one of the allowed ones: %s", fileType, ALLOWED_FILE_TYPES);

            addFile(fileName, fileType);
        }

        private void addFile(String fileName, String fileType) {

            SBPWorldProcessUserId wpuId = holder.getParent().getObj(Process.WORLD_PROCESS_USER);
            Process<?> process = holder.getParent();
            FileSystemModule fsModule = process.invoke(Process.GET_OPERATING_SYSTEM).getObj(OperatingSystem.FS_MODULE);

            String currentDir = holder.getObj(CURRENT_DIR);
            String filePath = PathUtils.resolve(currentDir, fileName);

            if (filePath.equals(currentDir)) {
                holder.getBridge().send(new GPWPUErrorEvent(wpuId, "createFile.invalidFileName", new String[] { fileName }));
                return;
            }

            File<?> file = StringFileTypeMapper.stringToNewInstance(fileType);
            Validate.notNull(file, "Allowed file type ('%s') is not known", fileType);

            FileAddAction addAction = fsModule.invoke(FileSystemModule.CREATE_ADD_FILE, file, filePath);
            User sessionUser = process.invoke(Process.GET_USER);

            if (addAction.invoke(FileAddAction.IS_EXECUTABLE_BY, sessionUser)) {
                try {
                    addAction.invoke(FileAddAction.EXECUTE);
                    sendUpdateView(process, holder.getBridge(), holder.getObj(CURRENT_DIR));
                } catch (OccupiedPathException e) {
                    holder.getBridge().send(new GPWPUErrorEvent(wpuId, "createFile.occupiedPath", new String[] { filePath }));
                } catch (OutOfSpaceException e) {
                    holder.getBridge().send(new GPWPUErrorEvent(wpuId, "createFile.outOfSpace", new String[] { filePath }));
                }
            } else {
                holder.getBridge().send(new GPWPUErrorEvent(wpuId, "createFile.missingWriteRight", new String[] { filePath }));
            }
        }

    }

    private static class RemoveFileCommandHandler implements SBPAwareEventHandler<FMPWorldRemoveFileCommand> {

        private FileManagerProgram holder;

        @Override
        public void handle(FMPWorldRemoveFileCommand event, SBPIdentity sender) {

            if (holder.getObj(CURRENT_DIR).equals(PathUtils.SEPARATOR)) {
                throw new IllegalStateException("Cannot delete a file when the current path is set to the absolute root");
            }

            String fileName = event.getFileName();
            Validate.notBlank(fileName, "File name cannot be blank");

            if (PathUtils.split(fileName).length != 1) {
                SBPWorldProcessUserId wpuId = holder.getParent().getObj(Process.WORLD_PROCESS_USER);
                holder.getBridge().send(new GPWPUErrorEvent(wpuId, "removeFile.invalidFileName", new String[] { fileName }));
                return;
            }

            removeFile(fileName);
        }

        private void removeFile(String fileName) {

            SBPWorldProcessUserId wpuId = holder.getParent().getObj(Process.WORLD_PROCESS_USER);
            Process<?> process = holder.getParent();
            FileSystemModule fsModule = process.invoke(Process.GET_OPERATING_SYSTEM).getObj(OperatingSystem.FS_MODULE);

            String currentDir = holder.getObj(CURRENT_DIR);
            String filePath = PathUtils.resolve(currentDir, fileName);

            if (filePath.equals(currentDir)) {
                holder.getBridge().send(new GPWPUErrorEvent(wpuId, "removeFile.invalidFileName", new String[] { fileName }));
                return;
            }

            File<?> file;
            try {
                file = fsModule.invoke(FileSystemModule.GET_FILE, filePath);
            } catch (UnknownMountpointException e) {
                return;
            }

            FileRemoveAction removeAction = file.invoke(File.CREATE_REMOVE);
            User sessionUser = process.invoke(Process.GET_USER);

            if (removeAction.invoke(FileRemoveAction.IS_EXECUTABLE_BY, sessionUser)) {
                removeAction.invoke(FileRemoveAction.EXECUTE);
                sendUpdateView(process, holder.getBridge(), holder.getObj(CURRENT_DIR));
            } else {
                holder.getBridge().send(new GPWPUErrorEvent(wpuId, "removeFile.missingDeleteRight", new String[] { filePath }));
            }
        }

    }

    private static void sendUpdateView(Process<?> process, Bridge bridge, String currentDir) {

        SBPWorldProcessUserId wpuId = process.getObj(Process.WORLD_PROCESS_USER);
        FileSystemModule fsModule = process.invoke(Process.GET_OPERATING_SYSTEM).getObj(OperatingSystem.FS_MODULE);

        // Current dir is absolute root
        if (currentDir.equals(PathUtils.SEPARATOR)) {
            List<FilePlaceholder> files = new ArrayList<>();

            for (KnownFileSystem fileSystem : fsModule.invoke(FileSystemModule.GET_MOUNTED)) {
                files.add(FileUtils.createFilePlaceholder(fileSystem));
            }

            FilePlaceholder[] fileArray = files.toArray(new FilePlaceholder[files.size()]);
            bridge.send(new FMPWPUUpdateViewCommand(wpuId, currentDir, fileArray));
        }
        // Current dir is normal dir
        else {
            String pathMountpoint = PathUtils.splitAfterMountpoint(currentDir)[0];
            List<FilePlaceholder> files = new ArrayList<>();

            ParentFile<?> dir = (ParentFile<?>) fsModule.invoke(FileSystemModule.GET_FILE, currentDir);
            for (File<?> file : dir.getCol(ParentFile.CHILDREN)) {
                files.add(FileUtils.createFilePlaceholder(pathMountpoint, file));
            }

            FilePlaceholder[] fileArray = files.toArray(new FilePlaceholder[files.size()]);
            bridge.send(new FMPWPUUpdateViewCommand(wpuId, currentDir, fileArray));
        }
    }

}
