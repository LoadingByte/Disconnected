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

package com.quartercode.disconnected.shared.event.comp.program.general;

import com.quartercode.disconnected.shared.event.comp.program.ProgramEvent;

/**
 * Subclasses of this event are events that are fired by the file manager program.
 */
public class FileManagerProgramEvent extends ProgramEvent {

    /**
     * Creates a new file manager program event.
     * 
     * @param computerId The id of the computer which runs the program the event is fired by.
     * @param pid The process id of the process which runs the program the event is fired by.
     */
    public FileManagerProgramEvent(String computerId, int pid) {

        super(computerId, pid);
    }

}
