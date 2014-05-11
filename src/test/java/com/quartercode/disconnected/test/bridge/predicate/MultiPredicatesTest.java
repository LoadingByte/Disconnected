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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.EventPredicate;
import com.quartercode.disconnected.bridge.predicate.MultiPredicates;
import com.quartercode.disconnected.bridge.predicate.TypePredicate;

@RunWith (Parameterized.class)
public class MultiPredicatesTest<T extends Event> {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        EventPredicate<?>[] predicates = new EventPredicate<?>[] { new TypePredicate<>(Event2.class), new TypePredicate<>(Event3Base.class) };
        data.add(new Object[] { predicates, new Event1(), false, false });
        data.add(new Object[] { predicates, new Event2(), true, false });
        data.add(new Object[] { predicates, new Event3(), true, true });

        return data;
    }

    private final EventPredicate<? super T>[] predicates;
    private final T                           event;
    private final boolean                     orResult;
    private final boolean                     andResult;

    public MultiPredicatesTest(EventPredicate<? super T>[] predicates, T event, boolean orResult, boolean andResult) {

        this.predicates = predicates;
        this.event = event;
        this.orResult = orResult;
        this.andResult = andResult;
    }

    @Test
    public void testOr() {

        EventPredicate<T> predicate = MultiPredicates.or(predicates);
        Assert.assertEquals("Result for or-linked predicates " + Arrays.toString(predicates) + " with event " + event, orResult, predicate.test(event));
    }

    @Test
    public void testAnd() {

        EventPredicate<T> predicate = MultiPredicates.and(predicates);
        Assert.assertEquals("Result for and-linked predicates " + Arrays.toString(predicates) + " with event " + event, andResult, predicate.test(event));
    }

    @SuppressWarnings ("serial")
    private static class Event1 implements Event {}

    @SuppressWarnings ("serial")
    private static class Event2 implements Event {

    }

    private static interface Event3Base extends Event {

    }

    @SuppressWarnings ("serial")
    private static class Event3 extends Event2 implements Event3Base {

    }

}
