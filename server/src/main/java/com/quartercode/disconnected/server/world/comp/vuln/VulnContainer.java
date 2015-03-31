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

package com.quartercode.disconnected.server.world.comp.vuln;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.factory.CollectionPropertyDefinitionFactory;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.disconnected.server.registry.VulnSource;
import com.quartercode.disconnected.server.registry.VulnSource.Action;
import com.quartercode.disconnected.server.util.ProbabilityUtils;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;

/**
 * A vulnerability container stores {@link Vuln vulnerabilities}, provides some access methods and allows to generate new vulnerabilities from {@link VulnSource vulnerability sources}.
 * It should be used for storing vulnerabilities instead of a plain list.
 * 
 * @see Vuln
 * @see VulnSource
 */
public class VulnContainer extends WorldFeatureHolder {

    // ----- Properties -----

    /**
     * The {@link Vuln vulnerabilities} that are stored inside the container.
     * New vulnerabilities can be added with {@link #GENERATE_VULN} and {@link #GENERATE_VULNS}.
     */
    public static final CollectionPropertyDefinition<Vuln, List<Vuln>> VULNS;

    static {

        VULNS = factory(CollectionPropertyDefinitionFactory.class).create("vulns", new StandardStorage<>(), new CloneValueFactory<>(new ArrayList<>()));

    }

    // ----- Functions -----

    /**
     * Generates one new {@link Vuln vulnerability} from the given {@link VulnSource vulnerability source} object.
     * The source is used as a general description of the new vulnerability.
     * See the javadoc of that class for more information on the generation process.
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
     * <td>{@link VulnSource}</td>
     * <td>source</td>
     * <td>The vulnerability source that generally describes the new vulnerability.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                       GENERATE_VULN;

    /**
     * Generates multiple new {@link Vuln vulnerabilities} from the given {@link VulnSource vulnerability source} objects.
     * The sources are used as general descriptions of the new vulnerabilities.
     * The algorithm selects sources for each new vulnerability using the source weights.
     * See the javadoc of the {@link VulnSource} class for more information on the generation process.<br>
     * <br>
     * Note that an amount of {@code 1} can be used to generate one vulnerability using a randomly selected source.
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
     * <td>{@link Collection}&lt;{@link VulnSource}&gt;</td>
     * <td>source</td>
     * <td>The vulnerability source that generally describes the new vulnerability.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link Integer}</td>
     * <td>amount</td>
     * <td>The amount of vulnerabilities that should be generated.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                       GENERATE_VULNS;

    /**
     * Returns the action (string) that should be executed because the given {@link Attack} arrived at the container user.
     * Note that {@code "null"} (yes, a string whose content is {@code "null"}) is also possible if the used {@link Vuln vulnerability} does not exist.
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
     * <td>{@link Attack}</td>
     * <td>attack</td>
     * <td>The attack whose resulting action should be returned.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<String>                     PROCESS_ATTACK;

    static {

        GENERATE_VULN = factory(FunctionDefinitionFactory.class).create("generateVuln", new Class[] { VulnSource.class });
        GENERATE_VULN.addExecutor("default", VulnContainer.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                VulnContainer vulnContainer = (VulnContainer) invocation.getCHolder();
                VulnSource vulnSource = (VulnSource) arguments[0];

                Vuln vuln = new Vuln();
                selectActions(vulnContainer, vulnSource, vuln);
                vulnContainer.addToColl(VULNS, vuln);

                return invocation.next(arguments);
            }

            private void selectActions(VulnContainer vulnContainer, VulnSource vulnSource, Vuln target) {

                Random random = vulnContainer.getWorld() != null ? vulnContainer.getRandom() : new Random();

                boolean selectedOneAction = false;
                while (!selectedOneAction) {
                    for (Action action : vulnSource.getActions()) {
                        if (ProbabilityUtils.gen(action.getVulnProbability(), random)) {
                            VulnAction worldAction = new VulnAction();

                            worldAction.setObj(VulnAction.NAME, action.getName());
                            worldAction.setObj(VulnAction.ATTACK_WEIGHT, action.getAttackWeight());

                            target.addToColl(Vuln.ACTIONS, worldAction);
                            selectedOneAction = true;
                        }
                    }
                }
            }

        });

        GENERATE_VULNS = factory(FunctionDefinitionFactory.class).create("generateVulns", new Class[] { Collection.class, Integer.class });
        GENERATE_VULNS.addExecutor("default", VulnContainer.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                VulnContainer vulnContainer = (VulnContainer) invocation.getCHolder();
                // Trust the user of the method
                @SuppressWarnings ("unchecked")
                Collection<VulnSource> vulnSources = (Collection<VulnSource>) arguments[0];
                int amount = (int) arguments[1];

                for (int counter = 0; counter < amount; counter++) {
                    vulnContainer.invoke(GENERATE_VULN, selectVulnSource(vulnContainer, vulnSources));
                }

                return invocation.next(arguments);
            }

            private VulnSource selectVulnSource(VulnContainer vulnContainer, Collection<VulnSource> vulnSources) {

                int totalWeights = 0;
                for (VulnSource vulnSource : vulnSources) {
                    totalWeights += vulnSource.getWeight();
                }

                Random random = vulnContainer.getWorld() != null ? vulnContainer.getRandom() : new Random();
                int randomPosition = random.nextInt(totalWeights);

                int currentPosition = 0;
                for (VulnSource vulnSource : vulnSources) {
                    int weight = vulnSource.getWeight();

                    if (randomPosition >= currentPosition && randomPosition < currentPosition + weight) {
                        return vulnSource;
                    } else {
                        currentPosition += weight;
                    }
                }

                // Should never happen
                throw new RuntimeException("Cannot select vulnerability source because generated position '" + randomPosition + "' cannot be found in '" + totalWeights + "'");
            }

        });

        PROCESS_ATTACK = factory(FunctionDefinitionFactory.class).create("processAttack", new Class[] { Attack.class });
        PROCESS_ATTACK.addExecutor("default", VulnContainer.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                CFeatureHolder vulnContainer = invocation.getCHolder();
                Attack attack = (Attack) arguments[0];
                Vuln vuln = attack.getObj(Attack.VULN);

                String action;
                if (vulnContainer.getColl(VULNS).contains(vuln)) {
                    action = attack.getObj(Attack.PREFERRED_ACTION);
                } else {
                    action = "null";
                }

                invocation.next(arguments);
                return action;
            }

        });

    }

}
