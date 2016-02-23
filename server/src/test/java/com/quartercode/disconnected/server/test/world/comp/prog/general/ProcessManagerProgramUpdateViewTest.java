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

import static org.junit.Assert.assertEquals;
import java.util.UUID;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.Session;
import com.quartercode.disconnected.server.world.comp.prog.general.ProcessManagerProgram;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessId;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessPlaceholder;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessState;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;

public class ProcessManagerProgramUpdateViewTest extends ProcessManagerProgramAbstractTest {

    private final ChildProcess[] testProcesses = new ChildProcess[5];

    private ChildProcess         process;
    private WorldProcessId       processId;

    @Before
    public void setUp() {

        // Launch some instances of the test program; process tree structure:
        // - root
        // --- 0
        // --- 1
        // ----- 2
        // ------- 3
        // ----- 4
        testProcesses[0] = launchProgram(mainRootProcess(), testProgramSourceFile);
        testProcesses[1] = launchProgram(mainRootProcess(), testProgramSourceFile);
        testProcesses[2] = launchProgram(testProcesses[1], testProgramSourceFile);
        testProcesses[3] = launchProgram(testProcesses[2], testProgramSourceFile);
        testProcesses[4] = launchProgram(testProcesses[1], testProgramSourceFile);

        // Launch the process manager program
        process = launchProgram(mainRootProcess(), getCommonLocation(ProcessManagerProgram.class));
        processId = process.invoke(Process.GET_WORLD_PROCESS_ID);
    }

    @Test
    public void test() {

        final MutableInt invocationCounter = new MutableInt();
        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<PMP_SBPWPU_UpdateViewCommand>() {

            @Override
            public void handle(PMP_SBPWPU_UpdateViewCommand event) {

                // Is always the same
                UUID computerUUID = processId.getComputerUUID();

                // Test processes
                WorldProcessPlaceholder test4 = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 5), WorldProcessState.RUNNING, "root", TEST_PROGRAM_PATH, "testProgram");
                WorldProcessPlaceholder test3 = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 4), WorldProcessState.RUNNING, "root", TEST_PROGRAM_PATH, "testProgram");
                WorldProcessPlaceholder test2 = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 3), WorldProcessState.RUNNING, "root", new WorldProcessPlaceholder[] { test3 }, TEST_PROGRAM_PATH, "testProgram");
                WorldProcessPlaceholder test1 = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 2), WorldProcessState.RUNNING, "root", new WorldProcessPlaceholder[] { test2, test4 }, TEST_PROGRAM_PATH, "testProgram");
                WorldProcessPlaceholder test0 = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 1), WorldProcessState.RUNNING, "root", TEST_PROGRAM_PATH, "testProgram");

                // The process manager process itself
                WorldProcessPlaceholder pmp = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 6), WorldProcessState.RUNNING, "root", getCommonLocation(ProcessManagerProgram.class).toString(), "processManager");

                // Root process
                WorldProcessPlaceholder root = new WorldProcessPlaceholder(new WorldProcessId(computerUUID, 0), WorldProcessState.RUNNING, "root", new WorldProcessPlaceholder[] { test0, test1, pmp }, getCommonLocation(Session.class).toString(), "session");

                assertEquals("Returned root process placeholder", root, event.getRootProcess());

                invocationCounter.increment();
            }

        }, UPDATE_VIEW_PREDICATE);

        // Update the scheduler in order to send one event
        for (int counter = 0; counter < 1 + ProcessManagerProgram.UPDATE_VIEW_SENDING_PERIOD * 2; counter++) {
            process.getObj(Process.EXECUTOR).get(ProcessManagerProgram.SCHEDULER).update("computer.processUpdate");
        }

        assertEquals("Update view handler invocation count", 3, invocationCounter.intValue());
    }

}
