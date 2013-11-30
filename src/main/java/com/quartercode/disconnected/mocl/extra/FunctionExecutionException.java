
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

    private static final long serialVersionUID = -5515825410776845247L;

    /**
     * Creates a new function execution exception with the given wrapped {@link Throwable}.
     * 
     * @param wrapped The {@link Throwable} the exception wraps around.
     */
    public FunctionExecutionException(Throwable wrapped) {

        super(wrapped);
    }

    /**
     * Creates a new function execution exception with the given wrapped {@link Throwable} and an explanation.
     * 
     * @param message A message which describes why the exception occurres.
     * @param wrapped The {@link Throwable} the exception wraps around.
     */
    public FunctionExecutionException(String message, Throwable wrapped) {

        super(message, wrapped);
    }

}
