
package com.quartercode.disconnected.server.world.comp.proc.prog;

import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskDefinition;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskExecutor;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata;

/*
 Note that this "frame" API is configured via the {@link ProgramTaskConfiguration @TaskConfiguration} annotation.
 * That annotation is put on a task class and specifies a {@link TaskMetadataProvider}, which in turn provides a {@link TaskMetadata} configuration object.<br>
 * If you don't need a dynamic "frame" API because the API is the same all the time (same input parameters, same callbacks), you can use {@link StaticTask} instead.
 * That adapter class makes the whole configuration process increadibly easy and removes the need for a manually-coded task metadata provider.
 */
public class ProgramTaskDefinition implements TaskDefinition {

    private final Class<? extends TaskExecutor> executorClass;
    private final TaskMetadata                  metadata;

    public ProgramTaskDefinition(Class<? extends TaskExecutor> executorClass) {

        Validate.notNull(executorClass, "Program task definition executor class cannot be null");

        this.executorClass = executorClass;
        metadata = retrieveTaskMetadata();
    }

    private TaskMetadata retrieveTaskMetadata() {

        Validate.validState(executorClass.isAnnotationPresent(ProgramTaskConfiguration.class),
                "Task '%s' doesn't have a @ProgramTaskConfiguration annotation", executorClass.getName());

        Class<? extends ProgramTaskMetadataProvider> metadataProviderClass = executorClass.getAnnotation(ProgramTaskConfiguration.class).metadataProvider();
        try {
            ProgramTaskMetadataProvider metadataProvider = executorClass.getAnnotation(ProgramTaskConfiguration.class).metadataProvider().newInstance();
            return metadataProvider.getMetadata(executorClass);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create new program task metadata provider instance (class '" + metadataProviderClass.getName() + "', for task executor '" + executorClass.getName() + "')", e);
        }
    }

    @Override
    public TaskMetadata getMetadata() {

        return metadata;
    }

    @Override
    public TaskExecutor createExecutorInstance() {

        try {
            return executorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create new program task executor instance (class '" + executorClass.getName() + "')", e);
        }
    }

}
