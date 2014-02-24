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
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;

public class FileRightsTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testGet() throws FunctionExecutionException {

        FileRights rights = new FileRights();
        rights.get(FileRights.FROM_STRING).invoke("-w------r---");

        Assert.assertTrue("Owner write right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OWNER, FileRight.WRITE));
        Assert.assertFalse("Group delete right is set", rights.get(FileRights.GET).invoke(FileAccessor.GROUP, FileRight.DELETE));
        Assert.assertTrue("Others read right isn't set", rights.get(FileRights.GET).invoke(FileAccessor.OTHERS, FileRight.READ));
    }

    @Test
    public void testSet() throws FunctionExecutionException {

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
    public void testFileUtilsHasRight() throws FunctionExecutionException {

    }

}
