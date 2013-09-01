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

package com.quartercode.disconnected.test.sim.comp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.OperatingSystem;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.hardware.HardDrive;
import com.quartercode.disconnected.sim.comp.media.File;
import com.quartercode.disconnected.sim.comp.media.File.FileType;
import com.quartercode.disconnected.util.ByteUnit;

public class FileTest {

    private Computer        computer;
    private OperatingSystem operatingSystem;
    private HardDrive       hardDrive;
    private File            testFile;

    @Before
    public void setUp() {

        computer = new Computer("0");

        operatingSystem = new OperatingSystem(computer, "OperatingSystem", new Version(1, 0, 0), null, 0, 0);
        computer.setOperatingSystem(operatingSystem);

        hardDrive = new HardDrive(computer, "HardDrive", new Version(1, 0, 0), null, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        hardDrive.setLetter('C');
        computer.addHardware(hardDrive);

        testFile = hardDrive.addFile("/test1/test2/test.txt", FileType.FILE);
        testFile.setContent("Test-Content");
    }

    @Test
    public void testGetFileLocal() {

        Assert.assertEquals("Returned file equals original", testFile, hardDrive.getFile("/test1/test2/test.txt"));
    }

    @Test
    public void testGetFileGlobal() {

        Assert.assertEquals("Globally returned file equals original", testFile, operatingSystem.getFile(hardDrive.getLetter() + ":/test1/test2/test.txt"));
    }

    @Test
    public void testCreateFileGlobal() {

        operatingSystem.addFile(hardDrive.getLetter() + ":/test1/test2/test-global.txt", FileType.FILE);
        Assert.assertNotNull("File was created using global method", hardDrive.getFile("/test1/test2/test-global.txt"));
    }

    @Test
    public void testCalcSpace() {

        Assert.assertEquals("Filled bytes", 12, hardDrive.getFilled());
        Assert.assertEquals("Free bytes", hardDrive.getSize() - 12, hardDrive.getFree());
        Assert.assertEquals("Filled + free = size", hardDrive.getSize(), hardDrive.getFilled() + hardDrive.getFree());
    }

    @Test
    public void testGetLocalPath() {

        Assert.assertEquals("Local path", "/test1/test2/test.txt", testFile.getLocalPath());
    }

    @Test
    public void testGetGlobalPath() {

        Assert.assertEquals("Global path", hardDrive.getLetter() + ":/test1/test2/test.txt", testFile.getGlobalPath());
    }

    @Test
    public void testMoveLocal() {

        testFile.move("/test1/test3/test.txt");

        Assert.assertEquals("Moved file exists", testFile, hardDrive.getFile("/test1/test3/test.txt"));
        Assert.assertEquals("Moved file has correct path", "/test1/test3/test.txt", testFile.getLocalPath());
        Assert.assertEquals("Moved file has correct content", "Test-Content", testFile.getContent());
    }

    @Test
    public void testMoveGlobal() {

        HardDrive hardDriveD = new HardDrive(computer, "HardDrive", new Version(1, 0, 0), null, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        hardDriveD.setLetter('D');
        computer.addHardware(hardDriveD);

        testFile.move(hardDriveD.getLetter() + ":/test1/test3/test.txt");
        Assert.assertEquals("Moved file exists", testFile, hardDriveD.getFile("/test1/test3/test.txt"));
        Assert.assertEquals("Moved file has correct path", hardDriveD.getLetter() + ":/test1/test3/test.txt", testFile.getGlobalPath());
        Assert.assertEquals("Moved file has correct content", "Test-Content", testFile.getContent());
    }

    @Test
    public void testRename() {

        testFile.rename("test2.txt");

        Assert.assertEquals("Renamed file exists", testFile, hardDrive.getFile("/test1/test2/test2.txt"));
        Assert.assertEquals("Renamed file has correct path", "/test1/test2/test2.txt", testFile.getLocalPath());
        Assert.assertEquals("Renamed file has correct content", "Test-Content", testFile.getContent());
    }

    @Test
    public void testRemove() {

        testFile.remove();

        Assert.assertEquals("Removed file no longer exists", null, hardDrive.getFile("/test1/test2/test1.txt"));
        Assert.assertEquals("Renamed file has null path", null, testFile.getLocalPath());
    }

}
