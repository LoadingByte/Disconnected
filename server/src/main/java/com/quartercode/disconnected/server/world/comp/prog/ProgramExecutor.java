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

package com.quartercode.disconnected.server.world.comp.prog;

import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessState;

/**
 * This abstract class defines a program executor which takes care of actually running a program.
 * A running program executor is generally managed by a {@link Process}.
 * On the file system, program executors are stored in {@link Program} objects which in turn are stored in content files.
 *
 * @see Program
 * @see Process
 */
public abstract class ProgramExecutor extends WorldNode<Process<?>> {

    /**
     * This callback is executed exactly one time when the program executor should start running its program.
     * For example, this method could schedule new tasks or add some event handlers.<br>
     * <br>
     * <b>Important note:</b>
     * If this method registers some kind of hooks apart from scheduler tasks (for example, if it registers event handlers), they must be removed when the program stops.
     * For doing that, {@link ProcessStateListener}s should be added to the executor's {@link Process} using {@link Process#addStateListener(ProcessStateListener)}.
     * Those listeners can then execute all cleanup activities as soon as the process state is changed to {@link WorldProcessState#STOPPED}.<br>
     * <br>
     * Note that you do not have to implement those listeners yourself.
     * The {@code com.quartercode.disconnected.server.world.comp.prog.util} package contains a lot of utility methods that register hooks and take care of the cleanup.
     * That way, most hook registrations are reduced to one-liners.
     */
    public abstract void run();

}
