
package com.quartercode.disconnected.mocl.extra.def;

import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Lockable;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;

/**
 * A lockable function executor wrapper is a {@link Lockable} {@link FunctionExecutor} which wraps around another {@link FunctionExecutor}.
 * That allows the wrapped {@link FunctionExecutor} to be {@link Lockable}.
 * 
 * @param <R> The type of the return value of the defined function.
 * @see FunctionExecutor
 * @see Lockable
 */
public class LockableFEWrapper<R> implements FunctionExecutor<R> {

    private final FunctionExecutor<R> executor;

    /**
     * Creates a new lockable function executor wrapper which wraps aroung the given {@link FunctionExecutor}.
     * 
     * @param executor The {@link FunctionExecutor} to wrap around.
     */
    public LockableFEWrapper(FunctionExecutor<R> executor) {

        this.executor = executor;
    }

    /**
     * Returns the {@link FunctionExecutor} the wrapper wraps around for making it {@link Lockable}.
     * 
     * @return The {@link FunctionExecutor} the wrapper wraps around.
     */
    public FunctionExecutor<R> getExecutor() {

        return executor;
    }

    @Override
    @Lockable
    public R invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

        return executor.invoke(holder, arguments);
    }

}
