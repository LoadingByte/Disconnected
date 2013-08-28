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

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.sim.comp.Media.File;
import com.quartercode.disconnected.sim.comp.Media.File.FileType;
import com.quartercode.disconnected.sim.run.TickTimer;
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
    protected OperatingSystem() {

    }

    /**
     * Creates a new operating system and sets the host computer, the name, the version, the vulnerabilities and the times the os needs for switching on/off.
     * 
     * @param host The host computer this part is built in.
     * @param name The name the operating system has.
     * @param version The current version the operating system has.
     * @param vulnerabilities The vulnerabilities the operating system has.
     * @param switchOnTime The amount of ticks the system needs to switch on (boot up).
     * @param switchOffTime The amount of ticks the system needs to switch off (shutdown).
     */
    public OperatingSystem(Computer host, String name, Version version, List<Vulnerability> vulnerabilities, int switchOnTime, int switchOffTime) {

        super(host, name, version, vulnerabilities);

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
            Disconnected.getTicker().getAction(TickTimer.class).schedule(new TimerTask(switchOnTime) {

                @Override
                public void run() {

                    OperatingSystem.this.state = State.ON;
                }
            });
        } else if (state == State.OFF && this.state == State.ON) {
            this.state = State.SWITCHING_OFF;
            Disconnected.getTicker().getAction(TickTimer.class).schedule(new TimerTask(switchOffTime) {

                @Override
                public void run() {

                    OperatingSystem.this.state = State.OFF;
                }
            });
        }
    }

    /**
     * Returns a list containing all media which are connected to this computer.
     * 
     * @return A list containing all media which are connected to this computer.
     */
    public List<MediaProvider> getMedia() {

        List<MediaProvider> media = new ArrayList<MediaProvider>();
        media.addAll(getHost().getHardware(MediaProvider.class));
        return media;
    }

    /**
     * Returns the connected media which uses the given letter.
     * If there is no media with the given letter, this will return null.
     * 
     * @param letter The letter the returned media needs to use.
     * @return The connected media which uses the given letter.
     */
    public MediaProvider getMedia(char letter) {

        for (MediaProvider media : getMedia()) {
            if (media.getLetter() == letter) {
                return media;
            }
        }

        return null;
    }

    /**
     * Returns the connected media on which the file under the given path is stored.
     * A path is a collection of files seperated by a seperator.
     * This requires a global os path.
     * If there is no media the file is stored on, this will return null.
     * 
     * @param path The file represented by this path is stored on the returned media.
     * @return The connected media on which the file under the given path is stored.
     */
    public MediaProvider getMedia(String path) {

        if (path.contains(":")) {
            return getMedia(path.split(":")[0].charAt(0));
        } else {
            return null;
        }
    }

    /**
     * Returns the file which is stored on a media of the computer this os is running on under the given path.
     * A path is a collection of files seperated by a seperator.
     * This will look up the file using a global os path.
     * 
     * @param path The path to look in for the file.
     * @return The file which is stored on a media of the computer this os is running on under the given path.
     */
    public File getFile(String path) {

        MediaProvider media = getMedia(path);
        if (media != null) {
            return media.getFile(path.split(":")[1]);
        } else {
            return null;
        }
    }

    /**
     * Creates a new file using the given path and type on this computer and returns it.
     * If the file already exists, the existing file will be returned.
     * A path is a collection of files seperated by a seperator.
     * This will get the file location using a global os path.
     * 
     * @param path The path the new file will be located under.
     * @param type The file type the new file should has.
     * @return The new file (or the existing one, if the file already exists).
     */
    public File addFile(String path, FileType type) {

        MediaProvider media = getMedia(path);
        if (media != null) {
            return media.addFile(path.split(":")[1], type);
        } else {
            return null;
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

        return getClass().getName() + " [switchOnTime=" + switchOnTime + ", switchOffTime=" + switchOffTime + ", state=" + state + ", toInfoString()=" + toInfoString() + "]";
    }

}
