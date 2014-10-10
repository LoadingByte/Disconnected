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
import static com.quartercode.disconnected.shared.file.PathUtils.splitAfterMountpoint;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.program.AbstractProgramTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
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
import com.quartercode.disconnected.shared.event.comp.program.ProgramMissingFileRightsEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramInvalidPathEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramRemoveRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramRemoveRequestEvent.FileManagerProgramRemoveSuccessReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramSetCurrentPathRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramUnknownMountpointEvent;
import com.quartercode.disconnected.shared.file.CommonFiles;
import com.quartercode.disconnected.shared.file.FileRights;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;

public class FileManagerProgramRemoveTest extends AbstractProgramTest {

    private static final String FILE_NAME         = "test.txt";
    private static final String LOCAL_PARENT_PATH = "test1/test2";
    private static final String PARENT_PATH       = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/" + LOCAL_PARENT_PATH;
    private static final String LOCAL_PATH        = LOCAL_PARENT_PATH + "/" + FILE_NAME;
    private static final String PATH              = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/" + LOCAL_PATH;

    public FileManagerProgramRemoveTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private File<?> removeFile;

    @Before
    public void setUp2() {

        removeFile = new ContentFile();
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, removeFile, LOCAL_PATH).invoke(FileAddAction.EXECUTE);
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

    private void sendRemoveRequest(ImportantData data, String subpath, EventHandler<?> returnHandler) {

        Event request = new FileManagerProgramRemoveRequestEvent(data.getComputerId(), data.getPid(), subpath);
        bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(request, returnHandler);
    }

    @Test
    public void testSuccess() {

        assertNotNull("Not yet removed file does not exist", fileSystem.invoke(FileSystem.GET_FILE, LOCAL_PATH));

        sendRemoveRequest(executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), PARENT_PATH), FILE_NAME, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return success event", event instanceof FileManagerProgramRemoveSuccessReturnEvent);
                assertNull("Removed file does still exist", fileSystem.invoke(FileSystem.GET_FILE, LOCAL_PATH));
            }

        });
    }

    @Test
    public void testUnknownMountpoint() {

        ImportantData data = executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), PARENT_PATH);

        // Unmount the file system
        FileSystemModule fsModule = os.getObj(OperatingSystem.FS_MODULE);
        fsModule.invoke(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT, fileSystemMountpoint).setObj(KnownFileSystem.MOUNTED, false);

        sendRemoveRequest(data, FILE_NAME, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return unknown mountpoint event", event instanceof FileManagerProgramUnknownMountpointEvent);
                assertEquals("Unknown mountpoint", CommonFiles.SYSTEM_MOUNTPOINT, ((FileManagerProgramUnknownMountpointEvent) event).getMountpoint());
            }

        });
    }

    @Test
    public void testInvalidPath() {

        // Replace file's parent with content file -> make the path invalid
        File<?> parentFile = (File<?>) removeFile.getParent();
        String parentPath = parentFile.invoke(File.GET_PATH);
        parentFile.invoke(ParentFile.CREATE_REMOVE).invoke(FileRemoveAction.EXECUTE);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new ContentFile(), parentPath).invoke(FileAddAction.EXECUTE);

        sendRemoveRequest(executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), CommonFiles.SYSTEM_MOUNTPOINT), LOCAL_PATH, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return invalid path event", event instanceof FileManagerProgramInvalidPathEvent);
                assertEquals("Invalid path", PATH, ((FileManagerProgramInvalidPathEvent) event).getPath());
            }

        });
    }

    @Test
    public void testMissingRights() {

        removeFile.setObj(File.RIGHTS, new FileRights());

        final User testUser = new User();
        testUser.setObj(User.NAME, "testUser");

        ChildProcess sessionProcess = processModule.getObj(ProcessModule.ROOT_PROCESS).invoke(Process.CREATE_CHILD);
        sessionProcess.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(Session.class).toString())[1]));
        sessionProcess.invoke(Process.INITIALIZE, 1);
        ProgramExecutor session = sessionProcess.getObj(Process.EXECUTOR);
        session.setObj(Session.USER, testUser);
        session.invoke(ProgramExecutor.RUN);

        sendRemoveRequest(executeProgramAndSetCurrentPath(sessionProcess, PARENT_PATH), FILE_NAME, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return missing rights event", event instanceof ProgramMissingFileRightsEvent);

                ProgramMissingFileRightsEvent missingRightsEvent = (ProgramMissingFileRightsEvent) event;
                assertEquals("User name", testUser.getObj(User.NAME), missingRightsEvent.getUser());
                assertFalse("More than one file is not accessible", missingRightsEvent.containsMultipleFiles());
                // Just check the path of the file placeholder because the internal routine is tested elsewhere
                assertEquals("File path", PATH, missingRightsEvent.getSingleFile().getPath());
                assertArrayEquals("Missing rights", new Character[] { FileRights.DELETE }, missingRightsEvent.getSingleRights());
            }

        });
    }

}
