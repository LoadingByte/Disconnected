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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.world.comp.file.Directory;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAction;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.ParentFile;

@RunWith (Parameterized.class)
public class FileAddActionTest extends AbstractFileActionTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[] { "test1/test2", "test1/test2/test.txt", true });
        data.add(new Object[] { "", "test.txt", false });

        return data;
    }

    private final String  addFileParentPath;
    private final String  addFilePath;
    private final boolean testRights;

    public FileAddActionTest(String addFileParentPath, String addFilePath, boolean testRights) {

        this.addFileParentPath = addFileParentPath;
        this.addFilePath = addFilePath;
        this.testRights = testRights;
    }

    private FileAddAction createAction(File<ParentFile<?>> file, String path) {

        FileAddAction action = new FileAddAction();
        action.get(FileAddAction.FILE_SYSTEM).set(fileSystem);
        action.get(FileAddAction.PATH).set(path);
        action.get(FileAddAction.FILE).set(file);
        return action;
    }

    @Test
    public void testExecute() {

        FileAddAction action = createAction(file, addFilePath);
        actuallyTestExecute(action, addFilePath);
    }

    @Test
    public void testFileSystemExecute() {

        FileAction action = fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, addFilePath);
        actuallyTestExecute(action, addFilePath);
    }

    private void actuallyTestExecute(FileAction action, String filePath) {

        action.get(FileAction.EXECUTE).invoke();
        Assert.assertEquals("Resolved file", file, fileSystem.get(FileSystem.GET_FILE).invoke(filePath));
    }

    @Test
    public void testIsExecutableBy() {

        if (testRights) {
            FileAddAction action = createAction(file, addFilePath);
            actuallyTestIsExecutableBy(action, addFileParentPath);
        }
    }

    @Test
    public void testFileSystemIsExecutableBy() {

        if (testRights) {
            FileAction action = fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, addFilePath);
            actuallyTestIsExecutableBy(action, addFileParentPath);
        }
    }

    private void actuallyTestIsExecutableBy(FileAction action, String parentFilePath) {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        Directory parentFile = new Directory();
        parentFile.get(File.OWNER).set(user);
        createAction(parentFile, parentFilePath).get(FileAction.EXECUTE).invoke();

        boolean[] executable = new boolean[2];
        parentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("-w----------");
        executable[0] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);
        parentFile.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke("------------");
        executable[1] = action.get(FileAction.IS_EXECUTABLE_BY).invoke(user);

        Assert.assertTrue("File add action is not executable although the write right is set on the parent directory", executable[0]);
        Assert.assertTrue("File add action is executable although the write right is not set on the parent directory", !executable[1]);
    }

}
