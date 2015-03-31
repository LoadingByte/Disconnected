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

package com.quartercode.disconnected.server.util;

import java.util.Random;

/**
 * This utility class can prepare probability values as well as generate results out of them.
 * The preparation calculates a valid probability value out of a pseudo one.
 * For example, if your pseudo value is 1.5, you will get 1, in the case of -2 you will get 0.
 * The generation generates a random boolean result which is true if the probability is hit.
 * The distribution of the results is equal to the probability.
 */
public class ProbabilityUtils {

    /**
     * Calculates a valid probability value out of a pseudo one.
     * For example, if your pseudo value is 1.5, you will get 1, in the case of -2 you will get 0.
     * 
     * @param pseudoProbability The pseudo probability (may be smaller than 0 or larger than 1).
     * @return The calculated valid probability.
     */
    public static float prepare(float pseudoProbability) {

        if (pseudoProbability < 0) {
            return 0;
        } else if (pseudoProbability > 1) {
            return 1;
        } else {
            return pseudoProbability;
        }
    }

    /**
     * Generates a random boolean result which is true if the probability is hit using the given random number generator.
     * The distribution of the results is equal to the probability.
     * 
     * @param probability The probability to use for generation.
     * @param random The random number generator to use for generating a random float.
     * @return The generated random boolean result.
     */
    public static boolean gen(float probability, Random random) {

        return random.nextFloat() < probability;
    }

    /**
     * Generates a random boolean result which is true if the probability is hit using the given random number generator.
     * The probability will get prepared before calculation.
     * The distribution of the results is equal to the probability.
     * 
     * @param pseudoProbability The pseudo probability to use for generation. It will get prepared before calculation.
     * @param random The random number generator to use for generating a random float.
     * @return The generated random boolean result.
     */
    public static boolean genPseudo(float pseudoProbability, Random random) {

        return gen(prepare(pseudoProbability), random);
    }

    private ProbabilityUtils() {

    }

}
