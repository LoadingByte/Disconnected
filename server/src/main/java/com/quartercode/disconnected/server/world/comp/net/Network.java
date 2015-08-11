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

package com.quartercode.disconnected.server.world.comp.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.hardware.NetInterface;
import com.quartercode.disconnected.server.world.comp.net.Network.NetworkConnectionListAdapter.AdaptedConnectionListMap;
import com.quartercode.disconnected.server.world.comp.net.nodes.ComputerConnectedNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.DownlinkRouterNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.UplinkRouterNode;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.disconnected.shared.util.XmlWorkaround;
import com.quartercode.disconnected.shared.util.XmlWorkaround.WorkaroundPropertyType;
import com.quartercode.disconnected.shared.world.comp.net.NetId;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.MapWrapper;

/**
 * This class represents one network layer as described in the documentation for {@link NetId}.
 * Such a network layer contains different {@link NetNode}s which can be interconnected with each other.
 * That allows for network {@link Packet}s to be sent from one node to another through the shortest possible path.<br>
 * <br>
 * A network is only able to weakly reference ({@link XmlIDREF}) net nodes that are connected to at least one other net node in the network.
 * Any node which suddenly has no more connections is automatically removed.<br>
 * Note, however, that it is possible to have a network which is split up into two or more separated parts.
 * Although only one of them will be able to fully access the Internet, the other parts can still exist.
 * That behavior is especially useful if you're moving things around and only leave the network temporarily split up.<br>
 * <br>
 * Two different networks on two consecutive layers (tiers) are connected through a pair of one {@link DownlinkRouterNode} (located in the upper-tier network)
 * and one {@link UplinkRouterNode} (located in the lower-tier subnetwork).
 * Any subnetwork (all networks apart from the tier {@code 0} one) should contain exactly one uplink router node.
 * While multiple ones "work", their behavior wouldn't make a lot of sense.
 * However, it is extremely important that only <b>one</b> downlink router node points to a single subnetwork in the whole <b>world</b>.
 * If that's not the case, exceptions will occur.<br>
 * <br>
 * Note that a network automatically assigns {@link NetNode#getNodeId() node ids} to new net nodes which are added.
 * Using those node ids, the {@link NetNode#getNetId()} method calculates and returns the {@link NetId} of any net node.
 * Also note that the network doesn't store net nodes in the XML data file of a world.
 * Therefore, the nodes need to be stored elsewhere (for example, a {@link ComputerConnectedNode} is stored by its linked {@link NetInterface}).
 *
 * @see NetNode
 * @see Packet
 */
public class Network extends WorldNode<Node<?>> {

    // The map wrapper only manages the parents of the net nodes stored as keys
    // That behavior is actually desired; otherwise, each net node would have this network as a parent multiple times
    @SubstituteWithWrapper (MapWrapper.class)
    // Note that this field is managed by a JAXB workaround at the bottom of the class
    private Map<NetNode, Set<NetNode>> connectionLists = new HashMap<>();

    @XmlAttribute
    private int                        nextNodeId;

    /**
     * Returns all the {@link NetNode}s which are part of this specific network.
     * Note that the returned {@link Set} is unmodifiable.
     *
     * @return All net nodes of the network.
     */
    public Set<NetNode> getNetNodes() {

        return Collections.unmodifiableSet(getConnectionLists().keySet());
    }

    /**
     * Returns this network's {@link NetNode} which has the given {@link NetNode#getNodeId() node id} assigned to it.
     *
     * @param nodeId The node id of the returned {@link NetNode}.
     * @return The net node with the given node id.
     */
    public NetNode getNetNodeByNodeId(int nodeId) {

        for (NetNode netNode : getConnectionLists().keySet()) {
            if (netNode.getNodeId() == nodeId) {
                return netNode;
            }
        }

        return null;
    }

    /**
     * Returns this network's {@link NetNode} which is an instance of the given type.
     * If the network contains multiple net nodes of the given type, an {@link IllegalStateException} is thrown.
     *
     * @param type The type the returned {@link NetNode} is an instance of.
     * @return The net node which is part of this network and an instance of the given type.
     * @throws IllegalStateException If the network contains multiple net nodes of the given type.
     */
    @SuppressWarnings ("unchecked")
    public <T> T getNetNodeByType(Class<T> type) {

        T result = null;

        for (NetNode netNode : getConnectionLists().keySet()) {
            if (type.isInstance(netNode)) {
                Validate.validState(result == null, "Cannot get net node with type '%s' because the network contains multiple nodes of that type", type.getName());
                result = (T) netNode;
            }
        }

        return result;
    }

    /**
     * Returns whether this network contains the given {@link NetNode}.
     * The result is the same as a call to {@link Set#contains(Object)} on {@link #getNetNodes()}.
     * However, this method has a significantly better performance.
     *
     * @param netNode The net node whose existence should be checked.
     * @return Whether the this network contains the given net node.
     */
    public boolean containsNetNode(NetNode netNode) {

        return getConnectionLists().containsKey(netNode);
    }

