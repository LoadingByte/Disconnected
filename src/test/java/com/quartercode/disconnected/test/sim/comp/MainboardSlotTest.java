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
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.sim.comp.Mainboard.MainboradSlot;
import com.quartercode.disconnected.sim.comp.hardware.CPU;
import com.quartercode.disconnected.sim.comp.hardware.RAM;

public class MainboardSlotTest {

    private MainboradSlot mainboradSlot;
    private CPU           content;

    @Before
    public void setUp() {

        mainboradSlot = new MainboradSlot(CPU.class);
        content = new CPU(null, null, 0, 0);
        mainboradSlot.setContent(content);
    }

    @Test
    public void testGetType() {

        Assert.assertEquals("Type class", CPU.class, mainboradSlot.getType());
    }

    @Test
    public void testAccept() {

        Assert.assertTrue("Accept CPU", mainboradSlot.accept(new CPU(null, null, 0, 0)));
    }

    @Test
    public void testNotAccept() {

        Assert.assertFalse("Accept RAM (invalid)", mainboradSlot.accept(new RAM(null, null, 0, 0)));
    }

    @Test
    public void testGetContent() {

        Assert.assertEquals("Content equals", content, mainboradSlot.getContent());
    }

    @Test
    public void testSetContent() {

        CPU newContent = new CPU(null, null, 0, 0);
        mainboradSlot.setContent(newContent);
        Assert.assertEquals("Content equals", newContent, mainboradSlot.getContent());
    }

}
