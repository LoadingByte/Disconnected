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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import com.quartercode.disconnected.server.world.comp.file.FileRights;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
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
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramInvalidPathEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramListRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramListRequestEvent.FileManagerProgramListMissingRightsReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramListRequestEvent.FileManagerProgramListSuccessReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramSetCurrentPathRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramUnknownMountpointEvent;
import com.quartercode.disconnected.shared.event.util.FilePlaceholder;
import com.quartercode.disconnected.shared.util.PathUtils;
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
            fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, LOCAL_PATH + "/file" + index + ".txt").get(FileAddAction.EXECUTE).invoke();
        }

        dir = (ParentFile<?>) fileSystem.get(FileSystem.GET_FILE).invoke(LOCAL_PATH);
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

    private void sendListRequest(ImportantData data, EventHandler<?> returnHandler) {

        Event request = new FileManagerProgramListRequestEvent(data.getComputerId(), data.getPid());
        bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(request, returnHandler);
    }

    @Test
    public void testSuccess() {

        sendListRequest(executeProgramAndSetCurrentPath(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH), new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return success event", event instanceof FileManagerProgramListSuccessReturnEvent);

                List<FilePlaceholder> expectedFiles = new ArrayList<>();
                for (File<?> file : testFiles) {
                    String path = PathUtils.resolve(PathUtils.SEPARATOR + fileSystemMountpoint, file.get(File.GET_PATH).invoke());
                    String type = file.getClass() == Directory.class ? "directory" : "contentFile";
                    long size = file.get(File.GET_SIZE).invoke();
                    expectedFiles.add(new FilePlaceholder(path, type, size, File.DEFAULT_FILE_RIGHTS, null, null));
                }

                List<FilePlaceholder> returnedFiles = new ArrayList<>( ((FileManagerProgramListSuccessReturnEvent) event).getFiles());
                assertEquals("Listed files", expectedFiles, returnedFiles);
            }

        });
    }

    @Test
    public void testSuccessWithRootFiles() {

        sendListRequest(executeProgramAndSetCurrentPath(processModule.get(ProcessModule.ROOT_PROCESS).get(), "/"), new EventHandler<Event>() {

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

        ImportantData data = executeProgramAndSetCurrentPath(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH);

        // Unmount the file system
        FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();
        fsModule.get(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT).invoke(fileSystemMountpoint).get(KnownFileSystem.MOUNTED).set(false);

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

        ImportantData data = executeProgramAndSetCurrentPath(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH);

        // Replace dir with content file -> make the path invalid
        dir.get(ParentFile.CREATE_REMOVE).invoke().get(FileRemoveAction.EXECUTE).invoke();
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), LOCAL_PATH).get(FileAddAction.EXECUTE).invoke();

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

        dir.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");

        User testUser = new User();
        testUser.get(User.NAME).set("testUser");

        ChildProcess sessionProcess = processModule.get(ProcessModule.ROOT_PROCESS).get().get(Process.CREATE_CHILD).invoke();
        sessionProcess.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke(getComponents(getCommonLocation(Session.class))[1]));
        sessionProcess.get(Process.INITIALIZE).invoke(1);
        ProgramExecutor session = sessionProcess.get(Process.EXECUTOR).get();
        session.get(Session.USER).set(testUser);
        session.get(ProgramExecutor.RUN).invoke();

        sendListRequest(executeProgramAndSetCurrentPath(sessionProcess, PATH), new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return missing rights event", event instanceof FileManagerProgramListMissingRightsReturnEvent);
                assertEquals("Path", PATH, ((FileManagerProgramInvalidPathEvent) event).getPath());
            }

        });
    }

}
