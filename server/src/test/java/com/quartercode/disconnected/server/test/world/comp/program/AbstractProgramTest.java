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

package com.quartercode.disconnected.server.test.world.comp.program;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import com.quartercode.disconnected.server.ServerInitializer;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.bridge.SBPIdentityExtension;
import com.quartercode.disconnected.server.identity.SBPIdentityService;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.gen.WorldGenerator;
import com.quartercode.disconnected.shared.SharedInitializer;
import com.quartercode.disconnected.shared.comp.file.SeparatedPath;
import com.quartercode.disconnected.shared.identity.ClientIdentity;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.registry.Registries;
import com.quartercode.disconnected.shared.registrydef.SharedRegistries;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;

public abstract class AbstractProgramTest {

    protected static final SBPIdentity SBP = new ClientIdentity("client");

    @BeforeClass
    public static void setUpBeforeClass() {

        SharedInitializer.initialize();
        ServerInitializer.initialize();
        SharedInitializer.initializeFinal();
    }

    @Rule
    public JUnitRuleMockery      context = new JUnitRuleMockery();

    protected final String       fileSystemMountpoint;

    @Mock
    protected SBPIdentityService sbpIdentityService;
    protected Bridge             bridge;
    protected World              world;
    protected Computer           computer;
    protected OperatingSystem    os;
    protected ProcessModule      processModule;
    protected FileSystem         fileSystem;

    protected AbstractProgramTest(String fileSystemMountpoint) {

        this.fileSystemMountpoint = fileSystemMountpoint;
    }

    @Before
    public void setUp() {

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

        world = new World();
        world.injectBridge(bridge);

        computer = WorldGenerator.generateComputer(false);
        world.addCol(World.COMPUTERS, computer);

        os = computer.getObj(Computer.OS);
        os.invoke(OperatingSystem.SET_RUNNING, true);

        processModule = os.getObj(OperatingSystem.PROC_MODULE);

        FileSystemModule fsModule = os.getObj(OperatingSystem.FS_MODULE);
        for (KnownFileSystem knownFs : fsModule.getCol(FileSystemModule.KNOWN_FS)) {
            if (knownFs.getObj(KnownFileSystem.MOUNTPOINT).equals(fileSystemMountpoint)) {
                fileSystem = knownFs.getObj(KnownFileSystem.FILE_SYSTEM);
                break;
            }
        }
    }

    protected SeparatedPath getCommonLocation(Class<?> programExecutor) {

        String programName = null;
        for (WorldProgram program : Registries.get(ServerRegistries.WORLD_PROGRAMS).getValues()) {
            if (program.getType() == programExecutor) {
                programName = program.getName();
            }
        }

        return Registries.get(SharedRegistries.WORLD_PROGRAM_COMLOCS).getRight(programName);
    }

}
