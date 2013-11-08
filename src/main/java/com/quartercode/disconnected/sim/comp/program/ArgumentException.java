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
 * For example, a required parameter could not been set.
 */
public class ArgumentException extends Exception {

    private static final long serialVersionUID = 2473994665811595894L;

    private final Parameter   parameter;

    /**
     * Creates a new argument exception.
     * 
     * @param parameter The parameter which triggered the exception.
     */
    protected ArgumentException(Parameter parameter) {

        super("Argument exception with parameter " + parameter.getName());

        this.parameter = parameter;
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
     * The missing parameter exception occurs if there's a missing parameter.
     * Example:
     * 
     * <pre>
     * Note: -b is a required parameter.
     * 
     * test -a -b -c
     * > Works
     * 
     * test -a -c
     * > MissingParameterException: -b is missing
     * </pre>
     * 
     * Of course, required parameters do only make sense in combination with arguments.
     */
    public static class MissingParameterException extends ArgumentException {

        private static final long serialVersionUID = 2138955512792779485L;

        /**
         * Creates a new missing parameter exception.
         * 
         * @param parameter The parameter which triggered the exception.
         */
        public MissingParameterException(Parameter parameter) {

            super(parameter);
        }

    }

    /**
     * The missing argument exception occurs if there's a missing argument for a parameter.
     * Example:
     * 
     * <pre>
     * Note: &lt;arg&gt; for -a is a required argument.
     * 
     * test
     * test -a something
     * > Works
     * 
     * test -a
     * > ArgumentRequiredException: &lt;arg&gt; for -a is missing
     * </pre>
     */
    public static class MissingArgumentException extends ArgumentException {

        private static final long serialVersionUID = -7408390821833517544L;

        /**
         * Creates a new missing argument exception.
         * 
         * @param parameter The parameter which triggered the exception.
         */
        public MissingArgumentException(Parameter parameter) {

            super(parameter);
        }

    }

    /**
     * The wrong argument type exception occurs if an argument has the wrong type or can't be parsed.
     * Example:
     * 
     * <pre>
     * Note: -a awaits an integer.
     * 
     * test -a 12345
     * > Works
     * 
     * test -a something
     * > WrongArgumentTypeException: "something" is not an integer
     * </pre>
     */
    public static class WrongArgumentTypeException extends ArgumentException {

        private static final long serialVersionUID = 9039285869652712981L;

        private final String      argument;

        /**
         * Creates a new wrong argument type exception.
         * 
         * @param parameter The parameter which triggered the exception.
         * @param argument The argument which has the wrong type as a string.
         */
        public WrongArgumentTypeException(Parameter parameter, String argument) {

            super(parameter);

            this.argument = argument;
        }

        /**
         * Returns the argument which has the wrong type as a string.
         * 
         * @return The argument which has the wrong type.
         */
        public String getArgument() {

            return argument;
        }

    }

}
