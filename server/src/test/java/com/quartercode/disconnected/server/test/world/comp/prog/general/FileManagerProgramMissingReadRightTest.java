/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.test.world.comp.prog.general;

import static org.junit.Assert.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.AbstractComplexComputerTest;
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.general.FileManagerProgram;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_ChangeDirCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramMissingReadRightTest extends AbstractComplexComputerTest {

    private static final String FS_MOUNTPOINT = CommonFiles.USER_MOUNTPOINT;
    private static final String PATH          = "/" + FS_MOUNTPOINT + "/test1/test2";

    private final Directory     dir           = new Directory();

    @Before
    public void setUp() {

        // Create the test directory
        mainFsModule().invoke(FileSystemModule.CREATE_ADD_FILE, dir, PATH).invoke(FileAddAction.EXECUTE);
    }

    private void executeProgramAndSendChangeDirCommand(Process<?> parentProcess, String change) {

        ChildProcess process = launchProgram(parentProcess, getCommonLocation(FileManagerProgram.class));
        WorldProcessId processId = process.invoke(Process.GET_WORLD_PROCESS_ID);

        bridge.send(new FMP_WP_ChangeDirCommand(processId, change));
    }

    @Test
    public void test() {

        // Remove all rights from the dir
        dir.setObj(File.RIGHTS, new FileRights());

        // Create a new user and a new session under which the process will run
        User testUser = new User();
        testUser.setObj(User.NAME, "testUser");
        ChildProcess sessionProcess = launchSession(mainRootProcess(), testUser, null);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FailEventHandler(), new TypePredicate<>(FMP_SBPWPU_UpdateViewCommand.class));

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "fileList.missingReadRight", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { PATH }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        executeProgramAndSendChangeDirCommand(sessionProcess, PATH);

        assertTrue("Missing right event handler hasn't been invoked", invoked.getValue());
    }

}
