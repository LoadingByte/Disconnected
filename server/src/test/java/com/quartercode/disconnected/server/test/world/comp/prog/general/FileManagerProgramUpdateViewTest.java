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

package com.quartercode.disconnected.server.test.world.comp.prog.general;

import static com.quartercode.disconnected.shared.world.comp.file.PathUtils.resolve;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.AbstractComplexComputerTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.general.FileManagerProgram;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.FMP_WP_ChangeDirCommand;
import com.quartercode.disconnected.shared.world.comp.ByteUnit;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class FileManagerProgramUpdateViewTest extends AbstractComplexComputerTest {

    private static final EventPredicate<?> UPDATE_VIEW_PREDICATE = new TypePredicate<>(FMP_SBPWPU_UpdateViewCommand.class);

    private static final String            FS_MOUNTPOINT         = CommonFiles.USER_MOUNTPOINT;
    private static final String            PATH                  = "/" + FS_MOUNTPOINT + "/test1/test2";

    private final File<?>[]                testFiles             = { new ContentFile(), new Directory(), new ContentFile() };
    private WorldProcessId                 processId;

    @Before
    public void setUp() {

        for (int index = 0; index < testFiles.length; index++) {
            File<?> file = testFiles[index];
            mainFsModule().invoke(FileSystemModule.CREATE_ADD_FILE, file, resolve(PATH, "file" + index + ".txt")).invoke(FileAddAction.EXECUTE);
        }

        // Launch the program
        ChildProcess process = launchProgram(mainRootProcess(), getCommonLocation(FileManagerProgram.class));
        processId = process.invoke(Process.GET_WORLD_PROCESS_ID);
    }

    private void sendChangeDirCommand(String change) {

        bridge.send(new FMP_WP_ChangeDirCommand(processId, change));
    }

    @Test
    public void testWithDirectory() {

        final MutableBoolean invoked = new MutableBoolean();

        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FMP_SBPWPU_UpdateViewCommand>() {

            @Override
            public void handle(FMP_SBPWPU_UpdateViewCommand event) {

                assertEquals("File path", PATH, event.getCurrentDir());

                List<FilePlaceholder> expectedFiles = new ArrayList<>();
                for (File<?> file : testFiles) {
                    String path = PathUtils.resolve("/" + FS_MOUNTPOINT, file.invoke(File.GET_PATH));
                    String type = file.getClass() == Directory.class ? "directory" : "contentFile";
                    long size = file.invoke(File.GET_SIZE);
                    expectedFiles.add(new FilePlaceholder(path, type, size, File.DEFAULT_FILE_RIGHTS, null, null));
                }

                List<FilePlaceholder> returnedFiles = new ArrayList<>(Arrays.asList(event.getFiles()));
                assertEquals("Listed files", expectedFiles, returnedFiles);

                invoked.setTrue();
            }

        }, UPDATE_VIEW_PREDICATE);

        sendChangeDirCommand(PATH);

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

    @Test
    public void testWithAbsoluteRoot() {

        final MutableBoolean invoked = new MutableBoolean();

        bridge.getModule(StandardHandlerModule.class).addHandler(new EventHandler<FMP_SBPWPU_UpdateViewCommand>() {

            @Override
            public void handle(FMP_SBPWPU_UpdateViewCommand event) {

                assertEquals("File path", "/", event.getCurrentDir());

                List<FilePlaceholder> expectedFiles = new ArrayList<>();
                long terabyte = ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE);
                expectedFiles.add(new FilePlaceholder("/" + CommonFiles.SYSTEM_MOUNTPOINT, "rootFile", terabyte, File.DEFAULT_FILE_RIGHTS, null, null));
                expectedFiles.add(new FilePlaceholder("/" + CommonFiles.USER_MOUNTPOINT, "rootFile", terabyte, File.DEFAULT_FILE_RIGHTS, null, null));

                List<FilePlaceholder> returnedFiles = new ArrayList<>(Arrays.asList(event.getFiles()));
                assertEquals("Listed files", expectedFiles, returnedFiles);

                invoked.setTrue();
            }

        }, UPDATE_VIEW_PREDICATE);

        sendChangeDirCommand("/");

        assertTrue("Handler hasn't been invoked", invoked.getValue());
    }

}
