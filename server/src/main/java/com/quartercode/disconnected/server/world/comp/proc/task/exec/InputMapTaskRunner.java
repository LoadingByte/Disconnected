
package com.quartercode.disconnected.server.world.comp.proc.task.exec;

import java.util.Map;
import org.apache.commons.lang3.Validate;

public class InputMapTaskRunner extends TaskRunner {

    private final Map<String, Object> inputArguments;

    public InputMapTaskRunner(Map<String, Object> inputArguments) {

        Validate.notNull(inputArguments, "Task input argument map cannot be null");

        this.inputArguments = inputArguments;
    }

    @Override
    protected void doRun() {

        getTaskExecutor().run(inputArguments);
    }

    @Override
    public void callback(String name, Map<String, Object> outputArguments) {

        // We don't process any callbacks
    }

}
