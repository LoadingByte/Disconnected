
package com.quartercode.disconnected.server.world.comp.proc.task.exec;

import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskDefinition;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskExecutor;
import com.quartercode.disconnected.server.world.util.InterfaceAdapter;
import com.quartercode.disconnected.server.world.util.WorldNode;

public abstract class TaskRunner extends WorldNode<TaskProcess> {

    @XmlElement
    @XmlJavaTypeAdapter (InterfaceAdapter.class)
    private TaskExecutor taskExecutor;

    public TaskExecutor getTaskExecutor() {

        return taskExecutor;
    }

    public void run(TaskDefinition task) {

        Validate.notNull(task, "Task definition provided to a task runner for execution cannot be null");

        // Create a new instance of the task
        taskExecutor = task.createExecutorInstance();

        // Let the custom method do the actual running
        doRun();
    }

    protected abstract void doRun();

    public abstract void callback(String name, Map<String, Object> outputArguments);

}
