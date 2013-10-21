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
import org.junit.Ignore;
import org.junit.Test;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.file.FileSystem;
import com.quartercode.disconnected.sim.comp.hardware.HardDrive;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.util.size.ByteUnit;

public class OSFileTest {

    private Computer        computer;
    private OperatingSystem operatingSystem;
    private FileSystem      fileSystem;
    private File            testFile;

    @Before
    public void setUp() {

        computer = new Computer("0");

        operatingSystem = new OperatingSystem(computer, "OperatingSystem", new Version(1, 0, 0), null);
        computer.setOperatingSystem(operatingSystem);

        HardDrive hardDrive = new HardDrive(computer, "HardDrive", new Version(1, 0, 0), null, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystem = hardDrive.getFileSystem();
        computer.addHardware(hardDrive);
        operatingSystem.getFileSystemManager().mount(fileSystem, 'C');

        testFile = fileSystem.addFile("/test1/test2/test.txt", FileType.FILE);
        testFile.setContent("Test-Content");
    }

    @Test
    public void testGetFile() {

        char mountpoint = operatingSystem.getFileSystemManager().getMountpoint(fileSystem);
        Assert.assertEquals("Returned file equals original", testFile, operatingSystem.getFileSystemManager().getFile(mountpoint + ":/test1/test2/test.txt"));
    }

    @Test
    @Ignore
    public void testCreateFile() {

        char mountpoint = operatingSystem.getFileSystemManager().getMountpoint(fileSystem);
        operatingSystem.getFileSystemManager().addFile(mountpoint + ":/test1/test2/test-global.txt", FileType.FILE);
        Assert.assertNotNull("File was created using global method", fileSystem.getFile("/test1/test2/test-global.txt"));
    }

    @Test
    @Ignore
    public void testGetGlobalPath() {

        char mountpoint = operatingSystem.getFileSystemManager().getMountpoint(fileSystem);
        Assert.assertEquals("Global path is correct", mountpoint + ":/test1/test2/test.txt", testFile.getGlobalPath(operatingSystem));
    }

    @Test
    @Ignore
    public void testMove() {

        HardDrive hardDrive2 = new HardDrive(computer, "HardDrive", new Version(1, 0, 0), null, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.addHardware(hardDrive2);
        operatingSystem.getFileSystemManager().mount(hardDrive2.getFileSystem(), 'D');
        char mountpoint = operatingSystem.getFileSystemManager().getMountpoint(hardDrive2.getFileSystem());

        testFile.move(mountpoint + ":/test1/test3/test.txt");
        Assert.assertEquals("Moved file exists", testFile, hardDrive2.getFileSystem().getFile("/test1/test3/test.txt"));
        Assert.assertEquals("Moved file has correct path", mountpoint + ":/test1/test3/test.txt", testFile.getGlobalPath(operatingSystem));
        Assert.assertEquals("Moved file has correct content", "Test-Content", testFile.getContent());
    }

}
