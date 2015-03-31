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

package com.quartercode.disconnected.shared.test.util.init;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Test;
import com.quartercode.disconnected.shared.util.init.Initializer;
import com.quartercode.disconnected.shared.util.init.InitializerSettings;
import com.quartercode.disconnected.shared.util.init.InitializerSorter;

public class InitializerSorterTest {

    private static final Map<String, Integer> GROUP_AMOUNTS = new HashMap<>();

    static {

        GROUP_AMOUNTS.put("1", 2);
        GROUP_AMOUNTS.put("2", 3);
        GROUP_AMOUNTS.put("3", 2);
        GROUP_AMOUNTS.put("4", 1);
        GROUP_AMOUNTS.put("5", 3);
        GROUP_AMOUNTS.put("6", 1);

    }

    @Test
    public void test() {

        List<Initializer> unsorted = new ArrayList<>();
        unsorted.add(new Initializer1());
        unsorted.add(new Initializer2_1());
        unsorted.add(new Initializer2_2());
        unsorted.add(new Initializer3());
        unsorted.add(new Initializer4());
        unsorted.add(new Initializer5_1());
        unsorted.add(new Initializer5_2());
        unsorted.add(new Initializer6());
        unsorted.add(new Initializer13());
        unsorted.add(new Initializer25());

        List<Initializer> sorted = InitializerSorter.sortByDependencies(unsorted);

        // Check
        Map<String, MutableInt> encounters = new HashMap<>();
        for (Initializer initializer : sorted) {
            InitializerSettings settings = initializer.getClass().getAnnotation(InitializerSettings.class);

            for (String dependency : settings.dependencies()) {
                boolean encounteredDependency = encounters.containsKey(dependency) && encounters.get(dependency).getValue() == GROUP_AMOUNTS.get(dependency);
                assertTrue("Wrong initializer order: '" + initializer.getClass().getName() + "' depends on yet unencountered '" + dependency + "'", encounteredDependency);
            }

            // Add encounter
            for (String group : settings.groups()) {
                if (encounters.containsKey(group)) {
                    encounters.get(group).increment();
                } else {
                    encounters.put(group, new MutableInt(1));
                }
            }
        }
    }

    private static class DummyInitializer implements Initializer {

        @Override
        public void initialize() {

        }

    }

    @InitializerSettings (groups = "1", dependencies = "3")
    private static class Initializer1 extends DummyInitializer {

    }

    @InitializerSettings (groups = "2", dependencies = { "1", "3" })
    private static class Initializer2_1 extends DummyInitializer {

    }

    @InitializerSettings (groups = "2", dependencies = "1")
    private static class Initializer2_2 extends DummyInitializer {

    }

    @InitializerSettings (groups = "3")
    private static class Initializer3 extends DummyInitializer {

    }

    @InitializerSettings (groups = "4", dependencies = "2")
    private static class Initializer4 extends DummyInitializer {

    }

    @InitializerSettings (groups = "5", dependencies = "1")
    private static class Initializer5_1 extends DummyInitializer {

    }

    @InitializerSettings (groups = "5")
    private static class Initializer5_2 extends DummyInitializer {

    }

    @InitializerSettings (groups = "6", dependencies = { "4", "5" })
    private static class Initializer6 extends DummyInitializer {

    }

    // Multiple groups

    @InitializerSettings (groups = { "1", "3" })
    private static class Initializer13 extends DummyInitializer {

    }

    @InitializerSettings (groups = { "2", "5" }, dependencies = "1")
    private static class Initializer25 extends DummyInitializer {

    }

}
