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

package com.quartercode.disconnected.server.test.world.comp.vuln;

import static com.quartercode.disconnected.server.test.ExtraAssert.assertCollectionEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.registry.VulnSource;
import com.quartercode.disconnected.server.registry.VulnSource.Action;
import com.quartercode.disconnected.server.world.comp.vuln.Attack;
import com.quartercode.disconnected.server.world.comp.vuln.Vuln;
import com.quartercode.disconnected.server.world.comp.vuln.VulnAction;
import com.quartercode.disconnected.server.world.comp.vuln.VulnContainer;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;

public class VulnContainerTest {

    @Rule
    public JUnitRuleMockery          context = new JUnitRuleMockery();

    private VulnContainer            container;
    private final WorldFeatureHolder parent  = new WorldFeatureHolder();

    @Before
    public void setUp() {

        container = new VulnContainer();
        container.setParent(parent);
    }

    @Test
    public void testGenerateVuln() {

        assertTrue("Empty vulnerability container is not empty", container.getColl(VulnContainer.VULNS).isEmpty());

        List<Action> actions = new ArrayList<>();
        actions.add(new Action("action1", 0, 1));
        actions.add(new Action("action2", 1, 1));
        actions.add(new Action("action3", 0, 1));
        actions.add(new Action("action4", 1, 1));
        final VulnSource vulnSource = new VulnSource("testVulnSource", null, 1, actions);

        container.invoke(VulnContainer.GENERATE_VULN, vulnSource);

        assertEquals("Amount of vulns after generation", 1, container.getColl(VulnContainer.VULNS).size());

        Vuln generatedVuln = container.getColl(VulnContainer.VULNS).get(0);
        assertCollectionEquals("Wrong actions in generated vuln", generatedVuln.getColl(Vuln.ACTIONS), action("action2"), action("action4"));
    }

    @Test
    public void testGenerateVulns() {

        assertTrue("'Empty' vulnerability container is not empty", container.getColl(VulnContainer.VULNS).isEmpty());

        VulnSource vulnSource1 = new VulnSource("testVulnSource1", null, 0, Collections.<Action> emptyList());
        final VulnSource vulnSource2 = new VulnSource("testVulnSource2", null, 1, Arrays.asList(new Action("action2", 1, 1)));
        VulnSource vulnSource3 = new VulnSource("testVulnSource3", null, 0, Collections.<Action> emptyList());

        container.invoke(VulnContainer.GENERATE_VULNS, Arrays.asList(vulnSource1, vulnSource2, vulnSource3), 2);

        assertEquals("Amount of vulns after generation", 2, container.getColl(VulnContainer.VULNS).size());

        Vuln generatedVuln1 = container.getColl(VulnContainer.VULNS).get(0);
        assertCollectionEquals("Actions of first generated vuln are wrong", generatedVuln1.getObj(Vuln.ACTIONS), action("action2"));

        Vuln generatedVuln2 = container.getColl(VulnContainer.VULNS).get(1);
        assertCollectionEquals("Actions of first generated vuln are wrong", generatedVuln2.getObj(Vuln.ACTIONS), action("action2"));
    }

    @Test
    public void testProcessAttack() {

        Vuln vuln1 = vuln(action("testAction1"));
        Vuln vuln2 = vuln(action("testAction2"));
        Vuln vuln3 = vuln(action("testAction3"));

        container.addToColl(VulnContainer.VULNS, vuln1);
        container.addToColl(VulnContainer.VULNS, vuln2);
        // Do not add vuln3

        String action1 = container.invoke(VulnContainer.PROCESS_ATTACK, attack(vuln1, "testAction1"));
        String action2 = container.invoke(VulnContainer.PROCESS_ATTACK, attack(vuln2, "testAction2"));
        String action3 = container.invoke(VulnContainer.PROCESS_ATTACK, attack(vuln3, "testAction3"));

        assertEquals("Action for vuln 1", "testAction1", action1);
        assertEquals("Action for vuln 2", "testAction2", action2);
        assertEquals("Action for vuln 3 (not added to container)", "null", action3);
    }

    private Vuln vuln(VulnAction... actions) {

        Vuln vuln = new Vuln();
        for (VulnAction action : actions) {
            vuln.addToColl(Vuln.ACTIONS, action);
        }
        return vuln;
    }

    private VulnAction action(String name) {

        VulnAction action = new VulnAction();
        action.setObj(VulnAction.NAME, name);
        action.setObj(VulnAction.ATTACK_WEIGHT, 1);
        return action;
    }

    private Attack attack(Vuln vuln, String preferredAction) {

        Attack attack = new Attack();
        attack.setObj(Attack.VULN, vuln);
        attack.setObj(Attack.PREFERRED_ACTION, preferredAction);
        return attack;
    }

}
