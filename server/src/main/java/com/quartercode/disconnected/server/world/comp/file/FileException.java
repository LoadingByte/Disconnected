
package com.quartercode.disconnected.server.world.comp.file;

/**
 * This exception is the superclass for all exceptions related to {@link File}s.
 * Although instances of this superclass can be constructed and thrown as well, subclasses (e.g. {@link InvalidPathException}) should be used instead.
 *
 * @see File
 */
public class FileException extends Exception {

    private static final long serialVersionUID = 2504374278824314671L;

    /**
     * Creates a new file exception.
     */
    public FileException() {

        super();
    }

    /**
     * Creates a new file exception with the given message.
     *
     * @param message The detail message.
     */
    public FileException(String message) {

        super(message);
    }

    /**
     * Creates a new file exception with the given cause.
     *
     * @param cause The child cause which caused the exception to be thrown.
     */
    public FileException(Throwable cause) {

        super(cause);
    }

    /**
     * Creates a new file exception with the given message and cause.
     *
     * @param message The detail message.
     * @param cause The child cause which caused the exception to be thrown.
     */
    public FileException(String message, Throwable cause) {

        super(message, cause);
    }

}
