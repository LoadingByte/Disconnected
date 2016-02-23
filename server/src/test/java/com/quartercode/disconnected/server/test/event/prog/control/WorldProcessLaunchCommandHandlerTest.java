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

package com.quartercode.disconnected.server.test.event.prog.control;

import static org.junit.Assert.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.event.prog.control.WorldProcessLaunchCommandHandler;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.prog.ProcModule;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.Program;
import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.prog.RootProcess;
import com.quartercode.disconnected.shared.CommonBootstrap;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchAcknowledgmentEvent;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.identity.ClientIdentity;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.MultipleValueRegistry;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.proc.SBPWorldProcessUserDetails;
import com.quartercode.disconnected.shared.world.comp.proc.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.world.comp.proc.UUIDSBPWorldProcessUserDetails;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessId;
import com.quartercode.eventbridge.bridge.Bridge;

public class WorldProcessLaunchCommandHandlerTest {

    private static final SBPIdentity SBP = new ClientIdentity("client");

    @BeforeClass
    public static void setUpBeforeClass() {

        CommonBootstrap.bootstrap();

        Registries.get(ServerRegistries.WORLD_PROGRAMS).addValue(new WorldProgram("_testProgram", TestProgram.class, 0, null));
    }

    @AfterClass
    public static void tearDownAfterClass() {

        MultipleValueRegistry<WorldProgram> wpRegistry = Registries.get(ServerRegistries.WORLD_PROGRAMS);
        wpRegistry.removeValue(NamedValueUtils.getByName(wpRegistry, "_testProgram"));
    }

    @Rule
    public JUnitRuleMockery                      context = new JUnitRuleMockery();

    private WorldProcessLaunchCommandHandlerMock handler;

    @Mock
    private Bridge                               bridge;
    private Computer                             sbpComputer;
    private ProcModule                           procModule;
    private Process<?>                           sessionProcess;
    private ContentFile                          sourceFile;

    @Before
    public void setUp() {

        sbpComputer = new Computer();
        procModule = new ProcModule();
        sessionProcess = new RootProcess();

        sourceFile = new ContentFile();
        Program program = new Program();
        program.setObj(Program.NAME, "_testProgram");
        sourceFile.setObj(ContentFile.CONTENT, program);

        handler = new WorldProcessLaunchCommandHandlerMock(bridge, sbpComputer, procModule, sessionProcess, sourceFile);
    }

    @Test
    public void test() {

        launchAndAssert(1, 0);
        launchAndAssert(2, 1);
        launchAndAssert(3, 2);
        launchAndAssert(4, 3);
        launchAndAssert(5, 4);

        procModule.setObj(ProcModule.NEXT_PID_VALUE, 10);
        launchAndAssert(6, 10);
        launchAndAssert(7, 11);
        launchAndAssert(8, 12);
        launchAndAssert(9, 13);
        launchAndAssert(10, 14);
    }

    private void launchAndAssert(int run, final int expectedPid) {

        handler.currentLaunchedProcessId = expectedPid;

        final SBPWorldProcessUserDetails wpuDetails = new UUIDSBPWorldProcessUserDetails(UUID.randomUUID());

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(bridge).send(new WorldProcessLaunchAcknowledgmentEvent(new SBPWorldProcessUserId(SBP, wpuDetails), new WorldProcessId(sbpComputer.getUUID(), expectedPid)));

        }});
        // @formatter:on

        WorldProcessLaunchCommand event = new WorldProcessLaunchCommand(wpuDetails, "testPath");
        handler.handle(event, SBP);

        assertTrue("Child process was not created", sessionProcess.getColl(Process.CHILDREN).size() == run);

        Process<?> child = sessionProcess.getColl(Process.CHILDREN).get(run - 1);
        assertNotNull("Null process was created", child);
        assertEquals("Pid of child process", expectedPid, (int) child.getObj(Process.PID));
        assertEquals("World process user id of child process", new SBPWorldProcessUserId(SBP, wpuDetails), child.getObj(Process.WORLD_PROCESS_USER));

        ProgramExecutor executor = child.getObj(Process.EXECUTOR);
        assertNotNull("Child process has no program executor", executor);
        assertTrue("Child process has an incorrect program executor (wrong type)", executor instanceof TestProgram);
    }

    @RequiredArgsConstructor
    private static class WorldProcessLaunchCommandHandlerMock extends WorldProcessLaunchCommandHandler {

        private final Bridge      bridge;
        private final Computer    sbpComputer;
        private final ProcModule  procModule;
        private final Process<?>  sessionProcess;
        private final ContentFile sourceFile;

        private int               currentLaunchedProcessId;

        @Override
        protected Bridge getBridge() {

            return bridge;
        }

        @Override
        protected Computer getSBPComputer(SBPIdentity sbp) {

            return sbpComputer;
        }

        @Override
        protected ProcModule getProcModule(Computer computer) {

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

            return new WorldProcessId(sbpComputer.getUUID(), currentLaunchedProcessId);
        }

    }

    // Must be protected in order to make newInstance() work
    protected static class TestProgram extends ProgramExecutor {

        public TestProgram() {

        }

    }

}
