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

package com.quartercode.disconnected.server.test.world.comp.file;

import static org.junit.Assert.*;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.FileRights;
import com.quartercode.disconnected.server.world.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.server.world.comp.file.FileRights.FileRight;

public class FileRightsTest {

    @Test
    public void testGet() {

        FileRights rights = new FileRights();
        rights.get(FileRights.FROM_STRING).invoke("-w------r---");

        assertTrue("Owner write right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.WRITE));
        assertFalse("Group delete right is set", rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.DELETE));
        assertTrue("Others read right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.READ));
    }

    @Test
    public void testSet() {

        FileRights rights = new FileRights();

        for (FileAccessor accessor : FileAccessor.values()) {
            for (FileRight right : FileRight.values()) {
                rights.get(FileRights.SET).invoke(accessor, right, true);
                assertTrue(accessor + " " + right + " right is false after setting it to true", rights.get(FileRights.GET).invoke(accessor, right));

                rights.get(FileRights.SET).invoke(accessor, right, false);
                assertFalse(accessor + " " + right + " right is true after setting it to false", rights.get(FileRights.GET).invoke(accessor, right));
            }
        }
    }

    @Test
    public void testSetAll() {

        FileRights rights = new FileRights();

        for (FileRight right : FileRight.values()) {
            rights.get(FileRights.SET).invoke(null, right, true);
            for (FileAccessor accessor : FileAccessor.values()) {
                assertTrue(accessor + " " + right + " right is false after setting it to true", rights.get(FileRights.GET).invoke(accessor, right));
            }

            rights.get(FileRights.SET).invoke(null, right, false);
            for (FileAccessor accessor : FileAccessor.values()) {
                assertFalse(accessor + " " + right + " right is true after setting it to false", rights.get(FileRights.GET).invoke(accessor, right));
            }
        }
    }

    @Test
    public void testFromString() {

        FileRights rights = new FileRights();
        rights.get(FileRights.FROM_STRING).invoke("rw-x--d-r--x");

        assertTrue("Owner read right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.READ));
        assertTrue("Owner write right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.WRITE));
        assertTrue("Owner delete right is set", !rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.DELETE));
        assertTrue("Owner execute right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.EXECUTE));

        assertTrue("Group read right is set", !rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.READ));
        assertTrue("Group write right is set", !rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.WRITE));
        assertTrue("Group delete right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.DELETE));
        assertTrue("Group execute right is set", !rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.EXECUTE));

        assertTrue("Others read right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.READ));
        assertTrue("Others write right is set", !rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.WRITE));
        assertTrue("Others delete right is set", !rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.DELETE));
        assertTrue("Others execute right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.EXECUTE));
    }

    @Test
    public void testToString() {

        FileRights rights = new FileRights();

        rights.get(FileRights.SET).invoke(FileAccessor.OWNER, FileRight.READ, true);
        rights.get(FileRights.SET).invoke(FileAccessor.OWNER, FileRight.WRITE, true);
        rights.get(FileRights.SET).invoke(FileAccessor.OWNER, FileRight.DELETE, false);
        rights.get(FileRights.SET).invoke(FileAccessor.OWNER, FileRight.EXECUTE, true);

        rights.get(FileRights.SET).invoke(FileAccessor.GROUP, FileRight.READ, false);
        rights.get(FileRights.SET).invoke(FileAccessor.GROUP, FileRight.WRITE, false);
        rights.get(FileRights.SET).invoke(FileAccessor.GROUP, FileRight.DELETE, true);
        rights.get(FileRights.SET).invoke(FileAccessor.GROUP, FileRight.EXECUTE, false);

        rights.get(FileRights.SET).invoke(FileAccessor.OTHERS, FileRight.READ, true);
        rights.get(FileRights.SET).invoke(FileAccessor.OTHERS, FileRight.WRITE, false);
        rights.get(FileRights.SET).invoke(FileAccessor.OTHERS, FileRight.DELETE, false);
        rights.get(FileRights.SET).invoke(FileAccessor.OTHERS, FileRight.EXECUTE, true);

        assertEquals("Generated file right string", "rw-x--d-r--x", rights.get(FileRights.TO_STRING).invoke());
    }

}
