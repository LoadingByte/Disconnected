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

package com.quartercode.disconnected.server.test.world.event;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.Program;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.RootProcess;
import com.quartercode.disconnected.server.world.event.ProgramLaunchCommandEventHandler;
import com.quartercode.disconnected.shared.event.comp.program.ProgramLaunchCommandEvent;

public class ProgramLaunchCommandEventHandlerTest {

    private ProgramLaunchCommandEventHandler handler;

    private Computer                         playerComputer;
    private Process<?>                       sessionProcess;
    private ContentFile                      sourceFile;

    @Before
    public void setUp() {

        playerComputer = new Computer();
        sessionProcess = new RootProcess();

        sourceFile = new ContentFile();
        Program program = new Program();
        program.setObj(Program.EXECUTOR_CLASS, TestProgram.class);
        sourceFile.setObj(ContentFile.CONTENT, program);

        handler = new ProgramLaunchCommandEventHandlerMock(playerComputer, sessionProcess, sourceFile);
    }

    @Test
    public void test() {

        ProgramLaunchCommandEvent event = new ProgramLaunchCommandEvent(10, null);
        handler.handle(event);

        Process<?> child = sessionProcess.getCol(Process.CHILDREN).get(0);
        assertNotNull("Child process was not created", child);
        assertEquals("Pid of child process", 10, (int) child.getObj(Process.PID));
        ProgramExecutor executor = child.getObj(Process.EXECUTOR);
        assertNotNull("Child process has no program executor", executor);
        assertTrue("Child process has an incorrect program executor (wrong type)", executor instanceof TestProgram);
    }

    private static class ProgramLaunchCommandEventHandlerMock extends ProgramLaunchCommandEventHandler {

        private final Computer    playerComputer;
        private final Process<?>  sessionProcess;
        private final ContentFile sourceFile;

        public ProgramLaunchCommandEventHandlerMock(Computer playerComputer, Process<?> sessionProcess, ContentFile sourceFile) {

            this.playerComputer = playerComputer;
            this.sessionProcess = sessionProcess;
            this.sourceFile = sourceFile;
        }

        @Override
        protected Computer getPlayerComputer() {

            return playerComputer;
        }

        @Override
        protected Process<?> getSessionProcess(Computer computer) {

            return sessionProcess;
        }

        @Override
        protected ContentFile getSourceFile(Computer computer, String path) {

            return sourceFile;
        }

    }

    // Must be protected in order to make newInstance() work
    protected static class TestProgram extends ProgramExecutor {

        public TestProgram() {

        }

    }

}
