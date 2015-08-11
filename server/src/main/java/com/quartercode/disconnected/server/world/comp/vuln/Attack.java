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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.Weak;

/**
 * An attack is targeted at a specific {@link Vulnerability vulnerability} of a computer part (e.g. a program).
 * It tries to execute a defined {@link #getPreferredAction() preferred action}.
 * That action describes what the attacked computer part should do when the attack arrives.
 * For example, there could be {@code "crash"} and {@code "executePayload"} actions for a buffer overflow vulnerability.<br>
 * <br>
 * Note that the preferred action is really "preferred" and might not always be executed.
 * Depending on the environment, something else might happen.
 * For example, the action might be unsafe and sometimes causes the attacked program to crash instead of executing the payload.
 *
 * @see Vulnerability
 * @see VulnerabilityAction
 * @see VulnerabilityAction#getAttackWeight()
 */
public class Attack extends WorldNode<Node<?>> {

    @Weak
    @XmlAttribute
    @XmlIDREF
    private Vulnerability vulnerability;
    @XmlAttribute
    private String        preferredAction;

    // JAXB constructor
    protected Attack() {

    }

    /**
     * Creates a new attack.
     *
     * @param vulnerability The {@link Vulnerability} the attack is using to attack a computer part.
     * @param preferredAction The name of the action which <i>should</i> be executed once the attack arrives at the target.
     *        See {@link Attack} for more information on the purpose of actions and why the field starts with "preferred".
     */
    public Attack(Vulnerability vulnerability, String preferredAction) {

        Validate.notNull(vulnerability, "Attack vulnerability cannot be null");
        Validate.notBlank(preferredAction, "Preferred attack action cannot be blank");

        this.vulnerability = vulnerability;
        this.preferredAction = preferredAction;
    }

    /**
     * Returns the {@link Vulnerability} the attack is using to attack a computer part.
     * This is only used to check that the attacked computer part actually has the assumed vulnerability.
     *
     * @return The vulnerability used by the attack.
     */
    public Vulnerability getVulnerability() {

        return vulnerability;
    }

    /**
     * Returns the name of the action which <i>should</i> be executed once the attack arrives at the target.
     * See {@link Attack} for more information on the purpose of actions and why the field starts with "preferred".
     *
     * @return The name of the vulnerability action which <i>should</i> be executed as a consequence of the attack.
     */
    public String getPreferredAction() {

        return preferredAction;
    }

}
