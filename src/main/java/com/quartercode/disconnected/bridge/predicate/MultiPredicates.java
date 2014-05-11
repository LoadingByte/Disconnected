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

package com.quartercode.disconnected.bridge.predicate;

import lombok.Data;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.EventPredicate;

/**
 * Multi predicates combine different predicates by linking them together with logical operations (like {@code OR} or {@code AND}).
 * The multi predicates class just provide some static utility methods that can be used to create such linkings.
 * By using static imports, multi predicates can be created in prefix notation without writing much boilerplate code:
 * 
 * <pre>
 * or(new TypePredicate&lt;&gt;(SomeEvent.class), and(..., ...))
 * </pre>
 * 
 * @see #or(EventPredicate...)
 * @see #and(EventPredicate...)
 * @see EventPredicate
 */
public class MultiPredicates {

    /**
     * Creates a new {@link EventPredicate} that connects the given predicates with a logical {@code OR}.
     * That means that the whole multi predicate is going to be {@code true} if one child predicate is {@code true}.
     * The returned predciate stops testing the remaining child predicates if a predicate is {@code true}.
     * 
     * @param predicates One of these predicates must be {@code true} in order to make the whole multi predicate {@code true}.
     * @return The {@code OR}-linking predicate.
     */
    @SafeVarargs
    public static <T extends Event> EventPredicate<T> or(EventPredicate<? super T>... predicates) {

        return new OrPredicate<>(predicates);
    }

    /**
     * Creates a new {@link EventPredicate} that connects the given predicates with a logical {@code AND}.
     * That means that the whole multi predicate is going to be {@code true} if all child predicates are {@code true}.
     * The returned predciate stops testing the remaining child predicates if a predicate is {@code false}.
     * 
     * @param predicates All of these predicates must be {@code true} in order to make the whole multi predicate {@code true}.
     * @return The {@code AND}-linking predicate.
     */
    @SafeVarargs
    public static <T extends Event> EventPredicate<T> and(EventPredicate<? super T>... predicates) {

        return new AndPredicate<>(predicates);
    }

    @Data
    private static class OrPredicate<T extends Event> implements EventPredicate<T> {

        private static final long                 serialVersionUID = 4566924849000854937L;

        private final EventPredicate<? super T>[] predicates;

        @Override
        public boolean test(T event) {

            for (EventPredicate<? super T> predicate : predicates) {
                if (predicate.test(event)) {
                    return true;
                }
            }

            return false;
        }

    }

    @Data
    private static class AndPredicate<T extends Event> implements EventPredicate<T> {

        private static final long                 serialVersionUID = -532845016351849577L;

        private final EventPredicate<? super T>[] predicates;

        @Override
        public boolean test(T event) {

            for (EventPredicate<? super T> predicate : predicates) {
                if (!predicate.test(event)) {
                    return false;
                }
            }

            return true;
        }

    }

}
