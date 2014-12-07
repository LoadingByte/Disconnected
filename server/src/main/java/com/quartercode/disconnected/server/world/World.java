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

package com.quartercode.disconnected.server.world;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.xml.bind.annotation.XmlRootElement;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.extra.valuefactory.ValueFactory;
import com.quartercode.classmod.factory.CollectionPropertyDefinitionFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.net.Backbone;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * A world is a space which contains one "game ecosystem".
 * It basically is the root of all logic objects the game uses.
 */
@XmlPersistent
@XmlRootElement
public class World extends DefaultCFeatureHolder {

    // ----- Properties -----

    /**
     * The {@link Backbone} (<i>"magical router connector"</i>) that is used by the world.
     * See its javadoc for more details.
     */
    public static final PropertyDefinition<Backbone>                           BACKBONE;

    /**
     * The {@link Computer}s which are present in the world.
     */
    public static final CollectionPropertyDefinition<Computer, List<Computer>> COMPUTERS;

    static {

        BACKBONE = factory(PropertyDefinitionFactory.class).create("backbone", new StandardStorage<>(), new ValueFactory<Backbone>() {

            @Override
            public Backbone get() {

                return new Backbone();
            }

        });

        COMPUTERS = factory(CollectionPropertyDefinitionFactory.class).create("computers", new StandardStorage<>(), new CloneValueFactory<>(new ArrayList<>()));

    }

    private Bridge                                                             bridge;
    private Random                                                             random;

    /**
     * Returns the {@link Bridge} that should be used for sending events by any object in the world tree.
     * It is must be injected using {@link #injectBridge(Bridge)}.
     * 
     * @return The world's bridge.
     */
    public Bridge getBridge() {

        return bridge;
    }

    /**
     * Returns the {@link Random} object that can be used by the world.
     * It is must be injected using {@link #injectRandom(Random)}.
     * 
     * @return The random object the world can use.
     */
    public Random getRandom() {

        return random;
    }

    /**
     * Injects a {@link Bridge} that can be used by the world.
     * It can be retrieved with {@link #getBridge()}.
     * 
     * @param bridge The bridge the world can use.
     */
    public void injectBridge(Bridge bridge) {

        this.bridge = bridge;
    }

    /**
     * Injects a {@link Random} object that can be used by the world.
     * It can be retrieved with {@link #getRandom()}.
     * 
     * @param random The random object the world can use.
     */
    public void injectRandom(Random random) {

        this.random = random;
    }

}
