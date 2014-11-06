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

package com.quartercode.disconnected.shared.test.comp.file;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.TreeSet;
import org.junit.Test;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

public class FileRightsTest {

    @Test
    public void testSetAndGet() {

        FileRights rights = new FileRights();

        // Set: "o:r,u:dw"
        rights.setRight(FileRights.OWNER, FileRights.WRITE, true);
        rights.setRight(FileRights.OWNER, FileRights.DELETE, true);
        rights.setRight(FileRights.OTHERS, FileRights.READ, true);

        assertTrue("Owner write right isn't set", rights.isRightSet(FileRights.OWNER, FileRights.WRITE));
        assertTrue("Owner delete right isn't set", rights.isRightSet(FileRights.OWNER, FileRights.DELETE));
        assertFalse("Group delete right is set", rights.isRightSet(FileRights.GROUP, FileRights.DELETE));
        assertTrue("Others read right isn't set", rights.isRightSet(FileRights.OTHERS, FileRights.READ));
    }

    @Test
    public void testSetAndGetAll() {

        FileRights rights = new FileRights();

        // Set: "o:r,u:dw"
        rights.setRight(FileRights.OWNER, FileRights.WRITE, true);
        rights.setRight(FileRights.OWNER, FileRights.DELETE, true);
        rights.setRight(FileRights.OTHERS, FileRights.READ, true);

        assertEquals("Owner rights", new TreeSet<>(Arrays.asList(FileRights.WRITE, FileRights.DELETE)), rights.getAllSetRights(FileRights.OWNER));
        assertEquals("Group rights", new TreeSet<>(), rights.getAllSetRights(FileRights.GROUP));
        assertEquals("Others rights", new TreeSet<>(Arrays.asList(FileRights.READ)), rights.getAllSetRights(FileRights.OTHERS));
    }

    @Test
    public void testUnsetAndGet() {

        FileRights rights = new FileRights();

        // Set: "o:r,u:w" (note that owner->delete is unset again)
        rights.setRight(FileRights.OWNER, FileRights.WRITE, true);
        rights.setRight(FileRights.OWNER, FileRights.DELETE, true);
        rights.setRight(FileRights.OTHERS, FileRights.READ, true);
        rights.setRight(FileRights.OWNER, FileRights.DELETE, false);

        assertTrue("Owner write right isn't set", rights.isRightSet(FileRights.OWNER, FileRights.WRITE));
        assertFalse("Owner delete right is set", rights.isRightSet(FileRights.OWNER, FileRights.DELETE));
        assertFalse("Group delete right is set", rights.isRightSet(FileRights.GROUP, FileRights.DELETE));
        assertTrue("Others read right isn't set", rights.isRightSet(FileRights.OTHERS, FileRights.READ));
    }

    @Test
    public void testFromString() {

        FileRights rights = new FileRights("g:d,o:rx,u:rwx");

        assertTrue("Owner read right isn't set", rights.isRightSet(FileRights.OWNER, FileRights.READ));
        assertTrue("Owner write right isn't set", rights.isRightSet(FileRights.OWNER, FileRights.WRITE));
        assertFalse("Owner delete right is set", rights.isRightSet(FileRights.OWNER, FileRights.DELETE));
        assertTrue("Owner execute right isn't set", rights.isRightSet(FileRights.OWNER, FileRights.EXECUTE));

        assertFalse("Group read right is set", rights.isRightSet(FileRights.GROUP, FileRights.READ));
        assertFalse("Group write right is set", rights.isRightSet(FileRights.GROUP, FileRights.WRITE));
        assertTrue("Group delete right isn't set", rights.isRightSet(FileRights.GROUP, FileRights.DELETE));
        assertFalse("Group execute right is set", rights.isRightSet(FileRights.GROUP, FileRights.EXECUTE));

        assertTrue("Others read right isn't set", rights.isRightSet(FileRights.OTHERS, FileRights.READ));
        assertFalse("Others write right is set", rights.isRightSet(FileRights.OTHERS, FileRights.WRITE));
        assertFalse("Others delete right is set", rights.isRightSet(FileRights.OTHERS, FileRights.DELETE));
        assertTrue("Others execute right isn't set", rights.isRightSet(FileRights.OTHERS, FileRights.EXECUTE));
    }

    @Test
    public void testToString() {

        FileRights rights = new FileRights();

        rights.setRight(FileRights.OWNER, FileRights.READ, true);
        rights.setRight(FileRights.OWNER, FileRights.WRITE, true);
        rights.setRight(FileRights.OWNER, FileRights.DELETE, false);
        rights.setRight(FileRights.OWNER, FileRights.EXECUTE, true);

        rights.setRight(FileRights.GROUP, FileRights.READ, false);
        rights.setRight(FileRights.GROUP, FileRights.WRITE, false);
        rights.setRight(FileRights.GROUP, FileRights.DELETE, true);
        rights.setRight(FileRights.GROUP, FileRights.EXECUTE, false);

        rights.setRight(FileRights.OTHERS, FileRights.READ, true);
        rights.setRight(FileRights.OTHERS, FileRights.WRITE, false);
        rights.setRight(FileRights.OTHERS, FileRights.DELETE, false);
        rights.setRight(FileRights.OTHERS, FileRights.EXECUTE, true);

        assertEquals("Generated file right string", "g:d,o:rx,u:rwx", rights.toString());
    }

}
