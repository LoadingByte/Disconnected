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

package com.quartercode.disconnected.world.comp.program.shell;

import java.text.MessageFormat;
import com.quartercode.disconnected.world.comp.net.PacketListener;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.session.ShellMessage;
import com.quartercode.disconnected.world.comp.session.ShellMessage.ShellMessageSender;

/**
 * This abstract class defines a shell program executor which takes care of acutally running a program in a shell.
 * Programs using a shell program executor can only be used in shell sessions.
 * The executor class is set in the program.
 * 
 * @see Program
 * @see Process
 * @see PacketListener
 */
public abstract class ShellProgramExecutor extends ProgramExecutor implements ShellMessageSender {

    /**
     * Creates a new empty shell program executor.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ShellProgramExecutor() {

    }

    /**
     * Creates a new shell program executor.
     * 
     * @param host The host process which uses the created executor for running the program instance.
     */
    public ShellProgramExecutor(Process host) {

        super(host);
    }

    @Override
    public String translateShellMessage(ShellMessage message) {

        return MessageFormat.format( ((Program) getHost().getFile().getContent()).getResourceBundle().getString(message.toKey()), message.getVariables());
    }

}
