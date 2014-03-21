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

package com.quartercode.disconnected.world.comp.hardware;

import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.NeedsMainboardSlot;

/**
 * This class represents a hard drive of a {@link Computer}.
 * A hard drive only has it's size stored (given in bytes).
 * The hard drive stores a {@link FileSystem} which stores {@link File}s that can be accessed like regular files.
 * 
 * @see Hardware
 * @see FileSystem
 */
@NeedsMainboardSlot
public class HardDrive extends Hardware {

    // ----- Properties -----

    /**
     * The {@link FileSystem} the hard drive contains.
     * It is constructed automatically after creation.
     */
    protected static final FeatureDefinition<ObjectProperty<FileSystem>> FILE_SYSTEM;

    static {

        FILE_SYSTEM = ObjectProperty.createDefinition("fileSystem", new FileSystem());

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link FileSystem} the hard drive contains.
     */
    public static final FunctionDefinition<FileSystem>                   GET_FILE_SYSTEM;

    /**
     * Changes the {@link FileSystem} the hard drive contains.
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
     * <td>{@link FileSystem}</td>
     * <td>fileSystem</td>
     * <td>The new {@link FileSystem}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                         SET_FILE_SYSTEM;

    static {

        GET_FILE_SYSTEM = FunctionDefinitionFactory.create("getFileSystem", HardDrive.class, PropertyAccessorFactory.createGet(FILE_SYSTEM));
        SET_FILE_SYSTEM = FunctionDefinitionFactory.create("setFileSystem", HardDrive.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(FILE_SYSTEM)), FileSystem.class);

    }

    // ----- Functions End -----

    /**
     * Creates a new hard drive.
     */
    public HardDrive() {

    }

}
