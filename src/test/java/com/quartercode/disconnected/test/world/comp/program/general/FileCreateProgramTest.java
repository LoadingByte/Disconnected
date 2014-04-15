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

import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystem;
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

    public FileCreateProgramTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private void executeProgram(Process<?> parentProcess, EventListener eventListener) {

        ChildProcess process = parentProcess.get(Process.CREATE_CHILD).invoke();
        process.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke("bin/filecreate.exe"));
        process.get(Process.INITIALIZE).invoke();

        ProgramExecutor program = process.get(Process.EXECUTOR).get();
        program.get(FileCreateProgram.PATH).set("/user/test1/test2/test.txt");
        program.get(FileCreateProgram.FILE_TYPE).set(ContentFile.class);
        program.get(ProgramExecutor.OUT_EVENT_LISTENERS).add(eventListener);
        program.get(ProgramExecutor.RUN).invoke();
    }

    @Test
    public void testSuccess() {

        QueueEventListener eventListener = new QueueEventListener();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), eventListener);

        Event event = eventListener.get(QueueEventListener.NEXT_EVENT).invoke(TrueEventMatcher.INSTANCE);
        Assert.assertTrue("File create program did not send SuccessEvent", event instanceof FileCreateProgram.SuccessEvent);
        File<?> file = event.get(FileCreateProgram.SuccessEvent.FILE).get();
        Assert.assertTrue("Created file is not a content file", file instanceof ContentFile);
        Assert.assertEquals("Name of created file", "test.txt", file.get(File.NAME).get());
        Assert.assertEquals("Path of created file", "test1/test2/test.txt", file.get(File.GET_PATH).invoke());
        Assert.assertEquals("Owner of created file", User.SUPERUSER_NAME, file.get(File.OWNER).get().get(User.NAME).get());
        Assert.assertEquals("Group of created file", null, file.get(File.GROUP).get());
    }

    @Test
    public void testMissingRights() {

        User testUser = new User();
        testUser.get(User.NAME).set("testUser");

        ChildProcess sessionProcess = processModule.get(ProcessModule.ROOT_PROCESS).get().get(Process.CREATE_CHILD).invoke();
        sessionProcess.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke("bin/session.exe"));
        sessionProcess.get(Process.INITIALIZE).invoke();
        ProgramExecutor session = sessionProcess.get(Process.EXECUTOR).get();
        session.get(Session.USER).set(testUser);
        session.get(ProgramExecutor.RUN).invoke();

        QueueEventListener eventListener = new QueueEventListener();
        executeProgram(sessionProcess, eventListener);

        Event event = eventListener.get(QueueEventListener.NEXT_EVENT).invoke(TrueEventMatcher.INSTANCE);
        Assert.assertTrue("File create program did not send MissingRightsEvent", event instanceof FileCreateProgram.MissingRightEvent);
    }

}
