
package com.quartercode.disconnected.sim.comp.program;

/**
 * A parameter is a placeholder for an actual argument.
 * Parameters and arguments are used for providing information for a new process.
 * Examples:
 * 
 * <pre>
 * cd ../
 * > Parameter "rest" is filled with ["../"].
 * 
 * test -a bcdef --gold
 * > Parameter "a" is filled with "bcdef".
 * > Switch "gold" is filled with "true".
 * > A fictional parameter "test" would be filled with "null".
 * </pre>
 * 
 * @see Program
 */
public class Parameter {

    /**
     * Creates a new boolean switch with the given name and short form.
     * A switch only has the two options "set" and "not set".
     * Examples:
     * 
     * <pre>
     * test --switch
     * test -s
     * > Switch "switch" is set.
     * 
     * test
     * > Switch "switch" is not set.
     * </pre>
     * 
     * @param name The name of the switch (usage: --name).
     * @param shortName The short form to use for usingthe switch (usage: -n).
     * @return The created switch.
     */
    public static Parameter createSwitch(String name, String shortName) {

        return new Parameter(name, shortName, null, false, false);
    }

    /**
     * Creates a new argument parameter with the given name and short form.
     * An argument parameter has an argument value which may has to be set.
     * Examples:
     * 
     * <pre>
     * test --argument somearg
     * test --a somearg
     * > Parameter "argument" is filled with the argument "somearg".
     * 
     * test --argument
     * test --a
     * > Parameter "argument" is filled with the argument "null" (only possible if argumentRequired is false).
     * 
     * test
     * > Parameter "argument" is filled with the argument "null" (only possible if required is false).
     * </pre>
     * 
     * 
     * @param name The name of the argument parameter (usage: --name).
     * @param shortName The short form to use for usingthe argument parameter (usage: -n).
     * @param type The type an argument given to the parameter should have.
     * @param required True if the parameter must be set (not the argument, so --param without an argument is possible).
     * @param argumentRequired True if the argument must be set if the parameter is set.
     * @return The created argument parameter.
     */
    public static Parameter createArgument(String name, String shortName, ArgumentType type, boolean required, boolean argumentRequired) {

        return new Parameter(name, shortName, type, required, argumentRequired);
    }

    /**
     * Creates a new "rest" parameter.
     * The rest parameter represents an array of unparsed arguments.
     * Examples:
     * 
     * <pre>
     * cd ../
     * > Parameter "rest" is filled with ["../"].
     * 
     * test -a 12345 something anything
     * > Parameter "rest" is filled with ["something", "anything"]
     * </pre>
     * 
     * @param name The name which is used for describing what the rest does.
     * @param required True if there must be an argument left over unparsed.
     * @return The created rest parameter.
     */
    public static Parameter createRest(String name, boolean required) {

        return new Parameter("rest-" + name, null, null, required, false);
    }

    /**
     * The argument type defines which java type should be accepted for an argument.
     */
    public static enum ArgumentType {

        /**
         * A normal {@link String}.
         */
        STRING {

            @Override
            public Object parse(String input) {

                return input;
            }
        },
        /**
         * A normal {@link Integer} number.
         */
        INTEGER {

            @Override
            public Object parse(String input) {

                return Integer.parseInt(input);
            }
        },
        /**
         * A normal {@link Double} number.
         */
        DOUBLE {

            @Override
            public Object parse(String input) {

                return Double.parseDouble(input);
            }
        };

        /**
         * Parses a given input string into a proper output object.
         * For example, a {@link ArgumentType#INTEGER} parses "123" into the primitive int 123.
         * 
         * @param input The input string to parse.
         * @return The parsed proper output object.
         */
        public abstract Object parse(String input);

    }

    private final String       name;
    private final String       shortName;
    private final ArgumentType type;
    private final boolean      required;
    private final boolean      argumentRequired;

    private Parameter(String name, String shortName, ArgumentType type, boolean required, boolean argumentRequired) {

        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.required = required;
        this.argumentRequired = argumentRequired;
    }

    /**
     * Returns true if this parameter is a switch.
     * A switch only has the two options "set" and "not set".
     * See {@link #createSwitch(String, String)} for more detail.
     * 
     * @return True if this parameter is a switch.
     */
    public boolean isSwitch() {

        return !name.startsWith("rest") && type == null;
    }

    /**
     * Returns true if this parameter is an argument parameter.
     * An argument parameter has an argument value which may has to be set.
     * See {@link #createArgument(String, String, Class, boolean, boolean)} for more detail.
     * 
     * @return True if this parameter is an argument parameter.
     */
    public boolean isArgument() {

        return !name.startsWith("rest") && type != null;
    }

    /**
     * Returns true if this parameter is the rest parameter.
     * The rest parameter represents an array of unparsed arguments.
     * See {@link #createRest(boolean)} for more detail.
     * 
     * @return True if this parameter is the rest parameter.
     */
    public boolean isRest() {

        return name.startsWith("rest") && type == null;
    }

    /**
     * Returns the name of this parameter (usage: --name).
     * The name is only set if this parameter is a switch ({@link #isSwitch()}) or an argument parameter ({@link #isArgument()}).
     * 
     * @return The name of this parameter.
     */
    public String getName() {

        return isRest() ? name.substring("rest-".length(), name.length()) : name;
    }

    /**
     * Returns the short form to use for using this parameter (usage: -n).
     * The short form is only set if this parameter is a switch ({@link #isSwitch()}) or an argument parameter ({@link #isArgument()}).
     * It is also only set if you used {@link #createSwitch(String, String)} or {@link #createArgument(String, String, Class, boolean, boolean)}.
     * 
     * @return The short form to use for using this parameter.
     */
    public String getShortName() {

        return shortName;
    }

    /**
     * Returns the argument type of this parameter.
     * Of course, the type is only set if this parameter is an argument parameter ({@link #isArgument()}).
     * 
     * @return The argument type of this parameter.
     */
    public ArgumentType getType() {

        return type;
    }

    /**
     * Returns true if the parameter must be set.
     * This is only set if this parameter is an argument parameter ({@link #isArgument()}) or a rest one ({@link #isRest()}).
     * This does not apply to the argument, so --param without an argument is possible.
     * 
     * @return True if the parameter must be set.
     */
    public boolean isRequired() {

        return required;
    }

    /**
     * Returns True if the argument must be set if the parameter is set.
     * Of course, this is only set if this parameter is an argument parameter ({@link #isArgument()}).
     * This does not apply to the parameter itself, so an argument string without this parameter is possible.
     * 
     * @return True if the argument must be set.
     */
    public boolean isArgumentRequired() {

        return argumentRequired;
    }

}
