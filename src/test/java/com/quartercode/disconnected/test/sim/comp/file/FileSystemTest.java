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
import com.quartercode.disconnected.sim.comp.file.FileSystem;
import com.quartercode.disconnected.sim.comp.file.MountException;
import com.quartercode.disconnected.sim.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.sim.comp.hardware.HardDrive;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.sim.comp.os.User;

public class FileSystemTest {

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
        testFile.setContent("Test-Content");
    }

    @Test
    public void testGetFile() {

        Assert.assertEquals("Returned file equals original", testFile, fileSystem.getFile("test1/test2/test.txt"));
    }

    @Test
    public void testCalcSpace() {

        Assert.assertEquals("Filled bytes", 12288, fileSystem.getFilled());
        Assert.assertEquals("Free bytes", fileSystem.getSize() - 12288, fileSystem.getFree());
        Assert.assertEquals("Filled + free = size", fileSystem.getSize(), fileSystem.getFilled() + fileSystem.getFree());
    }

}
