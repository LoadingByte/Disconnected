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

import static com.quartercode.disconnected.server.test.world.comp.file.FileAssert.assertInvalidPath;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileMoveAction;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

@RunWith (Parameterized.class)
public class FileMoveActionTest extends AbstractFileActionTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { "test1/test2/test.txt", "test3/test4", "test3/test4/test5.txt", true });
        data.add(new Object[] { "test1/test2/test.txt", "", "test5.txt", false });
        data.add(new Object[] { "test.txt", "test3/test4", "test3/test4/test5.txt", true });
        data.add(new Object[] { "test.txt", "", "test5.txt", false });

        return data;
    }

    private final String  oldPath;
    private final String  newParentPath;
    private final String  newPath;
    private final boolean testRights;

    public FileMoveActionTest(String oldPath, String newParentPath, String newPath, boolean testRights) {

        this.oldPath = oldPath;
        this.newParentPath = newParentPath;
        this.newPath = newPath;
        this.testRights = testRights;
    }

    @Before
    public void setUp2() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        fileSystem.prepareAddFile(cfile, oldPath).execute();
    }

    @Test
    public void testExecute() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileMoveAction action = new FileMoveAction(cfile, newPath);
        actuallyTestExecute(action);
    }

    @Test
    public void testFileExecute() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        FileMoveAction action = cfile.prepareMove(newPath);
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileMoveAction action) throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        action.execute();

        assertEquals("Resolved file for new path", cfile, fileSystem.getFile(newPath));
        assertInvalidPath("Old file hasn't been removed", fileSystem, oldPath);
        assertEquals("Path of moved file", newPath, cfile.getPath());
    }

    @Test
    public void testIsExecutableBy() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        if (testRights) {
            FileMoveAction action = new FileMoveAction(cfile, newPath);
            actuallyTestIsExecutableBy(action);
        }
    }

    @Test
    public void testFileIsExecutableBy() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        if (testRights) {
            FileMoveAction action = cfile.prepareMove(newPath);
            actuallyTestIsExecutableBy(action);
        }
    }

    private void actuallyTestIsExecutableBy(FileMoveAction action) throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        fileSystem.prepareAddFile(dir, newParentPath).execute();

        actuallyTestIsExecutableBy(action, "", "", false);
        actuallyTestIsExecutableBy(action, "u:d", "", false);
        actuallyTestIsExecutableBy(action, "", "u:w", false);
        actuallyTestIsExecutableBy(action, "u:d", "u:w", true);
    }

    private void actuallyTestIsExecutableBy(FileMoveAction action, String fileRights, String newParentFileRights, boolean expectedResult) {

        cfile.getRights().importRights(fileRights);
        dir.getRights().importRights(newParentFileRights);

        boolean result = action.isExecutableBy(user);
        assertEquals("IS_EXECUTABLE_BY result", expectedResult, result);
    }

    @Test
    public void testGetMissingRights() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        if (testRights) {
            FileMoveAction action = new FileMoveAction(cfile, newPath);
            actuallyTestGetMissingRights(action);
        }
    }

    @Test
    public void testFileGetMissingRights() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        if (testRights) {
            FileMoveAction action = cfile.prepareMove(newPath);
            actuallyTestGetMissingRights(action);
        }
    }

    private void actuallyTestGetMissingRights(FileMoveAction action) throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        fileSystem.prepareAddFile(dir, newParentPath).execute();

        // Test 1
        Map<File<?>, Character[]> test1Result = new HashMap<>();
        test1Result.put(cfile, new Character[] { FileRights.DELETE });
        test1Result.put(dir, new Character[] { FileRights.WRITE });
        actuallyTestGetMissingRights(action, "", "", test1Result);

        // Test 2
        Map<File<?>, Character[]> test2Result = new HashMap<>();
        test2Result.put(dir, new Character[] { FileRights.WRITE });
        actuallyTestGetMissingRights(action, "u:d", "", test2Result);

        // Test 3
        Map<File<?>, Character[]> test3Result = new HashMap<>();
        test3Result.put(cfile, new Character[] { FileRights.DELETE });
        actuallyTestGetMissingRights(action, "", "u:w", test3Result);

        // Test 4
        actuallyTestGetMissingRights(action, "u:d", "u:w", new HashMap<File<?>, Character[]>());
    }

    private void actuallyTestGetMissingRights(FileMoveAction action, String fileRights, String newParentFileRights, Map<File<?>, Character[]> expectedResult) {

        cfile.getRights().importRights(fileRights);
        dir.getRights().importRights(newParentFileRights);

        Map<File<?>, Character[]> result = action.getMissingRights(user);
        assertEquals("Missing file rights map", prepareMissingRightsMap(expectedResult), prepareMissingRightsMap(result));
    }

}
