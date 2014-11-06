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

package com.quartercode.disconnected.shared.util.registry.extra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A {@link MappedValueRegistry} that uses two {@link HashMap}s as internal data structures.
 * 
 * @param <L> The type of the left values, which are mapped to the right values.
 * @param <R> The type of the right values, which are mapped to the left values.
 * @see MappedValueRegistry
 */
public class MapRegistry<L, R> implements MappedValueRegistry<L, R> {

    private final Map<L, R>     leftToRight = new HashMap<>();
    private final Map<R, L>     rightToLeft = new HashMap<>();
    private List<Mapping<L, R>> listCache;

    @Override
    public L getLeft(R right) {

        return rightToLeft.get(right);
    }

    @Override
    public R getRight(L left) {

        return leftToRight.get(left);
    }

    @Override
    public List<Mapping<L, R>> getValues() {

        // Update the list cache
        if (listCache == null) {
            listCache = new ArrayList<>();
            for (Entry<L, R> entry : leftToRight.entrySet()) {
                listCache.add(new DefaultMapping<>(entry.getKey(), entry.getValue()));
            }
            listCache = Collections.unmodifiableList(listCache);
        }

        return listCache;
    }

    @Override
    public void addMapping(L left, R right) {

        // Clear any old mappings with the same left or right value
        removeLeft(left);
        removeRight(right);

        // Add the new mapping to both maps
        leftToRight.put(left, right);
        rightToLeft.put(right, left);

        // Invalidate the list cache
        listCache = null;
    }

    @Override
    public void addValue(Mapping<L, R> value) {

        addMapping(value.getLeft(), value.getRight());
    }

    @Override
    public void removeLeft(L left) {

        rightToLeft.remove(getRight(left));
        leftToRight.remove(left);

        // Invalidate the list cache
        listCache = null;
    }

    @Override
    public void removeRight(R right) {

        leftToRight.remove(getLeft(right));
        rightToLeft.remove(right);

        // Invalidate the list cache
        listCache = null;
    }

    @Override
    public void removeValue(Mapping<L, R> value) {

        removeLeft(value.getLeft());
        removeRight(value.getRight());
    }

    public static class DefaultMapping<L, R> implements Mapping<L, R> {

        private final L left;
        private final R right;

        public DefaultMapping(L left, R right) {

            this.left = left;
            this.right = right;
        }

        @Override
        public L getLeft() {

            return left;
        }

        @Override
        public L getKey() {

            return left;
        }

        @Override
        public String getName() {

            return left instanceof NamedValue ? ((NamedValue) left).getName() : String.valueOf(left);
        }

        @Override
        public R getRight() {

            return right;
        }

        @Override
        public R getValue() {

            return right;
        }

        @Override
        public R setValue(R value) {

            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {

            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {

            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString() {

            return ToStringBuilder.reflectionToString(this);
        }

    }

}
