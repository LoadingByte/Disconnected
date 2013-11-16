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
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.File.FileType;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.MountException;
import com.quartercode.disconnected.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.world.comp.file.StringContent;
import com.quartercode.disconnected.world.comp.hardware.HardDrive;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.User;

public class FileTest {

    private FileSystem fileSystem;
    private File       testFile;

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

        testFile = fileSystem.addFile("test1/test2/test.txt", FileType.FILE, new User(null, null));
        testFile.setContent(new StringContent("Test-Content"));
    }

    @Test
    public void testGetLocalPath() {

        Assert.assertEquals("Local path is correct", "test1/test2/test.txt", testFile.getLocalPath());
    }

    @Test
    public void testMove() throws OutOfSpaceException {

        testFile.move("../../test3/test.txt");

        Assert.assertEquals("Moved file exists", testFile, fileSystem.getFile("test1/test3/test.txt"));
        Assert.assertEquals("Moved file has correct path", "test1/test3/test.txt", testFile.getLocalPath());
        Assert.assertEquals("Moved file has correct content", new StringContent("Test-Content"), testFile.getContent());
    }

    @Test
    public void testRename() {

        testFile.rename("test2.txt");

        Assert.assertEquals("Renamed file exists", testFile, fileSystem.getFile("test1/test2/test2.txt"));
        Assert.assertEquals("Renamed file has correct path", "test1/test2/test2.txt", testFile.getLocalPath());
        Assert.assertEquals("Renamed file has correct content", new StringContent("Test-Content"), testFile.getContent());
    }

    @Test
    public void testRemove() {

        testFile.remove();

        Assert.assertEquals("Removed file no longer exists", null, fileSystem.getFile("test1/test2/test1.txt"));
        Assert.assertEquals("Renamed file has null path", null, testFile.getLocalPath());
    }

    @Test
    public void testResolvePath() {

        Assert.assertEquals("Resolved path of relative one is correct", "/user/homes/test2/docs", File.resolvePath("/user/homes/test/", "../test2/docs/"));
        Assert.assertEquals("Resolved path of relative one is correct", "/system/bin/kernel", File.resolvePath("/user/homes/test/", "../../../system/bin/kernel"));
        Assert.assertEquals("Resolved path of relative one is correct", "/", File.resolvePath("/user/homes/test/", "../../../"));
        Assert.assertEquals("Resolved path of absolute one is correct", "/system/bin/kernel", File.resolvePath("/user/homes/test/", "/system/bin/kernel"));
    }

}
