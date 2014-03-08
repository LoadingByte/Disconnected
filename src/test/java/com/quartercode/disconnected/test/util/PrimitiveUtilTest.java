
package com.quartercode.disconnected.test.util;

import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.util.PrimitiveUtil;

public class PrimitiveUtilTest {

    @Test
    public void testPreventNull() {

        Assert.assertEquals("Null-prevented byte", Byte.valueOf((byte) 0), PrimitiveUtil.preventNull((Byte) null));
        Assert.assertEquals("Not null-prevented byte", Byte.valueOf((byte) 10), PrimitiveUtil.preventNull((byte) 10));

        Assert.assertEquals("Null-prevented short", Short.valueOf((short) 0), PrimitiveUtil.preventNull((Short) null));
        Assert.assertEquals("Not null-prevented short", Short.valueOf((short) 10), PrimitiveUtil.preventNull((short) 10));

        Assert.assertEquals("Null-prevented integer", Integer.valueOf(0), PrimitiveUtil.preventNull((Integer) null));
        Assert.assertEquals("Not null-prevented integer", Integer.valueOf(10), PrimitiveUtil.preventNull(10));

        Assert.assertEquals("Null-prevented long", Long.valueOf(0), PrimitiveUtil.preventNull((Long) null));
        Assert.assertEquals("Not null-prevented long", Long.valueOf(10), PrimitiveUtil.preventNull(10L));

        Assert.assertEquals("Null-prevented boolean", Boolean.FALSE, PrimitiveUtil.preventNull((Boolean) null));
        Assert.assertEquals("Not null-prevented boolean", Boolean.TRUE, PrimitiveUtil.preventNull(true));

        Assert.assertEquals("Null-prevented string", "", PrimitiveUtil.preventNull((String) null));
        Assert.assertEquals("Not null-prevented string", "test", PrimitiveUtil.preventNull("test"));
    }

}
