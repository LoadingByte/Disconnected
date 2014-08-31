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

package com.quartercode.disconnected.test.world.event;

import static com.quartercode.disconnected.test.ExtraActions.storeArgument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.program.ProcessModule;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEvent.ProgramLaunchInfoResponseEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEventHandler;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.extra.extension.ReturnEventSender;

public class ProgramLaunchInfoRequestEventHandlerTest {

    @Rule
    public JUnitRuleMockery                          context = new JUnitRuleMockery();

    private ProgramLaunchInfoRequestEventHandlerMock handler;

    private Computer                                 playerComputer;
    private ProcessModule                            procModule;

    @Before
    public void setUp() {

        playerComputer = new Computer();
        procModule = new ProcessModule();

        handler = new ProgramLaunchInfoRequestEventHandlerMock(playerComputer, procModule);
    }

    @Test
    public void test() {

        sendAndAssert(0);
        sendAndAssert(1);
        sendAndAssert(2);
        sendAndAssert(3);
        sendAndAssert(4);

        procModule.get(ProcessModule.NEXT_PID_VALUE).set(10);
        sendAndAssert(10);
        sendAndAssert(11);
        sendAndAssert(12);
        sendAndAssert(13);
        sendAndAssert(14);
    }

    private void sendAndAssert(int expectedPid) {

        final ReturnEventSender returnEventSender = context.mock(ReturnEventSender.class, "returnEventSender" + expectedPid);
        final List<Event> returnEvents = new ArrayList<>();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(returnEventSender).send(with(any(Event.class)));
                will(storeArgument(0).in(returnEvents));

        }});
        // @formatter:on

        handler.handle(new ProgramLaunchInfoRequestEvent(), returnEventSender);

        assertEquals("Amount of events that were sent back by the handler", 1, returnEvents.size());
        Event rawReturnEvent = returnEvents.get(0);
        assertTrue("Event that was sent back by the handler is a '" + rawReturnEvent.getClass().getName() + "' (ProgramLaunchInfoResponseEvent required)", rawReturnEvent instanceof ProgramLaunchInfoResponseEvent);
        ProgramLaunchInfoResponseEvent returnEvent = (ProgramLaunchInfoResponseEvent) rawReturnEvent;
        assertEquals("Computer id that is stored in the return event", playerComputer.getId(), returnEvent.getComputerId());
        assertEquals("Pid that is stored in the return event", expectedPid, returnEvent.getPid());
    }

    private static class ProgramLaunchInfoRequestEventHandlerMock extends ProgramLaunchInfoRequestEventHandler {

        private final Computer      playerComputer;
        private final ProcessModule procModule;

        public ProgramLaunchInfoRequestEventHandlerMock(Computer playerComputer, ProcessModule procModule) {

            this.playerComputer = playerComputer;
            this.procModule = procModule;
        }

        @Override
        protected Computer getPlayerComputer() {

            return playerComputer;
        }

        @Override
        protected ProcessModule getProcessModule(Computer computer) {

            return procModule;
        }

    }

}
