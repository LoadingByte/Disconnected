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

import static com.quartercode.disconnected.server.world.comp.program.ProgramUtils.getCommonLocation;
import static com.quartercode.disconnected.shared.util.PathUtils.getComponents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.test.world.comp.program.AbstractProgramTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.os.CommonFiles;
import com.quartercode.disconnected.server.world.comp.program.ChildProcess;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils.ImportantData;
import com.quartercode.disconnected.server.world.comp.program.general.FileManagerProgram;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramGetCurrentPathRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramGetCurrentPathRequestEvent.FileManagerProgramGetCurrentPathReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramInvalidPathEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramSetCurrentPathRequestEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramSetCurrentPathRequestEvent.FileManagerProgramSetCurrentPathSuccessReturnEvent;
import com.quartercode.disconnected.shared.event.comp.program.general.FileManagerProgramUnknownMountpointEvent;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;

public class FileManagerProgramGetAndSetCurrentPathTest extends AbstractProgramTest {

    private static final String LOCAL_PATH_1 = "test1/test2";
    private static final String PATH_1       = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/" + LOCAL_PATH_1;
    private static final String LOCAL_PATH_2 = LOCAL_PATH_1 + "/test.txt";
    private static final String PATH_2       = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/" + LOCAL_PATH_2;
    private static final String PATH_3       = "/" + CommonFiles.SYSTEM_MOUNTPOINT + "/test3";

    public FileManagerProgramGetAndSetCurrentPathTest() {

        super(CommonFiles.SYSTEM_MOUNTPOINT);
    }

    @Before
    public void setUp2() {

        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(new ContentFile(), LOCAL_PATH_2).get(FileAddAction.EXECUTE).invoke();
    }

    private ImportantData executeProgram(Process<?> parentProcess) {

        ChildProcess process = parentProcess.get(Process.CREATE_CHILD).invoke();
        process.get(Process.SOURCE).set((ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke(getComponents(getCommonLocation(FileManagerProgram.class))[1]));
        process.get(Process.INITIALIZE).invoke(10);

        ProgramExecutor program = process.get(Process.EXECUTOR).get();
        program.get(ProgramExecutor.RUN).invoke();

        return ProgramUtils.getImportantData(program);
    }

    private void sendGetRequest(ImportantData data, EventHandler<?> returnHandler) {

        Event request = new FileManagerProgramGetCurrentPathRequestEvent(data.getComputerId(), data.getPid());
        bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(request, returnHandler);
    }

    private void sendSetRequest(ImportantData data, String path, EventHandler<?> returnHandler) {

        Event request = new FileManagerProgramSetCurrentPathRequestEvent(data.getComputerId(), data.getPid(), path);
        bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(request, returnHandler);
    }

    @Test
    public void testSetAndGetSuccess() {

        ImportantData data = executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get());

        sendSetRequest(data, PATH_1, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return set success event", event instanceof FileManagerProgramSetCurrentPathSuccessReturnEvent);
            }

        });

        sendGetRequest(data, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return get return event", event instanceof FileManagerProgramGetCurrentPathReturnEvent);
                assertEquals("Current path", PATH_1, ((FileManagerProgramGetCurrentPathReturnEvent) event).getPath());
            }

        });
    }

    @Test
    public void testSetSuccessWithAbsoluteRoot() {

        sendSetRequest(executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get()), "/", new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return success event", event instanceof FileManagerProgramSetCurrentPathSuccessReturnEvent);
            }

        });
    }

    @Test
    public void testSetSuccessWithRootFiles() {

        sendSetRequest(executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get()), CommonFiles.SYSTEM_MOUNTPOINT, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return success event", event instanceof FileManagerProgramSetCurrentPathSuccessReturnEvent);
            }

        });
    }

    @Test
    public void testUnknownMountpoint() {

        sendSetRequest(executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get()), "/testunknown/" + LOCAL_PATH_1, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return unknown mountpoint event", event instanceof FileManagerProgramUnknownMountpointEvent);
                assertEquals("Unknown mountpoint", "testunknown", ((FileManagerProgramUnknownMountpointEvent) event).getMountpoint());
            }

        });
    }

    @Test
    public void testInvalidPathWithContentFilePath() {

        sendSetRequest(executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get()), PATH_2, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return invalid path event", event instanceof FileManagerProgramInvalidPathEvent);
                assertEquals("Invalid path", PATH_2, ((FileManagerProgramInvalidPathEvent) event).getPath());
            }

        });
    }

    @Test
    public void testInvalidPathWithUnexistingPath() {

        sendSetRequest(executeProgram(processModule.get(ProcessModule.ROOT_PROCESS).get()), PATH_3, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                assertTrue("File manager program did not return invalid path event", event instanceof FileManagerProgramInvalidPathEvent);
                assertEquals("Invalid path", PATH_3, ((FileManagerProgramInvalidPathEvent) event).getPath());
            }

        });
    }

}
