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

package com.quartercode.disconnected.server.test.event.prog.control;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.server.event.prog.control.WorldProcessInterruptCommandHandler;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.RootProcess;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessInterruptCommand;
import com.quartercode.disconnected.shared.identity.ClientIdentity;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessState;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;

@RunWith (Parameterized.class)
public class WorldProcessInterruptCommandHandlerTest {

    private static final SBPIdentity SBP = new ClientIdentity("client");

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { false });
        data.add(new Object[] { true });

        return data;
    }

    private final boolean recursive;

    public WorldProcessInterruptCommandHandlerTest(boolean recursive) {

        this.recursive = recursive;
    }

    @Test
    public void testFlat() {

        RootProcess rootProcess = new RootProcess();

        Process<?> process = launch(rootProcess, 1);
        interruptAndAssert(process, recursive);
    }

    @Test
    public void testFlatMultipleAvailable() {

        RootProcess rootProcess = new RootProcess();

        Process<?> process1 = launch(rootProcess, 1);
        Process<?> process2 = launch(rootProcess, 2);

        interruptAndAssert(process1, recursive);

        assertEquals("State of not-interrupted process", WorldProcessState.RUNNING, process2.getObj(Process.STATE));
    }

    @Test
    public void testOneChild() {

        RootProcess rootProcess = new RootProcess();

        Process<?> process = launch(rootProcess, 1);
        launch(process, 2);

        interruptAndAssert(process, recursive);
    }

    @Test
    public void testMultipleChildren() {

        RootProcess rootProcess = new RootProcess();

        Process<?> process = launch(rootProcess, 1);
        launch(process, 2);
        Process<?> child2 = launch(process, 3);
        launch(child2, 4);

        interruptAndAssert(process, recursive);
    }

    private Process<?> launch(Process<?> parent, int pid) {

        ChildProcess process = parent.invoke(Process.CREATE_CHILD);
        process.setObj(Process.PID, pid);
        process.setObj(Process.STATE, WorldProcessState.RUNNING);
        process.setObj(Process.WORLD_PROCESS_USER, new SBPWorldProcessUserId(SBP, null));
        return process;
    }

    private void interruptAndAssert(Process<?> process, boolean recursive) {

        WorldProcessInterruptCommandHandlerMock handler = new WorldProcessInterruptCommandHandlerMock(process);

        WorldProcessInterruptCommand event = new WorldProcessInterruptCommand(process.getObj(Process.PID), recursive);
        handler.handle(event, SBP);

        assertEquals("State of interrupted process", WorldProcessState.INTERRUPTED, process.getObj(Process.STATE));

        for (Process<?> child : process.invoke(Process.GET_ALL_CHILDREN)) {
            if (recursive) {
                assertEquals("State of interrupted child process", WorldProcessState.INTERRUPTED, child.getObj(Process.STATE));
            } else {
                assertEquals("State of non-interrupted child process", WorldProcessState.RUNNING, child.getObj(Process.STATE));
            }
        }
    }

    @RequiredArgsConstructor
    private static class WorldProcessInterruptCommandHandlerMock extends WorldProcessInterruptCommandHandler {

        private final Process<?> process;

        @Override
        protected Process<?> getSBPProcess(SBPIdentity sbp, int pid) {

            assertEquals("Pid for interrupted process", (int) process.getObj(Process.PID), pid);

            return process;
        }

    }

}
