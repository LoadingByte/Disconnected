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

package com.quartercode.disconnected.sim.comp.program;

/**
 * The argument exception can occur during the parsing of arguments.
 * For example, a required argument could not been set.
 */
public class ArgumentException extends Exception {

    private static final long serialVersionUID = 1577607142852766455L;

    /**
     * This enumeration describes the different types of exceptions which can occur.
     */
    public static enum ArgumentExceptionType {

        /**
         * A required parameter is not set.
         */
        REQUIRED_NOT_SET,
        /**
         * The argument of an argument parameter which requires an argument is not set.
         */
        ARGUMENT_REQUIRED_NOT_SET,
        /**
         * An argument has the wrong type or can't be parsed.
         */
        WRONG_ARGUMENT_TYPE;

    }

    private final Parameter             parameter;
    private final ArgumentExceptionType type;

    /**
     * Creates a new argument exception and initalizes the important values.
     * 
     * @param parameter The parameter which triggered the exception.
     * @param type The type of argument error which occurred.
     */
    public ArgumentException(Parameter parameter, ArgumentExceptionType type) {

        super("Argument exception with parameter " + parameter + " (" + type + ")");

        this.parameter = parameter;
        this.type = type;
    }

    /**
     * Returns the parameter which triggered the exception.
     * 
     * @return The parameter which triggered the exception.
     */
    public Parameter getParameter() {

        return parameter;
    }

    /**
     * Returns the type of argument error which occurred.
     * 
     * @return The type of argument error which occurred.
     */
    public ArgumentExceptionType getType() {

        return type;
    }

}