    /**
     * Returns all the {@link NetNode}s which are <b>directly</b> connected to the given {@link NetNode}.
     * Of course, the given node must be a part of this network. Otherwise, the returned {@link Set} will be empty.
     * Since any node inside a network must have at least one connection to exist, there is no other possibility to achieve an empty set as result.<br>
     * <br>
     * If you want to modify the net node connections inside the network,
     * you can use {@link #addConnection(NetNode, NetNode)} and {@link #removeConnection(NetNode, NetNode)}.
     *
     * @param netNode The net node whose direct connection partners should be returned.
     * @return The net nodes which are directly connected to the given node.
     */
    public Set<NetNode> getConnectedNetNodes(NetNode netNode) {

        Set<NetNode> connectionList = getConnectionLists().get(netNode);

        if (connectionList == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(connectionList);
        }
    }

    /**
     * Returns the amount of {@link NetNode}s which are <b>directly</b> connected to the given {@link NetNode}.
     * This method effectively returns the {@link Set#size() size} of the set returned by {@link #getConnectedNetNodes(NetNode)}.
     * However, it has a significantly better performance.
     *
     * @param netNode The net node whose direct connection partner count should be returned.
     * @return The amount of net nodes which are directly connected to the given node.
     */
    public int getConnectedNetNodeCount(NetNode netNode) {

        Set<NetNode> adjacencyList = getConnectionLists().get(netNode);
        return adjacencyList == null ? 0 : adjacencyList.size();
    }

    /**
     * Adds a direct connection between two {@link NetNode}s inside this network; if the network doesn't yet contain any one of the two nodes, the unknown node is added.
     * Moreover, if a net node is new, it is assigned a new valid {@link NetNode#setNodeId(int) node id} as well.
     * Note that it is not possible to exceed the {@link NetNode#getMaxConnections() connection limit} imposed by the net nodes using this method.
     *
     * @param netNode1 The first one of the two net nodes which should be connected to each other.
     * @param netNode2 The second one of the two net nodes which should be connected to each other.
     * @throws ConnectionLimitExceededException If the {@link NetNode#getMaxConnections() connection limit} imposed by one of the two net nodes is exceeded.
     */
    public void addConnection(NetNode netNode1, NetNode netNode2) {

        Validate.isTrue(netNode1 != null && netNode2 != null, "Cannot connect null net nodes inside a network");
        checkConnectionLimit(netNode1);
        checkConnectionLimit(netNode2);

        // If any of the two nodes is new, assign a new node id to it
        assignNodeIdIfNew(netNode1);
        assignNodeIdIfNew(netNode2);

        addDirectedConnection(netNode1, netNode2);
        addDirectedConnection(netNode2, netNode1);
    }

    private void checkConnectionLimit(NetNode netNode) {

        if (getConnectedNetNodeCount(netNode) + 1 > netNode.getMaxConnections()) {
            throw new ConnectionLimitExceededException(netNode);
        }
    }

    private void assignNodeIdIfNew(NetNode netNode) {

        if (!containsNetNode(netNode)) {
            // The net node is new, so we need to assign a node id to it
            netNode.setNodeId(nextNodeId);
            nextNodeId++;
        }
    }

    private void addDirectedConnection(NetNode from, NetNode to) {

        if (!containsNetNode(from)) {
            getConnectionLists().put(from, new HashSet<NetNode>());
        }

        getConnectionLists().get(from).add(to);
    }

    /**
     * Removes the direct connection between two {@link NetNode}s inside this network.
     * If any one of the two nodes does no longer have any connection partners as a consequence of the removal, it is completely removed from the network
     * and its {@link NetNode#setNodeId(int) node id} is changed to {@code -1}.
     * If any one of the two net nodes is not {@link #containsNetNode(NetNode) part of this network}, nothing happens.
     *
     * @param netNode1 The first one of the two net nodes which should be disconnected from each other.
     * @param netNode2 The second one of the two net nodes which should be disconnected from each other.
     */
    public void removeConnection(NetNode netNode1, NetNode netNode2) {

        Validate.isTrue(netNode1 != null && netNode2 != null, "Cannot disconnect null net nodes inside a network");

        removeDirectedConnection(netNode1, netNode2);
        removeDirectedConnection(netNode2, netNode1);

        // If any of the two nodes does no longer exist, unassign its node id
        unassignNodeIdIfRemoved(netNode1);
        unassignNodeIdIfRemoved(netNode2);
    }

    private void removeDirectedConnection(NetNode from, NetNode to) {

        if (containsNetNode(from)) {
            Set<NetNode> connectionList = getConnectionLists().get(from);
            connectionList.remove(to);

            if (connectionList.isEmpty()) {
                getConnectionLists().remove(from);
            }
        }
    }

    private void unassignNodeIdIfRemoved(NetNode netNode) {

        if (!containsNetNode(netNode)) {
            // The net node has been removed, so we need to unassign its node id (set it to -1)
            netNode.setNodeId(-1);
        }
    }

