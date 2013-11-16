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

package com.quartercode.disconnected.test.sim.comp;

import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.net.SpeedUnit;

public class ByteSpeedUnitTest {

    @Test
    public void testByteConvert() {

        Assert.assertEquals("10 b to 10 b", 10, ByteUnit.BYTE.convert(10, ByteUnit.BYTE));
        Assert.assertEquals("10 kb to 10240 b", 10240, ByteUnit.BYTE.convert(10, ByteUnit.KILOBYTE));
        Assert.assertEquals("10240 b to 10 kb", 10, ByteUnit.KILOBYTE.convert(10240, ByteUnit.BYTE));

        Assert.assertEquals("10 tb to 10485760 mb", 10485760, ByteUnit.MEGABYTE.convert(10, ByteUnit.TERABYTE));
        Assert.assertEquals("5 pb to 5629499534213120 b", 5629499534213120L, ByteUnit.BYTE.convert(5, ByteUnit.PETABYTE));
    }

    @Test
    public void testSpeedConvert() {

        Assert.assertEquals("10 b to 10 b", 10, SpeedUnit.BYTE.convert(10, SpeedUnit.BYTE));
        Assert.assertEquals("10 kb to 10000 b", 10000, SpeedUnit.BYTE.convert(10, SpeedUnit.KILOBYTE));
        Assert.assertEquals("10000 b to 10 kb", 10, SpeedUnit.KILOBYTE.convert(10000, SpeedUnit.BYTE));

        Assert.assertEquals("10 tb to 10000000 mb", 10000000, SpeedUnit.MEGABYTE.convert(10, SpeedUnit.TERABYTE));
        Assert.assertEquals("5 pb to 5000000000000000 b", 5000000000000000L, SpeedUnit.BYTE.convert(5, SpeedUnit.PETABYTE));
    }

}
