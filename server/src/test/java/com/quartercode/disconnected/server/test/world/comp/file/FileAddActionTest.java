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

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

public class FileAddActionTest extends AbstractFileActionTest {

    private static final String ADD_FILE_PARENT_PATH = "test1/test2";
    private static final String ADD_FILE_PATH        = ADD_FILE_PARENT_PATH + "/test.txt";

    private FileAddAction createAction(File<ParentFile<?>> file, String path) {

        FileAddAction action = new FileAddAction();
        action.setObj(FileAddAction.FILE_SYSTEM, fileSystem);
        action.setObj(FileAddAction.PATH, path);
        action.setObj(FileAddAction.FILE, file);
        return action;
    }

    @Test
    public void testExecute() {

        FileAddAction action = createAction(file, ADD_FILE_PATH);
        actuallyTestExecute(action);
    }

    @Test
    public void testFileSystemExecute() {

        FileAddAction action = fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, ADD_FILE_PATH);
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileAddAction action) {

        action.invoke(FileAddAction.EXECUTE);
        assertEquals("Resolved file", file, fileSystem.invoke(FileSystem.GET_FILE, ADD_FILE_PATH));
    }

    @Test (expected = InvalidPathException.class)
    public void testExecuteInvalidPath() {

        FileAddAction action = createAction(file, ADD_FILE_PATH);
        actuallyTestExecuteInvalidPath(action);
    }

    @Test (expected = InvalidPathException.class)
    public void testFileSystemExecuteInvalidPath() {

        FileAddAction action = fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, ADD_FILE_PATH);
        actuallyTestExecuteInvalidPath(action);
    }

    private void actuallyTestExecuteInvalidPath(FileAddAction action) {

        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new ContentFile(), ADD_FILE_PARENT_PATH).invoke(FileAddAction.EXECUTE);
        action.invoke(FileAddAction.EXECUTE);
    }

    @Test (expected = OccupiedPathException.class)
    public void testExecutePathAlreadyOccupied() {

        FileAddAction action = createAction(file, ADD_FILE_PATH);
        actuallyTestExecutePathAlreadyOccupied(action);
    }

    @Test (expected = OccupiedPathException.class)
    public void testFileSystemExecutePathAlreadyOccupied() {

        FileAddAction action = fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, ADD_FILE_PATH);
        actuallyTestExecutePathAlreadyOccupied(action);
    }

    private void actuallyTestExecutePathAlreadyOccupied(FileAddAction action) {

        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, new ContentFile(), ADD_FILE_PATH).invoke(FileAddAction.EXECUTE);
        action.invoke(FileAddAction.EXECUTE);
    }

    @Test
    public void testIsExecutableBy() {

        FileAddAction action = createAction(file, ADD_FILE_PATH);
        actuallyTestIsExecutableBy(action);
    }

    @Test
    public void testFileSystemIsExecutableBy() {

        FileAddAction action = fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, ADD_FILE_PATH);
        actuallyTestIsExecutableBy(action);
    }

    private void actuallyTestIsExecutableBy(FileAddAction action) {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        Directory parentFile = new Directory();
        parentFile.setObj(File.OWNER, user);
        createAction(parentFile, ADD_FILE_PARENT_PATH).invoke(FileAddAction.EXECUTE);

        // Test 1
        parentFile.setObj(File.RIGHTS, new FileRights("u:w"));
        assertTrue("File add action is not executable although the write right is set on the parent directory", action.invoke(FileAddAction.IS_EXECUTABLE_BY, user));

        // Test 2
        parentFile.setObj(File.RIGHTS, new FileRights());
        assertFalse("File add action is executable although the write right is not set on the parent directory", action.invoke(FileAddAction.IS_EXECUTABLE_BY, user));
    }

    @Test
    public void testGetMissingRights() {

        FileAddAction action = createAction(file, ADD_FILE_PATH);
        actuallyTestGetMissingRights(action);
    }

    @Test
    public void testFileSystemGetMissingRights() {

        FileAddAction action = fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, ADD_FILE_PATH);
        actuallyTestGetMissingRights(action);
    }

    private void actuallyTestGetMissingRights(FileAddAction action) {

        // Add the directory that would hold the actual file (we need to modify its rights later on)
        Directory parentFile = new Directory();
        parentFile.setObj(File.OWNER, user);
        createAction(parentFile, ADD_FILE_PARENT_PATH).invoke(FileAddAction.EXECUTE);

        // Test 1
        parentFile.setObj(File.RIGHTS, new FileRights("u:w"));
        assertEquals("Missing file rights map with write right on parent dir", new HashMap<>(), prepareMissingRightsMap(action.invoke(FileAddAction.GET_MISSING_RIGHTS, user)));

        // Test 2
        parentFile.setObj(File.RIGHTS, new FileRights());
        Map<File<?>, Character[]> test2Result = new HashMap<>();
        test2Result.put(parentFile, new Character[] { FileRights.WRITE });
        assertEquals("Missing file rights map without write right on parent dir", prepareMissingRightsMap(test2Result), prepareMissingRightsMap(action.invoke(FileAddAction.GET_MISSING_RIGHTS, user)));
    }

}
