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

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.shared.world.comp.ByteUnit;

public class FileSystemTest {

    private FileSystem  fileSystem;
    private ContentFile testFile;

    @Before
    public void setUp() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        fileSystem = new FileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));

        testFile = new ContentFile(new User("user"));
        testFile.setContent("Test-Content");
        fileSystem.prepareAddFile(testFile, "test1/test2/test.txt").execute();
    }

    @Test
    public void testGetFile() throws InvalidPathException {

        assertEquals("Resolved file", testFile, fileSystem.getFile("test1/test2/test.txt"));
    }

    @Test (expected = InvalidPathException.class)
    public void testGetFileNotExisting() throws InvalidPathException {

        fileSystem.getFile("test1/test2/test2.txt");
    }

    @Test (expected = InvalidPathException.class)
    public void testGetFileWithInvalidPath() throws InvalidPathException {

        fileSystem.getFile("test1/test2/test.txt/test2.txt");
    }

    @Test
    public void testCalcSpace() throws InvalidPathException {

        // The size of the only top-level directory must be equal to the filled file system space
        long contentSize = fileSystem.getFile("test1").getSize();
        long filled = fileSystem.getFilledSpace();
        long free = fileSystem.getFreeSpace();

        assertEquals("Filled bytes", contentSize, filled);
        assertEquals("Free bytes", fileSystem.getSize() - contentSize, free);
        assertEquals("Size (Filled + free)", fileSystem.getSize(), free + filled);
    }

}
