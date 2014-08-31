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

import static org.junit.Assert.*;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.os.Group;
import com.quartercode.disconnected.world.comp.os.User;

public class FileUtilsTest {

    @Test
    public void testResolvePath() {

        assertEquals("Resolved path of relative one", "/user/homes/test2/docs", FileUtils.resolvePath("/user/homes/test/", "../test2/docs/"));
        assertEquals("Resolved path of relative one", "/system/bin/kernel", FileUtils.resolvePath("/user/homes/test/", "../../../system/bin/kernel"));
        assertEquals("Resolved path of relative one", "/", FileUtils.resolvePath("/user/homes/test/", "../../../"));

        assertEquals("Resolved path of absolute one", "/system/bin/kernel", FileUtils.resolvePath("/user/homes/test/", "/system/bin/kernel"));
        assertEquals("Resolved path of absolute one", "/system/bin/kernel", FileUtils.resolvePath("/", "/system/bin/kernel/"));
        assertEquals("Resolved path of absolute one", "/system/bin/kernel", FileUtils.resolvePath("/", "/system//bin/kernel"));
        assertEquals("Resolved path of absolute one", "/system/bin/kernel", FileUtils.resolvePath("/", "/system//bin/kernel/"));
    }

    @Test
    public void testGetComponents() {

        assertArrayEquals("Resolved components", new String[] { "system", "etc/test" }, FileUtils.getComponents("/system/etc/test"));
        assertArrayEquals("Resolved components", new String[] { "system", "etc/test/" }, FileUtils.getComponents("/system/etc/test/"));
        assertArrayEquals("Resolved components", new String[] { "system", null }, FileUtils.getComponents("/system"));
        assertArrayEquals("Resolved components", new String[] { "system", null }, FileUtils.getComponents("/system/"));
        assertArrayEquals("Resolved components", new String[] { null, "etc/test" }, FileUtils.getComponents("etc/test"));
        assertArrayEquals("Resolved components", new String[] { null, "etc/test/" }, FileUtils.getComponents("etc/test/"));
    }

    private User createUser(String name) {

        User user = new User();
        user.get(User.NAME).set(name);
        return user;
    }

    private Group createGroup(String name) {

        Group group = new Group();
        group.get(Group.NAME).set(name);
        return group;
    }

    @Test
    public void testHasRight() {

        ContentFile file = new ContentFile();
        User owner = createUser("owner");
        User groupuser = createUser("groupusers");

        Group group = createGroup("group");
        owner.get(User.GROUPS).add(group);
        groupuser.get(User.GROUPS).add(group);

        file.get(File.OWNER).set(owner);
        file.get(File.GROUP).set(group);

        FileRights rights = file.get(File.RIGHTS).get();

        rights.get(FileRights.FROM_STRING).invoke("------------");
        assertTrue("Owner has read right although it is not set for owner", !FileUtils.hasRight(owner, file, FileRight.READ));

        rights.get(FileRights.FROM_STRING).invoke("r-----------");
        assertTrue("Owner hasn't read right although it is set for owner", FileUtils.hasRight(owner, file, FileRight.READ));

        rights.get(FileRights.FROM_STRING).invoke("r-----------");
        assertTrue("Group user has read right although it is not set for group", !FileUtils.hasRight(groupuser, file, FileRight.READ));

        rights.get(FileRights.FROM_STRING).invoke("-w--r-------");
        assertTrue("Owner hasn't write right although it is set for owner", FileUtils.hasRight(owner, file, FileRight.WRITE));
        assertTrue("Owner hasn't read right although it is set for group", FileUtils.hasRight(owner, file, FileRight.READ));
        assertTrue("Group user has write right although it is not set for group", !FileUtils.hasRight(groupuser, file, FileRight.WRITE));
        assertTrue("Group user hasn't read right although it is set for group", FileUtils.hasRight(groupuser, file, FileRight.READ));
    }

    @Test
    public void testCanChangeRights() {

        ContentFile file = new ContentFile();
        User owner = createUser("owner");
        User other = createUser("other");
        file.get(File.OWNER).set(owner);

        assertTrue("Owner can't change rights", FileUtils.canChangeRights(owner, file));
        assertFalse("Other user can change rights", FileUtils.canChangeRights(other, file));
    }

}
