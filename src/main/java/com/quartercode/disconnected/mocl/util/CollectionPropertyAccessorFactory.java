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

package com.quartercode.disconnected.mocl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.ChildFeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Property;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;

/**
 * A utility class for creating {@link FunctionExecutor}s which can access simple {@link Collection} {@link Property}s.
 * 
 * @see Property
 * @see Collection
 * @see FunctionExecutor
 */
public class CollectionPropertyAccessorFactory {

    /**
     * Creates a new getter {@link FunctionExecutor} for the given {@link Collection} {@link Property} definition.
     * A getter function returns an unmodifiable instance of the {@link Collection} stored by a {@link Property}.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static <C extends Collection<E>, E> FunctionExecutor<C> createGet(final FeatureDefinition<? extends Property<? extends C>> propertyDefinition) {

        return createGet(propertyDefinition, new CriteriumMatcher<E>() {

            @Override
            public boolean matches(E element, Object... arguments) throws StopExecutionException {

                return true;
            }

        });
    }

    /**
     * Creates a new getter {@link FunctionExecutor} for the given {@link Collection} {@link Property} definition with the given {@link CriteriumMatcher}.
     * A getter function returns an unmodifiable instance of the {@link Collection} stored by a {@link Property}.
     * The {@link CriteriumMatcher} only lets certain elements through into the return collection.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @param matcher The {@link CriteriumMatcher} for checking if certain elements should be returned.
     * @return The created {@link FunctionExecutor}.
     */
    public static <C extends Collection<E>, E> FunctionExecutor<C> createGet(final FeatureDefinition<? extends Property<? extends C>> propertyDefinition, final CriteriumMatcher<E> matcher) {

        return new FunctionExecutor<C>() {

            @Override
            @SuppressWarnings ("unchecked")
            public C invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                C originalCollection = holder.get(propertyDefinition).get();

                Collection<E> collection = new ArrayList<E>();
                for (E element : originalCollection) {
                    if (matcher.matches(element, arguments)) {
                        collection.add(element);
                    }
                }

                if (originalCollection instanceof List) {
                    return (C) Collections.unmodifiableList(new ArrayList<E>(collection));
                } else if (originalCollection instanceof Set) {
                    return (C) Collections.unmodifiableSet(new HashSet<E>(collection));
                } else if (originalCollection instanceof SortedSet) {
                    return (C) Collections.unmodifiableSortedSet(new TreeSet<E>(collection));
                } else {
                    return (C) Collections.unmodifiableCollection(collection);
                }
            }

        };
    }

    /**
     * Creates a new single getter {@link FunctionExecutor} for the given {@link Collection} {@link Property} definition with the given {@link CriteriumMatcher}.
     * A single getter function returns the first element of the {@link Collection} stored by a {@link Property} recognized by a {@link CriteriumMatcher}.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @param matcher The {@link CriteriumMatcher} for checking the elements.
     * @return The created {@link FunctionExecutor}.
     */
    public static <E> FunctionExecutor<E> createGetSingle(final FeatureDefinition<? extends Property<? extends Collection<E>>> propertyDefinition, final CriteriumMatcher<E> matcher) {

        return new FunctionExecutor<E>() {

            @Override
            public E invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                for (E element : holder.get(propertyDefinition).get()) {
                    if (matcher.matches(element, arguments)) {
                        return element;
                    }
                }

                return null;
            }

        };
    }

    /**
     * Creates a new adder {@link FunctionExecutor} for the given {@link Collection} {@link Property} definition.
     * An adder function adds elements to the {@link Collection} of a {@link Property}.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static <E> FunctionExecutor<Void> createAdd(final FeatureDefinition<? extends Property<? extends Collection<E>>> propertyDefinition) {

        return new FunctionExecutor<Void>() {

            @Override
            @SuppressWarnings ("unchecked")
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                for (Object element : arguments) {
                    boolean changed = holder.get(propertyDefinition).get().add((E) element);

                    // Set the parent of the added element the new holder
                    if (changed && element instanceof ChildFeatureHolder) {
                        ((ChildFeatureHolder<FeatureHolder>) element).setParent(holder);
                    }
                }

                return null;
            }

        };
    }

    /**
     * Creates a new remover {@link FunctionExecutor} for the given {@link Collection} {@link Property} definition.
     * A remover function removes elements from the {@link Collection} of a {@link Property}.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static <E> FunctionExecutor<Void> createRemove(final FeatureDefinition<? extends Property<? extends Collection<E>>> propertyDefinition) {

        return new FunctionExecutor<Void>() {

            @Override
            @SuppressWarnings ("unchecked")
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                for (Object element : arguments) {
                    boolean changed = holder.get(propertyDefinition).get().remove(element);

                    // Set the parent of the removed element to null
                    if (changed && element instanceof ChildFeatureHolder) {
                        ((ChildFeatureHolder<FeatureHolder>) element).setParent(null);
                    }
                }

                return null;
            }

        };
    }

    /**
     * Creates a new peeker {@link FunctionExecutor} for the given {@link Queue} {@link Property} definition.
     * A peeker function looks up and returns the head element of a {@link Queue}.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Queue} {@link Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static <E> FunctionExecutor<E> createPeek(final FeatureDefinition<? extends Property<? extends Queue<E>>> propertyDefinition) {

        return new FunctionExecutor<E>() {

            @Override
            public E invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return holder.get(propertyDefinition).get().peek();
            }

        };
    }

    /**
     * Creates a new poller {@link FunctionExecutor} for the given {@link Queue} {@link Property} definition.
     * A poller function looks up, removes, and returns the head element of a {@link Queue}.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Queue} {@link Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static <E> FunctionExecutor<E> createPoll(final FeatureDefinition<? extends Property<? extends Queue<E>>> propertyDefinition) {

        return new FunctionExecutor<E>() {

            @Override
            @SuppressWarnings ("unchecked")
            public E invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                E element = holder.get(propertyDefinition).get().poll();

                // Set the parent of the removed (polled) element to null
                if (element != null && element instanceof ChildFeatureHolder) {
                    ((ChildFeatureHolder<FeatureHolder>) element).setParent(null);
                }

                return element;
            }

        };
    }

    /**
     * Criterium matchers are used for limiting the output of {@link CollectionPropertyAccessorFactory#createGet(String, FeatureDefinition, CriteriumMatcher)}.
     * 
     * @param <E> The type of elements the matcher checks.
     */
    public static interface CriteriumMatcher<E> {

        /**
         * Checks if the given element applies to the criterium defined by the matcher.
         * 
         * @param element The element to check.
         * @param arguments The arguments which were passed during invokation.
         * @return True if the given element matches, false if not.
         * @throws StopExecutionException The execution of the current invokation queue should stop.
         */
        public boolean matches(E element, Object... arguments) throws StopExecutionException;

    }

    private CollectionPropertyAccessorFactory() {

    }

}
