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
import com.quartercode.disconnected.server.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.program.ChildProcess;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.program.general.FileManagerProgram;
import com.quartercode.disconnected.shared.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.comp.program.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.comp.program.WorldProcessId;
import com.quartercode.disconnected.shared.event.program.general.FMPWPUUpdateViewCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldChangeDirCommand;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramChangeDirTest extends AbstractProgramTest {

    private static final String            ROOT               = "/" + CommonFiles.SYSTEM_MOUNTPOINT;
    private static final EventPredicate<?> RESPONSE_PREDICATE = new TypePredicate<>(FMPWPUUpdateViewCommand.class);

    private WorldProcessId                 processId;

    public FileManagerProgramChangeDirTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    @Before
    public void setUp2() {

        // Create the test directories
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test1/test2").invoke(FileAddAction.EXECUTE);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test1/test2/test5").invoke(FileAddAction.EXECUTE);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test1/test2/test6").invoke(FileAddAction.EXECUTE);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test1/test3").invoke(FileAddAction.EXECUTE);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test4").invoke(FileAddAction.EXECUTE);

        // Launch the program
        ChildProcess process = processModule.getObj(ProcessModule.ROOT_PROCESS).invoke(Process.CREATE_CHILD);
        process.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(FileManagerProgram.class).toString())[1]));
        process.setObj(Process.WORLD_PROCESS_USER, new SBPWorldProcessUserId(SBP, null));
        process.invoke(Process.INITIALIZE, 10);

        ProgramExecutor program = process.getObj(Process.EXECUTOR);
        program.invoke(ProgramExecutor.RUN);

        processId = ProgramUtils.getProcessId(program);
    }

    private void sendChangeDirCommand(String change) {

        bridge.send(new FMPWorldChangeDirCommand(processId, change));
    }

    @Test
    public void testWithDirectory() {

        MutableBoolean invoked = new MutableBoolean();

        // /test1/test2
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(ROOT + "/test1/test2", invoked), RESPONSE_PREDICATE);
        sendChangeDirCommand(ROOT + "/test1/test2");

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithDirectoryMultipleTimes() {

        MutableBoolean invoked1 = new MutableBoolean();
        MutableBoolean invoked2 = new MutableBoolean();
        MutableBoolean invoked3 = new MutableBoolean();
        MutableBoolean invoked4 = new MutableBoolean();

        // /test1
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(ROOT + "/test1", invoked1), RESPONSE_PREDICATE);
        sendChangeDirCommand(ROOT + "/test1");

        // /test1/test2
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(ROOT + "/test1/test2", invoked2), RESPONSE_PREDICATE);
        sendChangeDirCommand("test2");

        // /test1/test3
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(ROOT + "/test1/test3", invoked3), RESPONSE_PREDICATE);
        sendChangeDirCommand("../test3");

        // /test4
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(ROOT + "/test4", invoked4), RESPONSE_PREDICATE);
        sendChangeDirCommand(ROOT + "/test4");

        assertTrue("Handler 1 hasn't been invoked", invoked1.getValue());
        assertTrue("Handler 2 hasn't been invoked", invoked2.getValue());
        assertTrue("Handler 3 hasn't been invoked", invoked3.getValue());
        assertTrue("Handler 4 hasn't been invoked", invoked4.getValue());
    }

    @Test
    public void testWithAbsoluteRoot() {

        MutableBoolean invoked = new MutableBoolean();

        // /
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler("/", invoked), RESPONSE_PREDICATE);
        sendChangeDirCommand("/");

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithRootFile() {

        MutableBoolean invoked = new MutableBoolean();

        // /system
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler("/" + CommonFiles.SYSTEM_MOUNTPOINT, invoked), RESPONSE_PREDICATE);
        sendChangeDirCommand(CommonFiles.SYSTEM_MOUNTPOINT);

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithInvalidDirectory() {

        MutableBoolean invoked = new MutableBoolean();

        // /test1/test2/test7 (invalid)
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(null, invoked), RESPONSE_PREDICATE);
        sendChangeDirCommand(ROOT + "/test1/test2/test7");

        assertFalse("Handler has been invoked", invoked.getValue());
    }

    @Test
    public void testWithSuddenlyInvalidDirectory() {

        MutableBoolean invoked1 = new MutableBoolean();
        MutableBoolean invoked2 = new MutableBoolean();

        // /test1/test2/test5
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(ROOT + "/test1/test2/test5", invoked1), RESPONSE_PREDICATE);
        sendChangeDirCommand(ROOT + "/test1/test2/test5");

        // Delete /test1/test2
        fileSystem.invoke(FileSystem.GET_FILE, "/test1/test2").invoke(File.CREATE_REMOVE).invoke(FileRemoveAction.EXECUTE);

        // /test1/test2/test6 (should result in /test1 because /test1/test2 does no longer exist)
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(ROOT + "/test1", invoked2), RESPONSE_PREDICATE);
        sendChangeDirCommand(ROOT + "/test1/test2/test6");

        assertTrue("Handler 1 hasn't been invoked", invoked1.getValue());
        assertTrue("Handler 2 hasn't been invoked", invoked2.getValue());
    }

    private class FMPWPUUpdateViewCommandTestHandler implements EventHandler<FMPWPUUpdateViewCommand> {

        private final String         expectedCurrentDir;
        private final MutableBoolean invoked;

        private FMPWPUUpdateViewCommandTestHandler(String expectedCurrentDir, MutableBoolean invoked) {

            this.expectedCurrentDir = expectedCurrentDir;
            this.invoked = invoked;
        }

        @Override
        public void handle(FMPWPUUpdateViewCommand event) {

            if (expectedCurrentDir != null) {
                assertEquals("New current dir", expectedCurrentDir, event.getCurrentDir());
            }

            invoked.setTrue();
            bridge.getModule(StandardHandlerModule.class).removeHandler(this);
        }

    }

}
