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

package com.quartercode.disconnected.sim.comp.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.net.Address;
import com.quartercode.disconnected.sim.comp.net.Packet;
import com.quartercode.disconnected.sim.comp.net.PacketListener;
import com.quartercode.disconnected.sim.comp.session.Desktop.Window;
import com.quartercode.disconnected.sim.comp.session.DesktopSessionProgram.DesktopSession;
import com.quartercode.disconnected.sim.comp.session.SessionProgram.Session;

/**
 * This abstract class defines a program executor which takes care of acutally running a program.
 * The executor class is set in the program.
 * 
 * @see Program
 * @see Process
 * @see PacketListener
 */
public abstract class ProgramExecutor {

    @XmlIDREF
    private Process                    host;
    @XmlElementWrapper (name = "packetListeners")
    @XmlElement (name = "listener")
    private final List<PacketListener> packetListeners  = new ArrayList<PacketListener>();
    @XmlElementWrapper (name = "remainingPackets")
    @XmlElement (name = "packet")
    private final Queue<Packet>        remainingPackets = new LinkedList<Packet>();

    /**
     * Creates a new empty program executor.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ProgramExecutor() {

    }

    /**
     * Creates a new empty program executor.
     * 
     * @param host The host process which uses the created executor for running the program instance.
     */
    public ProgramExecutor(Process host) {

        this.host = host;
    }

    /**
     * Returns the host process which uses the created executor for running the program instance.
     * 
     * @return The host process which uses the created executor for running the program instance.
     */
    public Process getHost() {

        return host;
    }

    /**
     * Returns a list of packet listeners which take care of handling incoming packets.
     * 
     * @return A list of packet listeners which take care of handling incoming packets.
     */
    public List<PacketListener> getPacketListeners() {

        return Collections.unmodifiableList(packetListeners);
    }

    /**
     * Returns the packet listener which has the given name identifier.
     * This returns null if there isn't any packet listener with the given name identifier.
     * 
     * @return The packet listener which has the given name identifier.
     */
    public PacketListener getPacketListener(String name) {

        for (PacketListener packetListener : packetListeners) {
            if (packetListener.getName().equals(name)) {
                return packetListener;
            }
        }

        return null;
    }

    /**
     * Returns the packet listener which is bound to the given address.
     * This returns null if there isn't any packet listener with the given binding.
     * 
     * @return The packet listener which is bound to the given address.
     */
    public PacketListener getPacketListener(Address binding) {

        for (PacketListener packetListener : packetListeners) {
            if (packetListener.getBinding().equals(binding)) {
                return packetListener;
            }
        }

        return null;
    }

    /**
     * Registers a new packet listener to the executor.
     * 
     * @param packetListener The new packet listener to register to the executor.
     * @throws There's already a packet listener bound to the given binding.
     */
    public void addPacketListener(PacketListener packetListener) {

        if (host.getHost().getProcessManager().getProcess(packetListener.getBinding()) != null) {
            throw new IllegalStateException("There's already a packet listener bound to " + packetListener.getBinding().toInfoString());
        }

        packetListeners.add(packetListener);
    }

    /**
     * Unregisters a packet listener from the executor.
     * This doesn't close existing connections.
     * 
     * @param packetListener The packet listener to unregister from the executor.
     */
    public void removePacketListener(PacketListener packetListener) {

        packetListeners.remove(packetListener);
    }

    /**
     * Creates a new process using the program stored in the given file.
     * The new process will be a child of the process which hosts this executor.
     * 
     * @param file The process launch file which contains the program for the process.
     * @param arguments The argument map which contains values for the defined parameters.
     * @throws IllegalArgumentException No or wrong argument type for a specific parameter.
     */
    protected Process createProcess(File file, Map<String, Object> arguments) {

        return host.createChild(file, arguments);
    }

    /**
     * Opens a new already created window on the host's desktop.
     * Throws an exception if the host process isn't running under a desktop session.
     * 
     * @param window The window to open on the host's desktop.
     * @throws IllegalStateException The host process isn't running under a desktop session.
     */
    protected void openWindow(Window<?> window) {

        Session session = host.getSession();
        if (session instanceof DesktopSession) {
            ((DesktopSession) session).getDesktop().addWindow(window);
        } else {
            throw new IllegalStateException("The host process is running under " + session.toInfoString() + "; desktop session needed");
        }
    }

    /**
     * Adds a new packet to the list of all remaining packets which should be processed soon.
     * 
     * @param packet The new packet for processing.
     */
    public void receivePacket(Packet packet) {

        remainingPackets.offer(packet);
    }

    /**
     * Returns the packet which should be processed next.
     * 
     * @param remove True if the returned packet should be removed from the queue.
     * @return The packet which should be processed next.
     */
    protected Packet nextReceivedPacket(boolean remove) {

        if (remove) {
            return remainingPackets.poll();
        } else {
            return remainingPackets.peek();
        }
    }

    /**
     * Executes a tick update in the program executor.
     * Every program is written using the tick system.
     * The tick can change the state of the program, execute things related to the computer it's running on etc.
     * Every state of an executor can be recovered loading JAXB-marked variables of the object.
     */
    public abstract void update();

}
