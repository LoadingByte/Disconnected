/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.shared.test.world.comp.net;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.shared.world.comp.net.SpeedUnit;

@RunWith (Parameterized.class)
public class SpeedUnitTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { 10, SpeedUnit.BYTE, 10, SpeedUnit.BYTE });
        data.add(new Object[] { 10, SpeedUnit.KILOBYTE, 10000, SpeedUnit.BYTE });
        data.add(new Object[] { 10000, SpeedUnit.BYTE, 10, SpeedUnit.KILOBYTE });

        data.add(new Object[] { 10, SpeedUnit.TERABYTE, 10000000, SpeedUnit.MEGABYTE });
        data.add(new Object[] { 5, SpeedUnit.PETABYTE, 5000000000000000L, SpeedUnit.BYTE });

        return data;
    }

    private final long      source;
    private final SpeedUnit sourceUnit;
    private final long      target;
    private final SpeedUnit targetUnit;

    public SpeedUnitTest(long source, SpeedUnit sourceUnit, long target, SpeedUnit targetUnit) {

        this.source = source;
        this.sourceUnit = sourceUnit;
        this.target = target;
        this.targetUnit = targetUnit;
    }

    @Test
    public void testConvert() {

        assertEquals(source + " " + sourceUnit + " to " + targetUnit, target, targetUnit.convert(source, sourceUnit));
    }

}
