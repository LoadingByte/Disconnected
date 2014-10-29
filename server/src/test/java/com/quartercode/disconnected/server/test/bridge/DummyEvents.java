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

package com.quartercode.disconnected.server.test.bridge;

import com.quartercode.disconnected.shared.event.LimitedSBPEvent;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.eventbridge.basic.EventBase;
import com.quartercode.eventbridge.bridge.Event;

public class DummyEvents {

    @SuppressWarnings ("serial")
    public static class Event1 implements Event {

    }

    @SuppressWarnings ("serial")
    public static class Event2 implements Event {

    }

    @SuppressWarnings ("serial")
    public static class CallableEvent implements Event {

        public void call() {

        }

    }

    @SuppressWarnings ("serial")
    public static class TestLimitedSBPEvent extends EventBase implements LimitedSBPEvent {

        private final SBPIdentity[] sbps;

        public TestLimitedSBPEvent(SBPIdentity... sbps) {

            this.sbps = sbps;
        }

        @Override
        public SBPIdentity[] getSBPs() {

            return sbps;
        }

    }

    private DummyEvents() {

    }

}
