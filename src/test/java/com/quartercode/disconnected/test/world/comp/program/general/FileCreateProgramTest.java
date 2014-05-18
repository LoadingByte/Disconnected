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

import static com.quartercode.disconnected.world.comp.file.FileUtils.getComponents;
import static com.quartercode.disconnected.world.comp.program.ProgramUtils.getCommonLocation;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.QueueEventHandler;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.Session;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.ChildProcess;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProcessModule;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.program.general.FileCreateProgram;

public class FileCreateProgramTest extends AbstractProgramTest {

    private static final String PARENT_PATH = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/test1/test2";
    private static final String PATH        = PARENT_PATH + "/test.txt";

    public FileCreateProgramTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private void executeProgram(Process<?> parentProcess, String path) {

        ChildProcess process = parentProcess.get(Process.CREATE_CHILD).invoke();
        process.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke(getComponents(getCommonLocation(FileCreateProgram.class))[1]));
        process.get(Process.INITIALIZE).invoke(10);

        ProgramExecutor program = process.get(Process.EXECUTOR).get();
        program.get(FileCreateProgram.PATH).set(path);
        program.get(FileCreateProgram.FILE_TYPE).set(ContentFile.class);
        program.get(ProgramExecutor.RUN).invoke();
    }

    @Test
    public void testSuccess() {

        QueueEventHandler<Event> handler = new QueueEventHandler<>(Event.class);
        bridge.setHandler(handler);
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH);

        Event event = handler.next();
        Assert.assertTrue("File create program did not send SuccessEvent", event instanceof FileCreateProgram.SuccessEvent);
    }

    @Test
    public void testUnknownMountpoint() {

        QueueEventHandler<Event> handler = new QueueEventHandler<>(Event.class);
        bridge.setHandler(handler);
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), "/testunknown/" + FileUtils.getComponents(PATH)[1]);

        Event event = handler.next();
        Assert.assertTrue("File create program did not send UnknownMountpointEvent", event instanceof FileCreateProgram.UnknownMountpointEvent);
        Assert.assertEquals("Unknown mountpoint", "testunknown", ((FileCreateProgram.UnknownMountpointEvent) event).getMountpoint());
    }

    @Test
    public void testInvalidPath() {

        // Add content file that makes the path invalid
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), FileUtils.getComponents(PARENT_PATH)[1]).get(FileAddAction.EXECUTE).invoke();

        QueueEventHandler<Event> handler = new QueueEventHandler<>(Event.class);
        bridge.setHandler(handler);
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH);

        Event event = handler.next();
        Assert.assertTrue("File create program did not send InvalidPathEvent", event instanceof FileCreateProgram.InvalidPathEvent);
        Assert.assertEquals("Invalid path", PATH, ((FileCreateProgram.InvalidPathEvent) event).getPath());
    }

    @Test
    public void testOccupiedPath() {

        // Add content file that makes the path occupied
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), FileUtils.getComponents(PATH)[1]).get(FileAddAction.EXECUTE).invoke();

        QueueEventHandler<Event> handler = new QueueEventHandler<>(Event.class);
        bridge.setHandler(handler);
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH);

        Event event = handler.next();
        Assert.assertTrue("File create program did not send OccupiedPathEvent", event instanceof FileCreateProgram.OccupiedPathEvent);
        Assert.assertEquals("Occupied path", PATH, ((FileCreateProgram.OccupiedPathEvent) event).getPath());
    }

    @Test
    public void testOutOfSpace() {

        // Set size of the file system to something very small
        fileSystem.get(FileSystem.SIZE).set(40L);

        QueueEventHandler<Event> handler = new QueueEventHandler<>(Event.class);
        bridge.setHandler(handler);
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH);

        Event event = handler.next();
        Assert.assertTrue("File create program did not send OutOfSpaceEvent", event instanceof FileCreateProgram.OutOfSpaceEvent);
        FileCreateProgram.OutOfSpaceEvent castedEvent = (FileCreateProgram.OutOfSpaceEvent) event;
        Assert.assertEquals("File system which is out of space", CommonFiles.SYSTEM_MOUNTPOINT, castedEvent.getFileSystemMountpoint());
        Assert.assertTrue("Required space for a new file is greater than 0", castedEvent.getRequiredSpace() > 0);
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

        QueueEventHandler<Event> handler = new QueueEventHandler<>(Event.class);
        bridge.setHandler(handler);
        executeProgram(sessionProcess, PATH);

        Event event = handler.next();
        Assert.assertTrue("File create program did not send MissingRightsEvent", event instanceof FileCreateProgram.MissingRightsEvent);
    }

}
