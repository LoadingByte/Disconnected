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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.shared.comp.ByteUnit;

public class FileSystemTest {

    private FileSystem  fileSystem;
    private ContentFile testFile;

    @Before
    public void setUp() {

        fileSystem = new FileSystem();
        fileSystem.setObj(FileSystem.SIZE, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));

        testFile = new ContentFile();
        testFile.setObj(ContentFile.CONTENT, "Test-Content");
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, testFile, "test1/test2/test.txt").invoke(FileAddAction.EXECUTE);
    }

    @Test
    public void testGetFile() {

        assertEquals("Resolved file", testFile, fileSystem.invoke(FileSystem.GET_FILE, "test1/test2/test.txt"));
    }

    @Test
    public void testGetFileNotExisting() {

        assertNull("GET_FILE didn't return null for a not existing path", fileSystem.invoke(FileSystem.GET_FILE, "test1/test2/test2.txt"));
    }

    @Test
    public void testGetFileWithInvalidPath() {

        assertNull("GET_FILE didn't return null for an invalid path (one dir is a content file)", fileSystem.invoke(FileSystem.GET_FILE, "test1/test2/test.txt/test2.txt"));
    }

    @Test
    public void testCalcSpace() {

        long contentSize = 30;
        long filled = fileSystem.invoke(FileSystem.GET_FILLED);
        long free = fileSystem.invoke(FileSystem.GET_FREE);

        assertEquals("Filled bytes", contentSize, filled);
        assertEquals("Free bytes", fileSystem.invoke(DerivableSize.GET_SIZE) - contentSize, free);
        assertEquals("Size (Filled + free)", (long) fileSystem.invoke(DerivableSize.GET_SIZE), free + filled);
    }

}
