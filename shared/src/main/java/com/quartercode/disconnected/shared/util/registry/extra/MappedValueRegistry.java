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

package com.quartercode.disconnected.shared.util.registry.extra;

import java.util.Map;
import com.quartercode.disconnected.shared.util.registry.extra.MappedValueRegistry.Mapping;

/**
 * A mapped value registry is a {@link MultipleValueRegistry} that maps left values to right values (it is a simple map).
 * However, the mappings can be retrieved bidirectionally using {@link #getLeft(Object)} and {@link #getRight(Object)}.
 * New mappings can be added using {@link #addMapping(Object, Object)}.
 * Old ones can be removed with {@link #removeLeft(Object)} and {@link #removeRight(Object)}.
 * Internally, a mapped value registry is just a multiple value registry that stores {@link Mapping}s instead of custom objects.<br>
 * <br>
 * If one of the two value types is or should be handled as a {@link NamedValue}, it should be used as the left one.
 * Mappings implement the named value interface and redirect the named value call to the left object.
 * 
 * @param <L> The type of the left values, which are mapped to the right values.
 * @param <R> The type of the right values, which are mapped to the left values.
 * @see MultipleValueRegistry
 * @see Mapping
 */
public interface MappedValueRegistry<L, R> extends MultipleValueRegistry<Mapping<L, R>> {

    /**
     * Returns the left mapping for the given right object.
     * 
     * @param right The right object whose left partner should be returned.
     * @return The assigned left partner of the given right object.
     */
    public L getLeft(R right);

    /**
     * Returns the right mapping for the given left object.
     * 
     * @param left The left object whose right partner should be returned.
     * @return The assigned right partner of the given left object.
     */
    public R getRight(L left);

    /**
     * Adds the given mapping in order to make it available through the accessor methods.
     * Note that all mappings with the same left <b>or</b> right object are removed.
     * 
     * @param left The left object that should be mapped to the given right object.
     * @param right The right object that should be mapped to the given left object.
     */
    public void addMapping(L left, R right);

    /**
     * Removes the mapping that maps the given left object to some right object.
     * 
     * @param left The left object whose mapping should be removed.
     */
    public void removeLeft(L left);

    /**
     * Removes the mapping that maps the given right object to some left object.
     * 
     * @param right The right object whose mapping should be removed.
     */
    public void removeRight(R right);

    /**
     * An internal class that is used by {@link MappedValueRegistry} implementations to act as {@link MultipleValueRegistry}s.
     * The custom object type of the multiple value registry is replaced with such a mapping type.
     * Note that the all {@link #getName()} calls on a mapping object are redirected to the left object.
     * See the main mapped value registry class for more details.
     * 
     * @param <L> The type of the left value, which is mapped to the right value.
     * @param <R> The type of the right value, which is mapped to the left value.
     * @see MappedValueRegistry
     */
    public static interface Mapping<L, R> extends Map.Entry<L, R>, NamedValue {

        /**
         * Returns the left value, which is mapped to the right value.
         * 
         * @return The left value.
         */
        public L getLeft();

        /**
         * Returns the right value, which is mapped to the left value.
         * 
         * @return The right value.
         */
        public R getRight();

    }

}
