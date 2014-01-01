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
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Lockable;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;

/**
 * This class represents a simple version.
 * A version contains a major version, minor version and a patch level.
 */
public class Version extends DefaultFeatureHolder {

    // ----- Properties -----

    /**
     * The major version component which is changed after very large changes.
     */
    protected static final FeatureDefinition<ObjectProperty<Integer>> MAJOR;

    /**
     * The minor version component which is changed if there are new features.
     */
    protected static final FeatureDefinition<ObjectProperty<Integer>> MINOR;

    /**
     * The revision version component which is changed after fixes.
     */
    protected static final FeatureDefinition<ObjectProperty<Integer>> REVISION;

    static {

        MAJOR = new AbstractFeatureDefinition<ObjectProperty<Integer>>("major") {

            @Override
            public ObjectProperty<Integer> create(FeatureHolder holder) {

                return new ObjectProperty<Integer>(getName(), holder);
            }

        };

        MINOR = new AbstractFeatureDefinition<ObjectProperty<Integer>>("minor") {

            @Override
            public ObjectProperty<Integer> create(FeatureHolder holder) {

                return new ObjectProperty<Integer>(getName(), holder);
            }

        };

        REVISION = new AbstractFeatureDefinition<ObjectProperty<Integer>>("revision") {

            @Override
            public ObjectProperty<Integer> create(FeatureHolder holder) {

                return new ObjectProperty<Integer>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the major version component which is changed after very large changes.
     */
    public static final FunctionDefinition<Integer>                   GET_MAJOR;

    /**
     * Changes the major version component which is changed after very large changes.
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
     * <td>{@link Integer}</td>
     * <td>major</td>
     * <td>The new major version component.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_MAJOR;

    /**
     * Returns the minor version component which is changed if there are new features.
     */
    public static final FunctionDefinition<Integer>                   GET_MINOR;

    /**
     * Changes the minor version component which is changed if there are new features.
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
     * <td>{@link Integer}</td>
     * <td>minor</td>
     * <td>The new minor version component.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_MINOR;

    /**
     * Returns the revision version component which is changed after fixes.
     */
    public static final FunctionDefinition<Integer>                   GET_REVISION;

    /**
     * Changes the revision version component which is changed after fixes.
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
     * <td>{@link Integer}</td>
     * <td>revision</td>
     * <td>The new revision version component.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_REVISION;

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
    public static final FunctionDefinition<Void>                      FROM_OBJECT;

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
    public static final FunctionDefinition<Void>                      FROM_STRING;

    /**
     * Returns the stored version as a string.
     * The string is using the format MAJOR.MINOR.PATCHLEVEL (e.g. 1.2.5).
     */
    public static final FunctionDefinition<String>                    TO_STRING;

    static {

        GET_MAJOR = FunctionDefinitionFactory.create("getMajor", Version.class, PropertyAccessorFactory.createGet(MAJOR));
        SET_MAJOR = FunctionDefinitionFactory.create("setMajor", Version.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(MAJOR)), Integer.class);

        GET_MINOR = FunctionDefinitionFactory.create("getMinor", Version.class, PropertyAccessorFactory.createGet(MINOR));
        SET_MINOR = FunctionDefinitionFactory.create("setMinor", Version.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(MINOR)), Integer.class);

        GET_REVISION = FunctionDefinitionFactory.create("getRevision", Version.class, PropertyAccessorFactory.createGet(REVISION));
        SET_REVISION = FunctionDefinitionFactory.create("setRevision", Version.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(REVISION)), Integer.class);

        FROM_OBJECT = FunctionDefinitionFactory.create("fromObject", Version.class, new FunctionExecutor<Void>() {

            @Override
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                holder.get(SET_MAJOR).invoke( ((Version) arguments[0]).get(GET_MAJOR).invoke());
                holder.get(SET_MINOR).invoke( ((Version) arguments[0]).get(GET_MINOR).invoke());
                holder.get(SET_REVISION).invoke( ((Version) arguments[0]).get(GET_REVISION).invoke());

                return null;
            }

        }, Version.class);

        FROM_STRING = FunctionDefinitionFactory.create("fromString", Version.class, new FunctionExecutor<Void>() {

            @Override
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                String[] versionParts = ((String) arguments[0]).split("\\.");
                Validate.isTrue(versionParts.length == 3, "The version string must be splitted in 3 parts by dots (e.g. 1.2.5): ", Arrays.toString(versionParts));
                holder.get(SET_MAJOR).invoke(Integer.parseInt(versionParts[0]));
                holder.get(SET_MINOR).invoke(Integer.parseInt(versionParts[1]));
                holder.get(SET_REVISION).invoke(Integer.parseInt(versionParts[2]));

                return null;
            }

        }, String.class);

        TO_STRING = FunctionDefinitionFactory.create("toString", Version.class, new FunctionExecutor<String>() {

            @Override
            public String invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return holder.get(GET_MAJOR).invoke() + "." + holder.get(GET_MINOR).invoke() + "." + holder.get(GET_REVISION).invoke();
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new version object.
     */
    public Version() {

    }

}
