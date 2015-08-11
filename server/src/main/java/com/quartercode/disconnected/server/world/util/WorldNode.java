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

package com.quartercode.disconnected.server.world.util;

import java.util.Random;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistry;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistryProvider;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.jtimber.api.node.DefaultNode;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.util.ParentUtils;

/**
 * The world node is a special {@link DefaultNode} which provides some additional convenience methods.
 * For example, {@link #getWorld()} resolves the {@link World} the node is somehow (transitively) referenced by.
 * Moreover, the {@link SchedulerRegistryProvider} interface is implemented and returns the scheduler registry of the world.
 * Each {@link Node} should extend this class (apart from the root one).
 *
 * @param <P> The type of {@link Node}s that are able to be parents of this world node.
 *        Note that all parents are verified against this type at runtime.
 *        Only parent nodes which are a compatible with this type are allowed.
 * @see World
 * @see DefaultNode
 */
@XmlPersistent
@XmlAccessorType (XmlAccessType.NONE)
public class WorldNode<P extends Node<?>> extends DefaultNode<P> implements SchedulerRegistryProvider {

    private UUID uuid;

    /**
     * Returns the {@link UUID} of the world node (annotated with {@link XmlID}).
     * It is lazily generated using {@link UUID#randomUUID()} as soon as it is required and won't be unmarshalled from XML.
     *
     * @return The unique world node id.
     */
    @XmlAttribute (name = "uuid")
    @XmlJavaTypeAdapter (UUIDAdapter.class)
    @XmlID
    public UUID getUUID() {

        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    /*
     * This method is *only* used by JAXB to set the UUID on unmarshalling.
     * Without it, the whole @XmlIDREF resolution process doesn't work.
     */
    @SuppressWarnings ("unused")
    private void setUUID(UUID uuid) {

        this.uuid = uuid;
    }

    // ----- Convenience methods -----

    /**
     * Resolves the {@link World} this world node is somehow (transitively) referenced by.
     * That means that this node "is in" the returned world.
     *
     * @return The world which contains this node.
     */
    public World getWorld() {

        return ParentUtils.getFirstParentOfType(World.class, this);
    }

    /**
     * Returns the {@link Random} object that can be used by this world node.
     * Actually, this returns the random object of the {@link World} this node "is in" (see {@link #getWorld()}).
     * Note that the returned random object may be {@code null}.
     *
     * @return The random object this world node can use.
     *         May be {@code null}.
     * @see World#getRandom()
     */
    public Random getRandom() {

        World world = getWorld();
        return world != null ? world.getRandom() : null;
    }

    /**
     * Returns the {@link Bridge} that should be used for sending events by this world node.
     * Actually, this returns the bridge of the {@link World} this node "is in" (see {@link #getWorld()}).
     * Note that the returned bridge may be {@code null}.
     *
     * @return The bridge for this world node.
     * @see World#getBridge()
     */
    public Bridge getBridge() {

        World world = getWorld();
        return world != null ? world.getBridge() : null;
    }

    /**
     * Returns the {@link SchedulerRegistry} that can be used by all {@link Scheduler}s of this world node.
     * Actually, this returns the scheduler registry of the {@link World} this node "is in" (see {@link #getWorld()}).
     * Note that the returned scheduler registry may be {@code null}.
     *
     * @return The scheduler registry this world node can use.
     * @see World#getSchedulerRegistry()
     */
    @Override
    public SchedulerRegistry getSchedulerRegistry() {

        World world = getWorld();
        return world != null ? world.getSchedulerRegistry() : null;
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this, "uuid");
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj, "uuid");
    }

    @Override
    public String toString() {

        return ReflectionToStringBuilder.toStringExclude(this, "uuid");
    }

}
