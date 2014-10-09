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

import static com.quartercode.disconnected.server.world.comp.program.ProgramCommonLocationMapper.getCommonLocation;
import static com.quartercode.disconnected.shared.util.PathUtils.splitAfterMountpoint;
import static org.junit.Assert.*;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.program.AbstractProgramTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
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
import com.quartercode.disconnected.shared.constant.CommonFiles;
import com.quartercode.disconnected.shared.event.comp.program.ProgramMissingFileRightsEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateOccupiedPathReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateOutOfSpaceReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramCreateRequestEvent.FileManagerProgramCreateSuccessReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramInvalidPathEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramSetCurrentPathRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramUnknownMountpointEvent;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
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

        ChildProcess process = parentProcess.invoke(Process.CREATE_CHILD);
        process.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(FileManagerProgram.class).toString())[1]));
        process.invoke(Process.INITIALIZE, 10);

        ProgramExecutor program = process.getObj(Process.EXECUTOR);
        program.invoke(ProgramExecutor.RUN);

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

        sendCreateRequest(executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), CommonFiles.SYSTEM_MOUNTPOINT), LOCAL_PATH, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return success event", event instanceof FileManagerProgramCreateSuccessReturnEvent);

                File<?> file = fileSystem.invoke(FileSystem.GET_FILE, LOCAL_PATH);
                assertNotNull("File wasn't created", file);
                assertTrue("New file isn't ContentFile", file instanceof ContentFile);
            }

        });
    }

    @Test
    public void testUnknownMountpoint() {

        ImportantData data = executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), CommonFiles.SYSTEM_MOUNTPOINT);

        // Unmount the file system
        FileSystemModule fsModule = os.getObj(OperatingSystem.FS_MODULE);
        fsModule.invoke(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT, fileSystemMountpoint).setObj(KnownFileSystem.MOUNTED, false);

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
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new ContentFile(), LOCAL_PARENT_PATH).invoke(FileAddAction.EXECUTE);

        sendCreateRequest(executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), CommonFiles.SYSTEM_MOUNTPOINT), LOCAL_PATH, new EventHandler<Event>() {

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
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new ContentFile(), LOCAL_PATH).invoke(FileAddAction.EXECUTE);

        sendCreateRequest(executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), CommonFiles.SYSTEM_MOUNTPOINT), LOCAL_PATH, new EventHandler<Event>() {

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
        fileSystem.setObj(FileSystem.SIZE, 40L);

        sendCreateRequest(executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), CommonFiles.SYSTEM_MOUNTPOINT), PATH, new EventHandler<Event>() {

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

        final User testUser = new User();
        testUser.setObj(User.NAME, "testUser");

        ChildProcess sessionProcess = processModule.getObj(ProcessModule.ROOT_PROCESS).invoke(Process.CREATE_CHILD);
        sessionProcess.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(Session.class).toString())[1]));
        sessionProcess.invoke(Process.INITIALIZE, 1);
        ProgramExecutor session = sessionProcess.getObj(Process.EXECUTOR);
        session.setObj(Session.USER, testUser);
        session.invoke(ProgramExecutor.RUN);

        sendCreateRequest(executeProgramAndSetCurrentPath(sessionProcess, CommonFiles.SYSTEM_MOUNTPOINT), PATH, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return missing rights event", event instanceof ProgramMissingFileRightsEvent);

                ProgramMissingFileRightsEvent missingRightsEvent = (ProgramMissingFileRightsEvent) event;
                assertEquals("User name", testUser.getObj(User.NAME), missingRightsEvent.getUser());
                assertFalse("More than one file is not accessible", missingRightsEvent.containsMultipleFiles());
                // Just check the path of the file placeholder because the internal routine is tested elsewhere
                assertEquals("File path", "/" + CommonFiles.SYSTEM_MOUNTPOINT, missingRightsEvent.getSingleFile().getPath());
                assertArrayEquals("Missing rights", new Character[] { FileRights.WRITE }, missingRightsEvent.getSingleRights());
            }

        });
    }

}
