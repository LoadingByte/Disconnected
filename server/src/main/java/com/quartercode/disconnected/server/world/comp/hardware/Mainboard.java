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

package com.quartercode.disconnected.server.world.comp.hardware;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.jtimber.api.node.Weak;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * This class stores information about a mainboard.
 * A mainboard has {@link MainboardSlot}s that house different pieces of {@link Hardware}.
 *
 * @see Hardware
 */
public class Mainboard extends Hardware {

    @XmlElement (name = "slot")
    @XmlElementWrapper
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<MainboardSlot<?>> slots;

    // JAXB constructor
    protected Mainboard() {

        slots = new ArrayList<>();
    }

    /**
     * Creates a new mainboard.
     *
     * @param name The "model" name of the new mainboard.
     *        See {@link #getName()} for more details.
     * @param slots The {@link MainboardSlot}s the mainboard should offer.
     */
    public Mainboard(String name, List<MainboardSlot<?>> slots) {

        super(name);

        Validate.notEmpty(slots, "Mainboard slot list cannot be null or empty");
        this.slots = new ArrayList<>(slots);
    }

    /**
     * Returns the {@link MainboardSlot}s the mainboard offers.
     * The slots could have a content on them, you have to check before you set the content to a new one.
     *
     * @return The slots of the mainboard.
     */
    public List<MainboardSlot<?>> getSlots() {

        return Collections.unmodifiableList(slots);
    }

    /**
     * Returns the {@link MainboardSlot}s of the mainboard which have the given {@link MainboardSlot#getContentType() content type}.
     *
     * @param contentType The slot content type to look for.
     * @return All slots of the mainboard which have the given content type.
     */
    @SuppressWarnings ("unchecked")
    public <T extends Hardware> List<MainboardSlot<T>> getSlotsByContentType(Class<T> contentType) {

        List<MainboardSlot<T>> result = new ArrayList<>();

        for (MainboardSlot<?> slot : slots) {
            if (contentType.isAssignableFrom(slot.getContentType())) {
                result.add((MainboardSlot<T>) slot);
            }
        }

        return result;
    }

    /**
     * This class represents a mainboard slot which can have a {@link Hardware} part as content.
     * The {@link Hardware} type a slot can accept is defined using the type class.
     * A mainboard slot is only used by the mainboard class.
     *
     * @param <T> The type of hardware the mainboard slot can hold (similar to the {@link #getContentType() content type}).
     * @see Mainboard
     * @see Hardware
     */
    public static class MainboardSlot<T extends Hardware> extends WorldNode<Mainboard> {

        @XmlAttribute
        private Class<T> contentType;
        @Weak
        @XmlAttribute
        @XmlIDREF
        private T        content;

        // JAXB constructor
        protected MainboardSlot() {

        }

        /**
         * Creates a new mainboard slot.
         *
         * @param contentType The type of the {@link Hardware} part the mainboard slot accepts.
         *        See {@link #getContentType()} for more details.
         */
        public MainboardSlot(Class<T> contentType) {

            Validate.notNull(contentType, "Mainboard slot content type cannot be null");
            this.contentType = contentType;
        }

        /**
         * Returns the type of the {@link Hardware} part the mainboard slot accepts.
         * The slot can only have a hardware part of this type as {@link #getContent() content}.
         *
         * @return The allowed content type of the mainboard slot.
         */
        public Class<T> getContentType() {

            return contentType;
        }

        /**
         * Returns the {@link Hardware} part which currently uses the mainboard slot.
         * It must be of the allowed {@link #getContentType() content type}.
         *
         * @return The content of the mainboard slot.
         */
        public T getContent() {

            return content;
        }

        /**
         * Sets the {@link Hardware} part which uses the mainboard slot.
         * It must be of the allowed {@link #getContentType() content type}.
         *
         * @param content The new content of the mainboard slot.
         */
        public void setContent(T content) {

            Validate.notNull(content, "Mainboard slot content cannot be null");
            this.content = content;
        }

    }

    /**
     * This annotation marks {@link Hardware} parts which <b>need</b> a {@link MainboardSlot} to function.
     */
    @Target (ElementType.TYPE)
    @Retention (RetentionPolicy.RUNTIME)
    public static @interface NeedsMainboardSlot {

    }

}
