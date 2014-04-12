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

import org.junit.Before;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.file.Directory;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.os.User;

public abstract class AbstractFileActionTest {

    protected FileSystem fileSystem;
    protected Directory  file;
    protected User       user;

    @Before
    public void setUp() throws ExecutorInvocationException {

        fileSystem = new FileSystem();
        fileSystem.get(FileSystem.SIZE).set(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));

        user = new User();

        file = new Directory();
        file.get(File.OWNER).set(user);
    }

}
