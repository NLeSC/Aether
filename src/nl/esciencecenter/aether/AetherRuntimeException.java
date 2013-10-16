package nl.esciencecenter.aether;

/**
 * There are three base classes for Ibis exceptions: this one (which is an unchecked
 * exception), IbisException (which is a checked exception), and IbisIOException.
 * The latter exists because we want it to be a subclass of java.io.IOException.
 */
public class AetherRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>IbisRuntimeException</code> with
     * <code>null</code> as its error detail message.
     */
    public AetherRuntimeException() {
	super();
    }

    /**
     * Constructs an <code>IbisRuntimeException</code> with
     * the specified detail message.
     *
     * @param message
     *          the detail message
     */
    public AetherRuntimeException(String message) {
	super(message);
    }

    /**
     * Constructs an <code>IbisRuntimeException</code> with
     * the specified cause.
     *
     * @param cause
     *          the cause
     */
    public AetherRuntimeException(Throwable cause) {
	super(cause);
    }

    /**
     * Constructs an <code>IbisRuntimeException</code> with
     * the specified detail message and cause.
     *
     * @param message
     *          the detail message
     * @param cause
     *          the cause
     */
    public AetherRuntimeException(String message, Throwable cause) {
	super(message, cause);
    }
}
