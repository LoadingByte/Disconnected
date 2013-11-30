
package com.quartercode.disconnected.mocl.extra;

/**
 * The stop execution exception is thrown if a {@link FunctionExecutor} wants to quit the invokation queue.
 * The algorithm checks the {@link FunctionExecutor} with the highest priority first and then goes down.
 * If any {@link FunctionExecutor} in the line denies the execution of other {@link FunctionExecutor}s, the algorithm stops.
 * 
 * @see FunctionExecutor
 */
public class StopExecutionException extends Exception {

    private static final long serialVersionUID = -5169233409509164509L;

    /**
     * Creates a new stop execution exception signal with the given message why the execution should stop.
     * 
     * @param message The message which describes why the execution should stop.
     */
    public StopExecutionException(String message) {

        super(message);
    }

    /**
     * Creates a new stop execution exception signal with the given cause which activated the stop.
     * 
     * @param cause The {@link Throwable} which was caught and activated the stop signal.
     */
    public StopExecutionException(Throwable cause) {

        super(cause);
    }

    /**
     * Creates a new stop execution exception signal with the given message and cause.
     * 
     * @param message The message which describes why the execution should stop.
     * @param cause The {@link Throwable} which was caught and activated the stop signal.
     */
    public StopExecutionException(String message, Throwable cause) {

        super(message, cause);
    }

}
