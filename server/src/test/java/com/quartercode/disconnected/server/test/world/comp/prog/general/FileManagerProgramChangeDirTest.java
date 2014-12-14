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

import static com.quartercode.disconnected.shared.world.comp.file.PathUtils.resolve;
import static org.junit.Assert.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.AbstractComplexComputerTest;
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.FSModule;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.general.FileManagerProgram;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMPWPUUpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMPWorldChangeDirCommand;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramChangeDirTest extends AbstractComplexComputerTest {

    private static final EventPredicate<?> UPDATE_VIEW_PREDICATE = new TypePredicate<>(FMPWPUUpdateViewCommand.class);

    private static final String            FS_MOUNTPOINT         = CommonFiles.USER_MOUNTPOINT;
    private static final String            ROOT                  = "/" + FS_MOUNTPOINT;

    private WorldProcessId                 processId;

    @Before
    public void setUp() {

        // Create the test directories
        FileSystem fs = mainFs(FS_MOUNTPOINT);
        fs.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test1/test2").invoke(FileAddAction.EXECUTE);
        fs.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test1/test2/test5").invoke(FileAddAction.EXECUTE);
        fs.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test1/test2/test6").invoke(FileAddAction.EXECUTE);
        fs.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test1/test3").invoke(FileAddAction.EXECUTE);
        fs.invoke(FileSystem.CREATE_ADD_FILE, new Directory(), "/test4").invoke(FileAddAction.EXECUTE);

        // Launch the program
        ChildProcess process = launchProgram(mainRootProcess(), getCommonLocation(FileManagerProgram.class));
        processId = process.invoke(Process.GET_WORLD_PROCESS_ID);
    }

    private void sendChangeDirCommand(String change) {

        bridge.send(new FMPWorldChangeDirCommand(processId, change));
    }

    @Test
    public void testWithDirectory() {

        MutableBoolean invoked = new MutableBoolean();

        // /test1/test2
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(resolve(ROOT, "test1/test2"), invoked), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand(resolve(ROOT, "test1/test2"));

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithDirectoryMultipleTimes() {

        MutableBoolean invoked1 = new MutableBoolean();
        MutableBoolean invoked2 = new MutableBoolean();
        MutableBoolean invoked3 = new MutableBoolean();
        MutableBoolean invoked4 = new MutableBoolean();

        // /test1
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(resolve(ROOT, "test1"), invoked1), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand(resolve(ROOT, "test1"));

        // /test1/test2
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(resolve(ROOT, "test1/test2"), invoked2), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand("test2");

        // /test1/test3
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(resolve(ROOT, "test1/test3"), invoked3), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand("../test3");

        // /test4
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(resolve(ROOT, "test4"), invoked4), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand(resolve(ROOT, "test4"));

        assertTrue("Handler 1 hasn't been invoked", invoked1.getValue());
        assertTrue("Handler 2 hasn't been invoked", invoked2.getValue());
        assertTrue("Handler 3 hasn't been invoked", invoked3.getValue());
        assertTrue("Handler 4 hasn't been invoked", invoked4.getValue());
    }

    @Test
    public void testWithAbsoluteRoot() {

        MutableBoolean invoked = new MutableBoolean();

        // /
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler("/", invoked), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand("/");

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithRootFile() {

        MutableBoolean invoked = new MutableBoolean();

        // /system
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler("/" + CommonFiles.SYSTEM_MOUNTPOINT, invoked), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand(CommonFiles.SYSTEM_MOUNTPOINT);

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithInvalidDirectory() {

        MutableBoolean invoked = new MutableBoolean();

        // /test1/test2/test7 (invalid)
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(null, invoked), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand(resolve(ROOT, "test1/test2/test7"));

        assertFalse("Handler has been invoked", invoked.getValue());
    }

    @Test
    public void testWithSuddenlyInvalidDirectory() {

        MutableBoolean invoked1 = new MutableBoolean();
        MutableBoolean invoked2 = new MutableBoolean();

        // /test1/test2/test5
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(resolve(ROOT, "test1/test2/test5"), invoked1), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand(resolve(ROOT, "test1/test2/test5"));

        // Delete /test1/test2
        mainFsModule().invoke(FSModule.GET_FILE, resolve(ROOT, "test1/test2")).invoke(File.CREATE_REMOVE).invoke(FileRemoveAction.EXECUTE);

        // /test1/test2/test6 (should result in /test1 because /test1/test2 does no longer exist)
        bridge.getModule(StandardHandlerModule.class).addHandler(new FMPWPUUpdateViewCommandTestHandler(resolve(ROOT, "test1"), invoked2), UPDATE_VIEW_PREDICATE);
        sendChangeDirCommand(resolve(ROOT, "/test1/test2/test6"));

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
