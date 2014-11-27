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

import static org.junit.Assert.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.AbstractComplexComputerTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.os.user.User;
import com.quartercode.disconnected.server.world.comp.program.ChildProcess;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.program.general.FileManagerProgram;
import com.quartercode.disconnected.shared.event.comp.program.general.FMPWPUUpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.program.general.FMPWorldAddFileCommand;
import com.quartercode.disconnected.shared.event.comp.program.general.FMPWorldChangeDirCommand;
import com.quartercode.disconnected.shared.event.comp.program.generic.GPWPUErrorEvent;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.program.WorldProcessId;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramAddFileTest extends AbstractComplexComputerTest {

    private static final EventPredicate<?> UPDATE_VIEW_PREDICATE = new TypePredicate<>(FMPWPUUpdateViewCommand.class);

    private static final String            FS_MOUNTPOINT         = CommonFiles.USER_MOUNTPOINT;
    private static final String            DIR_PATH              = "/" + FS_MOUNTPOINT + "/test1/test2";

    private final Directory                parentFile            = new Directory();
    private WorldProcessId                 processId;

    @Before
    public void setUp() {

        mainFsModule().invoke(FileSystemModule.CREATE_ADD_FILE, parentFile, DIR_PATH).invoke(FileAddAction.EXECUTE);
    }

    private void executeProgramAndSendChangeDirCommand(Process<?> parentProcess, String change) {

        ChildProcess process = launchProgram(parentProcess, getCommonLocation(FileManagerProgram.class));
        processId = ProgramUtils.getProcessId(process);

        bridge.send(new FMPWorldChangeDirCommand(processId, change));
    }

    private void sendAddFileCommand(String fileName) {

        bridge.send(new FMPWorldAddFileCommand(processId, fileName, "contentFile"));
    }

    @Test
    public void testWithContentFile() {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), DIR_PATH);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FMPWPUUpdateViewCommand>() {

            @Override
            public void handle(FMPWPUUpdateViewCommand event) {

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

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "createFile.invalidFileName", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { "test/test.txt" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendAddFileCommand("test/test.txt");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithCurrentDir() {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), DIR_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UPDATE_VIEW_PREDICATE);

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

        executeProgramAndSendChangeDirCommand(mainRootProcess(), "/");

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UPDATE_VIEW_PREDICATE);

        sendAddFileCommand(".");
    }

    @Test (expected = IllegalStateException.class)
    public void testWithRootFile() {

        executeProgramAndSendChangeDirCommand(mainRootProcess(), "/");

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UPDATE_VIEW_PREDICATE);

        sendAddFileCommand(FS_MOUNTPOINT);
    }

    @Test
    public void testOccupiedPath() {

        // Add content file that makes the path occupied
        mainFsModule().invoke(FileSystemModule.CREATE_ADD_FILE, new ContentFile(), DIR_PATH + "/test.txt").invoke(FileAddAction.EXECUTE);

        executeProgramAndSendChangeDirCommand(mainRootProcess(), DIR_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "createFile.occupiedPath", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { DIR_PATH + "/test.txt" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendAddFileCommand("test.txt");

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testOutOfSpace() {

        // Set size of the file system to something very small
        mainFs(FS_MOUNTPOINT).setObj(FileSystem.SIZE, 10L);

        executeProgramAndSendChangeDirCommand(mainRootProcess(), DIR_PATH);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "createFile.outOfSpace", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { DIR_PATH + "/test.txt" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

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

        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPUpdateViewFailHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GPWPUErrorEvent>() {

            @Override
            public void handle(GPWPUErrorEvent event) {

                assertEquals("Error type", "createFile.missingWriteRight", event.getType());
                assertArrayEquals("Error arguments (file path)", new String[] { DIR_PATH + "/test.txt" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GPWPUErrorEvent.class));

        sendAddFileCommand("test.txt");

        assertTrue("Missing right error event handler hasn't been invoked", invoked.getValue());
    }

}
