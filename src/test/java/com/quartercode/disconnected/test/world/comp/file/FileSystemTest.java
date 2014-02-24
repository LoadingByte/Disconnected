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
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.FileSystem;

public class FileSystemTest {

    private FileSystem  fileSystem;
    private ContentFile testFile;

    @Before
    public void setUp() throws FunctionExecutionException {

        fileSystem = new FileSystem();
        fileSystem.setLocked(false);
        fileSystem.get(FileSystem.SET_SIZE).invoke(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystem.setLocked(true);

        testFile = new ContentFile();
        testFile.get(ContentFile.SET_CONTENT).invoke("Test-Content");
        fileSystem.get(FileSystem.ADD_FILE).invoke(testFile, "/test1/test2/test.txt");
    }

    @Test
    public void testGetFile() throws FunctionExecutionException {

        Assert.assertEquals("Resolved file", testFile, fileSystem.get(FileSystem.GET_FILE).invoke("/test1/test2/test.txt"));
    }

    @Test
    public void testCalcSpace() throws FunctionExecutionException {

        long contentSize = 30;
        long filled = fileSystem.get(FileSystem.GET_FILLED).invoke();
        long free = fileSystem.get(FileSystem.GET_FREE).invoke();

        Assert.assertEquals("Filled bytes", contentSize, filled);
        Assert.assertEquals("Free bytes", fileSystem.get(DerivableSize.GET_SIZE).invoke() - contentSize, free);
        Assert.assertEquals("Size (Filled + free)", (long) fileSystem.get(DerivableSize.GET_SIZE).invoke(), free + filled);
    }

}
