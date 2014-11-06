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

import static com.quartercode.disconnected.shared.comp.file.PathUtils.splitAfterMountpoint;
import static com.quartercode.disconnected.shared.comp.file.PathUtils.splitBeforeName;
import static org.junit.Assert.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.program.AbstractProgramTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.os.Session;
import com.quartercode.disconnected.server.world.comp.os.user.User;
import com.quartercode.disconnected.server.world.comp.program.ChildProcess;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.program.general.FileManagerProgram;
import com.quartercode.disconnected.shared.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.comp.file.FileRights;
import com.quartercode.disconnected.shared.comp.program.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.comp.program.WorldProcessId;
import com.quartercode.disconnected.shared.event.program.general.FMPWPUUpdateViewCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldAddFileCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldChangeDirCommand;
import com.quartercode.disconnected.shared.event.program.generic.GPWPUErrorEvent;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramAddFileTest extends AbstractProgramTest {

    private static final String            PARENT_PATH   = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/test1/test2";
    private static final String            PATH_1        = PARENT_PATH + "/test.txt";
    private static final String            PATH_2_PART_2 = "test3/test.txt";

    private static final EventPredicate<?> UW_PREDICATE  = new TypePredicate<>(FMPWPUUpdateViewCommand.class);

    public FileManagerProgramAddFileTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private final Directory parentFile = new Directory();
    private WorldProcessId  processId;

    @Before
    public void setUp2() {

        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, parentFile, splitAfterMountpoint(PARENT_PATH)[1]).invoke(FileAddAction.EXECUTE);
    }

    private void executeProgramAndSendChangeDirCommand(Process<?> parentProcess, String change) {

        ChildProcess process = parentProcess.invoke(Process.CREATE_CHILD);
        process.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(FileManagerProgram.class).toString())[1]));
        process.setObj(Process.WORLD_PROCESS_USER, new SBPWorldProcessUserId(SBP, null));
        process.invoke(Process.INITIALIZE, 10);

        ProgramExecutor program = process.getObj(Process.EXECUTOR);
        program.invoke(ProgramExecutor.RUN);

        processId = ProgramUtils.getProcessId(program);

        bridge.send(new FMPWorldChangeDirCommand(ProgramUtils.getProcessId(program), change));
    }

    private void sendAddFileCommand(String fileName) {

        bridge.send(new FMPWorldAddFileCommand(processId, fileName, "contentFile"));
    }

    @Test
    public void testWithContentFile() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), PARENT_PATH);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FMPWPUUpdateViewCommand>() {

            @Override
            public void handle(FMPWPUUpdateViewCommand event) {

                assertEquals("File path", PARENT_PATH, event.getCurrentDir());

                FilePlaceholder[] files = event.getFiles();
                assertTrue("The wrong amount of files (" + files.length + ") has been created", files.length == 1);
                FilePlaceholder file = files[0];
                assertTrue("File hasn't been created correctly", file.getPath().equals(PATH_1) && file.getType().equals("contentFile"));

                invoked.setTrue();
            }

        }, UW_PREDICATE);

        sendAddFileCommand(splitBeforeName(PATH_1)[1]);

        assertTrue("Update view handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithFileNameWithSeparators() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), PARENT_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "createFile.invalidFileName", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { PATH_2_PART_2 }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendAddFileCommand(PATH_2_PART_2);

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithCurrentDir() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), PARENT_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "createFile.invalidFileName", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { "." }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendAddFileCommand(".");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test (expected = IllegalStateException.class)
    public void testWithAbsoluteRoot() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), "/");

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        sendAddFileCommand(".");
    }

    @Test (expected = IllegalStateException.class)
    public void testWithRootFile() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), "/");

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        sendAddFileCommand(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    @Test
    public void testOccupiedPath() {

        // Add content file that makes the path occupied
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new ContentFile(), splitAfterMountpoint(PATH_1)[1]).invoke(FileAddAction.EXECUTE);

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), PARENT_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "createFile.occupiedPath", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { PATH_1 }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendAddFileCommand(splitBeforeName(PATH_1)[1]);

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testOutOfSpace() {

        // Set size of the file system to something very small
        fileSystem.setObj(FileSystem.SIZE, 10L);

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), PARENT_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "createFile.outOfSpace", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { PATH_1 }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendAddFileCommand(splitBeforeName(PATH_1)[1]);

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testMissingWriteRight() {

        // Create a new user and a new session under which the process will run
        final User testUser = new User();
        testUser.setObj(User.NAME, "testUser");
        ChildProcess sessionProcess = processModule.getObj(ProcessModule.ROOT_PROCESS).invoke(Process.CREATE_CHILD);
        sessionProcess.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(Session.class).toString())[1]));
        sessionProcess.invoke(Process.INITIALIZE, 1);
        ProgramExecutor session = sessionProcess.getObj(Process.EXECUTOR);
        session.setObj(Session.USER, testUser);
        session.invoke(ProgramExecutor.RUN);

        executeProgramAndSendChangeDirCommand(sessionProcess, PARENT_PATH);

        // Remove all rights from the parent file
        parentFile.setObj(File.RIGHTS, new FileRights());

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "createFile.missingWriteRight", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { PATH_1 }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendAddFileCommand(splitBeforeName(PATH_1)[1]);

        assertTrue("Missing right error event handler hasn't been invoked", invoked.getValue());
    }

}
