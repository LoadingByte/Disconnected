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

package com.quartercode.disconnected.test.world.event;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.classmod.util.FeatureDefinitionReference;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.program.RootProcess;
import com.quartercode.disconnected.world.event.ProgramLaunchCommandEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchCommandEventHandler;

public class ProgramLaunchCommandEventHandlerTest {

    private ProgramLaunchCommandEventHandler handler;

    private Computer                         playerComputer;
    private Process<?>                       sessionProcess;
    private ContentFile                      sourceFile;

    @Before
    public void setUp() {

        playerComputer = new Computer();
        sessionProcess = new RootProcess();

        sourceFile = new ContentFile();
        Program program = new Program();
        program.get(Program.EXECUTOR_CLASS).set(TestProgram.class);
        sourceFile.get(ContentFile.CONTENT).set(program);

        handler = new ProgramLaunchCommandEventHandlerMock(playerComputer, sessionProcess, sourceFile);
    }

    @Test
    public void test() {

        Map<FeatureDefinitionReference<?>, Object> executorProperties = new HashMap<>();
        executorProperties.put(new FeatureDefinitionReference<>(TestProgram.class, "VALUE_1"), 123);
        executorProperties.put(new FeatureDefinitionReference<>(TestProgram.class, "VALUE_2"), "somestring1");
        executorProperties.put(new FeatureDefinitionReference<>(TestProgram.class, "VALUE_3"), new SomeObject(0.123456789, "somestring2"));
        executorProperties.put(new FeatureDefinitionReference<>(TestProgram.class, "VALUE_4"), null);
        ProgramLaunchCommandEvent event = new ProgramLaunchCommandEvent(10, null, executorProperties);
        handler.handle(event);

        Process<?> child = sessionProcess.get(Process.CHILDREN).get().get(0);
        Assert.assertNotNull("Child process was not created", child);
        Assert.assertEquals("Pid of child process", 10, (int) child.get(Process.PID).get());
        ProgramExecutor executor = child.get(Process.EXECUTOR).get();
        Assert.assertNotNull("Child process has no program executor", executor);
        Assert.assertTrue("Child process has an incorrect program executor (wrong type)", executor instanceof TestProgram);

        Assert.assertEquals("Property VALUE_1 of TestProgram executor", 123, (int) executor.get(TestProgram.VALUE_1).get());
        Assert.assertEquals("Property VALUE_2 of TestProgram executor", "somestring1", executor.get(TestProgram.VALUE_2).get());
        Assert.assertEquals("Property VALUE_3 of TestProgram executor", new SomeObject(0.123456789, "somestring2"), executor.get(TestProgram.VALUE_3).get());
        Assert.assertEquals("Property VALUE_4 of TestProgram executor", null, executor.get(TestProgram.VALUE_4).get());
    }

    private static class ProgramLaunchCommandEventHandlerMock extends ProgramLaunchCommandEventHandler {

        private final Computer    playerComputer;
        private final Process<?>  sessionProcess;
        private final ContentFile sourceFile;

        public ProgramLaunchCommandEventHandlerMock(Computer playerComputer, Process<?> sessionProcess, ContentFile sourceFile) {

            this.playerComputer = playerComputer;
            this.sessionProcess = sessionProcess;
            this.sourceFile = sourceFile;
        }

        @Override
        protected Computer getPlayerComputer() {

            return playerComputer;
        }

        @Override
        protected Process<?> getSessionProcess(Computer computer) {

            return sessionProcess;
        }

        @Override
        protected ContentFile getSourceFile(Computer computer, String path) {

            return sourceFile;
        }

    }

    // Must be protected in order to make newInstance() work
    protected static class TestProgram extends ProgramExecutor {

        public static final PropertyDefinition<Integer>    VALUE_1 = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "value1", "storage", new StandardStorage<>());
        public static final PropertyDefinition<String>     VALUE_2 = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "value2", "storage", new StandardStorage<>());
        public static final PropertyDefinition<SomeObject> VALUE_3 = create(new TypeLiteral<PropertyDefinition<SomeObject>>() {}, "name", "value3", "storage", new StandardStorage<>());
        public static final PropertyDefinition<String>     VALUE_4 = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "value4", "storage", new StandardStorage<>(), "initialValue", new ConstantValueFactory<>("someOtherString"));

        public TestProgram() {

        }

    }

    private static class SomeObject {

        @SuppressWarnings ("unused")
        private final double value1;
        @SuppressWarnings ("unused")
        private final String value2;

        private SomeObject(double value1, String value2) {

            this.value1 = value1;
            this.value2 = value2;
        }

        @Override
        public int hashCode() {

            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {

            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString() {

            return ToStringBuilder.reflectionToString(this);
        }

    }

}
