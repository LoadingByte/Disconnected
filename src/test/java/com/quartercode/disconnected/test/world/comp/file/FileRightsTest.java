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

package com.quartercode.disconnected.test.world.comp.file;

import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;

public class FileRightsTest {

    @Test
    public void testGet() {

        FileRights rights = new FileRights();
        rights.get(FileRights.FROM_STRING).invoke("-w------r---");

        Assert.assertTrue("Owner write right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.WRITE));
        Assert.assertFalse("Group delete right is set", rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.DELETE));
        Assert.assertTrue("Others read right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.READ));
    }

    @Test
    public void testSet() {

        FileRights rights = new FileRights();
        rights.get(FileRights.FROM_STRING).invoke("------------");

        rights.get(FileRights.SET).invoke(FileAccessor.OWNER, FileRight.EXECUTE, true);
        Assert.assertTrue("Owner execute right can't get set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.EXECUTE));

        rights.get(FileRights.SET).invoke(FileAccessor.GROUP, FileRight.WRITE, true);
        Assert.assertTrue("Group write right can't get set", rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.WRITE));

        rights.get(FileRights.SET).invoke(FileAccessor.OTHERS, FileRight.READ, false);
        Assert.assertFalse("Others write can't get set", rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.READ));
    }

    @Test
    public void testFromString() {

        FileRights rights = new FileRights();
        rights.get(FileRights.FROM_STRING).invoke("rw-x--d-r--x");

        Assert.assertTrue("Owner read right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.READ));
        Assert.assertTrue("Owner write right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.WRITE));
        Assert.assertTrue("Owner delete right is set", !rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.DELETE));
        Assert.assertTrue("Owner execute right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.EXECUTE));

        Assert.assertTrue("Group read right is set", !rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.READ));
        Assert.assertTrue("Group write right is set", !rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.WRITE));
        Assert.assertTrue("Group delete right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.DELETE));
        Assert.assertTrue("Group execute right is set", !rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.EXECUTE));

        Assert.assertTrue("Others read right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.READ));
        Assert.assertTrue("Others write right is set", !rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.WRITE));
        Assert.assertTrue("Others delete right is set", !rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.DELETE));
        Assert.assertTrue("Others execute right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.EXECUTE));
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

        Assert.assertEquals("Generated file right string", "rw-x--d-r--x", rights.get(FileRights.TO_STRING).invoke());
    }

}
