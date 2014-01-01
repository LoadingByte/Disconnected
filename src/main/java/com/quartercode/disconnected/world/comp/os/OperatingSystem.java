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

package com.quartercode.disconnected.world.comp.os;

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.Vulnerability;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Process.ProcessState;

/**
 * This class stores information about an operating system.
 * 
 * @see ProcessManager
 * @see FileSystemManager
 * @see NetworkManager
 */
// TODO: Replace OperatingSystem with root process and managers with process deamons -> Everything will be done using processes
public class OperatingSystem extends WorldChildFeatureHolder<Computer> implements SyscallInvoker {

    // ----- Properties -----

    /**
     * The name of the operating system.
     */
    protected static final FeatureDefinition<ObjectProperty<String>>             NAME;

    /**
     * The {@link Version} of the operating system.
     */
    protected static final FeatureDefinition<ObjectProperty<Version>>            VERSION;

    /**
     * The {@link Vulnerability}s the operating system has.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<Vulnerability>>> VULNERABILITIES;

    static {

        NAME = new AbstractFeatureDefinition<ObjectProperty<String>>("name") {

            @Override
            public ObjectProperty<String> create(FeatureHolder holder) {

                return new ObjectProperty<String>(getName(), holder);
            }

        };

        VERSION = new AbstractFeatureDefinition<ObjectProperty<Version>>("version") {

            @Override
            public ObjectProperty<Version> create(FeatureHolder holder) {

                return new ObjectProperty<Version>(getName(), holder);
            }

        };

        VULNERABILITIES = new AbstractFeatureDefinition<ObjectProperty<Set<Vulnerability>>>("vulnerabilities") {

            @Override
            public ObjectProperty<Set<Vulnerability>> create(FeatureHolder holder) {

                return new ObjectProperty<Set<Vulnerability>>(getName(), holder, new HashSet<Vulnerability>());
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the name of the operating system.
     */
    public static final FunctionDefinition<String>                               GET_NAME;

    /**
     * Changes the name of the operating system.
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
     * <td>name</td>
     * <td>The new name for the operating system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 SET_NAME;

    /**
     * Returns the {@link Version} of the operating system.
     */
    public static final FunctionDefinition<Version>                              GET_VERSION;

    /**
     * Changes the {@link Version} of the operating system.
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
     * <td>The new {@link Version} for the operating system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 SET_VERSION;

    /**
     * Returns the {@link Vulnerability}s the operating system has.
     */
    public static final FunctionDefinition<Set<Vulnerability>>                   GET_VULNERABILITIES;

    /**
     * Adds {@link Vulnerability}s to the operating system.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Vulnerability}...</td>
     * <td>vulnerabilities</td>
     * <td>The {@link Vulnerability}s to add to the operating system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 ADD_VULNERABILITIES;

    /**
     * Removes {@link Vulnerability}s from the operating system.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Vulnerability}...</td>
     * <td>vulnerabilities</td>
     * <td>The {@link Vulnerability}s to remove from the operating system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 REMOVE_VULNERABILITIES;

    static {

        GET_NAME = FunctionDefinitionFactory.create("getName", OperatingSystem.class, PropertyAccessorFactory.createGet(NAME));
        SET_NAME = FunctionDefinitionFactory.create("setName", OperatingSystem.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(NAME)), String.class);

        GET_VERSION = FunctionDefinitionFactory.create("getVersion", OperatingSystem.class, PropertyAccessorFactory.createGet(VERSION));
        SET_VERSION = FunctionDefinitionFactory.create("setVersion", OperatingSystem.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(VERSION)), Version.class);

        GET_VULNERABILITIES = FunctionDefinitionFactory.create("getVulnerabilities", OperatingSystem.class, CollectionPropertyAccessorFactory.createGet(VULNERABILITIES));
        ADD_VULNERABILITIES = FunctionDefinitionFactory.create("addVulnerabilities", OperatingSystem.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createAdd(VULNERABILITIES)), Vulnerability[].class);
        REMOVE_VULNERABILITIES = FunctionDefinitionFactory.create("removeVulnerabilities", OperatingSystem.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createRemove(VULNERABILITIES)), Vulnerability[].class);

        GET_OPERATING_SYSTEM.addExecutor(OperatingSystem.class, "default", new FunctionExecutor<OperatingSystem>() {

            @Override
            public OperatingSystem invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return (OperatingSystem) holder;
            }
        });

    }

    // ----- Functions End -----

    /**
     * Creates a new operating system.
     */
    public OperatingSystem() {

        // ----- Temporary -----
        processManager = new ProcessManager(this);
        fileSystemManager = new FileSystemManager(this);
        networkManager = new NetworkManager(this);
        // ----- Temporary End -----
    }

    // TODO: Remove
    // ----- Temporary -----

    @XmlElement
    private final ProcessManager    processManager;
    @XmlElement
    private final FileSystemManager fileSystemManager;
    private final NetworkManager    networkManager;

    /**
     * Returns the process manager which is used for holding and modifying processes.
     * 
     * @return The process manager which is used for holding and modifying processes.
     */
    public ProcessManager getProcessManager() {

        return processManager;
    }

    /**
     * Returns the file system manager which is used for holding and modifying file systems.
     * 
     * @return The file system manager which is used for holding and modifying file systems.
     */
    public FileSystemManager getFileSystemManager() {

        return fileSystemManager;
    }

    /**
     * Returns the file system manager which is used for storing and delivering packets.
     * 
     * @return The file system manager which is used for storing and delivering packets.
     */
    public NetworkManager getNetworkManager() {

        return networkManager;
    }

    /**
     * Returns if the operating system is running.
     * 
     * @return True if the operating system is running, false if not.
     * @throws FunctionExecutionException Can't access the state information for the root {@link Process}.
     */
    @XmlTransient
    public boolean isRunning() throws FunctionExecutionException {

        return processManager.getRootProcess() != null && processManager.getRootProcess().get(Process.GET_STATE).invoke() != ProcessState.STOPPED;
    }

    /**
     * Changes the running state of the operating system.
     * 
     * @param running True if the operating system is running, false if not.
     * @throws FunctionExecutionException Something goes wrong while enabling or disabling some child managers.
     */
    public void setRunning(boolean running) throws FunctionExecutionException {

        if (running) {
            fileSystemManager.setRunning(true);
            processManager.setRunning(true);
        } else {
            processManager.setRunning(false);
            fileSystemManager.setRunning(false);
        }
    }

    // ----- Temporary End -----

}
