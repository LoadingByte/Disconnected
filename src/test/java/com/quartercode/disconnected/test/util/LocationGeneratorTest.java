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

package com.quartercode.disconnected.test.util;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.sim.comp.Location;
import com.quartercode.disconnected.util.LocationGenerator;

public class LocationGeneratorTest {

    @Test
    public void testGenerateLocations() {

        List<Location> locations = LocationGenerator.generateLocations(100);
        for (Location location : locations) {
            for (Location location2 : locations) {
                if (location != location2 && location.equals(location2)) {
                    Assert.fail("Location is duplicate");
                }
            }
        }
    }

}
