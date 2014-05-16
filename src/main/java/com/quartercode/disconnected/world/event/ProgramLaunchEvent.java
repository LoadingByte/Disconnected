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

package com.quartercode.disconnected.world.event;

import lombok.Data;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;

/**
 * The program launch event is sent by a program executor when it's started.
 * It contains general information about the type and identity of launched program.
 */
@Data
public class ProgramLaunchEvent implements Event {

    private static final long                      serialVersionUID = -3412734406679227132L;

    /**
     * The id of the computer that runs the newly launched program.
     */
    private final String                           computerId;

    /**
     * The process id of the process that runs the newly launched program.
     */
    private final int                              pid;

    /**
     * The program executor type (class object) that represents which kind of program was launched.
     */
    private final Class<? extends ProgramExecutor> type;

}
