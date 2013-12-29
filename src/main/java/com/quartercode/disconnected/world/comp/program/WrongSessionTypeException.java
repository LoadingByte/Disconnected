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

import com.quartercode.disconnected.world.comp.session.SessionProgram;

/**
 * The wrong session type exception is thrown if a program attempts to run a child process which doesn't support the session.
 * 
 * @see Process
 * @see ProgramExecutor
 * @see SessionProgram
 */
public class WrongSessionTypeException extends Exception {

    private static final long     serialVersionUID = 7508147217329959389L;

    private final Process<?>      process;
    private final ProgramExecutor executor;

    /**
     * Creates a new wrong session type exception and sets the process a program attempted to run and the executor with the wrong type.
     * 
     * @param process The process the parent program attempted to run.
     * @param executor The executor which has the wrong type for the session it's running in.
     */
    public WrongSessionTypeException(Process<?> process, ProgramExecutor executor) {

        super("Can't create process of type " + executor.getClass().getName() + ": Session is of wrong type");

        this.process = process;
        this.executor = executor;
    }

    /**
     * Returns the process the parent program attempted to run.
     * 
     * @return The process the parent program attempted to run.
     */
    public Process<?> getProcess() {

        return process;
    }

    /**
     * Returns the executor which has the wrong type for the session it's running in.
     * 
     * @return The executor which has the wrong type.
     */
    public ProgramExecutor getExecutor() {

        return executor;
    }

}
