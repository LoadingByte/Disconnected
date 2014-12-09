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
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FSModule;
import com.quartercode.disconnected.server.world.comp.file.FSModule.KnownFS;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.shared.world.comp.ByteUnit;

public class FSModuleTest {

    private FSModule           fsModule;
    private final FileSystem[] fileSystems = new FileSystem[3];
    private final KnownFS[]    knownFS     = new KnownFS[3];

    @Before
    public void setUp() {

        fsModule = new FSModule();

        fileSystems[0] = createFileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystems[1] = createFileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystems[2] = createFileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        knownFS[0] = addKnownFS(fsModule, fileSystems[0], "fs1", true);
        knownFS[1] = addKnownFS(fsModule, fileSystems[1], "fs2", true);
        knownFS[2] = addKnownFS(fsModule, fileSystems[2], "fs3", false);
    }

    private FileSystem createFileSystem(long size) {

        FileSystem fileSystem = new FileSystem();
        fileSystem.setObj(FileSystem.SIZE, size);
        return fileSystem;
    }

    private KnownFS addKnownFS(FSModule fsModule, FileSystem fileSystem, String mountpoint, boolean mounted) {

        KnownFS known = new KnownFS();
        known.setObj(KnownFS.FILE_SYSTEM, fileSystem);
        known.setObj(KnownFS.MOUNTPOINT, mountpoint);

        fsModule.addToColl(FSModule.KNOWN_FS, known);
        known.setObj(KnownFS.MOUNTED, mounted);

        return known;
    }

    @Test
    public void testGetKnownFS() {

        List<KnownFS> expected = Arrays.asList(knownFS);
        assertEquals("Known file systems", expected, fsModule.getColl(FSModule.KNOWN_FS));
    }

    @Test
    public void testGetMountedByMountpoint() {

        assertEquals("Mounted file system fs1", fileSystems[0], fsModule.invoke(FSModule.GET_MOUNTED_BY_MOUNTPOINT, "fs1").getObj(KnownFS.FILE_SYSTEM));
        assertEquals("Mounted file system fs2", fileSystems[1], fsModule.invoke(FSModule.GET_MOUNTED_BY_MOUNTPOINT, "fs2").getObj(KnownFS.FILE_SYSTEM));
        // File system 3 isn't mounted
        assertEquals("No mounted file system fs3", null, fsModule.invoke(FSModule.GET_MOUNTED_BY_MOUNTPOINT, "fs3"));
    }

    @Test (expected = IllegalStateException.class)
    public void testMountSameMountpoint() {

        FileSystem fileSystem = new FileSystem();
        addKnownFS(fsModule, fileSystem, "fs1", true);
    }

    @Test
    public void testGetFile() {

        File<?> file = new ContentFile();
        fileSystems[0].invoke(FileSystem.CREATE_ADD_FILE, file, "some/path/to/file").invoke(FileAddAction.EXECUTE);

        assertEquals("Added file", file, fsModule.invoke(FSModule.GET_FILE, "/fs1/some/path/to/file"));
        assertEquals("No added file", null, fsModule.invoke(FSModule.GET_FILE, "/fs2/some/path/to/file"));
    }

    @Test
    public void testAddFile() {

        File<?> file = new ContentFile();
        fsModule.invoke(FSModule.CREATE_ADD_FILE, file, "/fs1/some/path/to/file").invoke(FileAddAction.EXECUTE);

        assertEquals("Added file", file, fileSystems[0].invoke(FileSystem.GET_FILE, "some/path/to/file"));
        assertEquals("No added file", null, fileSystems[1].invoke(FileSystem.GET_FILE, "some/path/to/file"));
    }

}
