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

package com.quartercode.disconnected.server.test.world.comp;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.bridge.SBPIdentityExtension;
import com.quartercode.disconnected.server.identity.SBPIdentityService;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.sim.gen.WorldGenerator;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistry;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.prog.ChildProcess;
import com.quartercode.disconnected.server.world.comp.prog.ProcessModule;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.prog.RootProcess;
import com.quartercode.disconnected.server.world.comp.prog.Session;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.CommonBootstrap;
import com.quartercode.disconnected.shared.identity.ClientIdentity;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.util.ValueInjector;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.world.comp.file.SeparatedPath;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;

public abstract class AbstractComplexComputerTest {

    protected static final SBPIdentity SBP = new ClientIdentity("client");

    @BeforeClass
    public static void setUpBeforeClassInternal() {

        CommonBootstrap.bootstrap();
    }

    @Rule
    public JUnitRuleMockery      context = new JUnitRuleMockery();

    @Mock
    protected SBPIdentityService sbpIdentityService;
    protected Bridge             bridge;
    protected SchedulerRegistry  schedulerRegistry;
    protected World              world;

    protected Computer           mainComputer;

    @Before
    public void setUpInternal() {

        // @formatter:off
        context.checking(new Expectations() {{

            // Use null BridgeConnector because no bridge connector is used for the test
            allowing(sbpIdentityService).getIdentity(null);
                will(returnValue(SBP));

        }});
        // @formatter:on

        bridge = EventBridgeFactory.create(Bridge.class);
        bridge.addModule(EventBridgeFactory.create(ReturnEventExtensionRequester.class));
        bridge.addModule(EventBridgeFactory.create(ReturnEventExtensionReturner.class));
        bridge.addModule(EventBridgeFactory.create(SBPIdentityExtension.class));
        bridge.addModule(EventBridgeFactory.create(SBPAwareHandlerExtension.class));

        bridge.getModule(SBPIdentityExtension.class).setIdentityService(sbpIdentityService);
        bridge.getModule(SBPAwareHandlerExtension.class).setIdentityService(sbpIdentityService);

        schedulerRegistry = new SchedulerRegistry();

        world = new World();

        ValueInjector worldValueInjector = new ValueInjector();
        worldValueInjector.put("bridge", bridge);
        worldValueInjector.put("schedulerRegistry", schedulerRegistry);
        worldValueInjector.inject(world);

        mainComputer = newComputer(true);
    }

    protected Computer newComputer(boolean start) {

        mainComputer = WorldGenerator.generateComputer(false);
        world.addComputer( mainComputer);

        if (start) {
            mainComputer.getOs().invoke(OperatingSystem.SET_RUNNING, true);
        }

        return mainComputer;
    }

    // Utility methods

    protected SeparatedPath getCommonLocation(Class<?> programExecutor) {

        for (WorldProgram program : Registries.get(ServerRegistries.WORLD_PROGRAMS)) {
            if (program.getType() == programExecutor) {
                return program.getCommonLocation();
            }
        }

        return null;
    }

    protected ChildProcess launchProgram(Process<?> parentProcess, ContentFile programFile) {

        Computer computer = parentProcess.invoke(Process.GET_OS).getParent();

        ChildProcess process = parentProcess.invoke(Process.CREATE_CHILD);
        process.setObj(Process.SOURCE, programFile);
        process.setObj(Process.WORLD_PROCESS_USER, new SBPWorldProcessUserId(SBP, null));
        process.invoke(Process.INITIALIZE, procModule(computer).invoke(ProcessModule.NEXT_PID));

        ProgramExecutor program = process.getObj(Process.EXECUTOR);
        program.invoke(ProgramExecutor.RUN);

        return process;
    }

    protected ChildProcess launchProgram(Process<?> parentProcess, String programFilePath) {

        Computer computer = parentProcess.invoke(Process.GET_OS).getParent();
        return launchProgram(parentProcess, (ContentFile) fsModule(computer).invoke(FileSystem.GET_FILE, programFilePath));
    }

    protected ChildProcess launchProgram(Process<?> parentProcess, SeparatedPath programFilePath) {

        return launchProgram(parentProcess, programFilePath.toString());
    }

    protected ChildProcess launchSession(Process<?> parentProcess, User user, String password) {

        Computer computer = parentProcess.invoke(Process.GET_OS).getParent();

        ChildProcess process = parentProcess.invoke(Process.CREATE_CHILD);
        ContentFile programFile = (ContentFile) fsModule(computer).invoke(FileSystemModule.GET_FILE, getCommonLocation(Session.class).toString());
        process.setObj(Process.SOURCE, programFile);
        process.invoke(Process.INITIALIZE, procModule(computer).invoke(ProcessModule.NEXT_PID));

        ProgramExecutor session = process.getObj(Process.EXECUTOR);
        session.setObj(Session.USER, user);
        session.setObj(Session.PASSWORD, password);
        session.invoke(ProgramExecutor.RUN);

        return process;
    }

    // Computer getter shortcuts

    protected OperatingSystem os(Computer computer) {

        return computer.getOs();
    }

    protected OperatingSystem mainOs() {

        return os(mainComputer);
    }

    protected FileSystemModule fsModule(Computer computer) {

        return os(computer).getObj(OperatingSystem.FS_MODULE);
    }

    protected FileSystemModule mainFsModule() {

        return fsModule(mainComputer);
    }

    protected FileSystem fs(Computer computer, String mountpoint) {

        return fsModule(computer).invoke(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT, mountpoint).getObj(KnownFileSystem.FILE_SYSTEM);
    }

    protected FileSystem mainFs(String mountpoint) {

        return fs(mainComputer, mountpoint);
    }

    protected ProcessModule procModule(Computer computer) {

        return computer.getOs().getObj(OperatingSystem.PROC_MODULE);
    }

    protected ProcessModule mainProcModule() {

        return procModule(mainComputer);
    }

    protected RootProcess rootProcess(Computer computer) {

        return procModule(computer).getObj(ProcessModule.ROOT_PROCESS);
    }

    protected RootProcess mainRootProcess() {

        return rootProcess(mainComputer);
    }

    protected NetworkModule netModule(Computer computer) {

        return computer.getOs().getObj(OperatingSystem.NET_MODULE);
    }

    protected NetworkModule mainNetModule() {

        return netModule(mainComputer);
    }

    // Local getter shortcuts

    protected ProgramExecutor progExecutor(Process<?> process) {

        return process.getObj(Process.EXECUTOR);
    }

}
