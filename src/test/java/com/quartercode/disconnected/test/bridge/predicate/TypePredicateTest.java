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

package com.quartercode.disconnected.test.bridge.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.predicate.TypePredicate;

@RunWith (Parameterized.class)
public class TypePredicateTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { new TestEvent1(), TestEvent1.class, true });
        data.add(new Object[] { new TestEvent1(), TestEvent2.class, false });
        data.add(new Object[] { new TestEvent1(), TestEvent3.class, false });

        data.add(new Object[] { new TestEvent2(), TestEvent1.class, false });
        data.add(new Object[] { new TestEvent2(), TestEvent2.class, true });
        data.add(new Object[] { new TestEvent2(), TestEvent3.class, false });

        data.add(new Object[] { new TestEvent3(), TestEvent1.class, false });
        data.add(new Object[] { new TestEvent3(), TestEvent2.class, true });
        data.add(new Object[] { new TestEvent3(), TestEvent3.class, true });

        return data;
    }

    private final Event                  event;
    private final Class<? extends Event> type;
    private final boolean                result;

    public TypePredicateTest(Event event, Class<? extends Event> type, boolean result) {

        this.event = event;
        this.type = type;
        this.result = result;
    }

    @Test
    public void testMatches() {

        TypePredicate<Event> predicate = new TypePredicate<>(type);
        Assert.assertEquals("Result for event of type " + event.getClass().getName() + " and matching type " + type.getName(), result, predicate.test(event));
    }

    @SuppressWarnings ("serial")
    private static class TestEvent1 implements Event {

    }

    @SuppressWarnings ("serial")
    private static class TestEvent2 implements Event {

    }

    @SuppressWarnings ("serial")
    private static class TestEvent3 extends TestEvent2 {

    }

}
