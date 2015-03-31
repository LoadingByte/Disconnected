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
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;

/**
 * An attack is targeted at a specific {@link Vuln vulnerability} of a computer part (e.g. a program).
 * It tries to execute a defined {@link #PREFERRED_ACTION}.
 * That action describes what the attacked computer part should do when the attack arrives.
 * For example, there could be {@code "crash"} and {@code "executePayload"} actions for a buffer overflow vulnerability.<br>
 * <br>
 * Note that the preferred action is really "preferred" and might not always be executed.
 * Depending on the environment, something else might happen.
 * For example, the action might be unsafe and sometimes causes the attacked program to crash instead of executing the payload.
 * 
 * @see Vuln
 * @see VulnAction
 * @see VulnAction#ATTACK_WEIGHT
 */
public class Attack extends WorldFeatureHolder {

    // ----- Properties -----

    /**
     * The {@link Vuln vulnerability} the attack is using to attack a computer part.
     * This is only used to check that the attacked computer part actually has the assumed vulnerability.
     */
    public static final PropertyDefinition<Vuln>   VULN;

    /**
     * Returns the action which should be executed once the attack arrives at the target.
     * See {@link Attack} for more information on the purpose of actions and why the field starts with "preferred".
     */
    public static final PropertyDefinition<String> PREFERRED_ACTION;

    static {

        VULN = factory(PropertyDefinitionFactory.class).create("vuln", new ReferenceStorage<>());
        PREFERRED_ACTION = factory(PropertyDefinitionFactory.class).create("preferredAction", new StandardStorage<>());

    }

}
