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
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.Directory;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.ParentFile;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.Session;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.ChildProcess;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProcessModule;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.program.general.FileListProgram;
import com.quartercode.disconnected.world.event.Event;
import com.quartercode.disconnected.world.event.EventListener;
import com.quartercode.disconnected.world.event.QueueEventListener;
import com.quartercode.disconnected.world.event.TrueEventMatcher;

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
            fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, PATH + "/file" + index + ".txt").get(FileAddAction.EXECUTE).invoke();
        }

        dir = (ParentFile<?>) fileSystem.get(FileSystem.GET_FILE).invoke(PATH);
    }

    private void executeProgram(Process<?> parentProcess, String path, EventListener eventListener) {

        ChildProcess process = parentProcess.get(Process.CREATE_CHILD).invoke();
        process.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke(getComponents(getCommonLocation(FileListProgram.class))[1]));
        process.get(Process.INITIALIZE).invoke();

        ProgramExecutor program = process.get(Process.EXECUTOR).get();
        program.get(FileListProgram.DIR).set(dir);
        program.get(ProgramExecutor.OUT_EVENT_LISTENERS).add(eventListener);
        program.get(ProgramExecutor.RUN).invoke();
    }

    @Test
    public void testSuccess() {

        QueueEventListener eventListener = new QueueEventListener();
        executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get(), PATH, eventListener);

        Event event = eventListener.get(QueueEventListener.NEXT_EVENT).invoke(TrueEventMatcher.INSTANCE);
        Assert.assertTrue("File list program did not send SuccessEvent", event instanceof FileListProgram.SuccessEvent);
        List<File<ParentFile<?>>> files = event.get(FileListProgram.SuccessEvent.FILES).get();
        File<?>[] filesArray = files.toArray(new File<?>[files.size()]);
        Assert.assertArrayEquals("Listed files", testFiles, filesArray);
    }

    @Test
    public void testMissingRights() {

        dir.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");

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
        Assert.assertTrue("File list program did not send MissingRightsEvent", event instanceof FileListProgram.MissingRightsEvent);
    }

}
