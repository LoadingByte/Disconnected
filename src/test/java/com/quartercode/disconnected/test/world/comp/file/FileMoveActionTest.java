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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.world.comp.file.Directory;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileMoveAction;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.ParentFile;

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

        boolean[] executable = new boolean[4];

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        newParentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        executable[0] = action.get(FileMoveAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        newParentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        executable[1] = action.get(FileMoveAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        newParentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("-w----------");
        executable[2] = action.get(FileMoveAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        newParentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("-w----------");
        executable[3] = action.get(FileMoveAction.IS_EXECUTABLE_BY).invoke(user);

        assertTrue("File move action is executable although no required right is set", !executable[0]);
        assertTrue("File move action is executable although the write right is not set on the new parent file", !executable[1]);
        assertTrue("File move action is executable although the delete right is not set on the old file", !executable[2]);
        assertTrue("File move action is not executable although all required rights are set", executable[3]);
    }

}
