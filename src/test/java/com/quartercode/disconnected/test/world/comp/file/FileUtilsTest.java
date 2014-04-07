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
import com.quartercode.classmod.extra.ExecutorInvocationException;
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
        Assert.assertEquals("Resolved path of absolute one", "/system/bin/kernel", FileUtils.resolvePath("/", "/system/bin/kernel/"));
        Assert.assertEquals("Resolved path of absolute one", "/system/bin/kernel", FileUtils.resolvePath("/", "/system//bin/kernel"));
        Assert.assertEquals("Resolved path of absolute one", "/system/bin/kernel", FileUtils.resolvePath("/", "/system//bin/kernel/"));
    }

    @Test
    public void testGetComponents() {

        Assert.assertArrayEquals("Resolved components", new String[] { "system", "etc/test" }, FileUtils.getComponents("/system/etc/test"));
        Assert.assertArrayEquals("Resolved components", new String[] { "system", "etc/test/" }, FileUtils.getComponents("/system/etc/test/"));
        Assert.assertArrayEquals("Resolved components", new String[] { "system", null }, FileUtils.getComponents("/system"));
        Assert.assertArrayEquals("Resolved components", new String[] { "system", null }, FileUtils.getComponents("/system/"));
        Assert.assertArrayEquals("Resolved components", new String[] { null, "etc/test" }, FileUtils.getComponents("etc/test"));
        Assert.assertArrayEquals("Resolved components", new String[] { null, "etc/test/" }, FileUtils.getComponents("etc/test/"));
    }

    private User createUser(String name) throws ExecutorInvocationException {

        User user = new User();
        user.get(User.NAME).set(name);
        return user;
    }

    @Test
    public void testHasRight() throws ExecutorInvocationException {

        ContentFile file = new ContentFile();
        User owner = createUser("owner");
        file.get(File.OWNER).set(owner);

        file.get(File.RIGHTS).get().get(FileRights.SET).invoke(FileAccessor.OWNER, FileRight.READ, true);
        Assert.assertTrue("Owner hasn't read right", FileUtils.hasRight(owner, file, FileRight.READ));

        file.get(File.RIGHTS).get().get(FileRights.SET).invoke(FileAccessor.OWNER, FileRight.READ, false);
        Assert.assertTrue("Owner has read right", FileUtils.hasRight(owner, file, FileRight.READ));
    }

    @Test
    public void testCanChangeRights() throws ExecutorInvocationException {

        ContentFile file = new ContentFile();
        User owner = createUser("owner");
        User other = createUser("other");
        file.get(File.OWNER).set(owner);

        Assert.assertTrue("Owner can't change rights", FileUtils.canChangeRights(owner, file));
        Assert.assertFalse("Other user can change rights", FileUtils.canChangeRights(other, file));
    }

}
