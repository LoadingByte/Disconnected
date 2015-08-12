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
import java.util.UUID;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.Session;
import com.quartercode.disconnected.server.world.comp.prog.general.ProcessManagerProgram;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_WP_InterruptProcessCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessPlaceholder;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessState;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class ProcessManagerProgramInterruptProcessTest extends ProcessManagerProgramAbstractTest {

    private final ChildProcess[] testProcesses = new ChildProcess[3];

    private ChildProcess         process;
    private WorldProcessId       processId;

    @Before
    public void setUp() {

        // Launch some instances of the test program
        testProcesses[0] = launchProgram(mainRootProcess(), testProgramSourceFile);
        testProcesses[1] = launchProgram(mainRootProcess(), testProgramSourceFile);
        testProcesses[2] = launchProgram(mainRootProcess(), testProgramSourceFile);
    }

    private void executeProgram(Process<?> parentProcess) {

        process = launchProgram(parentProcess, getCommonLocation(ProcessManagerProgram.class));
        processId = process.invoke(Process.GET_WORLD_PROCESS_ID);
    }

    private void sendInterruptProcessCommand(int pid) {

        bridge.send(new PMP_WP_InterruptProcessCommand(processId, pid));
    }

    @Test
    public void testInterrupt() {

        executeProgram(mainRootProcess());

        // Interrupt some test processes
        sendInterruptProcessCommand(1);
        sendInterruptProcessCommand(3);

        // Interrupt an unknown process (should do nothing)
        sendInterruptProcessCommand(10);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<PMP_SBPWPU_UpdateViewCommand>() {

            @Override
            public void handle(PMP_SBPWPU_UpdateViewCommand event) {

                // Is always the same
                UUID computerUUID = processId.getComputerUUID();

                // Test processes
                WorldProcessPlaceholder test0 = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 1), WorldProcessState.INTERRUPTED, "root", TEST_PROGRAM_PATH, "testProgram");
                WorldProcessPlaceholder test1 = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 2), WorldProcessState.RUNNING, "root", TEST_PROGRAM_PATH, "testProgram");
                WorldProcessPlaceholder test2 = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 3), WorldProcessState.INTERRUPTED, "root", TEST_PROGRAM_PATH, "testProgram");

                // The process manager process itself
                WorldProcessPlaceholder pmp = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 4), WorldProcessState.RUNNING, "root", getCommonLocation(ProcessManagerProgram.class).toString(), "processManager");

                // Root process
                WorldProcessPlaceholder root = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 0), WorldProcessState.RUNNING, "root", new WorldProcessPlaceholder[] { test0, test1, test2, pmp }, getCommonLocation(Session.class).toString(), "session");

                assertEquals("Returned root process placeholder", root, event.getRootProcess());

                invoked.setTrue();
            }

        }, UPDATE_VIEW_PREDICATE);

        // Update the scheduler once in order to send one UpdateView event
        process.getObj(Process.EXECUTOR).get(ProcessManagerProgram.SCHEDULER).update("computer.programUpdate");

        assertTrue("Update view handler hasn't been invoked", invoked.booleanValue());
    }

    @Test
    public void testInterruptRootProcess() {

        executeProgram(mainRootProcess());

        bridge.getModule(StandardHandlerModule.class).addHandler(new FailEventHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "interruptProcess.missingRights", event.getType());
                assertArrayEquals("Error arguments (process name, pid)", new String[] { "session.exe", "0" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        // Interrupt the root process
        sendInterruptProcessCommand(0);

        assertTrue("Error event handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testMissingRights() {

        // Create a new user and a new session under which the process will run
        final User testUser = new User();
        testUser.setObj(User.NAME, "testUser");
        ChildProcess sessionProcess = launchSession(mainRootProcess(), testUser, null);

        executeProgram(sessionProcess);

        bridge.getModule(StandardHandlerModule.class).addHandler(new FailEventHandler(), UPDATE_VIEW_PREDICATE);

        final MutableBoolean invoked = new MutableBoolean();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<GP_SBPWPU_ErrorEvent>() {

            @Override
            public void handle(GP_SBPWPU_ErrorEvent event) {

                assertEquals("Error type", "interruptProcess.missingRights", event.getType());
                assertArrayEquals("Error arguments (process name, pid)", new String[] { PathUtils.splitBeforeName(TEST_PROGRAM_PATH)[1], "1" }, event.getArguments());

                invoked.setTrue();
            }

        }, new TypePredicate<>(GP_SBPWPU_ErrorEvent.class));

        // Interrupt a test process
        sendInterruptProcessCommand(1);

        assertTrue("Missing rights event handler hasn't been invoked", invoked.getValue());
    }

}
