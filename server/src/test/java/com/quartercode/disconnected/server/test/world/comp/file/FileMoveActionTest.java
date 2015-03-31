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

        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, oldPath).invoke(FileAddAction.EXECUTE);
    }

    private FileMoveAction createAction(File<ParentFile<?>> file, String newPath) {

        FileMoveAction action = new FileMoveAction();
        action.setObj(FileMoveAction.FILE_SYSTEM, fileSystem);
        action.setObj(FileMoveAction.PATH, newPath);
        action.setObj(FileMoveAction.FILE, file);
        return action;
    }

    @Test
    public void testExecute() {

        FileMoveAction action = createAction(file, newPath);
        actuallyTestExecute(action);
    }

    @Test
    public void testFileExecute() {

        FileMoveAction action = file.invoke(File.CREATE_MOVE, newPath);
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileMoveAction action) {

        action.invoke(FileMoveAction.EXECUTE);

        assertEquals("Resolved file for new path", file, fileSystem.invoke(FileSystem.GET_FILE, newPath));
        assertEquals("Resolved file for old path", null, fileSystem.invoke(FileSystem.GET_FILE, oldPath));
        assertEquals("Path of moved file", newPath, file.invoke(File.GET_PATH));
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
            FileMoveAction action = file.invoke(File.CREATE_MOVE, newPath);
            actuallyTestIsExecutableBy(action);
        }
    }

    private void actuallyTestIsExecutableBy(FileMoveAction action) {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        Directory newParentFile = new Directory();
        newParentFile.setObj(File.OWNER, user);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, newParentFile, newParentPath).invoke(FileMoveAction.EXECUTE);

        actuallyTestIsExecutableBy(action, newParentFile, "", "", false);
        actuallyTestIsExecutableBy(action, newParentFile, "u:d", "", false);
        actuallyTestIsExecutableBy(action, newParentFile, "", "u:w", false);
        actuallyTestIsExecutableBy(action, newParentFile, "u:d", "u:w", true);
    }

    private void actuallyTestIsExecutableBy(FileMoveAction action, File<?> newParentFile, String fileRights, String newParentFileRights, boolean expectedResult) {

        file.setObj(File.RIGHTS, new FileRights(fileRights));
        newParentFile.setObj(File.RIGHTS, new FileRights(newParentFileRights));

        boolean result = action.invoke(FileRemoveAction.IS_EXECUTABLE_BY, user);

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
            FileMoveAction action = file.invoke(File.CREATE_MOVE, newPath);
            actuallyTestGetMissingRights(action);
        }
    }

    private void actuallyTestGetMissingRights(FileMoveAction action) {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        Directory newParentFile = new Directory();
        newParentFile.setObj(File.OWNER, user);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, newParentFile, newParentPath).invoke(FileMoveAction.EXECUTE);

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

        file.setObj(File.RIGHTS, new FileRights(fileRights));
        newParentFile.setObj(File.RIGHTS, new FileRights(newParentFileRights));

        Map<File<?>, Character[]> result = action.invoke(FileRemoveAction.GET_MISSING_RIGHTS, user);

        assertEquals("Missing file rights map", prepareMissingRightsMap(expectedResult), prepareMissingRightsMap(result));
    }

}
