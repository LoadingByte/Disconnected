
package com.quartercode.disconnected.test.world.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.disconnected.world.event.Event;
import com.quartercode.disconnected.world.event.TypeEventMatcher;

@RunWith (Parameterized.class)
public class TypeEventMatcherTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<Object[]>();

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

    public TypeEventMatcherTest(Event event, Class<? extends Event> type, boolean result) {

        this.event = event;
        this.type = type;
        this.result = result;
    }

    @Test
    public void testMatches() throws ExecutorInvocationException {

        TypeEventMatcher matcher = new TypeEventMatcher(type);
        Assert.assertEquals("Result for event of type " + event.getClass().getName() + " and matching type " + type.getName(), result, matcher.matches(event));
    }

    private static class TestEvent1 extends Event {

    }

    private static class TestEvent2 extends Event {

    }

    private static class TestEvent3 extends TestEvent2 {

    }

}
