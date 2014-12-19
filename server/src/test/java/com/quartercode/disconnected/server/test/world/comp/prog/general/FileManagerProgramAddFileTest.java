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

package com.quartercode.disconnected.server.test.world.comp.prog.general;

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
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.FSModule;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.general.FileManagerProgram;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_ChangeDirCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_AddFileCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramAddFileTest extends AbstractComplexComputerTest {

    private static final EventPredicate<?> UPDATE_VIEW_PREDICATE = new TypePredicate<>(FMP_SBPWPU_UpdateViewCommand.class);

    private static final String            FS_MOUNTPOINT         = CommonFiles.USER_MOUNTPOINT;
    private static final String            DIR_PATH              = "/" + FS_MOUNTPOINT + "/test1/test2";

    private final Directory                parentFile            = new Directory();
    private WorldProcessId                 processId;

    @Before
    public void setUp() {

        mainFsModule().invoke(FSModule.CREATE_ADD_FILE, parentFile, DIR_PATH).invoke(FileAddAction.EXECUTE);
    }

    private void executeProgramAndSendChangeDirCommand(Process<?> parentProcess, String change) {

        ChildProcess process = launchProgram(parentProcess, getCommonLocation(FileManagerProgram.class));
        processId = process.invoke(Process.GET_WORLD_PROCESS_ID);

        bridge.send(new FMP_WP_ChangeDirCommand(processId, change));
    }

    private Event createAddFileCommand(String fileName) {

        return new FMP_WP_AddFileCommand(processId, fileName, "contentFile");
    }

    private void sendAddFileCommand(String fileName) {

        bridge.send(createAddFileCommand(fileName));
    }

    @Test
    public void testWithContentFile() {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), DIR_PATH);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FMP_SBPWPU_UpdateViewCommand>() {

            @Override
            public void handle(FMP_SBPWPU_UpdateViewCommand event) {

                assertEquals("File path", DIR_PATH, event.getCurrentDir());

                FilePlaceholder[] files = event.getFiles();
                assertTrue("The wrong amount of files (" + files.length + ") has been created", files.length == 1);
                FilePlaceholder file = files[0];
                assertTrue("File hasn't been created correctly", file.getPath().equals(DIR_PATH + "/test.txt") && file.getType().equals("contentFile"));

                invoked.setTrue();
            }

        }, UPDATE_VIEW_PREDICATE);

        sendAddFileCommand("test.txt");

        assertTrue("Update view handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithFileNameWithSeparators() {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), DIR_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMP_WPUSBP_UpdateViewCommandFailHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "createFile.invalidFileName", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { "test/test.txt" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        sendAddFileCommand("test/test.txt");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithCurrentDir() {

        internalTestWithCurrentDir(DIR_PATH);
    }

    private void internalTestWithCurrentDir(String dir) {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), dir);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMP_WPUSBP_UpdateViewCommandFailHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "createFile.invalidFileName", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { "." }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        sendAddFileCommand(".");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithAbsoluteRoot() {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), "/");

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMP_WPUSBP_UpdateViewCommandFailHandler(), UPDATE_VIEW_PREDICATE);

        final Event event = createAddFileCommand(".");

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
    public void testOccupiedPath() {

        // Add content file that makes the path occupied
        mainFsModule().invoke(FSModule.CREATE_ADD_FILE, new ContentFile(), DIR_PATH + "/test.txt").invoke(FileAddAction.EXECUTE);

        executeProgramAndSendChangeDirCommand(mainRootProcess(), DIR_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMP_WPUSBP_UpdateViewCommandFailHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "createFile.occupiedPath", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { DIR_PATH + "/test.txt" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        sendAddFileCommand("test.txt");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testOutOfSpace() {

        // Set size of the file system to something very small
        mainFs(FS_MOUNTPOINT).setObj(FileSystem.SIZE, 10L);

        executeProgramAndSendChangeDirCommand(mainRootProcess(), DIR_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMP_WPUSBP_UpdateViewCommandFailHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "createFile.outOfSpace", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { DIR_PATH + "/test.txt" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        sendAddFileCommand("test.txt");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testMissingWriteRight() {

        // Create a new user and a new session under which the process will run
        User testUser = new User();
        testUser.setObj(User.NAME, "testUser");
        ChildProcess sessionProcess = launchSession(mainRootProcess(), testUser, null);

        executeProgramAndSendChangeDirCommand(sessionProcess, DIR_PATH);

        // Remove all rights from the parent file
        parentFile.setObj(File.RIGHTS, new FileRights());

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMP_WPUSBP_UpdateViewCommandFailHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "createFile.missingWriteRight", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { DIR_PATH + "/test.txt" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        sendAddFileCommand("test.txt");

        assertTrue("Missing right error event handler hasn't been invoked", invoked.getValue());
    }

}
