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

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

public class FileAddActionTest extends AbstractFileActionTest {

    private static final String ADD_FILE_PARENT_PATH = "test1/test2";
    private static final String ADD_FILE_PATH        = ADD_FILE_PARENT_PATH + "/test.txt";

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

    @Test (expected = InvalidPathException.class)
    public void testExecuteInvalidPath() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = new FileAddAction(fileSystem, cfile, ADD_FILE_PATH);
        actuallyTestExecuteInvalidPath(action);
    }

    @Test (expected = InvalidPathException.class)
    public void testFileSystemExecuteInvalidPath() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = fileSystem.prepareAddFile(cfile, ADD_FILE_PATH);
        actuallyTestExecuteInvalidPath(action);
    }

    private void actuallyTestExecuteInvalidPath(FileAddAction action) throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        fileSystem.prepareAddFile(new ContentFile(user), ADD_FILE_PARENT_PATH).execute();
        action.execute();
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

    @Test
    public void testIsExecutableBy() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = new FileAddAction(fileSystem, cfile, ADD_FILE_PATH);
        actuallyTestIsExecutableBy(action);
    }

    @Test
    public void testFileSystemIsExecutableBy() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = fileSystem.prepareAddFile(cfile, ADD_FILE_PATH);
        actuallyTestIsExecutableBy(action);
    }

    private void actuallyTestIsExecutableBy(FileAddAction action) throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        new FileAddAction(fileSystem, dir, ADD_FILE_PARENT_PATH).execute();

        // Test 1
        dir.getRights().importRights("u:w");
        assertTrue("File add action is not executable although the write right is set on the parent directory", action.isExecutableBy(user));

        // Test 2
        dir.getRights().clearRights();
        assertFalse("File add action is executable although the write right is not set on the parent directory", action.isExecutableBy(user));
    }

    @Test
    public void testGetMissingRights() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = new FileAddAction(fileSystem, cfile, ADD_FILE_PATH);
        actuallyTestGetMissingRights(action);
    }

    @Test
    public void testFileSystemGetMissingRights() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileAddAction action = fileSystem.prepareAddFile(cfile, ADD_FILE_PATH);
        actuallyTestGetMissingRights(action);
    }

    private void actuallyTestGetMissingRights(FileAddAction action) throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        new FileAddAction(fileSystem, dir, ADD_FILE_PARENT_PATH).execute();

        // Test 1
        dir.getRights().importRights("u:w");
        assertEquals("Missing file rights map with write right on parent dir", new HashMap<>(), prepareMissingRightsMap(action.getMissingRights(user)));

        // Test 2
        dir.getRights().clearRights();
        Map<File<?>, Character[]> test2Result = new HashMap<>();
        test2Result.put(dir, new Character[] { FileRights.WRITE });
        assertEquals("Missing file rights map without write right on parent dir", prepareMissingRightsMap(test2Result), prepareMissingRightsMap(action.getMissingRights(user)));
    }

}
