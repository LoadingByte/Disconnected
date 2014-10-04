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
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileMoveAction;
import com.quartercode.disconnected.server.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
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
    public void setUp2() {

        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, oldPath).get(FileAddAction.EXECUTE).invoke();
    }

    private FileMoveAction createAction(File<ParentFile<?>> file, String newPath) {

        FileMoveAction action = new FileMoveAction();
        action.get(FileMoveAction.FILE_SYSTEM).set(fileSystem);
        action.get(FileMoveAction.PATH).set(newPath);
        action.get(FileMoveAction.FILE).set(file);
        return action;
    }

    @Test
    public void testExecute() {

        FileMoveAction action = createAction(file, newPath);
        actuallyTestExecute(action);
    }

    @Test
    public void testFileExecute() {

        FileMoveAction action = file.get(File.CREATE_MOVE).invoke(newPath);
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileMoveAction action) {

        action.get(FileMoveAction.EXECUTE).invoke();

        assertEquals("Resolved file for new path", file, fileSystem.get(FileSystem.GET_FILE).invoke(newPath));
        assertEquals("Resolved file for old path", null, fileSystem.get(FileSystem.GET_FILE).invoke(oldPath));
        assertEquals("Path of moved file", newPath, file.get(File.GET_PATH).invoke());
    }

    @Test
    public void testIsExecutableBy() {

        if (testRights) {
            FileMoveAction action = createAction(file, newPath);
            actuallyTestIsExecutableBy(action);
        }
    }

    @Test
    public void testFileIsExecutableBy() {

        if (testRights) {
            FileMoveAction action = file.get(File.CREATE_MOVE).invoke(newPath);
            actuallyTestIsExecutableBy(action);
        }
    }

    private void actuallyTestIsExecutableBy(FileMoveAction action) {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        Directory newParentFile = new Directory();
        newParentFile.get(File.OWNER).set(user);
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(newParentFile, newParentPath).get(FileMoveAction.EXECUTE).invoke();

        actuallyTestIsExecutableBy(action, newParentFile, "", "", false);
        actuallyTestIsExecutableBy(action, newParentFile, "u:d", "", false);
        actuallyTestIsExecutableBy(action, newParentFile, "", "u:w", false);
        actuallyTestIsExecutableBy(action, newParentFile, "u:d", "u:w", true);
    }

    private void actuallyTestIsExecutableBy(FileMoveAction action, File<?> newParentFile, String fileRights, String newParentFileRights, boolean expectedResult) {

        file.get(File.RIGHTS).set(new FileRights(fileRights));
        newParentFile.get(File.RIGHTS).set(new FileRights(newParentFileRights));

        boolean result = action.get(FileRemoveAction.IS_EXECUTABLE_BY).invoke(user);

        assertEquals("IS_EXECUTABLE_BY result", expectedResult, result);
    }

    @Test
    public void testGetMissingRights() {

        if (testRights) {
            FileMoveAction action = createAction(file, newPath);
            actuallyTestGetMissingRights(action);
        }
    }

    @Test
    public void testFileGetMissingRights() {

        if (testRights) {
            FileMoveAction action = file.get(File.CREATE_MOVE).invoke(newPath);
            actuallyTestGetMissingRights(action);
        }
    }

    private void actuallyTestGetMissingRights(FileMoveAction action) {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        Directory newParentFile = new Directory();
        newParentFile.get(File.OWNER).set(user);
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(newParentFile, newParentPath).get(FileMoveAction.EXECUTE).invoke();

        // Test 1
        Map<File<?>, Character[]> test1Result = new HashMap<>();
        test1Result.put(file, new Character[] { FileRights.DELETE });
        test1Result.put(newParentFile, new Character[] { FileRights.WRITE });
        actuallyTestGetMissingRights(action, newParentFile, "", "", test1Result);

        // Test 2
        Map<File<?>, Character[]> test2Result = new HashMap<>();
        test2Result.put(newParentFile, new Character[] { FileRights.WRITE });
        actuallyTestGetMissingRights(action, newParentFile, "u:d", "", test2Result);

        // Test 3
        Map<File<?>, Character[]> test3Result = new HashMap<>();
        test3Result.put(file, new Character[] { FileRights.DELETE });
        actuallyTestGetMissingRights(action, newParentFile, "", "u:w", test3Result);

        // Test 4
        actuallyTestGetMissingRights(action, newParentFile, "u:d", "u:w", new HashMap<File<?>, Character[]>());
    }

    private void actuallyTestGetMissingRights(FileMoveAction action, File<?> newParentFile, String fileRights, String newParentFileRights, Map<File<?>, Character[]> expectedResult) {

        file.get(File.RIGHTS).set(new FileRights(fileRights));
        newParentFile.get(File.RIGHTS).set(new FileRights(newParentFileRights));

        Map<File<?>, Character[]> result = action.get(FileRemoveAction.GET_MISSING_RIGHTS).invoke(user);

        assertEquals("Missing file rights map", prepareMissingRightsMap(expectedResult), prepareMissingRightsMap(result));
    }

}
