/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

import static com.quartercode.disconnected.server.test.world.comp.file.FileAssert.assertInvalidPath;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.world.comp.ByteUnit;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

public class FileTest {

    private FileSystem  fileSystem;
    private ContentFile testFile;

    @Before
    public void setUp() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        fileSystem = new FileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));

        testFile = new ContentFile(new User("user"));
        fileSystem.prepareAddFile(testFile, "test1/test2/test.txt").execute();
    }

    @Test
    public void testGetPath() {

        assertEquals("File path", "test1/test2/test.txt", testFile.getPath());
    }

    /*
     * This test is only here for testing whether setting the name triggers a bug.
     * In production, a FileMoveAction should be used instead of the direct name setting.
     */
    @Test
    public void testSetName() throws InvalidPathException {

        testFile.setName("test3.txt");

        assertTrue("Renamed file doesn't exist", testFile.equals(fileSystem.getFile("test1/test2/test3.txt")));
        assertInvalidPath("Removed file does exist", fileSystem, "test1/test2/test.txt");
        assertEquals("Path of renamed file", "test1/test2/test3.txt", testFile.getPath());
    }

    @Test
    public void testHasRight() {

        User owner = new User("owner");
        User groupuser = new User("groupusers");

        String group = "group";
        owner.addGroup(group);
        groupuser.addGroup(group);

        testFile.setOwner(owner);
        testFile.setGroup(group);

        testFile.getRights().clearRights();
        assertTrue("Owner has read right although it is not set for owner", !testFile.hasRight(owner, FileRights.READ));

        testFile.getRights().importRights("u:r");
        assertTrue("Owner hasn't read right although it is set for owner", testFile.hasRight(owner, FileRights.READ));
        assertTrue("Group user has read right although it is not set for group", !testFile.hasRight(groupuser, FileRights.READ));

        testFile.getRights().importRights("g:r,u:w");
        assertTrue("Owner hasn't write right although it is set for owner", testFile.hasRight(owner, FileRights.WRITE));
        assertTrue("Owner hasn't read right although it is set for group", testFile.hasRight(owner, FileRights.READ));
        assertTrue("Group user has write right although it is not set for group", !testFile.hasRight(groupuser, FileRights.WRITE));
        assertTrue("Group user hasn't read right although it is set for group", testFile.hasRight(groupuser, FileRights.READ));
    }

    @Test
    public void testCanChangeRights() {

        User owner = new User("owner");
        User other = new User("other");
        testFile.setOwner(owner);

        assertTrue("Owner can't change rights", testFile.canChangeRights(owner));
        assertFalse("Other user can change rights", testFile.canChangeRights(other));
    }

}
