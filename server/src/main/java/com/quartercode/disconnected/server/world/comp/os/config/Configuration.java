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

package com.quartercode.disconnected.server.world.comp.os.config;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;

/**
 * A configuration is used as content of a configuration file.
 * The configuration object could be represented as a string like
 * 
 * <pre>
 *           Column 1  Column 2  Column 3               Column 4
 * (Entry 1) value1    value2    listentry1,listentry2  value3
 * (Entry 2) value4    value5    listentry1             col4
 * (Entry 3) ...
 * </pre>
 * 
 * A configuration uses {@link ConfigurationEntry}s which contain different columns.
 * Every column contains a value or a list. If it contains a list, the list entries are seperated by commas.
 * 
 * @see ConfigurationEntry
 */
public class Configuration extends WorldFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The {@link ConfigurationEntry}s the configuration object contains.
     * Such {@link ConfigurationEntry}s represents the lines in a configuration file.
     */
    public static final CollectionPropertyDefinition<ConfigurationEntry, List<ConfigurationEntry>> ENTRIES;

    static {

        ENTRIES = create(new TypeLiteral<CollectionPropertyDefinition<ConfigurationEntry, List<ConfigurationEntry>>>() {}, "name", "entries", "storage", new StandardStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("entries", Configuration.class, SizeUtils.createGetSize(ENTRIES));

    }

}
