package nl.esciencecenter.aether;

import java.io.IOException;

/**
 * There are three base classes for Ibis exceptions: this one (which extends
 * java.io.IOException), IbisException (which is a checked exception), and IbisRuntimException
 * (which is an unchecked exception).
 */
public class AetherIOException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>IbisIOException</code> with
     * <code>null</code> as its error detail message.
     */
    public AetherIOException() {
	super();
    }

    /**
     * Constructs an <code>IbisIOException</code> with
     * the specified detail message.
     *
     * @param message
     *          the detail message
     */
    public AetherIOException(String message) {
	super(message);
    }

    /**
     * Constructs an <code>IbisIOException</code> with
     * the specified cause.
     *
     * @param cause
     *          the cause
     */
    public AetherIOException(Throwable cause) {
	super(cause);
    }

    /**
     * Constructs an <code>IbisIOException</code> with
     * the specified detail message and cause.
     *
     * @param message
     *          the detail message
     * @param cause
     *          the cause
     */
    public AetherIOException(String message, Throwable cause) {
	super(message, cause);
    }
}
