/* $Id: IbisConfigurationException.java 13036 2011-02-24 16:37:33Z ceriel $ */

package nl.esciencecenter.aether;

/**
 * Signals that there was an error in the Ibis configuration.
 * An <code>IbisConfigurationException</code> is thrown to indicate
 * that there is something wrong in the way Ibis was configured,
 * for instance because a method was invoked that requires capabilities
 * that were not configured.
 */
public class ConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>IbisConfigurationException</code> with
     * <code>null</code> as its error detail message.
     */
    public ConfigurationException() {
        super();
    }

    /**
     * Constructs a <code>IbisConfigurationException</code> with
     * the specified detail message.
     *
     * @param detailMessage
     *          the detail message
     */
    public ConfigurationException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a <code>IbisConfigurationException</code> with
     * the specified detail message and cause.
     *
     * @param detailMessage
     *          the detail message
     * @param cause
     *          the cause
     */
    public ConfigurationException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }

    /**
     * Constructs a <code>IbisConfigurationException</code> with
     * the specified cause.
     *
     * @param cause
     *          the cause
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