    /**
     * Completely removes the given {@link NetNode} from the network.
     * As a consequence, its {@link NetNode#setNodeId(int) node id} is changed to {@code -1}.
     * Note that this method might result in other removals. For example, imagine the following network topology:
     *
     * <pre>
     * ... --- Node6 --- Node7 --- Node8
     *                               |
     *                             Node9
     * </pre>
     *
     * If you removed node 8 from the network, node 9 suddenly has no more connections.
     * Therefore, it is removed from the network as well.<br>
     * <br>
     * If the given net node is not {@link #containsNetNode(NetNode) part of this network}, nothing happens.
     *
     * @param netNode The net node which should be totally removed from the network.
     */
    public void removeNetNode(NetNode netNode) {

        Set<NetNode> adjacencyList = getConnectionLists().get(netNode);

        if (adjacencyList != null) {
            for (NetNode adjacentNode : new HashSet<>(adjacencyList)) {
                removeConnection(netNode, adjacentNode);
            }
        }
    }

    /**
     * If this network is a subnetwork (e.g. stored by a {@link DownlinkRouterNode}), returns the {@link NetId} of downlink router node (or any other net node which stores a network).
     * If that's not the case, {@code null} is returned.
     * This method is extremely useful for computing the net ids of the {@link NetNode}s which are part of this network.
     * Therefore, net ids internally use this method.
     *
     * @return The net id of the net node which stores this network (e.g. a {@link DownlinkRouterNode}), or {@code null} if no such net node exists.
     */
    public NetId getUplinkTargetNetId() {

        if (getParents().size() == 1 && getSingleParent() instanceof NetNode) {
            return ((NetNode) getSingleParent()).getNetId();
        } else {
            return null;
        }
    }

    /*
     * The following is a workaround for a JAXB bug which basically makes it impossible to use @XmlIDREF in combination with XML adapters.
     * Therefore, the following code fakes the behavior of the regular XML adapter.
     * Note that the code will be removed as soon as the bug has been fixed.
     */

    @XmlElement (name = "connectionLists")
    @XmlWorkaround (WorkaroundPropertyType.WORKAROUND_PROPERTY)
    private AdaptedConnectionListMap adaptedConnectionLists;

    @SuppressWarnings ("unused")
    private void beforeMarshal(Marshaller marshaller) {

        // Just make sure that the after unmarshal callback has been invoked
        getConnectionLists();

        adaptedConnectionLists = NetworkConnectionListAdapter.INSTANCE.marshal(connectionLists);
    }

    // Anybody who wants to access the classe's persistent properties through reflection should use this method for the connection lists
    @XmlWorkaround (WorkaroundPropertyType.REAL_PROPERTY)
    // Note that we can't just use afterUnmarshal() because such a listener would be executed before @XmlIDREF has been processed
    private Map<NetNode, Set<NetNode>> getConnectionLists() {

        // After unmarshal & after @XmlIDREF resolving
        if (adaptedConnectionLists != null) {
            // Only read the unmarshalled (adapted) connection lists if this network object has just been unmarshalled as well (presumably)
            if (connectionLists.isEmpty()) {
                connectionLists = NetworkConnectionListAdapter.INSTANCE.unmarshal(adaptedConnectionLists);
            }

            adaptedConnectionLists = null;
        }

        return connectionLists;
    }

    static class NetworkConnectionListAdapter extends XmlAdapter<AdaptedConnectionListMap, Map<NetNode, Set<NetNode>>> {

        private static final NetworkConnectionListAdapter INSTANCE = new NetworkConnectionListAdapter();

        @Override
        public AdaptedConnectionListMap marshal(Map<NetNode, Set<NetNode>> map) {

            AdaptedConnectionListMap adaptedMap = new AdaptedConnectionListMap();

            for (Entry<NetNode, Set<NetNode>> entry : map.entrySet()) {
                Set<AdaptedConnectionListToElement> toElems = new HashSet<>();
                for (NetNode to : entry.getValue()) {
                    toElems.add(new AdaptedConnectionListToElement(to));
                }
                adaptedMap.getConnectionLists().add(new AdaptedConnectionList(entry.getKey(), toElems));
            }

            return adaptedMap;
        }

        @Override
        public Map<NetNode, Set<NetNode>> unmarshal(AdaptedConnectionListMap adaptedMap) {

            Map<NetNode, Set<NetNode>> map = new HashMap<>();

            for (AdaptedConnectionList adaptedList : adaptedMap.getConnectionLists()) {
                Set<NetNode> to = new HashSet<>();
                for (AdaptedConnectionListToElement toElem : adaptedList.getTo()) {
                    to.add(toElem.getNetNode());
                }
                map.put(adaptedList.getFrom(), to);
            }

            return map;
        }

        @Getter
        @XmlPersistent
        static class AdaptedConnectionListMap {

            @XmlElement (name = "connectionList")
            private final List<AdaptedConnectionList> connectionLists = new ArrayList<>();

        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Getter
        @XmlPersistent
        static class AdaptedConnectionList {

            @XmlAttribute
            @XmlIDREF
            private NetNode                             from;
            @XmlElement
            private Set<AdaptedConnectionListToElement> to;

        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Getter
        @XmlPersistent
        static class AdaptedConnectionListToElement {

            @XmlAttribute
            @XmlIDREF
            private NetNode netNode;

        }

    }

}
