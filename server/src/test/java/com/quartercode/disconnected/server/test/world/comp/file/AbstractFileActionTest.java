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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import com.quartercode.disconnected.server.world.comp.ByteUnit;
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.os.User;

public abstract class AbstractFileActionTest {

    protected FileSystem fileSystem;
    protected Directory  file;
    protected User       user;

    @Before
    public void setUp() {

        fileSystem = new FileSystem();
        fileSystem.setObj(FileSystem.SIZE, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));

        user = new User();

        file = new Directory();
        file.setObj(File.OWNER, user);
    }

    protected Map<File<?>, List<Character>> prepareMissingRightsMap(Map<File<?>, Character[]> map) {

        Map<File<?>, List<Character>> modMap = new HashMap<>();

        for (Entry<File<?>, Character[]> entry : map.entrySet()) {
            modMap.put(entry.getKey(), new ArrayList<>(Arrays.asList(entry.getValue())));
        }

        return modMap;
    }

}
