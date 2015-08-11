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
import com.quartercode.disconnected.server.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;

@RunWith (Parameterized.class)
public class FileRemoveActionTest extends AbstractFileActionTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { "test1/test2" });
        data.add(new Object[] { "test" });

        return data;
    }

    private final String removeFilePath;
    private final String removeFileChildPath;

    public FileRemoveActionTest(String removeFilePath) {

        this.removeFilePath = removeFilePath;
        removeFileChildPath = PathUtils.normalize(removeFilePath) + "/test.txt";
    }

    @Before
    public void setUp2() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        fileSystem.prepareAddFile(dir, removeFilePath).execute();
        fileSystem.prepareAddFile(cfile, removeFileChildPath).execute();
    }

    @Test
    public void testExecute() {

        FileRemoveAction action = new FileRemoveAction(dir);
        actuallyTestExecute(action);
    }

    @Test
    public void testFileExecute() {

        FileRemoveAction action = dir.prepareRemove();
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileRemoveAction action) {

        action.execute();

        assertEquals("File path", null, dir.getPath());
        assertInvalidPath("File has not been removed", fileSystem, removeFilePath);
    }

    @Test
    public void testIsExecutableBy() {

        FileRemoveAction action = new FileRemoveAction(dir);
        actuallyTestIsExecutableBy(action);
    }

    @Test
    public void testFileIsExecutableBy() {

        FileRemoveAction action = dir.prepareRemove();
        actuallyTestIsExecutableBy(action);
    }

    private void actuallyTestIsExecutableBy(FileRemoveAction action) {

        actuallyTestIsExecutableBy(action, "", "", false);
        actuallyTestIsExecutableBy(action, "u:d", "", false);
        actuallyTestIsExecutableBy(action, "", "u:d", false);
        actuallyTestIsExecutableBy(action, "u:d", "u:d", true);
    }

    private void actuallyTestIsExecutableBy(FileRemoveAction action, String fileRights, String childFileRights, boolean expectedResult) {

        dir.getRights().importRights(fileRights);
        cfile.getRights().importRights(childFileRights);

        boolean result = action.isExecutableBy(user);
        assertEquals("IS_EXECUTABLE_BY result", expectedResult, result);
    }

    @Test
    public void testGetMissingRights() {

        FileRemoveAction action = new FileRemoveAction(dir);
        actuallyTestGetMissingRights(action);
    }

    @Test
    public void testFileGetMissingRights() {

        FileRemoveAction action = dir.prepareRemove();
        actuallyTestGetMissingRights(action);
    }

    private void actuallyTestGetMissingRights(FileRemoveAction action) {

        // Test 1
        Map<File<?>, Character[]> test1Result = new HashMap<>();
        test1Result.put(dir, new Character[] { FileRights.DELETE });
        test1Result.put(cfile, new Character[] { FileRights.DELETE });
        actuallyTestGetMissingRights(action, "", "", test1Result);

        // Test 2
        Map<File<?>, Character[]> test2Result = new HashMap<>();
        test2Result.put(cfile, new Character[] { FileRights.DELETE });
        actuallyTestGetMissingRights(action, "u:d", "", test2Result);

        // Test 3
        Map<File<?>, Character[]> test3Result = new HashMap<>();
        test3Result.put(dir, new Character[] { FileRights.DELETE });
        actuallyTestGetMissingRights(action, "", "u:d", test3Result);

        // Test 4
        actuallyTestGetMissingRights(action, "u:d", "u:d", new HashMap<File<?>, Character[]>());
    }

    private void actuallyTestGetMissingRights(FileRemoveAction action, String fileRights, String childFileRights, Map<File<?>, Character[]> expectedResult) {

        dir.getRights().importRights(fileRights);
        cfile.getRights().importRights(childFileRights);

        Map<File<?>, Character[]> result = action.getMissingRights(user);
        assertEquals("Missing file rights map", prepareMissingRightsMap(expectedResult), prepareMissingRightsMap(result));
    }

}
