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

import static com.quartercode.disconnected.server.test.world.comp.file.FileAssert.assertInvalidPath;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.server.world.comp.file.ParentFile;
import com.quartercode.disconnected.server.world.comp.file.UnknownMountpointException;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.world.comp.ByteUnit;

public class FileSystemModuleTest {

    private FileSystemModule        fsModule;
    private final FileSystem[]      fileSystems = new FileSystem[3];
    private final KnownFileSystem[] knownFs     = new KnownFileSystem[3];

    @Before
    public void setUp() {

        fsModule = new FileSystemModule();

        fileSystems[0] = new FileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystems[1] = new FileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystems[2] = new FileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        knownFs[0] = addKnownFs(fsModule, fileSystems[0], "fs1", true);
        knownFs[1] = addKnownFs(fsModule, fileSystems[1], "fs2", true);
        knownFs[2] = addKnownFs(fsModule, fileSystems[2], "fs3", false);
    }

    private KnownFileSystem addKnownFs(FileSystemModule fsModule, FileSystem fileSystem, String mountpoint, boolean mounted) {

        KnownFileSystem known = new KnownFileSystem(fileSystem);
        known.setMountpoint(mountpoint);

        fsModule.addKnownFs(known);
        known.setMounted(mounted);

        return known;
    }

    @Test
    public void testGetKnownFs() {

        List<KnownFileSystem> expected = Arrays.asList(knownFs);
        assertArrayEquals("Known file systems", expected.toArray(), fsModule.getKnownFs().toArray());
    }

    @Test
    public void testGetMountedByMountpoint() {

        assertEquals("Mounted file system fs1", fileSystems[0], fsModule.getMountedKnownFsByMountpoint("fs1").getFileSystem());
        assertEquals("Mounted file system fs2", fileSystems[1], fsModule.getMountedKnownFsByMountpoint("fs2").getFileSystem());
        // File system 3 isn't mounted
        assertEquals("No mounted file system fs3", null, fsModule.getMountedKnownFsByMountpoint("fs3"));
    }

    @Test (expected = IllegalStateException.class)
    public void testMountSameMountpoint() {

        FileSystem fileSystem = new FileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        addKnownFs(fsModule, fileSystem, "fs1", true);
    }

    @Test
    public void testGetFile() throws InvalidPathException, OccupiedPathException, OutOfSpaceException, UnknownMountpointException {

        File<ParentFile<?>> file = new ContentFile(new User("user"));
        fileSystems[0].prepareAddFile(file, "some/path/to/file").execute();

        assertEquals("Added file", file, fsModule.getFile("/fs1/some/path/to/file"));

        try {
            fsModule.getFile("/fs2/some/path/to/file");
        } catch (InvalidPathException e) {
            // Assertion passed
            return;
        }
        fail("getFile() returned file from wrong file system");
    }

    @Test
    public void testAddFile() throws InvalidPathException, OccupiedPathException, OutOfSpaceException, UnknownMountpointException {

        File<ParentFile<?>> file = new ContentFile(new User("user"));
        fsModule.prepareAddFile(file, "/fs1/some/path/to/file").execute();

        assertEquals("Added file", file, fileSystems[0].getFile("some/path/to/file"));
        assertInvalidPath("File has been added to wrong file system", fileSystems[1], "some/path/to/file");
    }

}
