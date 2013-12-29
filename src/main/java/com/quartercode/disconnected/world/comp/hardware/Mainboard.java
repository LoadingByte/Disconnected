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

package com.quartercode.disconnected.world.comp.hardware;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.extra.def.ReferenceProperty;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory.CriteriumMatcher;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;

/**
 * This class stores information about a mainboard.
 * A mainboard has {@link MainboardSlot}s that house different pieces of {@link Hardware}.
 * 
 * @see ComputerPart
 * @see Hardware
 */
public class Mainboard extends Hardware {

    // ----- Properties -----

    /**
     * The {@link MainboardSlot}s the mainboard offers.
     * The slots may have a content on them, you have to check before you set the content to a new one.
     */
    protected static final FeatureDefinition<ObjectProperty<List<MainboardSlot>>> SLOTS;

    static {

        SLOTS = new AbstractFeatureDefinition<ObjectProperty<List<MainboardSlot>>>("slots") {

            @Override
            public ObjectProperty<List<MainboardSlot>> create(FeatureHolder holder) {

                return new ObjectProperty<List<MainboardSlot>>(getName(), holder, new ArrayList<MainboardSlot>());
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link MainboardSlot}s the mainboard offers.
     */
    public static final FunctionDefinition<List<MainboardSlot>>                   GET_SLOTS;

    /**
     * Returns the {@link MainboardSlot}s the mainboard offers which have the given content type.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Class}&lt;? extends {@link Hardware}&gt;</td>
     * <td>type</td>
     * <td>The content type to use for the selection.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<List<MainboardSlot>>                   GET_SLOTS_BY_CONTENT_TYPE;

    /**
     * Adds {@link MainboardSlot}s to the mainboard
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link MainboardSlot}...</td>
     * <td>slots</td>
     * <td>The {@link MainboardSlot}s to add to the mainboard.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                  ADD_SLOTS;

    /**
     * Removes {@link MainboardSlot}s from the mainboard
     * The slots may have a content on them, you have to check before removing them.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link MainboardSlot}...</td>
     * <td>slots</td>
     * <td>The {@link MainboardSlot}s to remove from the mainboard.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                  REMOVE_SLOTS;

    static {

        GET_SLOTS = FunctionDefinitionFactory.create("getSlots", Mainboard.class, CollectionPropertyAccessorFactory.createGet(SLOTS));
        GET_SLOTS_BY_CONTENT_TYPE = FunctionDefinitionFactory.create("getSlotsByContentType", Mainboard.class, CollectionPropertyAccessorFactory.createGet(SLOTS, new CriteriumMatcher<MainboardSlot>() {

            @Override
            public boolean matches(MainboardSlot element, Object... arguments) throws StopExecutionException {

                return ((Class<?>) arguments[0]).isAssignableFrom(element.get(MainboardSlot.GET_TYPE).invoke());
            }

        }), Class.class);
        ADD_SLOTS = FunctionDefinitionFactory.create("addSlots", Mainboard.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createAdd(SLOTS)), MainboardSlot[].class);
        REMOVE_SLOTS = FunctionDefinitionFactory.create("removeSlots", Mainboard.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createRemove(SLOTS)), MainboardSlot[].class);

    }

    /**
     * Creates a new mainboard.
     */
    public Mainboard() {

    }

    /**
     * This class represents a mainboard slot which can have a {@link Hardware} part as content.
     * The {@link Hardware} type a slot can accept is defined using the type class.
     * A mainboard slot is only used by the mainboard class.
     * 
     * @see Mainboard
     * @see Hardware
     */
    public static class MainboardSlot extends WorldChildFeatureHolder<Mainboard> {

        // ----- Properties -----

        /**
         * The {@link Hardware} type the mainboard slot accepts.
         */
        protected static final FeatureDefinition<ObjectProperty<Class<? extends Hardware>>> TYPE;

        /**
         * The {@link Hardware} part which currently uses the mainboard slot.
         */
        protected static final FeatureDefinition<ReferenceProperty<Hardware>>               CONTENT;

        static {

            TYPE = new AbstractFeatureDefinition<ObjectProperty<Class<? extends Hardware>>>("type") {

                @Override
                public ObjectProperty<Class<? extends Hardware>> create(FeatureHolder holder) {

                    return new ObjectProperty<Class<? extends Hardware>>(getName(), holder);
                }

            };

            CONTENT = new AbstractFeatureDefinition<ReferenceProperty<Hardware>>("content") {

                @Override
                public ReferenceProperty<Hardware> create(FeatureHolder holder) {

                    return new ReferenceProperty<Hardware>(getName(), holder);
                }

            };

        }

        // ----- Properties End -----

        // ----- Functions -----

        /**
         * Returns the {@link Hardware} type the mainboard slot accepts.
         */
        public static final FunctionDefinition<Class<? extends Hardware>>                   GET_TYPE;

        /**
         * Changes the {@link Hardware} type the mainboard slot accepts.
         * 
         * <table>
         * <tr>
         * <th>Index</th>
         * <th>Type</th>
         * <th>Parameter</th>
         * <th>Description</th>
         * </tr>
         * <tr>
         * <td>0</td>
         * <td>{@link Class}&lt;? extends {@link Hardware}&gt;</td>
         * <td>type</td>
         * <td>The new allowed {@link Hardware} type.</td>
         * </tr>
         * </table>
         */
        public static final FunctionDefinition<Void>                                        SET_TYPE;

        /**
         * Returns the {@link Hardware} part which currently uses the mainboard slot.
         */
        public static final FunctionDefinition<Hardware>                                    GET_CONTENT;

        /**
         * Changes the {@link Hardware} part which currently uses the mainboard slot.
         * 
         * <table>
         * <tr>
         * <th>Index</th>
         * <th>Type</th>
         * <th>Parameter</th>
         * <th>Description</th>
         * </tr>
         * <tr>
         * <td>0</td>
         * <td>{@link Hardware}</td>
         * <td>content</td>
         * <td>The new content {@link Hardware} part.</td>
         * </tr>
         * </table>
         */
        public static final FunctionDefinition<Void>                                        SET_CONTENT;

        static {

            GET_TYPE = FunctionDefinitionFactory.create("getType", MainboardSlot.class, PropertyAccessorFactory.createGet(TYPE));
            SET_TYPE = FunctionDefinitionFactory.create("setType", MainboardSlot.class, PropertyAccessorFactory.createSet(TYPE));

            GET_CONTENT = FunctionDefinitionFactory.create("getContent", MainboardSlot.class, PropertyAccessorFactory.createGet(CONTENT));
            SET_CONTENT = FunctionDefinitionFactory.create("setContent", MainboardSlot.class, PropertyAccessorFactory.createSet(CONTENT));

        }

        // ----- Functions End -----

        /**
         * Creates a new mainboard slot.
         */
        public MainboardSlot() {

        }

    }

    /**
     * This annotation marks {@link Hardware} types which are compatible with a {@link MainboardSlot} and need a {@link MainboardSlot} to function.
     */
    @Target (ElementType.TYPE)
    @Retention (RetentionPolicy.RUNTIME)
    public static @interface NeedsMainboardSlot {

    }

}
