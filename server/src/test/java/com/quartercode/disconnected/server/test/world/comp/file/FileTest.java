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
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.user.Group;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.world.comp.ByteUnit;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

public class FileTest {

    private FileSystem  fileSystem;
    private ContentFile testFile;

    @Before
    public void setUp() {

        fileSystem = new FileSystem();
        fileSystem.setObj(FileSystem.SIZE, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));

        testFile = new ContentFile();
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, testFile, "test1/test2/test.txt").invoke(FileAddAction.EXECUTE);
    }

    @Test
    public void testGetPath() {

        assertEquals("File path", "test1/test2/test.txt", testFile.invoke(File.GET_PATH));
    }

    /*
     * This test is only here for testing whether setting the name triggers a bug.
     * In production, a FileMoveAction should be used instead of the direct name setting.
     */
    @Test
    public void testSetName() {

        testFile.setObj(File.NAME, "test3.txt");

        assertTrue("Renamed file doesn't exist", testFile.equals(fileSystem.invoke(FileSystem.GET_FILE, "test1/test2/test3.txt")));
        assertTrue("Removed file does exist", fileSystem.invoke(FileSystem.GET_FILE, "test1/test2/test.txt") == null);
        assertEquals("Path of renamed file", "test1/test2/test3.txt", testFile.invoke(File.GET_PATH));
    }

    @Test
    public void testHasRight() {

        User owner = createUser("owner");
        User groupuser = createUser("groupusers");

        Group group = createGroup("group");
        owner.addToColl(User.GROUPS, group);
        groupuser.addToColl(User.GROUPS, group);

        testFile.setObj(File.OWNER, owner);
        testFile.setObj(File.GROUP, group);

        testFile.setObj(File.RIGHTS, new FileRights());
        assertTrue("Owner has read right although it is not set for owner", !testFile.invoke(File.HAS_RIGHT, owner, FileRights.READ));

        testFile.setObj(File.RIGHTS, new FileRights("u:r"));
        assertTrue("Owner hasn't read right although it is set for owner", testFile.invoke(File.HAS_RIGHT, owner, FileRights.READ));
        assertTrue("Group user has read right although it is not set for group", !testFile.invoke(File.HAS_RIGHT, groupuser, FileRights.READ));

        testFile.setObj(File.RIGHTS, new FileRights("g:r,u:w"));
        assertTrue("Owner hasn't write right although it is set for owner", testFile.invoke(File.HAS_RIGHT, owner, FileRights.WRITE));
        assertTrue("Owner hasn't read right although it is set for group", testFile.invoke(File.HAS_RIGHT, owner, FileRights.READ));
        assertTrue("Group user has write right although it is not set for group", !testFile.invoke(File.HAS_RIGHT, groupuser, FileRights.WRITE));
        assertTrue("Group user hasn't read right although it is set for group", testFile.invoke(File.HAS_RIGHT, groupuser, FileRights.READ));
    }

    @Test
    public void testCanChangeRights() {

        User owner = createUser("owner");
        User other = createUser("other");
        testFile.setObj(File.OWNER, owner);

        assertTrue("Owner can't change rights", testFile.invoke(File.CAN_CHANGE_RIGHTS, owner));
        assertFalse("Other user can change rights", testFile.invoke(File.CAN_CHANGE_RIGHTS, other));
    }

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

}
