
package com.quartercode.disconnected.server.world.comp.proc.task.def;

import java.util.Map;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata.OptionalParameter;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata.Parameter;

/**
 * A missing argument exception is thrown if a {@link TaskExecutor} is ran and doesn't find a non-optional argument in the input argument map.
 * See {@link TaskExecutor#run(Map)} for more information.
 *
 * @see TaskExecutor
 */
public class MissingArgumentException extends RuntimeException {

    private static final long  serialVersionUID = -3646697621461623472L;

    private final TaskExecutor task;
    private final Parameter    parameter;

    /**
     * Creates a new missing argument exception.
     *
     * @param task The {@link TaskExecutor} which should have been ran; however, its {@code run()} method detected the missing argument.
     * @param parameter A {@link Parameter} object which describes the missing input argument.
     */
    public MissingArgumentException(TaskExecutor task, Parameter parameter) {

        super("The task '" + task + "' requires an argument with name '" + parameter.getName() + "' (type '" + parameter.getType() + "') to be provided");

        this.task = task;
        this.parameter = parameter;
    }

    /**
     * Returns the {@link TaskExecutor} which should have been ran; however, its {@code run()} method detected the missing argument.
     *
     * @return The task executor which started running and noticed the missing argument.
     */
    public TaskExecutor getTask() {

        return task;
    }

    /**
     * Returns a {@link Parameter} object which describes the missing input argument.
     * Because of the nature of this exception, this cannot be an {@link OptionalParameter}.
     *
     * @return The required parameter which has no argument supplied for it.
     */
    public Parameter getParameter() {

        return parameter;
    }

}
