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
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.ParentFile;

@RunWith (Parameterized.class)
public class FileAddActionRootTest extends AbstractFileActionTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[] { "test.txt" });

        return data;
    }

    private final String addFilePath;

    public FileAddActionRootTest(String addFilePath) {

        this.addFilePath = addFilePath;
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
        actuallyTestExecute(action);
    }

    @Test
    public void testFileSystemExecute() {

        FileAddAction action = fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, addFilePath);
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileAddAction action) {

        action.get(FileAddAction.EXECUTE).invoke();
        Assert.assertEquals("Resolved file", file, fileSystem.get(FileSystem.GET_FILE).invoke(addFilePath));
    }

    @Test (expected = IllegalStateException.class)
    public void testExecutePathAlreadyOccupied() {

        FileAddAction action = createAction(file, addFilePath);
        actuallyTestExecutePathAlreadyOccupied(action);
    }

    @Test (expected = IllegalStateException.class)
    public void testFileSystemExecutePathAlreadyOccupied() {

        FileAddAction action = fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, addFilePath);
        actuallyTestExecutePathAlreadyOccupied(action);
    }

    private void actuallyTestExecutePathAlreadyOccupied(FileAddAction action) {

        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), addFilePath).get(FileAddAction.EXECUTE).invoke();
        action.get(FileAddAction.EXECUTE).invoke();
    }

}
