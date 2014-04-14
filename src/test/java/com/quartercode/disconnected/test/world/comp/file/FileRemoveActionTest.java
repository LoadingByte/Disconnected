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
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAction;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.ParentFile;

public class FileRemoveActionTest extends AbstractFileActionTest {

    private static final String PATH       = "/test1/test2";
    private static final String CHILD_PATH = PATH + "/test.txt";

    private File<ParentFile<?>> childFile;

    @Before
    public void setUp2() {

        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, PATH).get(FileAddAction.EXECUTE).invoke();

        childFile = new ContentFile();
        childFile.get(File.OWNER).set(user);
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(childFile, CHILD_PATH).get(FileAddAction.EXECUTE).invoke();
    }

    private FileRemoveAction createAction(File<ParentFile<?>> file) {

        FileRemoveAction action = new FileRemoveAction();
        action.get(FileAddAction.FILE).set(file);
        return action;
    }

    @Test
    public void testExecute() {

        FileRemoveAction action = createAction(file);
        actuallyTestExecute(action, PATH);
    }

    @Test
    public void testFileExecute() {

        FileAction action = file.get(File.CREATE_REMOVE).invoke();
        actuallyTestExecute(action, PATH);
    }

    private void actuallyTestExecute(FileAction action, String oldFilePath) {

        action.get(FileAddAction.EXECUTE).invoke();
        Assert.assertEquals("Resolved file for deleted file", null, fileSystem.get(FileSystem.GET_FILE).invoke(oldFilePath));
    }

    @Test
    public void testIsExecutableBy() {

        FileRemoveAction action = createAction(file);
        actuallyTestIsExecutableBy(action, PATH);
    }

    @Test
    public void testFileIsExecutableBy() {

        FileAction action = file.get(File.CREATE_REMOVE).invoke();
        actuallyTestIsExecutableBy(action, PATH);
    }

    private void actuallyTestIsExecutableBy(FileAction action, String oldFilePath) {

        boolean[] executable = new boolean[4];

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        childFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        executable[0] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        childFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        executable[1] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        childFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        executable[2] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        childFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        executable[3] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);

        Assert.assertTrue("File remove action is executable although no required right is set", !executable[0]);
        Assert.assertTrue("File remove action is executable although the delete right is not set on the child file", !executable[1]);
        Assert.assertTrue("File remove action is executable although the delete right is not set on the file for removal", !executable[2]);
        Assert.assertTrue("File remove action is not executable although all required rights are set", executable[3]);
    }

}
