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

package com.quartercode.disconnected.server.test.world.comp.program.general;

import static com.quartercode.disconnected.server.world.comp.program.ProgramCommonLocationMapper.getCommonLocation;
import static com.quartercode.disconnected.shared.comp.file.PathUtils.splitAfterMountpoint;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.program.AbstractProgramTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.program.ChildProcess;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.program.general.FileManagerProgram;
import com.quartercode.disconnected.shared.comp.ByteUnit;
import com.quartercode.disconnected.shared.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.comp.file.PathUtils;
import com.quartercode.disconnected.shared.comp.program.ClientProcessId;
import com.quartercode.disconnected.shared.comp.program.WorldProcessId;
import com.quartercode.disconnected.shared.event.program.general.FMPClientUpdateViewCommand;
import com.quartercode.disconnected.shared.event.program.general.FMPWorldChangeDirCommand;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramUpdateViewTest extends AbstractProgramTest {

    private static final String            PATH               = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/test1/test2";
    private static final EventPredicate<?> RESPONSE_PREDICATE = new TypePredicate<>(FMPClientUpdateViewCommand.class);

    public FileManagerProgramUpdateViewTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    private final File<?>[] testFiles = { new ContentFile(), new Directory(), new ContentFile() };
    private WorldProcessId  processId;

    @Before
    public void setUp2() {

        String localPath = splitAfterMountpoint(PATH)[1];

        for (int index = 0; index < testFiles.length; index++) {
            File<?> file = testFiles[index];
            fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, localPath + "/file" + index + ".txt").invoke(FileAddAction.EXECUTE);
        }

        // Launch the program
        ChildProcess process = processModule.getObj(ProcessModule.ROOT_PROCESS).invoke(Process.CREATE_CHILD);
        process.setObj(Process.SOURCE, (ContentFile) fileSystem.invoke(FileSystem.GET_FILE, splitAfterMountpoint(getCommonLocation(FileManagerProgram.class).toString())[1]));
        process.setObj(Process.CLIENT_PROCESS, new ClientProcessId(CLIENT, 0));
        process.invoke(Process.INITIALIZE, 10);

        ProgramExecutor program = process.getObj(Process.EXECUTOR);
        program.invoke(ProgramExecutor.RUN);

        processId = ProgramUtils.getProcessId(program);
    }

    private void sendChangeDirCommand(String change) {

        bridge.send(new FMPWorldChangeDirCommand(processId, change));
    }

    @Test
    public void testWithDirectory() {

        final MutableBoolean invoked = new MutableBoolean();

        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FMPClientUpdateViewCommand>() {

            @Override
            public void handle(FMPClientUpdateViewCommand event) {

                assertEquals("File path", PATH, event.getCurrentDir());

                List<FilePlaceholder> expectedFiles = new ArrayList<>();
                for (File<?> file : testFiles) {
                    String path = PathUtils.resolve(PathUtils.SEPARATOR + fileSystemMountpoint, file.invoke(File.GET_PATH));
                    String type = file.getClass() == Directory.class ? "directory" : "contentFile";
                    long size = file.invoke(File.GET_SIZE);
                    expectedFiles.add(new FilePlaceholder(path, type, size, File.DEFAULT_FILE_RIGHTS, null, null));
                }

                List<FilePlaceholder> returnedFiles = new ArrayList<>(Arrays.asList(event.getFiles()));
                assertEquals("Listed files", expectedFiles, returnedFiles);

                invoked.setTrue();
            }

        }, RESPONSE_PREDICATE);

        sendChangeDirCommand(PATH);

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithAbsoluteRoot() {

        final MutableBoolean invoked = new MutableBoolean();

        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FMPClientUpdateViewCommand>() {

            @Override
            public void handle(FMPClientUpdateViewCommand event) {

                assertEquals("File path", "/", event.getCurrentDir());

                List<FilePlaceholder> expectedFiles = new ArrayList<>();
                long terabyte = ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE);
                expectedFiles.add(new FilePlaceholder(PathUtils.SEPARATOR + CommonFiles.SYSTEM_MOUNTPOINT, "rootFile", terabyte, File.DEFAULT_FILE_RIGHTS, null, null));
                expectedFiles.add(new FilePlaceholder(PathUtils.SEPARATOR + CommonFiles.USER_MOUNTPOINT, "rootFile", terabyte, File.DEFAULT_FILE_RIGHTS, null, null));

                List<FilePlaceholder> returnedFiles = new ArrayList<>(Arrays.asList(event.getFiles()));
                assertEquals("Listed files", expectedFiles, returnedFiles);

                invoked.setTrue();
            }

        }, RESPONSE_PREDICATE);

        sendChangeDirCommand("/");

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

}
