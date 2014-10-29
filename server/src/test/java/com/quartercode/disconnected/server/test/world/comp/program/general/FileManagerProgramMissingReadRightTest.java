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
import static com.quartercode.disconnected.shared.comp.file.PathUtils.splitAfterMountpoint;
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
import com.quartercode.disconnected.shared.event.program.general.FMPWPUUpdateViewCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldChangeDirCommand;
import com.quartercode.disconnected.shared.event.program.generic.GPWPUErrorEvent;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramMissingReadRightTest extends AbstractProgramTest {

    private static final String PATH = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/test1/test2";

    public FileManagerProgramMissingReadRightTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private final Directory dir = new Directory();

    @Before
    public void setUp2() {

        // Create the test directory
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, dir, splitAfterMountpoint(PATH)[1]).invoke(FileAddAction.EXECUTE);
    }

    private void executeProgramAndSendChangeDirCommand(Process<?> parentProcess, String change) {

        ChildProcess process = parentProcess.invoke(Process.CREATE_CHILD);
        process.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(FileManagerProgram.class).toString())[1]));
        process.setObj(Process.WORLD_PROCESS_USER, new SBPWorldProcessUserId(SBP, null));
        process.invoke(Process.INITIALIZE, 10);

        ProgramExecutor program = process.getObj(Process.EXECUTOR);
        program.invoke(ProgramExecutor.RUN);

        bridge.send(new FMPWorldChangeDirCommand(ProgramUtils.getProcessId(program), change));
    }

    @Test
    public void test() {

        // Remove all rights from the dir
        dir.setObj(File.RIGHTS, new FileRights());

        // Create a new user and a new session under which the process will run
        final User testUser = new User();
        testUser.setObj(User.NAME, "testUser");
        ChildProcess sessionProcess = processModule.getObj(ProcessModule.ROOT_PROCESS).invoke(Process.CREATE_CHILD);
        sessionProcess.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(Session.class).toString())[1]));
        sessionProcess.invoke(Process.INITIALIZE, 1);
        ProgramExecutor session = sessionProcess.getObj(Process.EXECUTOR);
        session.setObj(Session.USER, testUser);
        session.invoke(ProgramExecutor.RUN);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), new TypePredicate<>(FMPWPUUpdateViewCommand.class));

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "fileList.missingReadRight", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { PATH }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        executeProgramAndSendChangeDirCommand(sessionProcess, PATH);

        assertTrue("Missing right event handler hasn't been invoked", invoked.getValue());
    }

}
