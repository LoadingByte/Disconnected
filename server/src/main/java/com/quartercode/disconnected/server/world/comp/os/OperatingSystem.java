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

package com.quartercode.disconnected.server.world.comp.os;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
import com.quartercode.disconnected.server.world.comp.os.mod.OSModule;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.ProcessModule;
import com.quartercode.disconnected.server.world.comp.prog.RootProcess;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.Version;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessState;

/**
 * This class stores information about an operating system.
 * It is the core part of a running computer and manages all required modules (e.g. the {@link FileSystemModule file system module}).
 */
public class OperatingSystem extends WorldNode<Computer> {

    @XmlAttribute
    private String           name;
    @XmlAttribute
    private Version          version;

    @XmlElement
    private FileSystemModule fsModule;
    @XmlElement
    private ProcessModule    procModule;
    @XmlElement
    private NetworkModule    netModule;

    // JAXB constructor
    protected OperatingSystem() {

    }

    /**
     * Creates a new operating system.
     *
     * @param name The {@link #getName() name} of the new operating system.
     *        It can be freely chosen.
     * @param version The current {@link #getVersion() version} of the new operating system.
     *        New versions of an OS usually fix vulnerabilities.
     */
    public OperatingSystem(String name, Version version) {

        this.name = name;
        this.version = version;

        fsModule = new FileSystemModule();
        procModule = new ProcessModule();
        netModule = new NetworkModule();
    }

    /**
     * Returns the name of the operating system.
     *
     * @return The name of the OS.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the current {@link Version} of the operating system.
     * New versions of an OS usually fix vulnerabilities.
     *
     * @return The version of the OS.
     */
    public Version getVersion() {

        return version;
    }

    /**
     * Returns the operating system's {@link FileSystemModule} which is used for managing and accessing {@link FileSystem}s.
     * Such file systems are able to store {@link File}s; moreover, the file system objects allow to manipulate those files.
     *
     * @return The file system module of the OS.
     */
    public FileSystemModule getFsModule() {

        return fsModule;
    }

    /**
     * Returns the operating system's {@link ProcessModule} which is used for managing the whole {@link Process} tree.
     * Processes are used to represent active or inactive "tasks", ranging from the most simple programs (e.g. file management programs) to more complex ones.
     *
     * @return The process module of the OS.
     */
    public ProcessModule getProcModule() {

        return procModule;
    }

    /**
     * Returns the operating system's {@link NetworkModule} which takes care of sending and receiving network {@link Packet}s ("networking").
     * It also allows to create {@link Socket}s for easier connection management.
     *
     * @return The network module of the OS.
     */
    public NetworkModule getNetModule() {

        return netModule;
    }

    /**
     * Returns whether the operating system has already been started up ({@code true}) or shut down ({@code false}).
     * The state is determined by the running state of the {@link RootProcess}, which is provided by the {@link #getProcModule() process module}.
     *
     * @return Whether the operating system (and therefore the entire computer) is running.
     */
    public boolean isRunning() {

        return procModule.getRootProcess().getState() != WorldProcessState.STOPPED;
    }

    /**
     * Boots up ({@code true}) or shuts down ({@code false}) the operating system.
     * If the operating system already is in the wanted state, nothing happens.<br>
     * <br>
     * Internally, this method calls similar {@link OSModule#setRunning(boolean) setRunning()} methods on every used {@link OSModule} which is stored by the OS.
     * Note that this method is dynamic and recognizes all OS module attributes of this class.
     * Therefore, you can safely "mod in" more module fields without having the fear that their {@code setRunning()} method won't be called.
     *
     * @param running {@code True} to boot up the operating system, {@code false} to shut it down.
     */
    public void setRunning(boolean running) {

        for (Object child : getChildren()) {
            if (child instanceof OSModule) {
                try {
                    ((OSModule) child).setRunning(running);
                } catch (RuntimeException e) {
                    if (running == true) {
                        // An unrecoverable error happened while trying to boot up the OS -> shut down the whole computer
                        setRunning(false);
                        return;
                    }
                    // Else: If the OS is currently shutting down and an exception is thrown by one module, ignore that error and continue with the next module
                }
            }
        }
    }

}
