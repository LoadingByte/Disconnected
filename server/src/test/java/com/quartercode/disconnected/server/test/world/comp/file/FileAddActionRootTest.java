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
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;

public class FileAddActionRootTest extends AbstractFileActionTest {

    private static final String ADD_FILE_PATH = "test.txt";

    private FileAddAction createAction(File<ParentFile<?>> file, String path) {

        FileAddAction action = new FileAddAction();
        action.get(FileAddAction.FILE_SYSTEM).set(fileSystem);
        action.get(FileAddAction.PATH).set(path);
        action.get(FileAddAction.FILE).set(file);
        return action;
    }

    @Test
    public void testExecute() {

        FileAddAction action = createAction(file, ADD_FILE_PATH);
        actuallyTestExecute(action);
    }

    @Test
    public void testFileSystemExecute() {

        FileAddAction action = fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, ADD_FILE_PATH);
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileAddAction action) {

        action.get(FileAddAction.EXECUTE).invoke();
        assertEquals("Resolved file", file, fileSystem.get(FileSystem.GET_FILE).invoke(ADD_FILE_PATH));
    }

    @Test (expected = OccupiedPathException.class)
    public void testExecutePathAlreadyOccupied() {

        FileAddAction action = createAction(file, ADD_FILE_PATH);
        actuallyTestExecutePathAlreadyOccupied(action);
    }

    @Test (expected = OccupiedPathException.class)
    public void testFileSystemExecutePathAlreadyOccupied() {

        FileAddAction action = fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, ADD_FILE_PATH);
        actuallyTestExecutePathAlreadyOccupied(action);
    }

    private void actuallyTestExecutePathAlreadyOccupied(FileAddAction action) {

        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), ADD_FILE_PATH).get(FileAddAction.EXECUTE).invoke();
        action.get(FileAddAction.EXECUTE).invoke();
    }

}
