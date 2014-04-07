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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystem;

public class FileTest {

    private FileSystem  fileSystem;
    private ContentFile testFile;

    @Before
    public void setUp() throws ExecutorInvocationException {

        fileSystem = new FileSystem();
        fileSystem.get(FileSystem.SIZE).set(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));

        testFile = new ContentFile();
        fileSystem.get(FileSystem.ADD_FILE).invoke(testFile, "/test1/test2/test.txt");

        ContentFile testFile2 = new ContentFile();
        fileSystem.get(FileSystem.ADD_FILE).invoke(testFile2, "/test1/test2/test2.txt");
    }

    @Test
    public void testGetPath() throws ExecutorInvocationException {

        Assert.assertEquals("Path", "/test1/test2/test.txt", testFile.get(File.GET_PATH).invoke());
    }

    @Test
    public void testSetPath() throws ExecutorInvocationException {

        testFile.get(File.SET_PATH).invoke("../../test3/test4.txt");

        Assert.assertTrue("Moved file doesn't exist", testFile.equals(fileSystem.get(FileSystem.GET_FILE).invoke("/test1/test3/test4.txt")));
        Assert.assertTrue("Removed file does exist", fileSystem.get(FileSystem.GET_FILE).invoke("/test1/test2/test.txt") == null);
        Assert.assertEquals("Path of moved file", "/test1/test3/test4.txt", testFile.get(File.GET_PATH).invoke());
    }

    @Test
    public void testSetName() throws ExecutorInvocationException {

        testFile.get(File.NAME).set("test3.txt");

        Assert.assertTrue("Renamed file doesn't exist", testFile.equals(fileSystem.get(FileSystem.GET_FILE).invoke("/test1/test2/test3.txt")));
        Assert.assertTrue("Removed file does exist", fileSystem.get(FileSystem.GET_FILE).invoke("/test1/test2/test.txt") == null);
        Assert.assertEquals("Path of renamed file", "/test1/test2/test3.txt", testFile.get(File.GET_PATH).invoke());
    }

    @Test
    public void testRemove() throws ExecutorInvocationException {

        testFile.get(File.REMOVE).invoke();

        Assert.assertNull("Resolved file after removal", fileSystem.get(FileSystem.GET_FILE).invoke("/test1/test2/test.txt"));
        Assert.assertEquals("Path of removed file (should be null)", null, testFile.get(File.GET_PATH).invoke());
    }

}
