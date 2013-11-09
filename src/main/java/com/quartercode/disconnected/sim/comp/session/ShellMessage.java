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

package com.quartercode.disconnected.sim.comp.session;

import com.quartercode.disconnected.util.InfoString;

/**
 * Shell messages are used to communicate between a shell and a shell user.
 * Because of the fact that the shell user can be a human or a computer, a shell message only contains information for understanding it's meaning.
 * For getting a human-readable string of a message, you can user {@link #translate()}.
 */
public class ShellMessage implements InfoString {

    private final ShellMessageSender sender;
    private final ShellMessageType   type;
    private final String             message;
    private final Object[]           variables;

    /**
     * Creates a new shell message.
     * 
     * @param sender The {@link ShellMessageSender} who sent the message.
     * @param type The type of the message (INFO, WARNING etc.).
     * @param message The actual message. This is provided by the program and should be easy-to-read for a program.
     * @param variables Some variables which can be different on every message.
     */
    public ShellMessage(ShellMessageSender sender, ShellMessageType type, String message, Object... variables) {

        this.sender = sender;
        this.type = type;
        this.message = message;
        this.variables = variables;
    }

    /**
     * Returns the {@link ShellMessageSender} who sent the message.
     * The sender is also used for translating the message, since he's the only one who exactly knows what it's about.
     * 
     * @return The {@link ShellMessageSender} who sent the message.
     */
    public ShellMessageSender getSender() {

        return sender;
    }

    /**
     * Returns The type of the message (INFO, WARNING etc.).
     * The type is some kind of level. For example, it's used for coloring outputs.
     * 
     * @return The type of the message.
     */
    public ShellMessageType getType() {

        return type;
    }

    /**
     * The actual message.
     * It's provided by the program and should be easy-to-read for a program.
     * Programs which send messages should document them heavily.
     * 
     * @return The actual message of message container.
     */
    public String getMessage() {

        return message;
    }

    /**
     * Returns some variables which can be different on every message.
     * Variables are used to describe the environment of the message and causes why the message occurred.
     * 
     * @return An array containing the variables.
     */
    public Object[] getVariables() {

        return variables;
    }

    /**
     * Returns a key which specifies the message and can be used for describing what the message is about.
     * 
     * @return A key which specifies the message.
     */
    public String toKey() {

        return type.toString().toLowerCase() + "." + message;
    }

    /**
     * Translates the message into a human-readable one.
     * This uses the {@link ShellMessageSender#translateShellMessage(ShellMessage)} method internally.
     * 
     * @return The message in a human-readable format.
     */
    public String translate() {

        return sender.translateShellMessage(this);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (message == null ? 0 : message.hashCode());
        result = prime * result + (sender == null ? 0 : sender.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
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
        ShellMessage other = (ShellMessage) obj;
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (sender == null) {
            if (other.sender != null) {
                return false;
            }
        } else if (!sender.equals(other.sender)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return sender.getClass().getSimpleName() + "." + toKey();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

    /**
     * Shell message senders are able to send {@link ShellMessage}s.
     * The interface is also used for translating messages into human-readable ones.
     */
    public static interface ShellMessageSender {

        /**
         * Translates the given message into a human-readable format.
         * 
         * @param message The message to translate.
         * @return The given message in a human-readable format.
         */
        public String translateShellMessage(ShellMessage message);

    }

    /**
     * The shell message type is used for declaring how important a message is.
     */
    public static enum ShellMessageType {

        /**
         * A help message is sent if a user requests help a the message sender.
         */
        HELP,
        /**
         * Only an informational message.
         * It can hold information, but it's not very important for the user.
         */
        INFO,
        /**
         * A warning which describes a problem that have occurred.
         * It often holds important information and should be processed.
         */
        WARNING,
        /**
         * An error message which describes a critical problem that have occurred.
         * It often holds important information and should definitely be processed.
         */
        ERROR;

    }

}
