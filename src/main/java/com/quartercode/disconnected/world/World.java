/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
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

package com.quartercode.disconnected.world;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.world.comp.Computer;

/**
 * A world is a space which contains one "game ecosystem".
 * It basically is the root of all logic objects the game uses.
 */
@XmlRootElement (namespace = "http://quartercode.com/")
public class World extends DefaultFeatureHolder {

    // ----- Properties -----

    /**
     * The {@link Computer}s which are present in the world.
     */
    public static final CollectionPropertyDefinition<Computer, List<Computer>> COMPUTERS;

    static {

        COMPUTERS = ObjectCollectionProperty.createDefinition("computers", new ArrayList<Computer>());

    }

    private RandomPool                                                         random;

    /**
     * Creates a new empty world.
     * Please note that a {@link RandomPool} must be injected after creation using {@link #injectRandom(RandomPool)}.
     */
    public World() {

    }

    /**
     * Returns the {@link RandomPool} that can be used by the world.
     * It is must be injected using {@link #injectRandom(RandomPool)}.
     * 
     * @return The random pool the world can use.
     */
    public RandomPool getRandom() {

        return random;
    }

    /**
     * Injects a {@link RandomPool} that can be used by the world.
     * It can be retrieved with {@link #getRandom()}.
     * 
     * @param random The random pool the world can use.
     */
    public void injectRandom(RandomPool random) {

        this.random = random;
    }

}
