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
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;

public class FileAddActionRootTest extends AbstractFileActionTest {

    private static final String ADD_FILE_PATH = "test.txt";

    @Test
    public void testExecute() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = new FileAddAction(fileSystem, cfile, ADD_FILE_PATH);
        actuallyTestExecute(action);
    }

    @Test
    public void testFileSystemExecute() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = fileSystem.prepareAddFile(cfile, ADD_FILE_PATH);
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileAddAction action) throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        action.execute();

        assertEquals("File path", ADD_FILE_PATH, cfile.getPath());
        assertEquals("Resolved file", cfile, fileSystem.getFile(ADD_FILE_PATH));
    }

    @Test (expected = OccupiedPathException.class)
    public void testExecutePathAlreadyOccupied() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = new FileAddAction(fileSystem, cfile, ADD_FILE_PATH);
        actuallyTestExecutePathAlreadyOccupied(action);
    }

    @Test (expected = OccupiedPathException.class)
    public void testFileSystemExecutePathAlreadyOccupied() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = fileSystem.prepareAddFile(cfile, ADD_FILE_PATH);
        actuallyTestExecutePathAlreadyOccupied(action);
    }

    private void actuallyTestExecutePathAlreadyOccupied(FileAddAction action) throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        fileSystem.prepareAddFile(new ContentFile(user), ADD_FILE_PATH).execute();
        action.execute();
    }

}
