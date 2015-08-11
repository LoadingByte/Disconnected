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

package com.quartercode.disconnected.shared.world.comp.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.disconnected.shared.world.comp.net.NetId.NetIdAdapter;

/**
 * This class represents a network id which is used to uniquely define the "location" of one computer in a global network (e.g. the Internet).
 * In that respect, it has the same function as an IP address in the real world.<br>
 * <br>
 * Global networks consist of multiple networks organized in different tiers.
 * Two networks on two consecutive tiers (e.g. tier 0 and 1) are connected by two routers (one being the downlink and the other the uplink).
 * However, each network is only allowed to have one uplink to a higher-tier network. Nevertheless, a network can have multiple downlinks to lower-tier networks.<br>
 * Each computer (including routers) in such a "tier-bound" network is assigned a simple numerical node id (e.g. {@code 4353}).
 * The location of a target computer in a global network can therefore be described by storing the node ids of the downlink routers that lead down to the target
 * computer's "tier-bound" network, and then the actual node id of the target computer.<br>
 * <br>
 * For example, the following is an excerpt of a global network.
 * Each network node inside a network has its node id written behind it in brackets.
 * The different "tier-bound" networks are depicted as two-dimensional layers in three-dimensional space.
 * As you can see, those "tier-bound" networks are connected through uplink ({@code U}) and downlink ({@code D}) routers.
 *
 * <pre>
 *           __________________________________________________
 *          /                                                 /
 * Tier 0  / ... --- DRouter(4353) --- DRouter(4354) --- ... /
 *        /_______________|_________________________________/
 *            ____________|_______________________________
 *           /            |                              /
 * Tier 1   / ... --- URouter(7) --- DRouter(8) --- ... /
 *         /_____________________________|_____________/
 *                    ___________________|_________________
 *                   /                   |                /
 * Tier 2           / Target(53) --- URouter(54) --- ... /
 *                 /____________________________________/
 * </pre>
 *
 * In order to reach the target computer in the depicted global network (its node id is {@code 53}), you would need the net id {@code 4353.8.53}.
 * As you can see, the different node ids are just separated by dots.
 *
 * @see Address
 */
@XmlPersistent
@XmlJavaTypeAdapter (NetIdAdapter.class)
public class NetId implements Serializable {

    private static final long serialVersionUID = -2274341156806061093L;

    private static List<Integer> parseNetIdString(String string) {

        List<Integer> nodeIds = new ArrayList<>();

        for (String stringPart : StringUtils.split(string, '.')) {
            try {
                nodeIds.add(Integer.parseInt(stringPart));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The net id string must be provided in the format nodeId1.nodeId2.nodeId3 ...; '" + string + "' is therefore invalid");
            }
        }

        return nodeIds;
    }

    @XmlValue
    @XmlList
    private final List<Integer> nodeIds;

    // JAXB constructor
    protected NetId() {

        nodeIds = new ArrayList<>();
    }

    /**
     * Creates a new net id object using the given node ids in correct tier order.
     * For example, the list {@code [4353, 8, 53]} would refer to the node id 4353 in tier 0, node id 8 in tier 1, and node id 53 in tier 2.
     * That means that the example list would describe the net id {@code 4353.8.53}.<br>
     * <br>
     * Note that the first node id is mandatory since at least a tier 0 computer needs to be identified.
     * Moreover, all node ids must be {@code >= 0}.
     *
     * @param nodeIds The node ids that should be used to define the path to the target computer in a global network.
     */
    public NetId(List<Integer> nodeIds) {

        Validate.notEmpty(nodeIds, "Net id must contain at least one node id");

        this.nodeIds = new ArrayList<>(nodeIds);

        for (int nodeId : nodeIds) {
            Validate.isTrue(nodeId >= 0, "All node ids of a net id must be >= 0; the net id '%s' are therefore invalid", toString());
        }
    }

