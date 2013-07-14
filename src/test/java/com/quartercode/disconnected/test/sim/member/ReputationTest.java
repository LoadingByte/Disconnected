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

package com.quartercode.disconnected.test.sim.member;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.sim.member.Reputation;

public class ReputationTest {

    private Reputation reputation;

    @Before
    public void setUp() {

        reputation = new Reputation();
        reputation.setValue(10);
    }

    @Test
    public void testGetValue() {

        Assert.assertEquals("Reputation value", 10, reputation.getValue());
    }

    @Test
    public void testSetValue() {

        reputation.setValue(50);
        Assert.assertEquals("Reputation value", 50, reputation.getValue());
    }

    @Test
    public void testAddValue() {

        reputation.addValue(60);
        Assert.assertEquals("Reputation value", 70, reputation.getValue());
    }

    @Test
    public void testRemoveValue() {

        reputation.removeValue(60);
        Assert.assertEquals("Reputation value", -50, reputation.getValue());
    }

}
