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

import static com.quartercode.disconnected.shared.world.comp.file.PathUtils.resolve;
import static com.quartercode.disconnected.shared.world.comp.file.PathUtils.splitBeforeName;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandlerExceptionCatcher;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.test.world.comp.AbstractComplexComputerTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.general.FileManagerProgram;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_ChangeDirCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_RemoveFileCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessId;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramRemoveFileTest extends AbstractComplexComputerTest {

    private static final EventPredicate<?> UPDATE_VIEW_PREDICATE = new TypePredicate<>(FMP_SBPWPU_UpdateViewCommand.class);

    private static final String            FS_MOUNTPOINT         = CommonFiles.USER_MOUNTPOINT;
    private static final String            ROOT                  = "/" + FS_MOUNTPOINT;
    private static final String            TEST_PATH             = ROOT + "/test1/test2/test.txt";

    private final ContentFile              path1File             = new ContentFile();
    private WorldProcessId                 processId;

    @Before
    public void setUp() {

        mainFsModule().invoke(FileSystemModule.CREATE_ADD_FILE, path1File, TEST_PATH).invoke(FileAddAction.EXECUTE);
    }

    private void executeProgramAndSendChangeDirCommand(Process<?> parentProcess, String change) {

        ChildProcess process = launchProgram(parentProcess, getCommonLocation(FileManagerProgram.class));
        processId = process.invoke(Process.GET_WORLD_PROCESS_ID);

        bridge.send(new FMP_WP_ChangeDirCommand(processId, change));
    }

    private Event createRemoveFileCommand(String fileName) {

        return new FMP_WP_RemoveFileCommand(processId, fileName);
    }

    private void sendRemoveFileCommand(String fileName) {

        bridge.send(createRemoveFileCommand(fileName));
    }

    @Test
    public void testWithContentFile() {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), splitBeforeName(TEST_PATH)[0]);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FMP_SBPWPU_UpdateViewCommand>() {

            @Override
            public void handle(FMP_SBPWPU_UpdateViewCommand event) {

                assertEquals("File path", splitBeforeName(TEST_PATH)[0], event.getCurrentDir());
                assertTrue("File hasn't been removed", event.getFiles().length == 0);

                invoked.setTrue();
            }

        }, UPDATE_VIEW_PREDICATE);

        sendRemoveFileCommand(splitBeforeName(TEST_PATH)[1]);

        assertTrue("Update view handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithFileNameWithSeparators() {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), resolve(ROOT, "test1/test2"));

        bridge.getModule(StandardHandlerModule.class).addHandler(new FailEventHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "removeFile.invalidFileName", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { "test4/test.txt" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        sendRemoveFileCommand("test4/test.txt");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithCurrentDir() {

        internalTestWithCurrentDir(splitBeforeName(TEST_PATH)[0]);
    }

    private void internalTestWithCurrentDir(String dir) {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), dir);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FailEventHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "removeFile.invalidFileName", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { "." }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        sendRemoveFileCommand(".");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithAbsoluteRoot() {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), "/");

        bridge.getModule(StandardHandlerModule.class).addHandler(new FailEventHandler(), UPDATE_VIEW_PREDICATE);

        final Event event = createRemoveFileCommand(".");

        final SBPAwareEventHandlerExceptionCatcher exceptionCatcher = context.mock(SBPAwareEventHandlerExceptionCatcher.class);
        bridge.getModule(SBPAwareHandlerExtension.class).addExceptionCatcher(exceptionCatcher);

        // @formatter:off
        context.checking(new Expectations() {{

            // Expect one IllegalStateException
            oneOf(exceptionCatcher).handle(with(any(IllegalStateException.class)), with(any(SBPAwareEventHandler.class)), with(event), with(nullValue(BridgeConnector.class)));

        }});
        // @formatter:on

        bridge.send(event);
    }

    @Test
    public void testWithRootFile() {

        internalTestWithCurrentDir("/" + FS_MOUNTPOINT);
    }

    @Test
    public void testMissingDeleteRight() {

        // Create a new user and a new session under which the process will run
        final User testUser = new User();
        testUser.setObj(User.NAME, "testUser");
        ChildProcess sessionProcess = launchSession(mainRootProcess(), testUser, null);

        executeProgramAndSendChangeDirCommand(sessionProcess, splitBeforeName(TEST_PATH)[0]);

        // Remove all rights from the file
        path1File.setObj(File.RIGHTS, new FileRights());

        bridge.getModule(StandardHandlerModule.class).addHandler(new FailEventHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "removeFile.missingDeleteRight", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { TEST_PATH }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        sendRemoveFileCommand(splitBeforeName(TEST_PATH)[1]);

        assertTrue("Missing right event handler hasn't been invoked", invoked.getValue());
    }

}
