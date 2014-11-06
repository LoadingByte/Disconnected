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
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.shared.world.comp.ByteUnit;

public class FileSystemModuleTest {

    private FileSystemModule        fsModule;
    private final FileSystem[]      fileSystems      = new FileSystem[3];
    private final KnownFileSystem[] knownFileSystems = new KnownFileSystem[3];

    @Before
    public void setUp() {

        fsModule = new FileSystemModule();

        fileSystems[0] = createFileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystems[1] = createFileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystems[2] = createFileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        knownFileSystems[0] = addKnown(fsModule, fileSystems[0], "fs1", true);
        knownFileSystems[1] = addKnown(fsModule, fileSystems[1], "fs2", true);
        knownFileSystems[2] = addKnown(fsModule, fileSystems[2], "fs3", false);
    }

    private FileSystem createFileSystem(long size) {

        FileSystem fileSystem = new FileSystem();
        fileSystem.setObj(FileSystem.SIZE, size);
        return fileSystem;
    }

    private KnownFileSystem addKnown(FileSystemModule fsModule, FileSystem fileSystem, String mountpoint, boolean mounted) {

        KnownFileSystem known = new KnownFileSystem();
        known.setObj(KnownFileSystem.FILE_SYSTEM, fileSystem);
        known.setObj(KnownFileSystem.MOUNTPOINT, mountpoint);

        fsModule.addToColl(FileSystemModule.KNOWN_FS, known);
        known.setObj(KnownFileSystem.MOUNTED, mounted);

        return known;
    }

    @Test
    public void testGetKnown() {

        List<KnownFileSystem> expected = Arrays.asList(knownFileSystems);
        assertEquals("Known file systems", expected, fsModule.getColl(FileSystemModule.KNOWN_FS));
    }

    @Test
    public void testGetMountedByMountpoint() {

        assertEquals("Mounted file system fs1", fileSystems[0], fsModule.invoke(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT, "fs1").getObj(KnownFileSystem.FILE_SYSTEM));
        assertEquals("Mounted file system fs2", fileSystems[1], fsModule.invoke(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT, "fs2").getObj(KnownFileSystem.FILE_SYSTEM));
        // File system 3 isn't mounted
        assertEquals("No mounted file system fs3", null, fsModule.invoke(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT, "fs3"));
    }

    @Test (expected = IllegalStateException.class)
    public void testMountSameMountpoint() {

        FileSystem fileSystem = new FileSystem();
        addKnown(fsModule, fileSystem, "fs1", true);
    }

    @Test
    public void testGetFile() {

        File<?> file = new ContentFile();
        fileSystems[0].invoke(FileSystem.CREATE_ADD_FILE, file, "some/path/to/file").invoke(FileAddAction.EXECUTE);

        assertEquals("Added file", file, fsModule.invoke(FileSystemModule.GET_FILE, "/fs1/some/path/to/file"));
        assertEquals("No added file", null, fsModule.invoke(FileSystemModule.GET_FILE, "/fs2/some/path/to/file"));
    }

    @Test
    public void testAddFile() {

        File<?> file = new ContentFile();
        fsModule.invoke(FileSystemModule.CREATE_ADD_FILE, file, "/fs1/some/path/to/file").invoke(FileAddAction.EXECUTE);

        assertEquals("Added file", file, fileSystems[0].invoke(FileSystem.GET_FILE, "some/path/to/file"));
        assertEquals("No added file", null, fileSystems[1].invoke(FileSystem.GET_FILE, "some/path/to/file"));
    }

}
