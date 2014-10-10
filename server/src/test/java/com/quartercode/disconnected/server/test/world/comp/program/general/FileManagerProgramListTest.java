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
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.program.AbstractProgramTest;
import com.quartercode.disconnected.server.world.comp.ByteUnit;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.Directory;
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
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramListRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramListRequestEvent.FileManagerProgramListSuccessReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramSetCurrentPathRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramUnknownMountpointEvent;
import com.quartercode.disconnected.shared.file.CommonFiles;
import com.quartercode.disconnected.shared.file.FilePlaceholder;
import com.quartercode.disconnected.shared.file.FileRights;
import com.quartercode.disconnected.shared.file.PathUtils;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;

public class FileManagerProgramListTest extends AbstractProgramTest {

    private static final String LOCAL_PATH = "test1/test2";
    private static final String PATH       = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/" + LOCAL_PATH;

    public FileManagerProgramListTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private ParentFile<?>   dir;
    private final File<?>[] testFiles = { new ContentFile(), new Directory(), new ContentFile() };

    @Before
    public void setUp2() {

        for (int index = 0; index < testFiles.length; index++) {
            File<?> file = testFiles[index];
            fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, LOCAL_PATH + "/file" + index + ".txt").invoke(FileAddAction.EXECUTE);
        }

        dir = (ParentFile<?>) fileSystem.invoke(FileSystem.GET_FILE, LOCAL_PATH);
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

    private void sendListRequest(ImportantData data, EventHandler<?> returnHandler) {

        Event request = new FileManagerProgramListRequestEvent(data.getComputerId(), data.getPid());
        bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(request, returnHandler);
    }

    @Test
    public void testSuccess() {

        sendListRequest(executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), PATH), new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return success event", event instanceof FileManagerProgramListSuccessReturnEvent);

                List<FilePlaceholder> expectedFiles = new ArrayList<>();
                for (File<?> file : testFiles) {
                    String path = PathUtils.resolve(PathUtils.SEPARATOR + fileSystemMountpoint, file.invoke(File.GET_PATH));
                    String type = file.getClass() == Directory.class ? "directory" : "contentFile";
                    long size = file.invoke(File.GET_SIZE);
                    expectedFiles.add(new FilePlaceholder(path, type, size, File.DEFAULT_FILE_RIGHTS, null, null));
                }

                List<FilePlaceholder> returnedFiles = new ArrayList<>( ((FileManagerProgramListSuccessReturnEvent) event).getFiles());
                assertEquals("Listed files", expectedFiles, returnedFiles);
            }

        });
    }

    @Test
    public void testSuccessWithRootFiles() {

        sendListRequest(executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), "/"), new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return success event", event instanceof FileManagerProgramListSuccessReturnEvent);

                List<FilePlaceholder> expectedFiles = new ArrayList<>();
                long terabyte = ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE);
                expectedFiles.add(new FilePlaceholder(PathUtils.SEPARATOR + CommonFiles.SYSTEM_MOUNTPOINT, "rootFile", terabyte, File.DEFAULT_FILE_RIGHTS, null, null));
                expectedFiles.add(new FilePlaceholder(PathUtils.SEPARATOR + CommonFiles.USER_MOUNTPOINT, "rootFile", terabyte, File.DEFAULT_FILE_RIGHTS, null, null));

                List<FilePlaceholder> returnedFiles = new ArrayList<>( ((FileManagerProgramListSuccessReturnEvent) event).getFiles());
                assertEquals("Listed files", expectedFiles, returnedFiles);
            }

        });
    }

    @Test
    public void testUnknownMountpoint() {

        ImportantData data = executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), PATH);

        // Unmount the file system
        FileSystemModule fsModule = os.getObj(OperatingSystem.FS_MODULE);
        fsModule.invoke(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT, fileSystemMountpoint).setObj(KnownFileSystem.MOUNTED, false);

        sendListRequest(data, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return unknown mountpoint event", event instanceof FileManagerProgramUnknownMountpointEvent);
                assertEquals("Unknown mountpoint", CommonFiles.SYSTEM_MOUNTPOINT, ((FileManagerProgramUnknownMountpointEvent) event).getMountpoint());
            }

        });
    }

    @Test
    public void testInvalidPath() {

        ImportantData data = executeProgramAndSetCurrentPath(processModule.getObj(ProcessModule.ROOT_PROCESS), PATH);

        // Replace dir with content file -> make the path invalid
        dir.invoke(ParentFile.CREATE_REMOVE).invoke(FileRemoveAction.EXECUTE);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new ContentFile(), LOCAL_PATH).invoke(FileAddAction.EXECUTE);

        sendListRequest(data, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return invalid path event", event instanceof FileManagerProgramInvalidPathEvent);
                assertEquals("Invalid path", PATH, ((FileManagerProgramInvalidPathEvent) event).getPath());
            }

        });
    }

    @Test
    public void testMissingRights() {

        dir.setObj(File.RIGHTS, new FileRights());

        final User testUser = new User();
        testUser.setObj(User.NAME, "testUser");

        ChildProcess sessionProcess = processModule.getObj(ProcessModule.ROOT_PROCESS).invoke(Process.CREATE_CHILD);
        sessionProcess.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(Session.class).toString())[1]));
        sessionProcess.invoke(Process.INITIALIZE, 1);
        ProgramExecutor session = sessionProcess.getObj(Process.EXECUTOR);
        session.setObj(Session.USER, testUser);
        session.invoke(ProgramExecutor.RUN);

        sendListRequest(executeProgramAndSetCurrentPath(sessionProcess, PATH), new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return missing rights event", event instanceof ProgramMissingFileRightsEvent);

                ProgramMissingFileRightsEvent missingRightsEvent = (ProgramMissingFileRightsEvent) event;
                assertEquals("User name", testUser.getObj(User.NAME), missingRightsEvent.getUser());
                assertFalse("More than one file is not accessible", missingRightsEvent.containsMultipleFiles());
                // Just check the path of the file placeholder because the internal routine is the same as the one used in the success test
                assertEquals("File path", PATH, missingRightsEvent.getSingleFile().getPath());
                assertArrayEquals("Missing rights", new Character[] { FileRights.READ }, missingRightsEvent.getSingleRights());
            }

        });
    }

}
