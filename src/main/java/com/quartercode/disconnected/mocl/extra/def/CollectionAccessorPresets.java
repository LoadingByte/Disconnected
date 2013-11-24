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

package com.quartercode.disconnected.mocl.extra.def;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.Function;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Property;

/**
 * A utility class for creating {@link FunctionDefinition}s which can access simple {@link Collection} {@link Property}s.
 * 
 * @see Property
 * @see Collection
 * @see FunctionDefinition
 */
public class CollectionAccessorPresets {

    /**
     * Creates a new getter {@link FunctionDefinition} for the given {@link Collection} {@link Property} definition.
     * A getter function returns an unmodifiable instance of the {@link Collection} stored by a {@link Property}.
     * 
     * @param name The name of the new {@link FunctionDefinition}.
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @return The created {@link FunctionDefinition}.
     */
    public static <C extends Collection<E>, E> FunctionDefinition<C> createGet(String name, final FeatureDefinition<? extends Property<? extends C>> propertyDefinition) {

        return createGet(name, propertyDefinition, new CriteriumMatcher<E>() {

            @Override
            public boolean matches(E element, Object... arguments) {

                return true;
            }

        });
    }

    /**
     * Creates a new getter {@link FunctionDefinition} for the given {@link Collection} {@link Property} definition with the given {@link CriteriumMatcher}.
     * A getter function returns an unmodifiable instance of the {@link Collection} stored by a {@link Property}.
     * The {@link CriteriumMatcher} only lets certain elements through into the return collection.
     * 
     * @param name The name of the new {@link FunctionDefinition}.
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @param matcher The {@link CriteriumMatcher} for checking if certain elements should be returned.
     * @return The created {@link FunctionDefinition}.
     */
    public static <C extends Collection<E>, E> FunctionDefinition<C> createGet(String name, final FeatureDefinition<? extends Property<? extends C>> propertyDefinition, final CriteriumMatcher<E> matcher) {

        FunctionDefinition<C> definition = new AbstractFunctionDefinition<C>(name) {

            @Override
            protected Function<C> create(FeatureHolder holder, List<FunctionExecutor<C>> executors) {

                return new AbstractFunction<C>(getName(), holder, executors);
            }

        };

        definition.addExecutor("default", new FunctionExecutor<C>() {

            @SuppressWarnings ("unchecked")
            @Override
            public C invoke(FeatureHolder holder, Object... arguments) {

                C originalCollection = holder.get(propertyDefinition).get();

                Collection<E> collection = new ArrayList<E>();
                for (E element : originalCollection) {
                    if (matcher.matches(element)) {
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

        });

        return definition;
    }

    /**
     * Creates a new single getter {@link FunctionDefinition} for the given {@link Collection} {@link Property} definition with the given {@link CriteriumMatcher}.
     * A single getter function returns the first element of the {@link Collection} stored by a {@link Property} recognized by a {@link CriteriumMatcher}.
     * 
     * @param name The name of the new {@link FunctionDefinition}.
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @param matcher The {@link CriteriumMatcher} for checking the elements.
     * @return The created {@link FunctionDefinition}.
     */
    public static <E> FunctionDefinition<E> createGetSingle(String name, final FeatureDefinition<? extends Property<? extends Collection<E>>> propertyDefinition, final CriteriumMatcher<E> matcher) {

        FunctionDefinition<E> definition = new AbstractFunctionDefinition<E>(name) {

            @Override
            protected Function<E> create(FeatureHolder holder, List<FunctionExecutor<E>> executors) {

                return new AbstractFunction<E>(getName(), holder, executors);
            }

        };

        definition.addExecutor("default", new FunctionExecutor<E>() {

            @Override
            public E invoke(FeatureHolder holder, Object... arguments) {

                for (E element : holder.get(propertyDefinition).get()) {
                    if (matcher.matches(element)) {
                        return element;
                    }
                }

                return null;
            }

        });

        return definition;
    }

    private static FunctionDefinition<Void> createModifier(String name, FunctionExecutor<Void> executor) {

        FunctionDefinition<Void> definition = new AbstractFunctionDefinition<Void>(name) {

            @Override
            protected Function<Void> create(FeatureHolder holder, List<FunctionExecutor<Void>> executors) {

                return new AbstractFunction<Void>(getName(), holder, executors);
            }

        };

        definition.addExecutor("default", executor);

        return definition;
    }

    /**
     * Creates a new adder {@link FunctionDefinition} for the given {@link Collection} {@link Property} definition.
     * An adder function adds values to the {@link Collection} of a {@link Property}.
     * 
     * @param name The name of the new {@link FunctionDefinition}.
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @return The created {@link FunctionDefinition}.
     */
    public static <E> FunctionDefinition<Void> createAdd(String name, final FeatureDefinition<? extends Property<? extends Collection<E>>> propertyDefinition) {

        return createModifier(name, new FunctionExecutor<Void>() {

            @SuppressWarnings ("unchecked")
            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) {

                try {
                    for (Object value : arguments) {
                        holder.get(propertyDefinition).get().add((E) value);
                    }
                }
                catch (ClassCastException e) {
                    throw new IllegalArgumentException("Wrong arguments: 'T value'");
                }

                return null;
            }

        });
    }

    /**
     * Creates a new remover {@link FunctionDefinition} for the given {@link Collection} {@link Property} definition.
     * A remover function removes values from the {@link Collection} of a {@link Property}.
     * 
     * @param name The name of the new {@link FunctionDefinition}.
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Collection} {@link Property} to access.
     * @return The created {@link FunctionDefinition}.
     */
    public static <E> FunctionDefinition<Void> createRemove(String name, final FeatureDefinition<? extends Property<? extends Collection<E>>> propertyDefinition) {

        return createModifier(name, new FunctionExecutor<Void>() {

            @SuppressWarnings ("unchecked")
            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) {

                try {
                    for (Object value : arguments) {
                        holder.get(propertyDefinition).get().remove((E) value);
                    }
                }
                catch (ClassCastException e) {
                    throw new IllegalArgumentException("Wrong arguments: 'T value'");
                }

                return null;
            }

        });
    }

    /**
     * Criterium matchers are used for limiting the output of {@link CollectionAccessorPresets#createGet(String, FeatureDefinition, CriteriumMatcher)}.
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
         */
        public boolean matches(E element, Object... arguments);

    }

    /**
     * The class matcher checks if the elements are either either the same as or a superclass or superinterface of a given {@link Class}.
     * 
     * @param <E> The type of elements the class matcher checks.
     * @see CriteriumMatcher
     */
    public static class ClassMatcher<E> implements CriteriumMatcher<E> {

        @Override
        public boolean matches(E element, Object... arguments) {

            Validate.isTrue(arguments.length == 1, "Wrong arguments: 'Class matchClass'");
            Validate.isTrue(arguments[0] instanceof Class, "Wrong arguments: 'Class matchClass'");

            return ((Class<?>) arguments[0]).isAssignableFrom(element.getClass());
        }

    }

    private CollectionAccessorPresets() {

    }

}
