
package com.quartercode.disconnected.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.util.NullPreventer;

public class NullPreventerTest {

    @Test
    public void testPreventNull() {

        Assert.assertEquals("Null-prevented byte", Byte.valueOf((byte) 0), NullPreventer.prevent((Byte) null));
        Assert.assertEquals("Not null-prevented byte", Byte.valueOf((byte) 10), NullPreventer.prevent((byte) 10));

        Assert.assertEquals("Null-prevented short", Short.valueOf((short) 0), NullPreventer.prevent((Short) null));
        Assert.assertEquals("Not null-prevented short", Short.valueOf((short) 10), NullPreventer.prevent((short) 10));

        Assert.assertEquals("Null-prevented integer", Integer.valueOf(0), NullPreventer.prevent((Integer) null));
        Assert.assertEquals("Not null-prevented integer", Integer.valueOf(10), NullPreventer.prevent(10));

        Assert.assertEquals("Null-prevented long", Long.valueOf(0), NullPreventer.prevent((Long) null));
        Assert.assertEquals("Not null-prevented long", Long.valueOf(10), NullPreventer.prevent(10L));

        Assert.assertEquals("Null-prevented boolean", Boolean.FALSE, NullPreventer.prevent((Boolean) null));
        Assert.assertEquals("Not null-prevented boolean", Boolean.TRUE, NullPreventer.prevent(true));

        Assert.assertEquals("Null-prevented string", "", NullPreventer.prevent((String) null));
        Assert.assertEquals("Not null-prevented string", "test", NullPreventer.prevent("test"));

        Assert.assertEquals("Null-prevented list", new ArrayList<Object>(), NullPreventer.prevent((List<?>) null));
        List<Integer> testList = Arrays.asList(21, 64, 127, 256);
        Assert.assertEquals("Not null-prevented list", testList, NullPreventer.prevent(testList));

        Assert.assertEquals("Null-prevented set", new HashSet<Object>(), NullPreventer.prevent((Set<?>) null));
        Set<Integer> testSet = new HashSet<Integer>(testList);
        Assert.assertEquals("Not null-prevented set", testSet, NullPreventer.prevent(testSet));

        Assert.assertEquals("Null-prevented queue", new LinkedList<Object>(), NullPreventer.prevent((Queue<?>) null));
        Queue<Integer> testQueue = new LinkedList<Integer>(testList);
        Assert.assertEquals("Not null-prevented queue", testQueue, NullPreventer.prevent(testQueue));

        Assert.assertEquals("Null-prevented list", new HashMap<Object, Object>(), NullPreventer.prevent((Map<?, ?>) null));
        Map<Integer, String> testMap = new HashMap<Integer, String>();
        testMap.put(21, "test21");
        testMap.put(256, "test256");
        Assert.assertEquals("Not null-prevented list", testMap, NullPreventer.prevent(testMap));
    }

}
