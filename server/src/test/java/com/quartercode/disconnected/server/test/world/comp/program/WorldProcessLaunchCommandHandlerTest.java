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

package com.quartercode.disconnected.server.test.world.comp.program;

import static org.junit.Assert.*;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.comp.program.Program;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.RootProcess;
import com.quartercode.disconnected.server.world.comp.program.WorldProcessLaunchCommandHandler;
import com.quartercode.disconnected.shared.client.ClientIdentity;
import com.quartercode.disconnected.shared.event.program.control.WorldProcessLaunchAcknowledgement;
import com.quartercode.disconnected.shared.event.program.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.program.ClientProcessId;
import com.quartercode.disconnected.shared.program.WorldProcessId;
import com.quartercode.eventbridge.bridge.Bridge;

public class WorldProcessLaunchCommandHandlerTest {

    @Rule
    public JUnitRuleMockery                      context = new JUnitRuleMockery();

    private WorldProcessLaunchCommandHandlerMock handler;

    @Mock
    private Bridge                               bridge;
    private Computer                             clientComputer;
    private ProcessModule                        procModule;
    private Process<?>                           sessionProcess;
    private ContentFile                          sourceFile;

    @Before
    public void setUp() {

        clientComputer = new Computer();
        procModule = new ProcessModule();
        sessionProcess = new RootProcess();

        sourceFile = new ContentFile();
        Program program = new Program();
        program.setObj(Program.EXECUTOR_CLASS, TestProgram.class);
        sourceFile.setObj(ContentFile.CONTENT, program);

        handler = new WorldProcessLaunchCommandHandlerMock(bridge, clientComputer, procModule, sessionProcess, sourceFile);
    }

    @Test
    public void test() {

        launchAndAssert(1, 0);
        launchAndAssert(2, 1);
        launchAndAssert(3, 2);
        launchAndAssert(4, 3);
        launchAndAssert(5, 4);

        procModule.setObj(ProcessModule.NEXT_PID_VALUE, 10);
        launchAndAssert(6, 10);
        launchAndAssert(7, 11);
        launchAndAssert(8, 12);
        launchAndAssert(9, 13);
        launchAndAssert(10, 14);
    }

    private void launchAndAssert(int run, final int expectedPid) {

        handler.currentLaunchedProcessId = expectedPid;

        final ClientIdentity clientIdentity = new ClientIdentity("client");
        final int clientPid = 20;

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(bridge).send(new WorldProcessLaunchAcknowledgement(new ClientProcessId(clientIdentity, clientPid), new WorldProcessId(clientComputer.getId(), expectedPid)));

        }});
        // @formatter:on

        WorldProcessLaunchCommand event = new WorldProcessLaunchCommand(clientPid, "testPath");
        handler.handle(event, clientIdentity);

        Process<?> child = sessionProcess.getCol(Process.CHILDREN).get(run - 1);
        assertNotNull("Child process was not created", child);
        assertEquals("Pid of child process", expectedPid, (int) child.getObj(Process.PID));
        assertEquals("Client process id of child process", new ClientProcessId(clientIdentity, clientPid), child.getObj(Process.CLIENT_PROCESS));

        ProgramExecutor executor = child.getObj(Process.EXECUTOR);
        assertNotNull("Child process has no program executor", executor);
        assertTrue("Child process has an incorrect program executor (wrong type)", executor instanceof TestProgram);
    }

    private static class WorldProcessLaunchCommandHandlerMock extends WorldProcessLaunchCommandHandler {

        private final Bridge        bridge;
        private final Computer      clientComputer;
        private final ProcessModule procModule;
        private final Process<?>    sessionProcess;
        private final ContentFile   sourceFile;

        private int                 currentLaunchedProcessId;

        private WorldProcessLaunchCommandHandlerMock(Bridge bridge, Computer clientComputer, ProcessModule procModule, Process<?> sessionProcess, ContentFile sourceFile) {

            this.bridge = bridge;
            this.clientComputer = clientComputer;
            this.procModule = procModule;
            this.sessionProcess = sessionProcess;
            this.sourceFile = sourceFile;
        }

        @Override
        protected Bridge getBridge() {

            return bridge;
        }

        @Override
        protected Computer getClientComputer(ClientIdentity client) {

            return clientComputer;
        }

        @Override
        protected ProcessModule getProcessModule(Computer computer) {

            return procModule;
        }

        @Override
        protected Process<?> getSessionProcess(Computer computer) {

            return sessionProcess;
        }

        @Override
        protected ContentFile getSourceFile(Computer computer, String path) {

            assertEquals("Provided program file path", "testPath", path);

            return sourceFile;
        }

        @Override
        protected WorldProcessId getProcessId(ProgramExecutor executor) {

            return new WorldProcessId(clientComputer.getId(), currentLaunchedProcessId);
        }

    }

    // Must be protected in order to make newInstance() work
    protected static class TestProgram extends ProgramExecutor {

        public TestProgram() {

        }

    }

}
