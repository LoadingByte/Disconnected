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

/**
 * The child process is a simple {@link Process} which has no functions but a generics definition.
 * It is used as a shortcut for creating new {@link Process}es.
 * 
 * @see Process
 */
public class ChildProcess extends Process<Process<?>> {

    /**
     * Creates a new empty child process.
     * You can start the new process using {@link Process#INITIALIZE} and then {@link ProgramExecutor#RUN} on {@link Process#EXECUTOR}.
     */
    public ChildProcess() {

        setParentType(Process.class);
    }

}
