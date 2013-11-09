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
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.sim.comp.ByteUnit;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.file.FileRights;
import com.quartercode.disconnected.sim.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.sim.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.sim.comp.file.FileSystem;
import com.quartercode.disconnected.sim.comp.file.MountException;
import com.quartercode.disconnected.sim.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.sim.comp.hardware.HardDrive;
import com.quartercode.disconnected.sim.comp.os.Group;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.sim.comp.os.User;

public class FileRightsTest {

    private FileSystem fileSystem;
    private File       testFile;
    private User       testUser;
    private Group      testGroup;

    @Before
    public void setUp() throws MountException, OutOfSpaceException {

        Computer computer = new Computer();

        OperatingSystem operatingSystem = new OperatingSystem(computer, "OperatingSystem", new Version(1, 0, 0), null);
        computer.setOperatingSystem(operatingSystem);

        HardDrive hardDrive = new HardDrive(computer, "HardDrive", new Version(1, 0, 0), null, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystem = hardDrive.getFileSystem();
        computer.addHardware(hardDrive);
        operatingSystem.getFileSystemManager().setMountpoint(fileSystem, "test");
        operatingSystem.getFileSystemManager().setMounted(fileSystem, true);

        testUser = new User(operatingSystem, "testu");
        testGroup = new Group(operatingSystem, "testg");
        testUser.addToGroup(testGroup, true);
        operatingSystem.getUserManager().addUser(testUser);
        operatingSystem.getUserManager().addGroup(testGroup);

        testFile = fileSystem.addFile("test1/test2/test.txt", FileType.FILE, testUser);
        testFile.setContent("Test-Content");
    }

    @Test
    public void testGetRight() {

        FileRights rights = testFile.getRights();

        Assert.assertEquals("Owner write right is set correctly", true, rights.getRight(FileAccessor.OWNER, FileRight.WRITE));
        Assert.assertEquals("Group delete right is set correctly", false, rights.getRight(FileAccessor.GROUP, FileRight.DELETE));
        Assert.assertEquals("Others read right is set correctly", true, rights.getRight(FileAccessor.OTHERS, FileRight.READ));
    }

    @Test
    public void testSetRight() {

        FileRights rights = testFile.getRights();

        rights.setRight(FileAccessor.OWNER, FileRight.EXECUTE, true);
        Assert.assertEquals("Owner execute right can be set", true, rights.getRight(FileAccessor.OWNER, FileRight.EXECUTE));

        rights.setRight(FileAccessor.GROUP, FileRight.WRITE, true);
        Assert.assertEquals("Group write right is set correctly", true, rights.getRight(FileAccessor.GROUP, FileRight.WRITE));

        rights.setRight(FileAccessor.OTHERS, FileRight.READ, false);
        Assert.assertEquals("Others write right is set correctly", false, rights.getRight(FileAccessor.OTHERS, FileRight.READ));
    }

    @Test
    public void testHasRight() {

        testFile.setRights(new FileRights("------------"));
        testFile.getRights().setRight(FileAccessor.OWNER, FileRight.READ, true);
        Assert.assertEquals("Owner has read right", true, FileRights.hasRight(testUser, testFile, FileRight.READ));

        testFile.setRights(new FileRights("------------"));
        testFile.getRights().setRight(FileAccessor.GROUP, FileRight.READ, true);
        Assert.assertEquals("User in group has read right", true, FileRights.hasRight(testUser, testFile, FileRight.READ));
    }

}
