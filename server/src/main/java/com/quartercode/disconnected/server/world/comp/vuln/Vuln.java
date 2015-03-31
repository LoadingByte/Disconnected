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
import java.util.List;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.factory.CollectionPropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;

/**
 * A data object that represents a vulnerability a piece of hardware or software, which is related to a computer, may have.
 * Vulnerabilities are general and can be exploited to do many things.
 * For example, a buffer overflow vulnerability can be used to crash a program or to execute a payload (e.g. for opening a remote session).
 * On the other hand, an SQL injection vulnerability can be used to access a database.
 * 
 * @see VulnContainer
 */
public class Vuln extends WorldFeatureHolder {

    // ----- Properties -----

    /**
     * Returns the {@link VulnAction vulnerability actions} which can be executed when the vulnerability is exploited.
     * Such actions describe what an attacked computer part (e.g. a program) should do.
     * For example, a buffer overflow vulnerability could provide actions to crash a program or to execute a payload (e.g. for opening a remote session).
     * On the other hand, an SQL injection vulnerability could be used to access a database.<br>
     * <br>
     * Actually, only one "preferred" action can be used for an attack.
     * Therefore, the {@link VulnAction#ATTACK_WEIGHT}s are used to select which action is used for an attack.
     * See that property for more information on the details of the process.
     */
    public static final CollectionPropertyDefinition<VulnAction, List<VulnAction>> ACTIONS;

    static {

        ACTIONS = factory(CollectionPropertyDefinitionFactory.class).create("actions", new StandardStorage<>(), new CloneValueFactory<>(new ArrayList<>()));

    }

}
