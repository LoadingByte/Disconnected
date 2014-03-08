
package com.quartercode.disconnected.test.world.comp.file;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.file.FileSystemModule.KnownFileSystem;

public class FileSystemModuleTest {

    private FileSystemModule        fsModule;
    private final FileSystem[]      fileSystems      = new FileSystem[3];
    private final KnownFileSystem[] knownFileSystems = new KnownFileSystem[3];

    @Before
    public void setUp() throws FunctionExecutionException {

        fsModule = new FileSystemModule();

        fileSystems[0] = createFileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystems[1] = createFileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        fileSystems[2] = createFileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        knownFileSystems[0] = addKnown(fsModule, fileSystems[0], "fs1", true);
        knownFileSystems[1] = addKnown(fsModule, fileSystems[1], "fs2", true);
        knownFileSystems[2] = addKnown(fsModule, fileSystems[2], "fs3", false);
    }

    private FileSystem createFileSystem(long size) throws FunctionExecutionException {

        FileSystem fileSystem = new FileSystem();
        fileSystem.setLocked(false);
        fileSystem.get(FileSystem.SET_SIZE).invoke(size);
        fileSystem.setLocked(true);
        return fileSystem;
    }

    private KnownFileSystem addKnown(FileSystemModule fsModule, FileSystem fileSystem, String mountpoint, boolean mounted) throws FunctionExecutionException {

        KnownFileSystem known = new KnownFileSystem();
        known.setLocked(false);
        known.get(KnownFileSystem.SET_FILE_SYSTEM).invoke(fileSystem);
        known.setLocked(true);
        known.get(KnownFileSystem.SET_MOUNTPOINT).invoke(mountpoint);

        fsModule.get(FileSystemModule.ADD_KNOWN).invoke(known);
        known.get(KnownFileSystem.SET_MOUNTED).invoke(mounted);

        return known;
    }

    @Test
    public void testGetKnown() throws FunctionExecutionException {

        Set<KnownFileSystem> expected = new HashSet<KnownFileSystem>(Arrays.asList(knownFileSystems));
        Assert.assertEquals("Known file systems", expected, fsModule.get(FileSystemModule.GET_KNOWN).invoke());
    }

    @Test
    public void testGetMountedByMountpoint() throws FunctionExecutionException {

        Assert.assertEquals("Mounted file system fs1", fileSystems[0], fsModule.get(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT).invoke("fs1").get(KnownFileSystem.GET_FILE_SYSTEM).invoke());
        Assert.assertEquals("Mounted file system fs2", fileSystems[1], fsModule.get(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT).invoke("fs2").get(KnownFileSystem.GET_FILE_SYSTEM).invoke());
        // File system 3 isn't mounted
        Assert.assertEquals("No mounted file system fs3", null, fsModule.get(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT).invoke("fs3"));
    }

    @Test (expected = FunctionExecutionException.class)
    public void testMountSameMountpoint() throws FunctionExecutionException {

        FileSystem fileSystem = new FileSystem();
        addKnown(fsModule, fileSystem, "fs1", true);
    }

    @Test
    public void testGetFile() throws FunctionExecutionException {

        File<?> file = new ContentFile();
        fileSystems[0].get(FileSystem.ADD_FILE).invoke(file, "some/path/to/file");

        Assert.assertEquals("Added file", file, fsModule.get(FileSystemModule.GET_FILE).invoke("/fs1/some/path/to/file"));
        Assert.assertEquals("No added file", null, fsModule.get(FileSystemModule.GET_FILE).invoke("/fs2/some/path/to/file"));
    }

    @Test
    public void testAddFile() throws FunctionExecutionException {

        File<?> file = new ContentFile();
        fsModule.get(FileSystemModule.ADD_FILE).invoke(file, "/fs1/some/path/to/file");

        Assert.assertEquals("Added file", file, fileSystems[0].get(FileSystem.GET_FILE).invoke("some/path/to/file"));
        Assert.assertEquals("No added file", null, fileSystems[1].get(FileSystem.GET_FILE).invoke("some/path/to/file"));
    }

}
