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

package com.quartercode.disconnected.test.sim.comp.file;

import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.os.User;

public class FileUtilsTest {

    @Test
    public void testResolvePath() {

        Assert.assertEquals("Resolved path of relative one", "/user/homes/test2/docs", FileUtils.resolvePath("/user/homes/test/", "../test2/docs/"));
        Assert.assertEquals("Resolved path of relative one", "/system/bin/kernel", FileUtils.resolvePath("/user/homes/test/", "../../../system/bin/kernel"));
        Assert.assertEquals("Resolved path of relative one", "/", FileUtils.resolvePath("/user/homes/test/", "../../../"));
        Assert.assertEquals("Resolved path of absolute one", "/system/bin/kernel", FileUtils.resolvePath("/user/homes/test/", "/system/bin/kernel"));
    }

    @Test
    public void testHasRight() throws FunctionExecutionException {

        ContentFile file = new ContentFile();
        User owner = new User(null, "owner");
        file.get(File.SET_OWNER).invoke(owner);

        file.get(File.GET_RIGHTS).invoke().get(FileRights.SET).invoke(FileAccessor.OWNER, FileRight.READ, true);
        Assert.assertTrue("Owner hasn't read right", FileUtils.hasRight(owner, file, FileRight.READ));

        file.get(File.GET_RIGHTS).invoke().get(FileRights.SET).invoke(FileAccessor.OWNER, FileRight.READ, false);
        Assert.assertTrue("Owner has read right", FileUtils.hasRight(owner, file, FileRight.READ));
    }

    @Test
    public void testCanChangeRights() throws FunctionExecutionException {

        ContentFile file = new ContentFile();
        User owner = new User(null, "owner");
        User other = new User(null, "other");
        file.get(File.SET_OWNER).invoke(owner);

        Assert.assertTrue("Owner can't change rights", FileUtils.canChangeRights(owner, file));
        Assert.assertFalse("Other user can change rights", FileUtils.canChangeRights(other, file));
    }

}
