
package com.quartercode.disconnected.server.world.comp.proc.prog;

import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskExecutor;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata;

public interface ProgramTaskMetadataProvider {

    public TaskMetadata getMetadata(Class<? extends TaskExecutor> executorClass);

}
