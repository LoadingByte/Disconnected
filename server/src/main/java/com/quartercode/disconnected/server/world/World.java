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

package com.quartercode.disconnected.server.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistry;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistryProvider;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.shared.util.ValueInjector.InjectValue;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.jtimber.api.node.DefaultNode;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * A world is a space which contains one "game ecosystem".
 * It basically is the root of all logic objects the game uses.
 */
@XmlPersistent
@XmlAccessorType (XmlAccessType.NONE)
public class World extends DefaultNode<Node<?>> implements SchedulerRegistryProvider {

    private static final String[] EXCLUDED_FIELDS = { "random", "bridge", "schedulerRegistry" };

    @InjectValue (value = "random", allowNull = true)
    private Random                random;
    @InjectValue ("bridge")
    private Bridge                bridge;
    @InjectValue (value = "schedulerRegistry", allowNull = true)
    private SchedulerRegistry     schedulerRegistry;

    @XmlElement
    private final Network         rootNetwork     = new Network();
    @XmlElementWrapper
    @XmlElement (name = "computer")
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<Computer>  computers       = new ArrayList<>();

    /**
     * Returns the {@link Random} object that can be used by the world.
     * Note that it may be {@code null}.
     *
     * @return The random object the world can use.
     */
    public Random getRandom() {

        return random;
    }

    /**
     * Returns the {@link Bridge} that should be used for sending events by any object in the world tree.
     * Note that it may not be {@code null}.
     *
     * @return The bridge the world can use.
     */
    public Bridge getBridge() {

        return bridge;
    }

    /**
     * Returns the {@link SchedulerRegistry} that can be used by all {@link Scheduler} features in the world tree.
     * Note that it may be {@code null}.
     *
     * @return The scheduler registry the world can use.
     */
    @Override
    public SchedulerRegistry getSchedulerRegistry() {

        return schedulerRegistry;
    }

    /**
     * Returns the root tier 0 {@link Network} all {@link NetNode}s must directly or indirectly (through subnetworks) part of.
     * This network practically represents the highest tier network of the Internet.
     *
     * @return The root network.
     */
    public Network getRootNetwork() {

        return rootNetwork;
    }

    /**
     * Returns the {@link Computer}s which are present in the world.
     *
     * @return The world's computers.
     */
    public List<Computer> getComputers() {

        return Collections.unmodifiableList(computers);
    }

    /**
     * Adds a {@link Computer} to the world.
     *
     * @param computer The computer to add to the world.
     */
    public void addComputer(Computer computer) {

        Validate.notNull(computer, "Cannot add null computer to world");
        computers.add(computer);
    }

    /**
     * Removes a {@link Computer} from the world.
     *
     * @param computer The computer to remove from the world.
     */
    public void removeComputer(Computer computer) {

        computers.remove(computer);
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this, EXCLUDED_FIELDS);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj, EXCLUDED_FIELDS);
    }

    @Override
    public String toString() {

        return ReflectionToStringBuilder.toStringExclude(this, EXCLUDED_FIELDS);
    }

}
