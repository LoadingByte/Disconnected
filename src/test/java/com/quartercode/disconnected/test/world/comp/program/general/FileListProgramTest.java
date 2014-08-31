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

package com.quartercode.disconnected.test.world.comp.program.general;

import static com.quartercode.disconnected.test.ExtraAssert.assertCollectionSize;
import static com.quartercode.disconnected.world.comp.file.FileUtils.getComponents;
import static com.quartercode.disconnected.world.comp.program.ProgramUtils.getCommonLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.Directory;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.file.ParentFile;
import com.quartercode.disconnected.world.comp.file.RootFile;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.Group;
import com.quartercode.disconnected.world.comp.os.Session;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.ChildProcess;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProcessModule;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.program.general.FileListProgram;
import com.quartercode.disconnected.world.event.FilePlaceholder;
import com.quartercode.eventbridge.bridge.Event;

public class FileListProgramTest extends AbstractProgramTest {

    private static final String LOCAL_PATH = "test1/test2";
    private static final String PATH       = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/" + LOCAL_PATH;

    public FileListProgramTest() {

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

    private void executeProgram(Process<?> parentProcess, String path) {

        ChildProcess process = parentProcess.get(Process.CREATE_CHILD).invoke();
        process.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke(getComponents(getCommonLocation(FileListProgram.class))[1]));
        process.get(Process.INITIALIZE).invoke(10);

        ProgramExecutor program = process.get(Process.EXECUTOR).get();
        program.get(FileListProgram.PATH).set(path);
        program.get(ProgramExecutor.RUN).invoke();
    }

    @Test
    public void testSuccess() {

        restartEventRecording();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH);

        assertCollectionSize("File list program did not send correct number of events", events, 1);
        Event event = events.get(0);
        assertTrue("File list program did not send SuccessEvent", event instanceof FileListProgram.SuccessEvent);

        List<FilePlaceholder> files = new ArrayList<>( ((FileListProgram.SuccessEvent) event).getFiles());
        List<FilePlaceholder> actualFiles = new ArrayList<>();
        for (File<?> file : testFiles) {
            String[] filePath = FileUtils.resolvePath(File.SEPARATOR + fileSystemMountpoint, file.get(File.GET_PATH).invoke()).substring(1).split(File.SEPARATOR);
            @SuppressWarnings ("unchecked")
            Class<? extends File<?>> type = (Class<? extends File<?>>) file.getClass();
            long size = file.get(File.GET_SIZE).invoke();
            String rights = file.get(File.RIGHTS).get().get(FileRights.TO_STRING).invoke();
            User ownerObject = file.get(File.OWNER).get();
            String owner = ownerObject == null ? null : ownerObject.get(User.NAME).get();
            Group groupObject = file.get(File.GROUP).get();
            String group = groupObject == null ? null : groupObject.get(Group.NAME).get();
            actualFiles.add(new FilePlaceholder(filePath, type, size, rights, owner, group));
        }
        assertEquals("Listed files", actualFiles, files);
    }

    @Test
    public void testSuccessWithRootFiles() {

        restartEventRecording();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), "/");

        assertCollectionSize("File list program did not send correct number of events", events, 1);
        Event event = events.get(0);
        assertTrue("File list program did not send SuccessEvent", event instanceof FileListProgram.SuccessEvent);

        List<FilePlaceholder> files = new ArrayList<>( ((FileListProgram.SuccessEvent) event).getFiles());
        List<FilePlaceholder> actualFiles = new ArrayList<>();
        long terabyte = ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE);
        String rights = "rwd-r---r---";
        actualFiles.add(new FilePlaceholder(new String[] { CommonFiles.SYSTEM_MOUNTPOINT }, RootFile.class, terabyte, rights, null, null));
        actualFiles.add(new FilePlaceholder(new String[] { CommonFiles.USER_MOUNTPOINT }, RootFile.class, terabyte, rights, null, null));
        assertEquals("Listed files", actualFiles, files);
    }

    @Test
    public void testUnknownMountpoint() {

        restartEventRecording();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), "/testunknown/" + FileUtils.getComponents(PATH)[1]);

        assertCollectionSize("File list program did not send correct number of events", events, 1);
        Event event = events.get(0);
        assertTrue("File list program did not send UnknownMountpointEvent", event instanceof FileListProgram.UnknownMountpointEvent);
        assertEquals("Unknown mountpoint", "testunknown", ((FileListProgram.UnknownMountpointEvent) event).getMountpoint());
    }

    @Test
    public void testInvalidPath() {

        // Replace dir with content file -> make the path invalid
        dir.get(ParentFile.CREATE_REMOVE).invoke().get(FileRemoveAction.EXECUTE).invoke();
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), FileUtils.getComponents(LOCAL_PATH)[1]).get(FileAddAction.EXECUTE).invoke();

        restartEventRecording();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH);

        assertCollectionSize("File list program did not send correct number of events", events, 1);
        Event event = events.get(0);
        assertTrue("File list program did not send InvalidPathEvent", event instanceof FileListProgram.InvalidPathEvent);
        assertEquals("Invalid path", PATH, ((FileListProgram.InvalidPathEvent) event).getPath());
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

        restartEventRecording();
        executeProgram(sessionProcess, PATH);

        assertCollectionSize("File list program did not send correct number of events", events, 1);
        Event event = events.get(0);
        assertTrue("File list program did not send MissingRightsEvent", event instanceof FileListProgram.MissingRightsEvent);
    }

}
