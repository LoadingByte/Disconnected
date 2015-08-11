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

package com.quartercode.disconnected.shared.util.init;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

/**
 * A utility class which uses a topological sorting algorithm to sort a {@link Collection} of {@link Initializer} according to their dependencies.
 * That means that all initializers which depend on other initializers are put after those dependencies.
 * The resulting {@link List} could be executed iteratively since the initializers are in a valid order.
 *
 * @see Initializer
 */
public class InitializerSorter {

    /**
     * Sorts the given {@link Initializer} {@link Collection} according to their dependencies.
     * That means that all initializers which depend on other initializers are put after those dependencies.
     * The resulting {@link List} could be executed iteratively since the initializers are in a valid order.<br>
     * <br>
     * Internally, this method uses a topological sorting algorithm.
     *
     * @param initializers The initializers which should be brought into a valid order.
     * @return The topologically sorted initializer list.
     */
    public static List<Initializer> sortByDependencies(Collection<Initializer> initializers) {

        Validate.noNullElements(initializers, "Null initializers are not allowed");

        List<Node> unsortedNodes = new ArrayList<>(initializers.size());

        // Convert the initializer list to node objects
        for (Initializer initializer : initializers) {
            InitializerSettings settings = initializer.getClass().getAnnotation(InitializerSettings.class);

            if (settings == null) {
                throw new IllegalArgumentException("Initializer '" + initializer.getClass().getName() + "' doesn't have the InitializerSettings annotation");
            } else {
                unsortedNodes.add(new Node(initializer, settings));
            }
        }

        // Resolve the dependency node objects of each node
        for (Node node : unsortedNodes) {
            for (String dependency : node.settings.dependencies()) {
                Collection<Node> dependencyNodes = getNodesByGroup(unsortedNodes, dependency);

                if (dependencyNodes.isEmpty()) {
                    throw new IllegalStateException("Initializer '" + node.initializer.getClass().getName() + "' has unknown initializer dependency '" + dependency + "'");
                } else {
                    node.dependencies.addAll(dependencyNodes);
                }
            }
        }

        // Visit all unvisited nodes
        List<Node> sortedNodes = new ArrayList<>(unsortedNodes.size());
        for (Node node : unsortedNodes) {
            if (!node.visited) {
                visitNode(node, sortedNodes);
            }
        }

        List<Initializer> sortedInitializers = new ArrayList<>();
        for (Node node : sortedNodes) {
            sortedInitializers.add(node.initializer);
        }
        return sortedInitializers;
    }

    private static Collection<Node> getNodesByGroup(Iterable<Node> nodes, String group) {

        Collection<Node> result = new ArrayList<>();

        for (Node node : nodes) {
            for (String nodeGroup : node.settings.groups()) {
                if (nodeGroup.equals(group)) {
                    result.add(node);
                    break;
                }
            }
        }

        return result;
    }

    private static void visitNode(Node node, List<Node> target) {

        // Mark the current node as visited
        node.visited = true;

        // Visit all unvisited dependencies of the current node
        for (Node dependency : node.dependencies) {
            if (!dependency.visited) {
                visitNode(dependency, target);
            }
        }

        // Add the current node to the end of the target list
        target.add(node);
    }

    private InitializerSorter() {

    }

    @RequiredArgsConstructor
    private static class Node {

        private final Initializer         initializer;
        private final InitializerSettings settings;
        private final List<Node>          dependencies = new ArrayList<>();

        private boolean                   visited;

    }

}
