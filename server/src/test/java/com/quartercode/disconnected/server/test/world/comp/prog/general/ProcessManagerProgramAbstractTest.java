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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.test.world.comp.AbstractComplexComputerTest;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.prog.Program;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.file.SeparatedPath;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class ProcessManagerProgramAbstractTest extends AbstractComplexComputerTest {

    protected static final EventPredicate<?> UPDATE_VIEW_PREDICATE = new TypePredicate<>(PMP_SBPWPU_UpdateViewCommand.class);

    protected static final String            TEST_PROGRAM_PATH     = "/" + CommonFiles.USER_MOUNTPOINT + "/test/testProgram.exe";

    @BeforeClass
    public static void setUpBeforeClassInternal2() {

        Registries.get(ServerRegistries.WORLD_PROGRAMS).addValue(new WorldProgram("testProgram", TestProgram.class, 0, new SeparatedPath(TEST_PROGRAM_PATH)));
    }

    @AfterClass
    public static void tearDownAfterClassInternal2() {

        Registries.get(ServerRegistries.WORLD_PROGRAMS).removeValue(NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), "testProgram"));
    }

    protected Program     testProgram;
    protected ContentFile testProgramSourceFile;

    @Before
    public void setUpInternal2() {

        // Create a test program
        testProgram = new Program();
        testProgram.setObj(Program.NAME, "testProgram");

        // Create a test program file
        testProgramSourceFile = new ContentFile();
        testProgramSourceFile.setObj(ContentFile.CONTENT, testProgram);
        mainFsModule().invoke(FileSystemModule.CREATE_ADD_FILE, testProgramSourceFile, TEST_PROGRAM_PATH).invoke(FileAddAction.EXECUTE);
    }

}
