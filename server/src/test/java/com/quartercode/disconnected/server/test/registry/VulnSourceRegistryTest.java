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

package com.quartercode.disconnected.server.test.registry;

import static com.quartercode.disconnected.server.test.ExtraAssert.assertCollectionEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.registry.VulnSource;
import com.quartercode.disconnected.server.registry.VulnSourceRegistry;

public class VulnSourceRegistryTest {

    private VulnSourceRegistry registry;

    private VulnSource         entry1;
    private VulnSource         entry2;
    private VulnSource         entry3;
    private VulnSource         entry4;
    private VulnSource         entry5;

    @Before
    public void setUp() {

        // Create a test registry
        registry = new VulnSourceRegistry();

        // Create the test entries
        List<VulnSource.Action> emptyActionList = new ArrayList<>();
        entry1 = new VulnSource("entry1", "node1", 1, emptyActionList);
        entry2 = new VulnSource("entry2", "node1.node1", 1, emptyActionList);
        entry3 = new VulnSource("entry3", "node1.node2", 1, emptyActionList);
        entry4 = new VulnSource("entry4", "node1.node2.node1", 1, emptyActionList);
        entry5 = new VulnSource("entry5", "node2", 1, emptyActionList);

        // Add the test entries to the test registry
        registry.addValue(entry1);
        registry.addValue(entry2);
        registry.addValue(entry3);
        registry.addValue(entry4);
        registry.addValue(entry5);
    }

    @Test
    public void testGetValuesByUsage() {

        // Test three times for testing the internal cache
        for (int counter = 0; counter < 3; counter++) {
            assertCollectionEquals("Wrong entries for usage node 'node1'", registry.getValuesByUsage("node1"), entry1, entry2, entry3, entry4);
            assertCollectionEquals("Wrong entries for usage nodes 'node1, node1'", registry.getValuesByUsage("node1", "node1"), entry2);
            assertCollectionEquals("Wrong entries for usage nodes 'node1, node2'", registry.getValuesByUsage("node1", "node2"), entry3, entry4);
            assertCollectionEquals("Wrong entries for usage nodes 'node1, node2, node1'", registry.getValuesByUsage("node1", "node2", "node1"), entry4);
            assertCollectionEquals("Wrong entries for usage node 'node2'", registry.getValuesByUsage("node2"), entry5);

            assertTrue("Entries were returned for for unknown usage node 'node3'", registry.getValuesByUsage("node3").isEmpty());
        }
    }

}
