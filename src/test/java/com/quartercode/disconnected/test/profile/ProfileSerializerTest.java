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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.Main;
import com.quartercode.disconnected.profile.ProfileSerializer;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.run.util.SimulationGenerator;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.util.Registry;
import com.quartercode.disconnected.util.ResourceStore;

public class ProfileSerializerTest {

    private Simulation simulation;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {

        Disconnected.setRegistry(new Registry());
        Main.fillRegistry(Disconnected.getRegistry());

        Disconnected.setRS(new ResourceStore());
        Main.fillResourceStore(Disconnected.getRS());
    }

    @Before
    public void setUp() {

        simulation = SimulationGenerator.generateSimulation(10, 2, new RandomPool(Simulation.RANDOM_POOL_SIZE));
    }

    @Test
    public void testSerializeEquals() throws IOException, JAXBException {

        StringWriter serialized = new StringWriter();
        WriterOutputStream outputStream = new WriterOutputStream(serialized);
        ProfileSerializer.serializeSimulation(outputStream, simulation);
        outputStream.flush();

        Simulation copy = ProfileSerializer.deserializeSimulation(new ReaderInputStream(new StringReader(serialized.toString())));
        Assert.assertEquals("Simulation equals serialized-deserialized copy", simulation, copy);
    }

}
