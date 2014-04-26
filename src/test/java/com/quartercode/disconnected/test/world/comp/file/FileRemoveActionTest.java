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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.file.ParentFile;

@RunWith (Parameterized.class)
public class FileRemoveActionTest extends AbstractFileActionTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[] { "test1/test2" });
        data.add(new Object[] { "test" });

        return data;
    }

    private final String        removeFilePath;
    private final String        removeFileChildPath;

    private File<ParentFile<?>> childFile;

    public FileRemoveActionTest(String removeFilePath) {

        this.removeFilePath = removeFilePath;
        removeFileChildPath = FileUtils.normalizePath(removeFilePath) + "/test.txt";
    }

    @Before
    public void setUp2() {

        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, removeFilePath).get(FileAddAction.EXECUTE).invoke();

        childFile = new ContentFile();
        childFile.get(File.OWNER).set(user);
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(childFile, removeFileChildPath).get(FileAddAction.EXECUTE).invoke();
    }

    private FileRemoveAction createAction(File<ParentFile<?>> file) {

        FileRemoveAction action = new FileRemoveAction();
        action.get(FileAddAction.FILE).set(file);
        return action;
    }

    @Test
    public void testExecute() {

        FileRemoveAction action = createAction(file);
        actuallyTestExecute(action);
    }

    @Test
    public void testFileExecute() {

        FileRemoveAction action = file.get(File.CREATE_REMOVE).invoke();
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileRemoveAction action) {

        action.get(FileRemoveAction.EXECUTE).invoke();
        Assert.assertEquals("Resolved file for deleted file", null, fileSystem.get(FileSystem.GET_FILE).invoke(removeFilePath));
    }

    @Test
    public void testIsExecutableBy() {

        FileRemoveAction action = createAction(file);
        actuallyTestIsExecutableBy(action);
    }

    @Test
    public void testFileIsExecutableBy() {

        FileRemoveAction action = file.get(File.CREATE_REMOVE).invoke();
        actuallyTestIsExecutableBy(action);
    }

    private void actuallyTestIsExecutableBy(FileRemoveAction action) {

        boolean[] executable = new boolean[4];

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        childFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        executable[0] = action.get(FileRemoveAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        childFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        executable[1] = action.get(FileRemoveAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        childFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        executable[2] = action.get(FileRemoveAction.IS_EXECUTABLE_BY).invoke(user);

        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        childFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("--d---------");
        executable[3] = action.get(FileRemoveAction.IS_EXECUTABLE_BY).invoke(user);

        Assert.assertTrue("File remove action is executable although no required right is set", !executable[0]);
        Assert.assertTrue("File remove action is executable although the delete right is not set on the child file", !executable[1]);
        Assert.assertTrue("File remove action is executable although the delete right is not set on the file for removal", !executable[2]);
        Assert.assertTrue("File remove action is not executable although all required rights are set", executable[3]);
    }

}
