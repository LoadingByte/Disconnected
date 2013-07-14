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
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.MemberGroup;

public class MemberTest {

    private Member      member;
    private MemberGroup group;

    @Before
    public void setUp() throws Exception {

        member = new Member("member");
        group = new MemberGroup();
        member.getReputation(group).setValue(10);
        member.addInterest(new EmptyInterest(1));
        member.addInterest(new EmptyInterest(1));
    }

    @Test
    public void testGetName() {

        Assert.assertEquals("member", member.getName());
    }

    @Test
    public void testGetReputation() {

        Assert.assertEquals(10, member.getReputation(group).getValue());
    }

    @Test
    public void testGetInterests() {

        Assert.assertEquals("Interest count", 2, member.getInterests().size());
    }

    @Test
    public void testAddInterest() {

        member.addInterest(new EmptyInterest(1));
        Assert.assertEquals("Interest count", 3, member.getInterests().size());
    }

    @Test
    public void testRemoveInterest() {

        member.removeInterest(member.getInterests().get(0));
        Assert.assertEquals("Member count", 1, member.getInterests().size());
    }

}
