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

package com.quartercode.disconnected.test.world.comp.program.general;

import org.junit.Before;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.program.ProcessModule;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.gen.WorldGenerator;

public abstract class AbstractProgramTest {

    private final String      fileSystemMountpoint;

    protected Computer        computer;
    protected OperatingSystem os;
    protected ProcessModule   processModule;
    protected FileSystem      fileSystem;

    public AbstractProgramTest(String fileSystemMountpoint) {

        this.fileSystemMountpoint = fileSystemMountpoint;
    }

    @Before
    public void setUp() {

        computer = WorldGenerator.generateComputer();

        os = computer.get(Computer.OS).get();
        os.get(OperatingSystem.SET_RUNNING).invoke(true);

        processModule = os.get(OperatingSystem.PROC_MODULE).get();

        FileSystemModule fsModule = os.get(OperatingSystem.FS_MODULE).get();
        for (KnownFileSystem knownFs : fsModule.get(FileSystemModule.KNOWN_FS).get()) {
            if (knownFs.get(KnownFileSystem.MOUNTPOINT).get().equals(fileSystemMountpoint)) {
                fileSystem = knownFs.get(KnownFileSystem.FILE_SYSTEM).get();
            }
        }
    }

    protected Program createProgram(Class<? extends ProgramExecutor> executorClass, int majorVersion, int minorVersion, int revisionVersion) {

        Program program = new Program();
        program.get(Program.EXECUTOR_CLASS).set(executorClass);

        Version version = new Version();
        version.get(Version.MAJOR).set(majorVersion);
        version.get(Version.MINOR).set(minorVersion);
        version.get(Version.REVISION).set(revisionVersion);
        program.get(Program.VERSION).set(version);

        return program;
    }

    protected ContentFile addProgram(FileSystem fileSystem, Program program, String path) {

        ContentFile file = new ContentFile();
        file.get(ContentFile.CONTENT).set(program);
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, path).get(FileAddAction.EXECUTE).invoke();
        return file;
    }

}
