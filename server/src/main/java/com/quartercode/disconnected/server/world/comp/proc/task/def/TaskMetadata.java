
package com.quartercode.disconnected.server.world.comp.proc.task.def;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.disconnected.server.world.comp.proc.task.exec.TaskRunner;

/**
 * A data object that defines the "frame" API of a specific {@link TaskExecutor}.
 * One part are the {@link #getInputParameters() input parameters}, which supply the task with some starting information
 * For example, a file delete task would be supplied with a file path so it knows which file to delete.
 * At some defined points (in most cases on finish), the task can report by calling one or more of the defined {@link #getCallbacks() callbacks}.
 * Such callbacks also carry {@link Callback#getOutputParameters() output parameters} which inform the task runner about the task's status or
 * return some requested information (e.g. a file listing).
 *
 * @see TaskDefinition
 * @see TaskExecutor
 */
public class TaskMetadata {

    private final List<Parameter> inputParameters;
    private final List<Callback>  callbacks;

    /**
     * Creates a new world program task metadata object.
     *
     * @param inputParameters The {@link #getInputParameters() input parameters} which supply the task with some starting information.
     * @param callbacks The {@link #getCallbacks() callbacks} which can be called by the task in order to report its status.
     */
    public TaskMetadata(List<Parameter> inputParameters, List<Callback> callbacks) {

        Validate.notNull(inputParameters, "Task input parameter list cannot be null");
        Validate.notNull(callbacks, "Task callback list cannot be null");

        this.inputParameters = Collections.unmodifiableList(inputParameters);
        this.callbacks = Collections.unmodifiableList(callbacks);
    }

    /**
     * Returns the input {@link Parameter}s which supply the task with some starting information
     * For example, a file delete task would be supplied with a file path so it knows which file to delete.
     *
     * @return The task's starting parameters.
     */
    public List<Parameter> getInputParameters() {

        return inputParameters;
    }

    /**
     * Returns the {@link Callback}s which can be called by the task in order to report its status (e.g. that it's finished).
     * Note that callbacks also carry {@link Callback#getOutputParameters() output parameters} which inform the task runner about the task's status or
     * return some requested information (e.g. a file listing).
     *
     * @return The task's available callbacks.
     */
    public List<Callback> getCallbacks() {

        return callbacks;
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * A simple class which defines an input or output parameter with a {@link #getName() name} and a {@link #getType() type}.
     * This class only defines a parameter and how the argument it carries must look like.
     * For example, a file parameter must carry a file object/argument.
     *
     * @see TaskMetadata#getInputParameters()
     * @see Callback#getOutputParameters()
     */
    public static class Parameter {

        private final String   name;
        private final Class<?> type;

        /**
         * Creates a new parameter.
         *
         * @param name The name of the parameter (e.g. {@code "password"}).
         * @param type The {@link #getType() type} any argument which fills in the parameter must be an instance of.
         */
        public Parameter(String name, Class<?> type) {

            Validate.notNull(name, "Task parameter name cannot be null");
            Validate.notNull(type, "Task parameter type cannot be null");

            this.name = name;
            this.type = ClassUtils.primitiveToWrapper(type);
        }

        /**
         * Returns the name of the parameter (e.g. {@code "password"}).
         *
         * @return The parameter name.
         */
        public String getName() {

            return name;
        }

        /**
         * Returns the {@link Class type} any argument which fills in the parameter must be an instance of.
         * For example, parameter with the type {@code File} must carry a file object/argument.
         * In the case of primitives, this method always returns a {@link Class} representing the boxed version.
         *
         * @return The parameter type.
         */
        public Class<?> getType() {

            return type;
        }

        @Override
        public int hashCode() {

            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {

            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString() {

            return ToStringBuilder.reflectionToString(this);
        }

    }

    /**
     * An advanced <b>input</b> parameter which also provides a default value in case the user doesn't specify one.
     * For example, tasks can provide configuration options with default values so that the user doesn't have to input the same typical configuration over and over again.
     * Note that optional parameters should only be used for input parameters. On output ones, they don't have any effect.
     */
    public static class OptionalParameter extends Parameter {

        private final Object defaultValue;

        /**
         * Creates a new optional parameter.
         *
         * @param name The name of the parameter (e.g. {@code "password"}).
         * @param type The {@link #getType() type} any argument which fills in the parameter must be an instance of.
         * @param defaultValue The {@link #getDefaultValue() default value} which is used when the user doesn't specifiy his own value for this parameter.
         *        Note that {@code null} is allowed.
         */
        public OptionalParameter(String name, Class<?> type, Object defaultValue) {

            super(name, type);

            this.defaultValue = defaultValue;
        }

        /**
         * Returns the default value which is used when the user doesn't specifiy his own value for this parameter.
         * For example, tasks can provide configuration options with default values so that the user doesn't have to input the same typical configuration over and over again.
         *
         * @return The parameter's default value.
         *         Note that {@code null} is allowed.
         */
        public Object getDefaultValue() {

            return defaultValue;
        }

    }

    /**
     * A callback which can be called by a running {@link TaskExecutor} at some defined points (in most cases on finish) in order to report something.
     * In practice, tasks report to their assigned {@link TaskRunner} by calling one or more of the defined callbacks.
     * The task runner can then decide how to process those callbacks.
     * Note that callbacks also carry {@link Callback#getOutputParameters() output parameters} which inform the receiver about the task's status or
     * return some requested information (e.g. a file listing).
     */
    public static class Callback {

        private final String          name;
        private final List<Parameter> outputParameters;

        public Callback(String name, List<Parameter> outputParameters) {

            Validate.notNull(name, "Task callback name cannot be null");
            Validate.notNull(outputParameters, "Task callback output parameter list cannot be null");

            this.name = name;
            this.outputParameters = Collections.unmodifiableList(outputParameters);
        }

        /**
         * Returns the name of the callback (e.g. {@code "onFinish"}).
         *
         * @return The callback name.
         */
        public String getName() {

            return name;
        }

        /**
         * Returns the output {@link Parameter}s which carry information to the receiver of a callback call.
         * For example, a file listing task would supply an array with the listed files here.
         *
         * @return The callback's output parameters.
         */
        public List<Parameter> getOutputParameters() {

            return outputParameters;
        }

        @Override
        public int hashCode() {

            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {

            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString() {

            return ToStringBuilder.reflectionToString(this);
        }

    }

}
