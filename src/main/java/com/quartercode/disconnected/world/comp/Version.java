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

package com.quartercode.disconnected.world.comp;

import java.util.Arrays;
import org.apache.commons.lang.Validate;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.world.StringRepresentable;

/**
 * This class represents a simple version.
 * A version contains a major version, minor version and a patch level.
 */
public class Version extends DefaultFeatureHolder implements StringRepresentable {

    // ----- Properties -----

    /**
     * The major version component which is changed after very large changes.
     */
    public static final PropertyDefinition<Integer> MAJOR;

    /**
     * The minor version component which is changed if there are new features.
     */
    public static final PropertyDefinition<Integer> MINOR;

    /**
     * The revision version component which is changed after fixes.
     */
    public static final PropertyDefinition<Integer> REVISION;

    static {

        MAJOR = ObjectProperty.createDefinition("major");
        MINOR = ObjectProperty.createDefinition("minor");
        REVISION = ObjectProperty.createDefinition("revision");

    }

    // ----- Functions -----

    /**
     * Changes the stored version to the ones stored in the given version object.
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
     * <td>{@link Version}</td>
     * <td>version</td>
     * <td>The version object to get the new version from.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>    FROM_OBJECT;

    /**
     * Returns the stored version as a string.
     * The string is using the format MAJOR.MINOR.PATCHLEVEL (e.g. 1.2.5).
     */
    public static final FunctionDefinition<String>  TO_STRING   = StringRepresentable.TO_STRING;

    /**
     * Changes the stored version to the ones stored in the given version string.
     * The string is using the format MAJOR.MINOR.PATCHLEVEL (e.g. 1.2.5).
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
     * <td>{@link String}</td>
     * <td>version</td>
     * <td>The new version to parse.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>    FROM_STRING = StringRepresentable.FROM_STRING;

    static {

        FROM_OBJECT = FunctionDefinitionFactory.create("fromObject", Version.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                holder.get(MAJOR).set( ((Version) arguments[0]).get(MAJOR).get());
                holder.get(MINOR).set( ((Version) arguments[0]).get(MINOR).get());
                holder.get(REVISION).set( ((Version) arguments[0]).get(REVISION).get());

                return invocation.next(arguments);
            }

        }, Version.class);

        TO_STRING.addExecutor("default", Version.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                String string = holder.get(MAJOR).get() + "." + holder.get(MINOR).get() + "." + holder.get(REVISION).get();

                invocation.next(arguments);
                return string;
            }

        });
        FROM_STRING.addExecutor("default", Version.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                String[] versionParts = ((String) arguments[0]).split("\\.");
                Validate.isTrue(versionParts.length == 3, "The version string must be splitted in 3 parts by dots (e.g. 1.2.5): ", Arrays.toString(versionParts));

                FeatureHolder holder = invocation.getHolder();
                holder.get(MAJOR).set(Integer.parseInt(versionParts[0]));
                holder.get(MINOR).set(Integer.parseInt(versionParts[1]));
                holder.get(REVISION).set(Integer.parseInt(versionParts[2]));

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new version object.
     */
    public Version() {

    }

}
