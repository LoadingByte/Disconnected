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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileUtils;
import com.quartercode.disconnected.server.world.comp.os.Group;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.shared.comp.file.FileRights;

public class FileUtilsTest {

    private User createUser(String name) {

        User user = new User();
        user.setObj(User.NAME, name);
        return user;
    }

    private Group createGroup(String name) {

        Group group = new Group();
        group.setObj(Group.NAME, name);
        return group;
    }

    @Test
    public void testHasRight() {

        ContentFile file = new ContentFile();
        User owner = createUser("owner");
        User groupuser = createUser("groupusers");

        Group group = createGroup("group");
        owner.addCol(User.GROUPS, group);
        groupuser.addCol(User.GROUPS, group);

        file.setObj(File.OWNER, owner);
        file.setObj(File.GROUP, group);

        file.setObj(File.RIGHTS, new FileRights());
        assertTrue("Owner has read right although it is not set for owner", !FileUtils.hasRight(owner, file, FileRights.READ));

        file.setObj(File.RIGHTS, new FileRights("u:r"));
        assertTrue("Owner hasn't read right although it is set for owner", FileUtils.hasRight(owner, file, FileRights.READ));
        assertTrue("Group user has read right although it is not set for group", !FileUtils.hasRight(groupuser, file, FileRights.READ));

        file.setObj(File.RIGHTS, new FileRights("g:r,u:w"));
        assertTrue("Owner hasn't write right although it is set for owner", FileUtils.hasRight(owner, file, FileRights.WRITE));
        assertTrue("Owner hasn't read right although it is set for group", FileUtils.hasRight(owner, file, FileRights.READ));
        assertTrue("Group user has write right although it is not set for group", !FileUtils.hasRight(groupuser, file, FileRights.WRITE));
        assertTrue("Group user hasn't read right although it is set for group", FileUtils.hasRight(groupuser, file, FileRights.READ));
    }

    @Test
    public void testCanChangeRights() {

        ContentFile file = new ContentFile();
        User owner = createUser("owner");
        User other = createUser("other");
        file.setObj(File.OWNER, owner);

        assertTrue("Owner can't change rights", FileUtils.canChangeRights(owner, file));
        assertFalse("Other user can change rights", FileUtils.canChangeRights(other, file));
    }

}
