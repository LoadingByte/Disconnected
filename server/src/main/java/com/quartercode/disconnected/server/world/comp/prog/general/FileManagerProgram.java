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

package com.quartercode.disconnected.server.world.comp.prog.general;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import static com.quartercode.disconnected.server.world.comp.prog.util.ProgEventUtils.registerSBPAwareEventHandler;
import static com.quartercode.disconnected.server.world.comp.prog.util.ProgStateUtils.addInterruptionStopperRegisteringExecutor;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.world.comp.file.FSModule;
import com.quartercode.disconnected.server.world.comp.file.FSModule.KnownFS;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.server.world.comp.file.FileUtils;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.comp.os.OS;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_AddFileCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_ChangeDirCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_RemoveFileCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.world.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;
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

        CURRENT_DIR = factory(PropertyDefinitionFactory.class).create("currentPath", new StandardStorage<>(), new ConstantValueFactory<>(PathUtils.SEPARATOR));

    }

    // ----- Functions -----

    static {

        addInterruptionStopperRegisteringExecutor(FileManagerProgram.class);

        RUN.addExecutor("registerChangeDirCommandHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getCHolder();
                registerSBPAwareEventHandler(holder, FMP_WP_ChangeDirCommand.class, new ChangeDirCommandHandler(holder), true);

                return invocation.next(arguments);
            }

        });
        RUN.addExecutor("registerAddFileCommandHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getCHolder();
                registerSBPAwareEventHandler(holder, FMP_WP_AddFileCommand.class, new AddFileCommandHandler(holder), true);

                return invocation.next(arguments);
            }

        });
        RUN.addExecutor("registerRemoveFileCommandHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getCHolder();
                registerSBPAwareEventHandler(holder, FMP_WP_RemoveFileCommand.class, new RemoveFileCommandHandler(holder), true);

                return invocation.next(arguments);
            }

        });

    }

    /**
     * An {@link SBPAwareEventHandler} that processes {@link FMP_WP_ChangeDirCommand} for a specific {@link FileManagerProgram} instance.
     * 
     * @see FileManagerProgram
     */
    @RequiredArgsConstructor
    public static class ChangeDirCommandHandler implements SBPAwareEventHandler<FMP_WP_ChangeDirCommand> {

        private final FileManagerProgram holder;

        @Override
        public void handle(FMP_WP_ChangeDirCommand event, SBPIdentity sender) {

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
                    // New dir is valid -> update current dir and send change to SBP later on
                    holder.setObj(CURRENT_DIR, newDirPath);
                } else if (validationResult == 3) {
                    // Missing read right on new dir -> do not update current dir and send error message to SBP
                    SBPWorldProcessUserId wpuId = holder.getParent().getObj(Process.WORLD_PROCESS_USER);
                    holder.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "fileList.missingReadRight", new String[] { newDirPath }));
                    return;
                } else {
                    // New dir does not exist or is not a directory -> do not update current dir
                    return;
                }

                holder.setObj(CURRENT_DIR, newDirPath);
            }

            // Send the updated dir to the SBP
            sendUpdateView(holder.getParent(), holder.getObj(CURRENT_DIR));
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
                    FSModule fsModule = process.invoke(Process.GET_OS).getObj(OS.FS_MODULE);
                    File<?> dir = fsModule.invoke(FSModule.GET_FILE, path);

                    if (! (dir instanceof ParentFile)) {
                        // File does not exist or is not a directory
                        return 2;
                    }

                    User sessionUser = process.invoke(Process.GET_USER);
                    if (!dir.invoke(File.HAS_RIGHT, sessionUser, FileRights.READ)) {
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

    /**
     * An {@link SBPAwareEventHandler} that processes {@link FMP_WP_AddFileCommand} for a specific {@link FileManagerProgram} instance.
     * 
     * @see FileManagerProgram
     */
    @RequiredArgsConstructor
    public static class AddFileCommandHandler implements SBPAwareEventHandler<FMP_WP_AddFileCommand> {

        private final FileManagerProgram holder;

        @Override
        public void handle(FMP_WP_AddFileCommand event, SBPIdentity sender) {

            Validate.validState(!holder.getObj(CURRENT_DIR).equals(PathUtils.SEPARATOR), "Cannot create a file when the current path is set to the absolute root");

            String fileName = event.getFileName();
            Validate.notBlank(fileName, "File name cannot be blank");

            if (PathUtils.split(fileName).length != 1) {
                SBPWorldProcessUserId wpuId = holder.getParent().getObj(Process.WORLD_PROCESS_USER);
                holder.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "createFile.invalidFileName", new String[] { fileName }));
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
            FSModule fsModule = process.invoke(Process.GET_OS).getObj(OS.FS_MODULE);

            String currentDir = holder.getObj(CURRENT_DIR);
            String filePath = PathUtils.resolve(currentDir, fileName);

            if (filePath.equals(currentDir)) {
                holder.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "createFile.invalidFileName", new String[] { fileName }));
                return;
            }

            Class<?> fileClass = Registries.get(ServerRegistries.FILE_TYPES).getRight(fileType);
            Validate.notNull(fileClass, "Allowed file type ('%s') is not known", fileType);
            File<?> file;
            try {
                file = (File<?>) fileClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Error while creating new instance of file type '" + fileType + "'", e);
            }

            FileAddAction addAction = fsModule.invoke(FSModule.CREATE_ADD_FILE, file, filePath);
            User sessionUser = process.invoke(Process.GET_USER);

            if (addAction.invoke(FileAddAction.IS_EXECUTABLE_BY, sessionUser)) {
                try {
                    addAction.invoke(FileAddAction.EXECUTE);
                    sendUpdateView(process, holder.getObj(CURRENT_DIR));
                } catch (OccupiedPathException e) {
                    holder.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "createFile.occupiedPath", new String[] { filePath }));
                } catch (OutOfSpaceException e) {
                    holder.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "createFile.outOfSpace", new String[] { filePath }));
                }
            } else {
                holder.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "createFile.missingWriteRight", new String[] { filePath }));
            }
        }

    }

    /**
     * An {@link SBPAwareEventHandler} that processes {@link FMP_WP_RemoveFileCommand} for a specific {@link FileManagerProgram} instance.
     * 
     * @see FileManagerProgram
     */
    @RequiredArgsConstructor
    public static class RemoveFileCommandHandler implements SBPAwareEventHandler<FMP_WP_RemoveFileCommand> {

        private final FileManagerProgram holder;

        @Override
        public void handle(FMP_WP_RemoveFileCommand event, SBPIdentity sender) {

            Validate.validState(!holder.getObj(CURRENT_DIR).equals(PathUtils.SEPARATOR), "Cannot delete a file when the current path is set to the absolute root");

            String fileName = event.getFileName();
            Validate.notBlank(fileName, "File name cannot be blank");

            if (PathUtils.split(fileName).length != 1) {
                SBPWorldProcessUserId wpuId = holder.getParent().getObj(Process.WORLD_PROCESS_USER);
                holder.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "removeFile.invalidFileName", new String[] { fileName }));
                return;
            }

            removeFile(fileName);
        }

        private void removeFile(String fileName) {

            SBPWorldProcessUserId wpuId = holder.getParent().getObj(Process.WORLD_PROCESS_USER);
            Process<?> process = holder.getParent();
            FSModule fsModule = process.invoke(Process.GET_OS).getObj(OS.FS_MODULE);

            String currentDir = holder.getObj(CURRENT_DIR);
            String filePath = PathUtils.resolve(currentDir, fileName);

            if (filePath.equals(currentDir)) {
                holder.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "removeFile.invalidFileName", new String[] { fileName }));
                return;
            }

            File<?> file;
            try {
                file = fsModule.invoke(FSModule.GET_FILE, filePath);
            } catch (UnknownMountpointException e) {
                return;
            }

            FileRemoveAction removeAction = file.invoke(File.CREATE_REMOVE);
            User sessionUser = process.invoke(Process.GET_USER);

            if (removeAction.invoke(FileRemoveAction.IS_EXECUTABLE_BY, sessionUser)) {
                removeAction.invoke(FileRemoveAction.EXECUTE);
                sendUpdateView(process, holder.getObj(CURRENT_DIR));
            } else {
                holder.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "removeFile.missingDeleteRight", new String[] { filePath }));
            }
        }

    }

    /**
     * Sends an {@link FMP_SBPWPU_UpdateViewCommand} from the given {@link FileManagerProgram} {@link Process}.
     * Note that this method is completely independent from any state (it's a utility method).
     * 
     * @param process The file manager program process which sends the command.
     * @param currentDir The directory whose contents should be sent inside the command.
     */
    public static void sendUpdateView(Process<?> process, String currentDir) {

        Bridge bridge = process.getBridge();
        SBPWorldProcessUserId wpuId = process.getObj(Process.WORLD_PROCESS_USER);
        FSModule fsModule = process.invoke(Process.GET_OS).getObj(OS.FS_MODULE);

        // Current dir is absolute root
        if (currentDir.equals(PathUtils.SEPARATOR)) {
            List<FilePlaceholder> files = new ArrayList<>();

            for (KnownFS fileSystem : fsModule.invoke(FSModule.GET_MOUNTED)) {
                files.add(FileUtils.createFilePlaceholder(fileSystem));
            }

            FilePlaceholder[] fileArray = files.toArray(new FilePlaceholder[files.size()]);
            bridge.send(new FMP_SBPWPU_UpdateViewCommand(wpuId, currentDir, fileArray));
        }
        // Current dir is normal dir
        else {
            String pathMountpoint = PathUtils.splitAfterMountpoint(currentDir)[0];
            List<FilePlaceholder> files = new ArrayList<>();

            ParentFile<?> dir = (ParentFile<?>) fsModule.invoke(FSModule.GET_FILE, currentDir);
            for (File<?> file : dir.getColl(ParentFile.CHILDREN)) {
                files.add(FileUtils.createFilePlaceholder(pathMountpoint, file));
            }

            FilePlaceholder[] fileArray = files.toArray(new FilePlaceholder[files.size()]);
            bridge.send(new FMP_SBPWPU_UpdateViewCommand(wpuId, currentDir, fileArray));
        }
    }

}
