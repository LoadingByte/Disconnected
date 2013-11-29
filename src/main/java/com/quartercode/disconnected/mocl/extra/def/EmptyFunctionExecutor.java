
package com.quartercode.disconnected.mocl.extra.def;

import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;

/**
 * The empty function executor is a simple implementation of the {@link FunctionExecutor} which just does nothing.
 * The {@link #invoke(FeatureHolder, Object...)} method has no content apart from "return null".
 * 
 * @param <R> The type of the expected return object.
 * @see FunctionExecutor
 */
public class EmptyFunctionExecutor<R> implements FunctionExecutor<R> {

    /**
     * Creates a new empty function executor.
     */
    public EmptyFunctionExecutor() {

    }

    @Override
    public R invoke(FeatureHolder holder, Object... arguments) {

        // Do nothing
        return null;
    }

}
