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

package com.quartercode.disconnected.test.world.comp.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileSystem;

public class FileTest {

    private FileSystem  fileSystem;
    private ContentFile testFile;

    @Before
    public void setUp() {

        fileSystem = new FileSystem();
        fileSystem.get(FileSystem.SIZE).set(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));

        testFile = new ContentFile();
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(testFile, "test1/test2/test.txt").get(FileAddAction.EXECUTE).invoke();
    }

    @Test
    public void testGetPath() {

        assertEquals("File path", "test1/test2/test.txt", testFile.get(File.GET_PATH).invoke());
    }

    /*
     * This test is only here for testing whether setting the name triggers a bug.
     * In production, a FileMoveAction should be used instead of the direct name setting.
     */
    @Test
    public void testSetName() {

        testFile.get(File.NAME).set("test3.txt");

        assertTrue("Renamed file doesn't exist", testFile.equals(fileSystem.get(FileSystem.GET_FILE).invoke("test1/test2/test3.txt")));
        assertTrue("Removed file does exist", fileSystem.get(FileSystem.GET_FILE).invoke("test1/test2/test.txt") == null);
        assertEquals("Path of renamed file", "test1/test2/test3.txt", testFile.get(File.GET_PATH).invoke());
    }

}
