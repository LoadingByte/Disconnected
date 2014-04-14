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
import com.quartercode.disconnected.world.comp.file.Directory;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAction;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileMoveAction;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.ParentFile;

public class FileMoveActionTest extends AbstractFileActionTest {

    private static final String PARENT_PATH_1 = "/test1/test2";
    private static final String PATH_1        = PARENT_PATH_1 + "/test.txt";
    private static final String PARENT_PATH_2 = "/test3/test4";
    private static final String PATH_2        = PARENT_PATH_2 + "/test5.txt";

    @Before
    public void setUp2() {

        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, PATH_1).get(FileAddAction.EXECUTE).invoke();
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

        FileMoveAction action = createAction(file, PATH_2);
        actuallyTestExecute(action, PATH_1, PATH_2);
    }

    @Test
    public void testFileExecute() {

        FileAction action = file.get(File.CREATE_MOVE).invoke(PATH_2);
        actuallyTestExecute(action, PATH_1, PATH_2);
    }

    private void actuallyTestExecute(FileAction action, String oldFilePath, String newFilePath) {

        action.get(FileAction.EXECUTE).invoke();

        Assert.assertEquals("Resolved file for new path", file, fileSystem.get(FileSystem.GET_FILE).invoke(newFilePath));
        Assert.assertEquals("Resolved file for old path", null, fileSystem.get(FileSystem.GET_FILE).invoke(oldFilePath));
        Assert.assertEquals("Path of moved file", newFilePath, file.get(File.GET_PATH).invoke());
    }

    @Test
    public void testIsExecutableBy() {

        FileMoveAction action = createAction(file, PATH_2);
        actuallyTestIsExecutableBy(action, PARENT_PATH_2);
    }

    @Test
    public void testFileIsExecutableBy() {

        FileAction action = file.get(File.CREATE_MOVE).invoke(PATH_2);
        actuallyTestIsExecutableBy(action, PARENT_PATH_2);
    }

    private void actuallyTestIsExecutableBy(FileAction action, String newParentPath) {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        Directory newParentFile = new Directory();
        newParentFile.get(File.OWNER).set(user);
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(newParentFile, newParentPath).get(FileAddAction.EXECUTE).invoke();

        boolean[] executable = new boolean[4];

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        newParentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        executable[0] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        newParentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        executable[1] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        newParentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("-w----------");
        executable[2] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        newParentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("-w----------");
        executable[3] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);

        Assert.assertTrue("File move action is executable although no required right is set", !executable[0]);
        Assert.assertTrue("File move action is executable although the write right is not set on the new parent file", !executable[1]);
        Assert.assertTrue("File move action is executable although the delete right is not set on the old file", !executable[2]);
        Assert.assertTrue("File move action is not executable although all required rights are set", executable[3]);
    }

}
