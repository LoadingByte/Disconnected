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

package com.quartercode.disconnected.server.test.world.comp.program.general;

import static com.quartercode.disconnected.server.world.comp.program.ProgramUtils.getCommonLocation;
import static com.quartercode.disconnected.shared.util.PathUtils.getComponents;
import static org.junit.Assert.*;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.program.AbstractProgramTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.os.CommonFiles;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.Session;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.server.world.comp.program.ChildProcess;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils.ImportantData;
import com.quartercode.disconnected.server.world.comp.program.general.FileManagerProgram;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateMissingRightsReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateOccupiedPathReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateOutOfSpaceReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateSuccessReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramInvalidPathEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramSetCurrentPathRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramUnknownMountpointEvent;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;

public class FileManagerProgramCreateTest extends AbstractProgramTest {

    private static final String LOCAL_PARENT_PATH = "test1/test2";
    private static final String LOCAL_PATH        = LOCAL_PARENT_PATH + "/test.txt";
    private static final String PATH              = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/" + LOCAL_PATH;

    public FileManagerProgramCreateTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private ImportantData executeProgramAndSetCurrentPath(Process<?> parentProcess, String path) {

        ChildProcess process = parentProcess.get(Process.CREATE_CHILD).invoke();
        process.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke(getComponents(getCommonLocation(FileManagerProgram.class))[1]));
        process.get(Process.INITIALIZE).invoke(10);

        ProgramExecutor program = process.get(Process.EXECUTOR).get();
        program.get(ProgramExecutor.RUN).invoke();

        ImportantData data = ProgramUtils.getImportantData(program);

        Event setCurrentPathEvent = new FileManagerProgramSetCurrentPathRequestEvent(data.getComputerId(), data.getPid(), path);
        bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(setCurrentPathEvent, null);

        return data;
    }

    private void sendCreateRequest(ImportantData data, String subpath, EventHandler<?> returnHandler) {

        Event request = new FileManagerProgramCreateRequestEvent(data.getComputerId(), data.getPid(), subpath, "contentFile");
        bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(request, returnHandler);
    }

    @Test
    public void testSuccess() {

        sendCreateRequest(executeProgramAndSetCurrentPath(processModule.get(ProcessModule.ROOT_PROCESS).get(), CommonFiles.SYSTEM_MOUNTPOINT), LOCAL_PATH, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return success event", event instanceof FileManagerProgramCreateSuccessReturnEvent);

                File<?> file = fileSystem.get(FileSystem.GET_FILE).invoke(LOCAL_PATH);
                assertNotNull("File wasn't created", file);
                assertTrue("New file isn't ContentFile", file instanceof ContentFile);
            }

        });
    }

    @Test
    public void testUnknownMountpoint() {

        ImportantData data = executeProgramAndSetCurrentPath(processModule.get(ProcessModule.ROOT_PROCESS).get(), CommonFiles.SYSTEM_MOUNTPOINT);

        // Unmount the file system
        FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();
        fsModule.get(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT).invoke(fileSystemMountpoint).get(KnownFileSystem.MOUNTED).set(false);

        sendCreateRequest(data, LOCAL_PATH, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return unknown mountpoint event", event instanceof FileManagerProgramUnknownMountpointEvent);
                assertEquals("Unknown mountpoint", CommonFiles.SYSTEM_MOUNTPOINT, ((FileManagerProgramUnknownMountpointEvent) event).getMountpoint());
            }

        });
    }

    @Test
    public void testInvalidPath() {

        // Add content file that makes the path invalid
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), LOCAL_PARENT_PATH).get(FileAddAction.EXECUTE).invoke();

        sendCreateRequest(executeProgramAndSetCurrentPath(processModule.get(ProcessModule.ROOT_PROCESS).get(), CommonFiles.SYSTEM_MOUNTPOINT), LOCAL_PATH, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return invalid path event", event instanceof FileManagerProgramInvalidPathEvent);
                assertEquals("Invalid path", PATH, ((FileManagerProgramInvalidPathEvent) event).getPath());
            }

        });
    }

    @Test
    public void testOccupiedPath() {

        // Add content file that makes the path occupied
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), LOCAL_PATH).get(FileAddAction.EXECUTE).invoke();

        sendCreateRequest(executeProgramAndSetCurrentPath(processModule.get(ProcessModule.ROOT_PROCESS).get(), CommonFiles.SYSTEM_MOUNTPOINT), LOCAL_PATH, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return occupied path event", event instanceof FileManagerProgramCreateOccupiedPathReturnEvent);
                assertEquals("Occupied path", PATH, ((FileManagerProgramCreateOccupiedPathReturnEvent) event).getPath());
            }

        });
    }

    @Test
    public void testOutOfSpace() {

        // Set size of the file system to something very small
        fileSystem.get(FileSystem.SIZE).set(40L);

        sendCreateRequest(executeProgramAndSetCurrentPath(processModule.get(ProcessModule.ROOT_PROCESS).get(), CommonFiles.SYSTEM_MOUNTPOINT), PATH, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return out of space event", event instanceof FileManagerProgramCreateOutOfSpaceReturnEvent);

                FileManagerProgramCreateOutOfSpaceReturnEvent castedEvent = (FileManagerProgramCreateOutOfSpaceReturnEvent) event;
                assertEquals("File system which is out of space", fileSystemMountpoint, castedEvent.getFileSystemMountpoint());
                assertTrue("Required space for a new file is greater than 0", castedEvent.getRequiredSpace() > 0);
            }

        });
    }

    @Test
    public void testMissingRights() {

        User testUser = new User();
        testUser.get(User.NAME).set("testUser");

        ChildProcess sessionProcess = processModule.get(ProcessModule.ROOT_PROCESS).get().get(Process.CREATE_CHILD).invoke();
        sessionProcess.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke(getComponents(getCommonLocation(Session.class))[1]));
        sessionProcess.get(Process.INITIALIZE).invoke(1);
        ProgramExecutor session = sessionProcess.get(Process.EXECUTOR).get();
        session.get(Session.USER).set(testUser);
        session.get(ProgramExecutor.RUN).invoke();

        sendCreateRequest(executeProgramAndSetCurrentPath(sessionProcess, CommonFiles.SYSTEM_MOUNTPOINT), PATH, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return missing rights event", event instanceof FileManagerProgramCreateMissingRightsReturnEvent);
            }

        });
    }

}
