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
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileUtils;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
import com.quartercode.disconnected.server.world.comp.file.StringFileTypeMapper;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.comp.os.CommonFiles;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.server.world.comp.program.CommonLocation;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils.ImportantData;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateMissingRightsReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateOccupiedPathReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateOutOfSpaceReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateSuccessReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramGetCurrentPathRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramGetCurrentPathRequestEvent.FileManagerProgramGetCurrentPathReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramInvalidPathEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramListRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramListRequestEvent.FileManagerProgramListMissingRightsReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramListRequestEvent.FileManagerProgramListSuccessReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramSetCurrentPathRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramUnknownMountpointEvent;
import com.quartercode.disconnected.shared.event.util.FilePlaceholder;
import com.quartercode.disconnected.shared.util.PathUtils;
import com.quartercode.eventbridge.extra.extension.RequestEventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventSender;

/**
 * The file manager program is used to list, create, and remove {@link File}s.
 * 
 * @see ProgramExecutor
 */
@CommonLocation (dir = CommonFiles.SYS_BIN_DIR, file = "filemanager.exe")
public class FileManagerProgram extends ProgramExecutor {

    // ----- Properties -----

    /**
     * The current path of the file manager.
     * All operations are done in the directory represented by this path.
     */
    public static final PropertyDefinition<String> CURRENT_PATH;

