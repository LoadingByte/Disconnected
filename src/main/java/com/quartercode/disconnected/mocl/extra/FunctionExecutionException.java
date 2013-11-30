
package com.quartercode.disconnected.mocl.extra;

/**
 * The function execution exception is thrown if an {@link Throwable} occurres during the invokation of a {@link FunctionExecutor}.
 * Function execution exception are constructed with the cause {@link Throwable} of a caught {@link StopExecutionException}.
 * 
 * @see Function
 * @see FunctionExecutor
 * @see StopExecutionException
 */
public class FunctionExecutionException extends StopExecutionException {

    private static final long serialVersionUID = -6644948686243835336L;

    /**
     * Creates a new function execution exception with the given wrapped {@link Throwable}.
     * 
     * @param wrapped The {@link Throwable} the exception wraps around.
     */
    public FunctionExecutionException(Throwable wrapped) {

        super(wrapped);
    }

}
