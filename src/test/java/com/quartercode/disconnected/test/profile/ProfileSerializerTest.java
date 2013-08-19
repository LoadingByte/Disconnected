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

package com.quartercode.disconnected.test.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.profile.ProfileSerializer;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.run.util.SimulationGenerator;

public class ProfileSerializerTest {

    private Simulation simulation;

    @Before
    public void setUp() {

        simulation = SimulationGenerator.generateSimulation(10, 2);
    }

    @Test
    public void testSerializeEquals() throws IOException {

        store(simulation, new File("test1.xml"));

        StringWriter serialized = new StringWriter();
        WriterOutputStream outputStream = new WriterOutputStream(serialized);
        ProfileSerializer.serialize(outputStream, simulation);
        outputStream.close();

        Simulation copy = ProfileSerializer.deserialize(new ReaderInputStream(new StringReader(serialized.toString())));
        store(copy, new File("test2.xml"));
        Assert.assertEquals("Simulation equals serialized-deserialized copy", simulation, copy);
    }

    private void store(Simulation simulation, File file) throws IOException {

        file.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(file);
        ProfileSerializer.serialize(outputStream, simulation);
        outputStream.close();
    }

}