    static {

        CURRENT_PATH = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "currentPath", "storage", new StandardStorage<>(), "initialValue", new ConstantValueFactory<>(PathUtils.SEPARATOR));

    }

    // ----- Functions -----

    static {

        RUN.addExecutor("registerGetCurrentPathHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getHolder();
                GetCurrentPathRequestEventHandler handler = new GetCurrentPathRequestEventHandler();
                handler.holder = holder;
                ProgramUtils.registerRequestEventHandler(holder, FileManagerProgramGetCurrentPathRequestEvent.class, handler);

                return invocation.next(arguments);
            }

        });
        RUN.addExecutor("registerSetCurrentPathHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getHolder();
                SetCurrentPathRequestEventHandler handler = new SetCurrentPathRequestEventHandler();
                handler.holder = holder;
                ProgramUtils.registerRequestEventHandler(holder, FileManagerProgramSetCurrentPathRequestEvent.class, handler);

                return invocation.next(arguments);
            }

        });
        RUN.addExecutor("registerListHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getHolder();
                ListRequestEventHandler handler = new ListRequestEventHandler();
                handler.holder = holder;
                ProgramUtils.registerRequestEventHandler(holder, FileManagerProgramListRequestEvent.class, handler);

                return invocation.next(arguments);
            }

        });
        RUN.addExecutor("registerCreateHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FileManagerProgram holder = (FileManagerProgram) invocation.getHolder();
                CreateRequestEventHandler handler = new CreateRequestEventHandler();
                handler.holder = holder;
                ProgramUtils.registerRequestEventHandler(holder, FileManagerProgramCreateRequestEvent.class, handler);

                return invocation.next(arguments);
            }

        });
        // RUN.addExecutor("registerRemoveHandler", FileManagerProgram.class, new FunctionExecutor<Void>() {
        //
        // @Override
        // public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {
        //
        // FileManagerProgram holder = (FileManagerProgram) invocation.getHolder();
        // RemoveRequestEventHandler handler = new RemoveRequestEventHandler();
        // handler.holder = holder;
        // ProgramUtils.registerRequestEventHandler(holder, FileManagerProgramRemoveRequestEvent.class, handler);
        //
        // return invocation.next(arguments);
        // }
        //
        // });

    }

    private static class GetCurrentPathRequestEventHandler implements RequestEventHandler<FileManagerProgramGetCurrentPathRequestEvent> {

        private FileManagerProgram holder;

        @Override
        public void handle(FileManagerProgramGetCurrentPathRequestEvent request, ReturnEventSender sender) {

            ImportantData data = ProgramUtils.getImportantData(holder);
            sender.send(new FileManagerProgramGetCurrentPathReturnEvent(data.getComputerId(), data.getPid(), holder.get(CURRENT_PATH).get()));
        }

    }

    private static class SetCurrentPathRequestEventHandler implements RequestEventHandler<FileManagerProgramSetCurrentPathRequestEvent> {

        private FileManagerProgram holder;

        @Override
        public void handle(FileManagerProgramSetCurrentPathRequestEvent request, ReturnEventSender sender) {

            String path = PathUtils.normalize(request.getPath());

            ImportantData data = ProgramUtils.getImportantData(holder);
            Process<?> process = holder.getParent();
            OperatingSystem os = process.get(Process.GET_OPERATING_SYSTEM).invoke();
            FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();

            if (!path.equals(PathUtils.SEPARATOR)) {
                File<?> rawDir = null;
                try {
                    rawDir = fsModule.get(FileSystemModule.GET_FILE).invoke(path);
                } catch (UnknownMountpointException e) {
                    sender.send(new FileManagerProgramUnknownMountpointEvent(data.getComputerId(), data.getPid(), e.getMountpoint()));
                    return;
                }

                if (! (rawDir instanceof ParentFile)) {
                    sender.send(new FileManagerProgramInvalidPathEvent(data.getComputerId(), data.getPid(), path));
                    return;
                }
            }

            holder.get(CURRENT_PATH).set(path);
        }

    }

    private static class ListRequestEventHandler implements RequestEventHandler<FileManagerProgramListRequestEvent> {

        private FileManagerProgram holder;

        @Override
        public void handle(FileManagerProgramListRequestEvent request, ReturnEventSender sender) {

            String path = holder.get(CURRENT_PATH).get();

            ImportantData data = ProgramUtils.getImportantData(holder);
            Process<?> process = holder.getParent();
            OperatingSystem os = process.get(Process.GET_OPERATING_SYSTEM).invoke();
            FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();

            if (path.equals(PathUtils.SEPARATOR)) {
                List<FilePlaceholder> files = new ArrayList<>();

                for (KnownFileSystem fileSystem : fsModule.get(FileSystemModule.GET_MOUNTED).invoke()) {
                    files.add(FileUtils.createFilePlaceholder(fileSystem));
                }

                sender.send(new FileManagerProgramListSuccessReturnEvent(data.getComputerId(), data.getPid(), files));
            } else {
                File<?> rawDir = null;
                try {
                    rawDir = fsModule.get(FileSystemModule.GET_FILE).invoke(path);
                } catch (UnknownMountpointException e) {
                    sender.send(new FileManagerProgramUnknownMountpointEvent(data.getComputerId(), data.getPid(), e.getMountpoint()));
                    return;
                }

                if (! (rawDir instanceof ParentFile)) {
                    sender.send(new FileManagerProgramInvalidPathEvent(data.getComputerId(), data.getPid(), path));
                } else {
                    ParentFile<?> dir = (ParentFile<?>) rawDir;

                    User sessionUser = process.get(Process.GET_USER).invoke();
                    if (!FileUtils.hasRight(sessionUser, dir, FileRight.READ)) {
                        sender.send(new FileManagerProgramListMissingRightsReturnEvent(data.getComputerId(), data.getPid(), path));
                    } else {
                        List<FilePlaceholder> files = new ArrayList<>();

                        String pathMountpoint = PathUtils.getComponents(path)[0];
                        for (File<?> file : dir.get(ParentFile.CHILDREN).get()) {
                            files.add(FileUtils.createFilePlaceholder(pathMountpoint, file));
                        }

                        sender.send(new FileManagerProgramListSuccessReturnEvent(data.getComputerId(), data.getPid(), files));
                    }
                }
            }
        }

    }

    private static class CreateRequestEventHandler implements RequestEventHandler<FileManagerProgramCreateRequestEvent> {

        private FileManagerProgram holder;

        @Override
        public void handle(FileManagerProgramCreateRequestEvent request, ReturnEventSender sender) {

            Validate.notNull(request.getSubpath(), "File creation subpath cannot be null");
            String path = PathUtils.resolve(holder.get(CURRENT_PATH).get(), request.getSubpath());
            Validate.notNull(request.getType(), "File type cannot be null");
            Class<? extends File<?>> fileType = StringFileTypeMapper.stringToClass(request.getType());
            Validate.notNull(request.getType(), "File type '%s' is unknown", request.getType());

            ImportantData data = ProgramUtils.getImportantData(holder);
            Process<?> process = holder.getParent();
            OperatingSystem os = process.get(Process.GET_OPERATING_SYSTEM).invoke();

            File<?> addFile = null;
            try {
                addFile = fileType.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot create instance of file type '" + fileType.getName() + "'", e);
            }

            User sessionUser = process.get(Process.GET_USER).invoke();
            addFile.get(File.OWNER).set(sessionUser);
            addFile.get(File.GROUP).set(sessionUser.get(User.GET_PRIMARY_GROUP).invoke());

            FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();
            FileAddAction addAction = null;
            try {
                addAction = fsModule.get(FileSystemModule.CREATE_ADD_FILE).invoke(addFile, path);
            } catch (UnknownMountpointException e) {
                sender.send(new FileManagerProgramUnknownMountpointEvent(data.getComputerId(), data.getPid(), e.getMountpoint()));
            }

            if (addAction != null) {
                if (addAction.get(FileAddAction.IS_EXECUTABLE_BY).invoke(sessionUser)) {
                    try {
                        addAction.get(FileAddAction.EXECUTE).invoke();
                        sender.send(new FileManagerProgramCreateSuccessReturnEvent(data.getComputerId(), data.getPid()));
                    } catch (InvalidPathException e) {
                        sender.send(new FileManagerProgramInvalidPathEvent(data.getComputerId(), data.getPid(), path));
                    } catch (OccupiedPathException e) {
                        sender.send(new FileManagerProgramCreateOccupiedPathReturnEvent(data.getComputerId(), data.getPid(), path));
                    } catch (OutOfSpaceException e) {
                        sender.send(new FileManagerProgramCreateOutOfSpaceReturnEvent(data.getComputerId(), data.getPid(), PathUtils.getComponents(path)[0], e.getSize()));
                    }
                } else {
                    sender.send(new FileManagerProgramCreateMissingRightsReturnEvent(data.getComputerId(), data.getPid(), path));
                }
            }
        }

    }

    // private static class RemoveRequestEventHandler implements RequestEventHandler<FileManagerProgramRemoveRequestEvent> {
    //
    // private FileManagerProgram holder;
    //
    // @Override
    // public void handle(FileManagerProgramRemoveRequestEvent request, ReturnEventSender sender) {
    //
    // String path = PathUtils.normalize(request.getPath());
    // Validate.notNull(path, "File deletion path cannot be null");
    //
    // ImportantData data = ProgramUtils.getImportantData(holder);
    // Process<?> process = holder.getParent();
    // OperatingSystem os = process.get(Process.GET_OPERATING_SYSTEM).invoke();
    // FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();
    //
    // File<?> removeFile = null;
    // try {
    // removeFile = fsModule.get(FileSystemModule.GET_FILE).invoke(path);
    // } catch (UnknownMountpointException e) {
    // sender.send(new FileManagerProgramRemoveUnknownMountpointReturnEvent(data.getComputerId(), data.getPid(), e.getMountpoint()));
    // return;
    // }
    //
    // if (removeFile == null) {
    // sender.send(new FileManagerProgramRemoveInvalidPathReturnEvent(data.getComputerId(), data.getPid(), path));
    // } else {
    // FileRemoveAction removeAction = removeFile.get(File.CREATE_REMOVE).invoke();
    //
    // User sessionUser = process.get(Process.GET_USER).invoke();
    // if (removeAction.get(FileAddAction.IS_EXECUTABLE_BY).invoke(sessionUser)) {
    // removeAction.get(FileRemoveAction.EXECUTE).invoke();
    // sender.send(new FileManagerProgramRemoveSuccessReturnEvent(data.getComputerId(), data.getPid()));
    // } else {
    // sender.send(new FileManagerProgramRemoveMissingRightsReturnEvent(data.getComputerId(), data.getPid()));
    // }
    // }
    // }
    //
    // }

}
