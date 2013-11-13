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

package com.quartercode.disconnected.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.sim.Location;

/**
 * This utility class generates random locations on an earth map.
 * 
 * @see Location
 */
public class LocationGenerator {

    /**
     * Generates the given amount of locations on an earth map.
     * 
     * @param amount The amount of locations to generate.
     * @param random The random pool to use for generating the random locations.
     * @return The generated locations.
     */
    public static List<Location> generateLocations(int amount, RandomPool random) {

        return generateLocations(amount, null, random);
    }

    /**
     * Generates the given amount of locations on an earth map, ignoring the given ignore locations.
     * 
     * @param amount The amount of locations to generate.
     * @param ignore The method will definiteley not return any of these locations.
     * @param random The random pool to use for generating the random locations.
     * @return The generated locations.
     */
    public static List<Location> generateLocations(int amount, List<Location> ignore, RandomPool random) {

        Validate.isTrue(amount > 0, "Generation amount must be > 0: ", amount);

        BufferedImage map = (BufferedImage) Disconnected.getRS().get("map.png");

        if (ignore == null) {
            ignore = new ArrayList<Location>();
        }

        int width = map.getWidth();
        int height = map.getHeight();

        List<Location> result = new ArrayList<Location>();
        int blackRGB = Color.BLACK.getRGB();
        while (result.size() < amount) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            if (map.getRGB(x, y) == blackRGB) {
                Location location = new Location((float) x / (float) width, (float) y / (float) height);
                if (!ignore.contains(location) && !result.contains(location)) {
                    result.add(location);
                }
            }
        }

        return result;
    }

    private LocationGenerator() {

    }

}
