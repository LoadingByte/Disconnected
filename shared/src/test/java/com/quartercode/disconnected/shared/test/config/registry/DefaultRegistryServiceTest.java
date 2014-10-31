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

package com.quartercode.disconnected.shared.test.config.registry;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Test;
import com.quartercode.disconnected.shared.registry.DefaultRegistryService;
import com.quartercode.disconnected.shared.registry.Registry;
import com.quartercode.disconnected.shared.registry.RegistryDefinition;

public class DefaultRegistryServiceTest {

    @Test
    public void testGetRegistry() {

        DefaultRegistryService service = new DefaultRegistryService();
        RegistryDefinition<TestRegistry> registryDefinition = new RegistryDefinition<>("test", new TypeLiteral<TestRegistry>() {});

        TestRegistry registry = service.getRegistry(registryDefinition);
        assertNotNull("Returned registry is null", registry);
        assertNull("Returned registry was already filled with value", registry.value);

        registry.value = "Test";
        // Should be the same registry
        TestRegistry registry2 = service.getRegistry(registryDefinition);
        assertEquals("Results from two equal registry queries", registry, registry2);
        assertEquals("Value stored in the registry", "Test", registry.value);
    }

    protected static class TestRegistry implements Registry<String> {

        private String value;

        public TestRegistry() {

        }

        @Override
        public List<String> getValues() {

            return Arrays.asList(value);
        }

    }

}
