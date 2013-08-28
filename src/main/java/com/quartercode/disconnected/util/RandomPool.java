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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * A random pool stores a list of randoms for generating more independent randoms out of one object.
 * The randoms can get added manually or generated using another random which generates their seeds.
 * If a random number is requested, the next avaiable random in the list generates the number, and the index for the next generation increases by one.
 * The next time, the next random will generate the number.
 * This is especially useful for generating large fields of random numbers without any pattern. If you need a pattern, you should use the original class instead.
 * 
 * @see Random
 */
public class RandomPool {

    /**
     * This is a public random pool you can quickly access if you need random numbers and don't want to create a new pool.
     */
    public static final RandomPool PUBLIC  = new RandomPool(1000);

    private final List<Random>     randoms = new ArrayList<Random>();
    private int                    currentIndex;

    /**
     * Creates a new empty random pool.
     */
    public RandomPool() {

    }

    /**
     * Creates a new ramdom pool and adds one random to the pool.
     * 
     * @param random The random to add to the pool.
     */
    public RandomPool(Random random) {

        add(random);
    }

    /**
     * Creates a new ramdom pool and adds a collection of randoms to the pool.
     * 
     * @param randoms The collection of randoms to add to the pool.
     */
    public RandomPool(Collection<Random> randoms) {

        addAll(randoms);
    }

    /**
     * Creates a new random pool and generates the given amount of randoms which then will get added to the pool.
     * The seed for the random which then generates the other seeds is the current timestamp.
     * 
     * @param amount The amount of randoms to generate.
     */
    public RandomPool(int amount) {

        generate(amount);
    }

    /**
     * Creates a new random pool and generates the given amount of randoms which then will get added to the pool.
     * The seed for the random which then generates the other seeds is also given.
     * 
     * @param amount The amount of randoms to generate.
     * @param seed The seed for the random which then generates the other seeds.
     */
    public RandomPool(int amount, long seed) {

        generate(amount, seed);
    }

    /**
     * Adds a random to the random pool.
     * 
     * @param random The random to add to the pool.
     */
    public void add(Random random) {

        randoms.add(random);
    }

    /**
     * Adds a collection of randoms to the pool.
     * 
     * @param randoms The collection of randoms to add to the pool.
     */
    public void addAll(Collection<Random> randoms) {

        this.randoms.addAll(randoms);
    }

    /**
     * Generates the given amount of randoms which then will get added to the pool.
     * The seed for the random which then generates the other seeds is the current timestamp.
     * 
     * @param amount The amount of randoms to generate.
     */
    public void generate(int amount) {

        generate(amount, System.currentTimeMillis());
    }

    /**
     * Generates the given amount of randoms which then will get added to the pool.
     * The seed for the random which then generates the other seeds is also given.
     * 
     * @param amount The amount of randoms to generate.
     * @param seed The seed for the random which then generates the other seeds.
     */
    public void generate(int amount, long seed) {

        Random seedRandom = new Random(seed);

        for (int counter = 0; counter < amount; counter++) {
            randoms.add(new Random(seedRandom.nextLong()));
        }
    }

    /**
     * Selects the next random which can be used for generating one thing.
     * The returned random should only be used for only one generation.
     * 
     * @return The next random which can be used for generating one thing.
     */
    protected Random nextRandom() {

        Random next = randoms.get(currentIndex);

        currentIndex++;
        if (currentIndex >= randoms.size()) {
            currentIndex = 0;
        }

        return next;
    }

    /**
     * Fills the byte array with random bytes.
     * 
     * @see Random#nextBytes(byte[])
     * @param bytes The byte array to fill.
     */
    public void nextBytes(byte[] bytes) {

        nextRandom().nextBytes(bytes);
    }

    /**
     * Returns a random integer value.
     * 
     * @see Random#nextInt()
     * @return The generated integer value.
     */
    public int nextInt() {

        return nextRandom().nextInt();
    }

    /**
     * Returns a random integer value between 0 (inclusive) and the specified value (exclusive).
     * 
     * @see Random#nextInt(int)
     * @param n The bound on the random integer value to be returned.
     * @return The generated integer value.
     */
    public int nextInt(int n) {

        return nextRandom().nextInt(n);
    }

    /**
     * Returns a random long value. This can't return all possible long values.
     * 
     * @see Random#nextLong()
     * @return The generated long value.
     */
    public long nextLong() {

        return nextRandom().nextLong();
    }

    /**
     * Returns a random boolean value.
     * 
     * @see Random#nextBoolean()
     * @return The generated boolean value.
     */
    public boolean nextBoolean() {

        return nextRandom().nextBoolean();
    }

    /**
     * Returns a random float value between 0.0 (inclusive) and 1.0 (exclusive).
     * 
     * @see Random#nextFloat()
     * @return The generated float value.
     */
    public float nextFloat() {

        return nextRandom().nextFloat();
    }

    /**
     * Returns a random double value between 0.0 (inclusive) and 1.0 (exclusive).
     * 
     * @see Random#nextDouble()
     * @return The generated double value.
     */
    public double nextDouble() {

        return nextRandom().nextDouble();
    }

    /**
     * Returns a random Gaussian ("normally") distributed double value with mean 0.0 and standard deviation 1.0 from this random number generator's sequence.
     * 
     * @see Random#nextGaussian()
     * @return The generated Gaussian ("normally") distributed double value.
     */
    public double nextGaussian() {

        return nextRandom().nextGaussian();
    }

}
