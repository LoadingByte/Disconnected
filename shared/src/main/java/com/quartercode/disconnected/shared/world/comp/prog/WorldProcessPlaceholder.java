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

package com.quartercode.disconnected.shared.world.comp.prog;

import java.io.Serializable;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A process placeholder represents a world process by storing commonly used data about it.
 */
public class WorldProcessPlaceholder implements Serializable {

    private static final long               serialVersionUID = 166313506712944198L;

    private final WorldProcessId            id;
    private final WorldProcessState         state;
    private final String                    user;
    private final WorldProcessPlaceholder[] children;                              // Optional

    private final String                    sourcePath;
    private final String                    programName;

    /**
     * Creates a new process placeholder <b>without</b> the child processes of the represented process using the provided data.
     * 
     * @param id The {@link WorldProcessId} which identifies the represented world process across the whole world.
     * @param state The {@link WorldProcessState} which defines the global state of the represented world process as seen by the OS.
     * @param user The resolved name of the user the represented world process is running under.
     * @param sourcePath The file path to the file which contains the world program ran by the represented world process.
     * @param programName The name of the world program ran by the represented world process (e.g. {@code fileManager}).
     */
    public WorldProcessPlaceholder(WorldProcessId id, WorldProcessState state, String user, String sourcePath, String programName) {

        this(id, state, user, null, sourcePath, programName);
    }

    /**
     * Creates a new process placeholder <b>with</b> the child processes of the represented process using the provided data.
     * 
     * @param id The {@link WorldProcessId} which identifies the represented world process across the whole world.
     * @param state The {@link WorldProcessState} which defines the global state of the represented world process as seen by the OS.
     * @param user The resolved name of the user the represented world process is running under.
     * @param children The {@link WorldProcessPlaceholder}s which represent the processes that were launched by the represented world process.
     * @param sourcePath The file path to the file which contains the world program ran by the represented world process.
     * @param programName The name of the world program ran by the represented world process (e.g. {@code fileManager}).
     */
    public WorldProcessPlaceholder(WorldProcessId id, WorldProcessState state, String user, WorldProcessPlaceholder[] children, String sourcePath, String programName) {

        Validate.notNull(id, "Process placeholder pid cannot be null");
        Validate.notNull(state, "Process placeholder state cannot be null");
        Validate.notNull(user, "Process placeholder user cannot be null");
        Validate.notNull(sourcePath, "Process placeholder source path cannot be null");
        Validate.notNull(programName, "Process placeholder program name cannot be null");

        this.id = id;
        this.state = state;
        this.user = user;
        this.children = children == null ? null : children.clone();
        this.sourcePath = sourcePath;
        this.programName = programName;
    }

    /**
     * Returns a {@link WorldProcessId} which identifies the represented world process across the whole world.
     * It also contains the numerical local pid value ({@link WorldProcessId#getPid()}).
     * 
     * @return The world process id.
     */
    public WorldProcessId getId() {

        return id;
    }

    /**
     * Returns the {@link WorldProcessState} which defines the global state of the represented world process as seen by the OS.
     * It stores whether the process is running, interrupted etc.
     * 
     * @return The process state.
     */
    public WorldProcessState getState() {

        return state;
    }

    /**
     * Returns the resolved name of the user the represented world process is running under.
     * That user is retrieved by looking at the first session process that occurs when walking up the process tree from the represented process.
     * 
     * @return The process user which defines the rights of the process.
     */
    public String getUser() {

        return user;
    }

    /**
     * Returns an array of {@link WorldProcessPlaceholder}s which represent the processes that were launched by the represented world process.
     * Therefore, they are called "child processes".<br>
     * <br>
     * Note that this child array may be {@code null} if the creator of the placeholder didn't want to add this information.
     * That might be the case if the information is not needed and would result in useless data being sent over the network.
     * 
     * @return The child processes of the represented process.
     *         May be {@code null} if the creator of the placeholder didn't want to add this information.
     */
    public WorldProcessPlaceholder[] getChildren() {

        return children == null ? null : children.clone();
    }

    /**
     * Returns the file path to the file which contains the world program ran by the represented world process.
     * Note that a resolved program name can be retrieved using the {@link #getProgramName()} method.
     * 
     * @return The program file path.
     * @see #getProgramName()
     */
    public String getSourcePath() {

        return sourcePath;
    }

    /**
     * Returns the name of the world program ran by the represented world process (e.g. {@code fileManager}).
     * It represents the program executor class as seen by this application.
     * 
     * @return The program name.
     */
    public String getProgramName() {

        return programName;
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

}
