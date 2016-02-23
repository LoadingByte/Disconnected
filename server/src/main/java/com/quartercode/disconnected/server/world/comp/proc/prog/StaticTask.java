
package com.quartercode.disconnected.server.world.comp.proc.prog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import com.quartercode.disconnected.server.world.comp.proc.prog.StaticTask.StaticTaskMetadataProvider;
import com.quartercode.disconnected.server.world.comp.proc.task.def.MissingArgumentException;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskExecutor;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskExecutorAdapter;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata.OptionalParameter;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata.Parameter;

@ProgramTaskConfiguration (metadataProvider = StaticTaskMetadataProvider.class)
public abstract class StaticTask extends TaskExecutorAdapter {

    // ----- API -----

    protected abstract void run();

    protected void callback(String name, Object... outputArguments) {

        // Get the output parameter list so that the parameter names can be resolved from it
        List<Parameter> outputParams = null;
        for (TaskMetadata.Callback callback : getTaskMetadata().getCallbacks()) {
            if (callback.getName().equals(name)) {
                outputParams = callback.getOutputParameters();
            }
        }
        Validate.isTrue(outputParams != null, "Callback '%s.%s' is unknown", getClass().getName(), name);

        // Parse the output arguments
        Validate.isTrue(outputParams.size() == outputArguments.length,
                "Callback '%s.%s': Amount of configured output parameters doesn't match amount of supplied output arguments", getClass().getName(), name);
        Map<String, Object> parsedOutputArgs = new HashMap<>();
        for (int index = 0; index < outputArguments.length; index++) {
            Parameter outputParam = outputParams.get(index);
            Object outputArg = outputArguments[index];
            Validate.isTrue(outputArg == null || outputParam.getType().isInstance(outputArg),
                    "Callback '%s.%s': Type of configured output parameter '%s' ('%s') doesn't match type of supplied output argument ('%s')",
                    getClass().getName(), name, outputParam.getName(), outputParam.getType().getName(), outputArg.getClass().getName());
            parsedOutputArgs.put(outputParam.getName(), outputArg);
        }

        // Actually invoke the callback
        getSingleParent().callback(name, parsedOutputArgs);
    }

    @Target (ElementType.FIELD)
    @Retention (RetentionPolicy.RUNTIME)
    public static @interface InputParameter {

        boolean optional () default false;

    }

    @Target (ElementType.METHOD)
    @Retention (RetentionPolicy.RUNTIME)
    public static @interface Callback {

        String[] params () default {};

    }

    // ----- Implementation -----

    private TaskMetadata getTaskMetadata() {

        return new StaticTaskMetadataProvider().getMetadata(getClass());
    }

    @Override
    public final void run(Map<String, Object> inputArguments) {

        // Set the input argument fields
        for (Parameter inputParameter : getTaskMetadata().getInputParameters()) {
            String inputParamName = inputParameter.getName();

            // Only set the field if an actual value is present; otherwise, the task has automatically set the default value
            if (inputArguments.containsKey(inputParamName)) {
                Field field = FieldUtils.getField(getClass(), inputParamName, true);
                Class<?> inputParamType = ClassUtils.primitiveToWrapper(field.getType());
                Object inputArg = inputArguments.get(inputParamName);

                Validate.isTrue(inputArg == null || inputParamType.isInstance(inputArg),
                        "Type of configured input parameter '%s.%s' ('%s') doesn't match type of supplied input argument ('%s')",
                        getClass().getName(), inputParamName, inputParamType.getName(), inputArg.getClass().getName());

                try {
                    field.set(this, inputArg);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    // Will never happen
                }
            } else if (! (inputParameter instanceof OptionalParameter)) {
                // If the parameter is non-optional and no argument is specified
                throw new MissingArgumentException(this, inputParameter);
            }
        }

        // Actually run the task
        run();
    }

    public static class StaticTaskMetadataProvider implements ProgramTaskMetadataProvider {

        @Override
        public TaskMetadata getMetadata(Class<? extends TaskExecutor> executorClass) {

            return new TaskMetadata(computeInputParameters(executorClass), computeCallbacks(executorClass));
        }

        private List<Parameter> computeInputParameters(Class<? extends TaskExecutor> executorClass) {

            List<Parameter> inputParameters = new ArrayList<>();

            // This instance of the task will probably be used later on in the loop
            TaskExecutor executorInstance;
            try {
                executorInstance = executorClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Unable to create new task instance (class '" + executorClass.getName() + "')", e);
            }

            // Compute the input parameters
            for (Field field : FieldUtils.getAllFields(executorClass)) {
                if (field.isAnnotationPresent(InputParameter.class)) {
                    InputParameter annotation = field.getAnnotation(InputParameter.class);
                    String inputParamName = field.getName();
                    Class<?> inputParamType = field.getType();

                    if (annotation.optional()) {
                        // Sadly, we have to use an instance of the task in order to deduce a field's default value
                        field.setAccessible(true);
                        try {
                            Object defaultValue = field.get(executorInstance);
                            inputParameters.add(new OptionalParameter(inputParamName, inputParamType, defaultValue));
                        } catch (IllegalAccessException e) {
                            // Will never happen
                        }
                    } else {
                        inputParameters.add(new Parameter(inputParamName, inputParamType));
                    }
                }
            }

            return inputParameters;
        }

        private List<TaskMetadata.Callback> computeCallbacks(Class<? extends TaskExecutor> executorClass) {

            List<TaskMetadata.Callback> callbacks = new ArrayList<>();

            // Compute the callbacks
            for (Method method : getAllMethods(executorClass)) {
                if (method.isAnnotationPresent(Callback.class)) {
                    Callback annotation = method.getAnnotation(Callback.class);
                    String callbackName = method.getName();

                    // Compute the output parameters
                    Class<?>[] outputParamTypes = method.getParameterTypes();
                    String[] outputParamNames = annotation.params();
                    Validate.validState(outputParamTypes.length == outputParamNames.length,
                            "Callback '%s.%s': Amount of callback method parameters doesn't match amount of configured output parameter names", executorClass.getName(), callbackName);
                    List<Parameter> outputParameters = new ArrayList<>();
                    for (int paramIndex = 0; paramIndex < outputParamNames.length; paramIndex++) {
                        outputParameters.add(new Parameter(outputParamNames[paramIndex], outputParamTypes[paramIndex]));
                    }

                    callbacks.add(new TaskMetadata.Callback(callbackName, outputParameters));
                }
            }

            return callbacks;
        }

        private List<Method> getAllMethods(Class<?> c) {

            List<Method> methods = new ArrayList<>();

            for (Class<?> currentClass : ClassUtils.hierarchy(c)) {
                for (Method method : currentClass.getDeclaredMethods()) {
                    methods.add(method);
                }
            }

            return methods;
        }

    }

}
