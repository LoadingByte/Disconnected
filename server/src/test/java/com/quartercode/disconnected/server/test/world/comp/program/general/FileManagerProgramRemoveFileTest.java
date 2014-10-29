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
import static com.quartercode.disconnected.shared.comp.file.PathUtils.*;
import static org.junit.Assert.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.program.AbstractProgramTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.os.Session;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.server.world.comp.program.ChildProcess;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.program.general.FileManagerProgram;
import com.quartercode.disconnected.shared.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.comp.file.FileRights;
import com.quartercode.disconnected.shared.comp.program.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.comp.program.WorldProcessId;
import com.quartercode.disconnected.shared.event.program.general.FMPWPUUpdateViewCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldChangeDirCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldRemoveFileCommand;
import com.quartercode.disconnected.shared.event.program.generic.GPWPUErrorEvent;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramRemoveFileTest extends AbstractProgramTest {

    private static final String            PATH_1        = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/test1/test2/test.txt";
    private static final String            PATH_2_PART_1 = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/test1/test3";
    private static final String            PATH_2_PART_2 = "test4/test.txt";

    private static final EventPredicate<?> UW_PREDICATE  = new TypePredicate<>(FMPWPUUpdateViewCommand.class);

    public FileManagerProgramRemoveFileTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private final ContentFile path1File = new ContentFile();
    private WorldProcessId    processId;

    @Before
    public void setUp2() {

        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, path1File, splitAfterMountpoint(PATH_1)[1]).invoke(FileAddAction.EXECUTE);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new ContentFile(), splitAfterMountpoint(resolve(PATH_2_PART_1, PATH_2_PART_2))[1]).invoke(FileAddAction.EXECUTE);
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

    private void sendRemoveFileCommand(String fileName) {

        bridge.send(new FMPWorldRemoveFileCommand(processId, fileName));
    }

    @Test
    public void testWithContentFile() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), splitBeforeName(PATH_1)[0]);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FMPWPUUpdateViewCommand>() {

            @Override
            public void handle(FMPWPUUpdateViewCommand event) {

                assertEquals("File path", splitBeforeName(PATH_1)[0], event.getCurrentDir());
                assertTrue("File hasn't been removed", event.getFiles().length == 0);

                invoked.setTrue();
            }

        }, UW_PREDICATE);

        sendRemoveFileCommand(splitBeforeName(PATH_1)[1]);

        assertTrue("Update view handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithFileNameWithSeparators() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), PATH_2_PART_1);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "removeFile.invalidFileName", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { PATH_2_PART_2 }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendRemoveFileCommand(PATH_2_PART_2);

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithCurrentDir() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), splitBeforeName(PATH_1)[0]);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "removeFile.invalidFileName", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { "." }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendRemoveFileCommand(".");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test (expected = IllegalStateException.class)
    public void testWithAbsoluteRoot() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), "/");

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        sendRemoveFileCommand(".");
    }

    @Test (expected = IllegalStateException.class)
    public void testWithRootFile() {

        executeProgramAndSendChangeDirCommand(processModule.getObj(ProcessModule.ROOT_PROCESS), "/");

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        sendRemoveFileCommand(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    @Test
    public void testMissingDeleteRight() {

        // Create a new user and a new session under which the process will run
        final User testUser = new User();
        testUser.setObj(User.NAME, "testUser");
        ChildProcess sessionProcess = processModule.getObj(ProcessModule.ROOT_PROCESS).invoke(Process.CREATE_CHILD);
        sessionProcess.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(Session.class).toString())[1]));
        sessionProcess.invoke(Process.INITIALIZE, 1);
        ProgramExecutor session = sessionProcess.getObj(Process.EXECUTOR);
        session.setObj(Session.USER, testUser);
        session.invoke(ProgramExecutor.RUN);

        executeProgramAndSendChangeDirCommand(sessionProcess, splitBeforeName(PATH_1)[0]);

        // Remove all rights from the file
        path1File.setObj(File.RIGHTS, new FileRights());

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "removeFile.missingDeleteRight", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { PATH_1 }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendRemoveFileCommand(splitBeforeName(PATH_1)[1]);

        assertTrue("Missing right event handler hasn't been invoked", invoked.getValue());
    }

}
