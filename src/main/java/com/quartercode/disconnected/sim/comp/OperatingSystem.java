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

package com.quartercode.disconnected.sim.comp;

import java.util.List;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.sim.run.TickTimer.TimerTask;

/**
 * This class stores information about an operating system.
 * This also contains a list of all vulnerabilities this operating system has.
 * 
 * @see ComputerPart
 */
public class OperatingSystem extends ComputerPart {

    /**
     * This enum represents the right levels a user can has on an operating system.
     * The right level defines what a user can or cannot do. If a user has a right level, he can use every other right level below his one.
     * 
     * @see OperatingSystem
     * @see Program
     */
    public static enum RightLevel {

        GUEST, USER, ADMIN, SYSTEM;
    }

    /**
     * The state of an operating system defines if the system is turned on/off, is switching states etc.
     * 
     * @see OperatingSystem
     */
    public static enum State {

        OFF, SWITCHING_OFF, ON, SWITCHING_ON;
    }

    private static final long serialVersionUID = 1L;

    private int               switchOnTime;
    private int               switchOffTime;

    private State             state;

    /**
     * Creates a new empty operating system.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    public OperatingSystem() {

    }

    /**
     * Creates a new operating system and sets the computer, the name, the version, the vulnerabilities and the times the os needs for switching on/off.
     * 
     * @param name The name the operating system has.
     * @param version The current version the operating system has.
     * @param vulnerabilities The vulnerabilities the operating system has.
     * @param switchOnTime The amount of ticks the system needs to switch on (boot up).
     * @param switchOffTime The amount of ticks the system needs to switch off (shutdown).
     */
    public OperatingSystem(Computer computer, String name, Version version, List<Vulnerability> vulnerabilities, int switchOnTime, int switchOffTime) {

        super(computer, name, version, vulnerabilities);

        this.switchOnTime = switchOnTime;
        this.switchOffTime = switchOffTime;
    }

    /**
     * Returns the amount of ticks the system needs to switch on (boot up).
     * 
     * @return The amount of ticks the system needs to switch on (boot up).
     */
    public int getSwitchOnTime() {

        return switchOnTime;
    }

    /**
     * Returns the amount of ticks the system needs to switch off (shutdown).
     * 
     * @return The amount of ticks the system needs to switch off (shutdown).
     */
    public int getSwitchOffTime() {

        return switchOffTime;
    }

    /**
     * Returns the current state of the operation system.
     * 
     * @return The current state of the operation system.
     */
    public State getState() {

        return state;
    }

    /**
     * Switches the state of the system.
     * This may take a while (for example, this method will boot the system or shut it down).
     * 
     * @param state The state to switch the system to.
     */
    public void switchState(final State state) {

        if (state == State.ON && this.state == State.OFF) {
            this.state = State.SWITCHING_ON;
            Disconnected.getSimulator().getTickTimer().schedule(new TimerTask(switchOnTime) {

                @Override
                public void run() {

                    OperatingSystem.this.state = State.ON;
                }
            });
        } else if (state == State.OFF && this.state == State.ON) {
            this.state = State.SWITCHING_OFF;
            Disconnected.getSimulator().getTickTimer().schedule(new TimerTask(switchOffTime) {

                @Override
                public void run() {

                    OperatingSystem.this.state = State.OFF;
                }
            });
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (state == null ? 0 : state.hashCode());
        result = prime * result + switchOffTime;
        result = prime * result + switchOnTime;
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OperatingSystem other = (OperatingSystem) obj;
        if (state != other.state) {
            return false;
        }
        if (switchOffTime != other.switchOffTime) {
            return false;
        }
        if (switchOnTime != other.switchOnTime) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [switchOnTime=" + switchOnTime + ", switchOffTime=" + switchOffTime + ", state=" + state + ", getName()=" + getName() + ", getVersion()=" + getVersion() + ", getVulnerabilities()=" + getVulnerabilities() + "]";
    }

}
