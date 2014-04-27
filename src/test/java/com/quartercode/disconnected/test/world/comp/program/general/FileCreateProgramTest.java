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
import static com.quartercode.disconnected.world.comp.program.ExecutorUtils.getCommonLocation;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
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
import com.quartercode.disconnected.world.event.Event;
import com.quartercode.disconnected.world.event.EventListener;
import com.quartercode.disconnected.world.event.QueueEventListener;
import com.quartercode.disconnected.world.event.TrueEventMatcher;

public class FileCreateProgramTest extends AbstractProgramTest {

    private static final String PARENT_PATH = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/test1/test2";
    private static final String PATH        = PARENT_PATH + "/test.txt";

    public FileCreateProgramTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private void executeProgram(Process<?> parentProcess, String path, EventListener eventListener) {

        ChildProcess process = parentProcess.get(Process.CREATE_CHILD).invoke();
        process.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke(getComponents(getCommonLocation(FileCreateProgram.class))[1]));
        process.get(Process.INITIALIZE).invoke();

        ProgramExecutor program = process.get(Process.EXECUTOR).get();
        program.get(FileCreateProgram.PATH).set(path);
        program.get(FileCreateProgram.FILE_TYPE).set(ContentFile.class);
        program.get(ProgramExecutor.OUT_EVENT_LISTENERS).add(eventListener);
        program.get(ProgramExecutor.RUN).invoke();
    }

    @Test
    public void testSuccess() {

        QueueEventListener eventListener = new QueueEventListener();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH, eventListener);

        Event event = eventListener.get(QueueEventListener.NEXT_EVENT).invoke(TrueEventMatcher.INSTANCE);
        Assert.assertTrue("File create program did not send SuccessEvent", event instanceof FileCreateProgram.SuccessEvent);
        File<?> file = event.get(FileCreateProgram.SuccessEvent.FILE).get();
        Assert.assertTrue("Created file is not a content file", file instanceof ContentFile);
        Assert.assertEquals("Name of created file", PATH.substring(PATH.lastIndexOf("/") + 1), file.get(File.NAME).get());
        Assert.assertEquals("Path of created file", FileUtils.getComponents(PATH)[1], file.get(File.GET_PATH).invoke());
        Assert.assertEquals("Owner of created file", User.SUPERUSER_NAME, file.get(File.OWNER).get().get(User.NAME).get());
        Assert.assertEquals("Group of created file", null, file.get(File.GROUP).get());
    }

    @Test
    public void testUnknownMountpoint() {

        QueueEventListener eventListener = new QueueEventListener();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), "/testunknown/" + FileUtils.getComponents(PATH)[1], eventListener);

        Event event = eventListener.get(QueueEventListener.NEXT_EVENT).invoke(TrueEventMatcher.INSTANCE);
        Assert.assertTrue("File create program did not send UnknownMountpointEvent", event instanceof FileCreateProgram.UnknownMountpointEvent);
        Assert.assertEquals("Unknown mountpoint", "testunknown", event.get(FileCreateProgram.UnknownMountpointEvent.MOUNTPOINT).get());
    }

    @Test
    public void testMissingRights() {

        User testUser = new User();
        testUser.get(User.NAME).set("testUser");

        ChildProcess sessionProcess = processModule.get(ProcessModule.ROOT_PROCESS).get().get(Process.CREATE_CHILD).invoke();
        sessionProcess.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke(getComponents(getCommonLocation(Session.class))[1]));
        sessionProcess.get(Process.INITIALIZE).invoke();
        ProgramExecutor session = sessionProcess.get(Process.EXECUTOR).get();
        session.get(Session.USER).set(testUser);
        session.get(ProgramExecutor.RUN).invoke();

        QueueEventListener eventListener = new QueueEventListener();
        executeProgram(sessionProcess, PATH, eventListener);

        Event event = eventListener.get(QueueEventListener.NEXT_EVENT).invoke(TrueEventMatcher.INSTANCE);
        Assert.assertTrue("File create program did not send MissingRightsEvent", event instanceof FileCreateProgram.MissingRightsEvent);
    }

    @Test
    public void testInvalidPath() {

        // Add content file that makes the path invalid
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), FileUtils.getComponents(PARENT_PATH)[1]).get(FileAddAction.EXECUTE).invoke();

        QueueEventListener eventListener = new QueueEventListener();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH, eventListener);

        Event event = eventListener.get(QueueEventListener.NEXT_EVENT).invoke(TrueEventMatcher.INSTANCE);
        Assert.assertTrue("File create program did not send InvalidPathEvent", event instanceof FileCreateProgram.InvalidPathEvent);
        Assert.assertEquals("Invalid path", PATH, event.get(FileCreateProgram.InvalidPathEvent.PATH).get());
    }

    @Test
    public void testOccupiedPath() {

        // Add content file that makes the path occupied
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), FileUtils.getComponents(PATH)[1]).get(FileAddAction.EXECUTE).invoke();

        QueueEventListener eventListener = new QueueEventListener();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH, eventListener);

        Event event = eventListener.get(QueueEventListener.NEXT_EVENT).invoke(TrueEventMatcher.INSTANCE);
        Assert.assertTrue("File create program did not send OccupiedPathEvent", event instanceof FileCreateProgram.OccupiedPathEvent);
        Assert.assertEquals("Occupied path", PATH, event.get(FileCreateProgram.OccupiedPathEvent.PATH).get());
    }

    @Test
    public void testOutOfSpace() {

        // Set size of the file system to something very small
        fileSystem.get(FileSystem.SIZE).set(40L);

        QueueEventListener eventListener = new QueueEventListener();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH, eventListener);

        Event event = eventListener.get(QueueEventListener.NEXT_EVENT).invoke(TrueEventMatcher.INSTANCE);
        Assert.assertTrue("File create program did not send OutOfSpaceEvent", event instanceof FileCreateProgram.OutOfSpaceEvent);
        Assert.assertEquals("File system which is out of space", fileSystem, event.get(FileCreateProgram.OutOfSpaceEvent.FILE_SYSTEM).get());
        Assert.assertTrue("Required space for a new file is greater than 0", event.get(FileCreateProgram.OutOfSpaceEvent.REQUIRED_SPACE).get() > 0);
    }

}
