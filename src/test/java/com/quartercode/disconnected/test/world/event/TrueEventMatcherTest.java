
package com.quartercode.disconnected.test.world.event;

import org.junit.Assert;
import org.junit.Test;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.disconnected.world.event.Event;
import com.quartercode.disconnected.world.event.TrueEventMatcher;

public class TrueEventMatcherTest {

    @Test
    public void testMatches() throws ExecutorInvocationException {

        TrueEventMatcher matcher = new TrueEventMatcher();
        Assert.assertEquals("True event matcher's result", true, matcher.matches(new TestEvent()));
        Assert.assertEquals("True event matcher's result", true, matcher.matches(null));
    }

    private static class TestEvent extends Event {

    }

}