    /**
     * Creates a new net id object using the given node ids in correct tier order.
     * For example, the array {@code [4353, 8, 53]} would refer to the node id 4353 in tier 0, node id 8 in tier 1, and node id 53 in tier 2.
     * That means that the example array would describe the net id {@code 4353.8.53}.<br>
     * <br>
     * Note that the first node id is mandatory since at least a tier 0 computer needs to be identified.
     * Moreover, all node ids must be {@code >= 0}.
     *
     * @param nodeIds The node ids that should be used to define the path to the target computer in a global network.
     */
    public NetId(Integer... nodeIds) {

        this(Arrays.asList(nodeIds));
    }

    /**
     * Creates a new net id object using the net id that is stored in the given net id string.
     * The string must be using the format {@code nodeId0.nodeId1.nodeId2 ...} (e.g. {@code 4353.8.53}).<br>
     * <br>
     * Note that {@code nodeId0} is mandatory since at least a tier 0 computer needs to be identified.
     * Moreover, all node ids must be {@code >= 0}.
     *
     * @param string The net id string to parse.
     */
    public NetId(String string) {

        this(parseNetIdString(string));
    }

    /**
     * Returns the number of node ids the net id has, starting at {@code 0} for only one node id.
     * For example, the net id {@code 4353.8.53} has a tier of {@code 2}, while the net id {@code 4353} just has the tier number {@code 0}.<br>
     * Visually, this number defines how "deep" a net node is within the network hierarchy.
     * See {@link NetId} for more documentation of nested networks,
     *
     * @return The number of node ids of the net id, starting at {@code 0} for one node id.
     */
    public int getTier() {

        return nodeIds.size() - 1;
    }

    /**
     * Returns the node ids that identify the network nodes, which lead down to the target computer, in correct tier order.
     * For example, for the net id {@code 4353.8.53}, this method would return the list {@code [4353, 8, 53]}.
     * If you wonder what node ids are all about, read the general {@link NetId} documentation.
     *
     * @return The node ids that are used to define the path to the target computer in a global network.
     */
    public List<Integer> getNodeIdsAtTiers() {

        return Collections.unmodifiableList(nodeIds);
    }

    /**
     * Returns the node id that identifies the specific network node which leads down to the target computer at the given tier.
     * For example, for the net id {@code 4353.8.53} and the tier 1, this method would return the node id {@code 8}.
     * If you wonder what node ids are all about, read the general {@link NetId} documentation.
     *
     * @param tier The node id which leads down to the target computer at this given tier should be returned.
     * @return The numerical node id of the specific network node which leads down to the target computer at the given tier.
     */
    public int getNodeIdAtTier(int tier) {

        return nodeIds.get(tier);
    }

    /**
     * Returns the (local) node id of the specific network node the whole net id describes globally.
     * For example, for the net id {@code 4353.8.53}, this method would return the node id {@code 53}.
     * If you wonder what node ids are all about, read the general {@link NetId} documentation.
     *
     * @return The numerical node id of the specific network node the whole net id describes globally.
     */
    public int getNodeIdAtLowestTier() {

        return getNodeIdAtTier(getTier());
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (nodeIds == null ? 0 : nodeIds.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null || ! (obj instanceof NetId)) {
            return false;
        } else {
            NetId other = (NetId) obj;
            return nodeIds.equals(other.nodeIds);
        }
    }

    /**
     * Returns the stored net id as a string.
     * The returned string is using the format {@code nodeId0.nodeId1.nodeId2 ...} (e.g. {@code 4353.8.53}).
     *
     * @return A string representation of the net id.
     */
    @Override
    public String toString() {

        return StringUtils.join(nodeIds, '.');
    }

    /**
     * An {@link XmlAdapter} that binds {@link NetId} objects using their {@link NetId#toString() string representation}.
     * If a JAXB property references a net id object and doesn't specify a custom XML adapter, this adapter is used by default.
     *
     * @see NetId
     */
    public static class NetIdAdapter extends XmlAdapter<String, NetId> {

        @Override
        public String marshal(NetId v) {

            return v.toString();
        }

        @Override
        public NetId unmarshal(String v) {

            return new NetId(v);
        }

    }

}
