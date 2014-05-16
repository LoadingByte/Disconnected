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

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.bridge.AbstractEventHandler;
import com.quartercode.disconnected.bridge.Bridge;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.test.bridge.BridgeMock;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.program.ProcessModule;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEvent.ProgramLaunchInfoResponseEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEventHandler;

public class ProgramLaunchInfoRequestEventHandlerTest {

    private BridgeMock                               bridge;
    private ProgramLaunchInfoRequestEventHandlerMock handler;

    private Computer                                 playerComputer;
    private ProcessModule                            procModule;

    @Before
    public void setUp() {

        playerComputer = new Computer();
        procModule = new ProcessModule();

        bridge = new BridgeMock();
        handler = new ProgramLaunchInfoRequestEventHandlerMock(bridge, playerComputer, procModule);
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

        final List<Event> returnEvents = new ArrayList<>();
        bridge.setHandler(new AbstractEventHandler<Event>(Event.class) {

            @Override
            public void handle(Event event) {

                returnEvents.add(event);
            }

        });

        handler.handle(new ProgramLaunchInfoRequestEvent().withNextReturnId("testid"));

        Assert.assertEquals("Amount of events that were sent back by the handler", 1, returnEvents.size());
        Event rawReturnEvent = returnEvents.get(0);
        Assert.assertTrue("Event that was sent back by the handler is a '" + rawReturnEvent.getClass().getName() + "' (ProgramLaunchInfoResponseEvent required)", rawReturnEvent instanceof ProgramLaunchInfoResponseEvent);
        ProgramLaunchInfoResponseEvent returnEvent = (ProgramLaunchInfoResponseEvent) rawReturnEvent;
        Assert.assertEquals("Return id of the return event", "testid", returnEvent.getReturnId());
        Assert.assertEquals("Computer id that is stored in the return event", playerComputer.getId(), returnEvent.getComputerId());
        Assert.assertEquals("Pid that is stored in the return event", expectedPid, returnEvent.getPid());

        bridge.setHandler(null);
    }

    private static class ProgramLaunchInfoRequestEventHandlerMock extends ProgramLaunchInfoRequestEventHandler {

        private final Computer      playerComputer;
        private final ProcessModule procModule;

        public ProgramLaunchInfoRequestEventHandlerMock(Bridge bridge, Computer playerComputer, ProcessModule procModule) {

            super(bridge);

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
