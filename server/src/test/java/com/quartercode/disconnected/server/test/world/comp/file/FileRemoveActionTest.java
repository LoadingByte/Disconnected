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
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileRemoveAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;

@RunWith (Parameterized.class)
public class FileRemoveActionTest extends AbstractFileActionTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { "test1/test2" });
        data.add(new Object[] { "test" });

        return data;
    }

    private final String        removeFilePath;
    private final String        removeFileChildPath;

    private File<ParentFile<?>> childFile;

    public FileRemoveActionTest(String removeFilePath) {

        this.removeFilePath = removeFilePath;
        removeFileChildPath = PathUtils.normalize(removeFilePath) + "/test.txt";
    }

    @Before
    public void setUp2() {

        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, removeFilePath).invoke(FileAddAction.EXECUTE);

        childFile = new ContentFile();
        childFile.setObj(File.OWNER, user);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, childFile, removeFileChildPath).invoke(FileAddAction.EXECUTE);
    }

    private FileRemoveAction createAction(File<ParentFile<?>> file) {

        FileRemoveAction action = new FileRemoveAction();
        action.setObj(FileAddAction.FILE, file);
        return action;
    }

    @Test
    public void testExecute() {

        FileRemoveAction action = createAction(file);
        actuallyTestExecute(action);
    }

    @Test
    public void testFileExecute() {

        FileRemoveAction action = file.invoke(File.CREATE_REMOVE);
        actuallyTestExecute(action);
    }

    private void actuallyTestExecute(FileRemoveAction action) {

        action.invoke(FileRemoveAction.EXECUTE);
        assertEquals("Resolved file for deleted file", null, fileSystem.invoke(FileSystem.GET_FILE, removeFilePath));
    }

    @Test
    public void testIsExecutableBy() {

        FileRemoveAction action = createAction(file);
        actuallyTestIsExecutableBy(action);
    }

    @Test
    public void testFileIsExecutableBy() {

        FileRemoveAction action = file.invoke(File.CREATE_REMOVE);
        actuallyTestIsExecutableBy(action);
    }

    private void actuallyTestIsExecutableBy(FileRemoveAction action) {

        actuallyTestIsExecutableBy(action, "", "", false);
        actuallyTestIsExecutableBy(action, "u:d", "", false);
        actuallyTestIsExecutableBy(action, "", "u:d", false);
        actuallyTestIsExecutableBy(action, "u:d", "u:d", true);
    }

    private void actuallyTestIsExecutableBy(FileRemoveAction action, String fileRights, String childFileRights, boolean expectedResult) {

        file.setObj(File.RIGHTS, new FileRights(fileRights));
        childFile.setObj(File.RIGHTS, new FileRights(childFileRights));

        boolean result = action.invoke(FileRemoveAction.IS_EXECUTABLE_BY, user);

        assertEquals("IS_EXECUTABLE_BY result", expectedResult, result);
    }

    @Test
    public void testGetMissingRights() {

        FileRemoveAction action = createAction(file);
        actuallyTestGetMissingRights(action);
    }

    @Test
    public void testFileGetMissingRights() {

        FileRemoveAction action = file.invoke(File.CREATE_REMOVE);
        actuallyTestGetMissingRights(action);
    }

    private void actuallyTestGetMissingRights(FileRemoveAction action) {

        // Test 1
        Map<File<?>, Character[]> test1Result = new HashMap<>();
        test1Result.put(file, new Character[] { FileRights.DELETE });
        test1Result.put(childFile, new Character[] { FileRights.DELETE });
        actuallyTestGetMissingRights(action, "", "", test1Result);

        // Test 2
        Map<File<?>, Character[]> test2Result = new HashMap<>();
        test2Result.put(childFile, new Character[] { FileRights.DELETE });
        actuallyTestGetMissingRights(action, "u:d", "", test2Result);

        // Test 3
        Map<File<?>, Character[]> test3Result = new HashMap<>();
        test3Result.put(file, new Character[] { FileRights.DELETE });
        actuallyTestGetMissingRights(action, "", "u:d", test3Result);

        // Test 4
        actuallyTestGetMissingRights(action, "u:d", "u:d", new HashMap<File<?>, Character[]>());
    }

    private void actuallyTestGetMissingRights(FileRemoveAction action, String fileRights, String childFileRights, Map<File<?>, Character[]> expectedResult) {

        file.setObj(File.RIGHTS, new FileRights(fileRights));
        childFile.setObj(File.RIGHTS, new FileRights(childFileRights));

        Map<File<?>, Character[]> result = action.invoke(FileRemoveAction.GET_MISSING_RIGHTS, user);

        assertEquals("Missing file rights map", prepareMissingRightsMap(expectedResult), prepareMissingRightsMap(result));
    }

}
