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

package com.quartercode.disconnected.test.world.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.world.comp.ByteUnit;

@RunWith (Parameterized.class)
public class ByteUnitTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { 10, ByteUnit.BYTE, 10, ByteUnit.BYTE });
        data.add(new Object[] { 10, ByteUnit.KILOBYTE, 10240, ByteUnit.BYTE });
        data.add(new Object[] { 10240, ByteUnit.BYTE, 10, ByteUnit.KILOBYTE });

        data.add(new Object[] { 10, ByteUnit.TERABYTE, 10485760, ByteUnit.MEGABYTE });
        data.add(new Object[] { 5, ByteUnit.PETABYTE, 5629499534213120L, ByteUnit.BYTE });

        return data;
    }

    private final long     source;
    private final ByteUnit sourceUnit;
    private final long     target;
    private final ByteUnit targetUnit;

    public ByteUnitTest(long source, ByteUnit sourceUnit, long target, ByteUnit targetUnit) {

        this.source = source;
        this.sourceUnit = sourceUnit;
        this.target = target;
        this.targetUnit = targetUnit;
    }

    @Test
    public void testConvert() {

        Assert.assertEquals(source + " " + sourceUnit + " to " + targetUnit, target, targetUnit.convert(source, sourceUnit));
    }

}
