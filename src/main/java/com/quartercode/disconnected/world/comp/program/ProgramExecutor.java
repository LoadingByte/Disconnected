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

package com.quartercode.disconnected.world.comp.program;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.util.InfoString;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.file.NoFileRightException;
import com.quartercode.disconnected.world.comp.net.Address;
import com.quartercode.disconnected.world.comp.net.Packet;
import com.quartercode.disconnected.world.comp.net.PacketListener;

/**
 * This abstract class defines a program executor which takes care of acutally running a program.
 * The executor class is set in the program.
 * 
 * @see Program
 * @see Process
 * @see PacketListener
 */
public abstract class ProgramExecutor implements InfoString {

    private static final Logger        LOGGER           = Logger.getLogger(ProgramExecutor.class.getName());

    @XmlElement (name = "updateTask")
    private final List<UpdateTask>     updateTasks      = new ArrayList<UpdateTask>();

    @XmlIDREF
    private Process                    host;
    @XmlElement (name = "packetListener")
    private final List<PacketListener> packetListeners  = new ArrayList<PacketListener>();
    @XmlElement (name = "remainingPacket")
    private final Queue<Packet>        remainingPackets = new LinkedList<Packet>();

    /**
     * Creates a new empty program executor.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ProgramExecutor() {

    }

    /**
     * Creates a new program executor.
     * 
     * @param host The host process which uses the created executor for running the program instance.
     */
    public ProgramExecutor(Process host) {

        this.host = host;

        registerTask(new UpdateTask("update", 0, 1));
    }

    /**
     * Registers the given update task so it will be scheduled.
     * Every important parameter for scheduling (e.g. the delay) is set in the task object.
     * 
     * @param task The update task to register/schedule.
     */
    protected void registerTask(UpdateTask task) {

        updateTasks.add(task);
    }

    /**
     * Unregisters the given update task so it will be canceled.
     * Canceled tasks wont elapse any more ticks.
     * 
     * @param task The update timer task to unregister/cancel.
     */
    protected void unregisterTask(UpdateTask task) {

        updateTasks.remove(task);
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
     * Returns null if the host process of this executor hasn't the rights to execute the file.
     * 
     * @param file The process launch file which contains the program for the process.
     * @param arguments The argument map which contains values for the defined parameters.
     * @throws NoFileRightException The host process of this executor hasn't the rights to execute the file.
     * @throws WrongSessionTypeException The executor doesn't support the session it is running in.
     * @throws ArgumentException Some parameters/arguments are not set correctly.
     */
    protected Process createProcess(File file, Map<String, Object> arguments) throws NoFileRightException, WrongSessionTypeException, ArgumentException {

        FileRights.checkRight(host, file, FileRight.EXECUTE);
        return host.createChild(file, arguments);
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
     * Elapses one tick on every update task and invokes the task's method if the timing condition is true.
     * Every program is written using the tick system.
     * The tick can change the state of the program, execute things related to the computer it's running on etc.
     * Every state of an executor can be recovered loading JAXB-marked variables of the object.
     */
    public final void updateTasks() {

        for (UpdateTask task : new ArrayList<UpdateTask>(updateTasks)) {
            if (task.getElapsed() < 0) {
                // Unregister cancelled tasks
                unregisterTask(task);
            } else {
                // Elapse one tick.
                task.elapse();

                try {
                    if (task.getPeriod() <= 0 && task.getElapsed() == task.getDelay()) {
                        getClass().getMethod(task.getMethod()).invoke(this);
                        unregisterTask(task);
                    } else if (task.getPeriod() > 0 && (task.getElapsed() - task.getDelay()) % task.getPeriod() == 0) {
                        Method method = getClass().getMethod(task.getMethod());
                        method.setAccessible(true);
                        method.invoke(this);
                    }
                }
                catch (SecurityException e) {
                    LOGGER.log(Level.SEVERE, "Can't access method '" + task.getMethod() + "' in '" + getClass().getName() + "' for update task", e);
                }
                catch (NoSuchMethodException e) {
                    LOGGER.log(Level.SEVERE, "Can't find method '" + task.getMethod() + "' in '" + getClass().getName() + "' for update task", e);
                }
                catch (IllegalAccessException e) {
                    LOGGER.log(Level.SEVERE, "Can't access method '" + task.getMethod() + "' in '" + getClass().getName() + "' for update task", e);
                }
                catch (InvocationTargetException e) {
                    LOGGER.log(Level.SEVERE, "Can't invoke method '" + task.getMethod() + "' in '" + getClass().getName() + "' for update task", e);
                }
            }
        }
    }

    /**
     * Executes the default tick update in the program executor.
     * Every program is written using the tick system.
     * The tick can change the state of the program, execute things related to the computer it's running on etc.
     * Every state of an executor can be recovered loading JAXB-marked variables of the object.
     */
    public abstract void update();

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (packetListeners == null ? 0 : packetListeners.hashCode());
        result = prime * result + (updateTasks == null ? 0 : updateTasks.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProgramExecutor other = (ProgramExecutor) obj;
        if (packetListeners == null) {
            if (other.packetListeners != null) {
                return false;
            }
        } else if (!packetListeners.equals(other.packetListeners)) {
            return false;
        }
        if (updateTasks == null) {
            if (other.updateTasks != null) {
                return false;
            }
        } else if (!updateTasks.equals(other.updateTasks)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return updateTasks.size() + " update tasks, " + remainingPackets.size() + " remaining packets on " + packetListeners.size() + " listeners";
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
