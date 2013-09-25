/* $Id: PortMismatchException.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether;

/**
 * Signals an attempt to connect ports of different types.
 */
public class PortMismatchException extends ConnectionFailedException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>PortMismatchException</code> with the specified
     * parameters.
     * @param detailMessage
     *          the detail message.
     * @param receivePortIdentifier
     *          identifies the target port of the failed connection attempt.
     * @param cause
     *          cause of the failure.
     */
    public PortMismatchException(String detailMessage,
            ReceivePortIdentifier receivePortIdentifier, Throwable cause) {
        super(detailMessage, receivePortIdentifier, cause);
    }
    
    /**
     * Constructs a <code>PortMismatchException</code> with the specified
     * parameters.
     * @param detailMessage
     *          the detail message.
     * @param receivePortIdentifier
     *          identifies the target port of the failed connection attempt.
     */
    public PortMismatchException(String detailMessage,
            ReceivePortIdentifier receivePortIdentifier) {
        super(detailMessage, receivePortIdentifier);
    }
    
    /**
     * Constructs a <code>PortMismatchException</code> with the specified
     * parameters.
     * @param detailMessage
     *          the detail message.
     * @param ibisIdentifier 
     *          identifies the Ibis instance of the target port of
     *          the failed connection attempt.
     * @param receivePortName
     *          the name of the receive port of the failed connection attempt.
     * @param cause
     *          the cause of the failure.
     */
    public PortMismatchException(String detailMessage,
            IbisIdentifier ibisIdentifier, String receivePortName,
            Throwable cause) {
        super(detailMessage, ibisIdentifier, receivePortName, cause);
    }
    
    /**
     * Constructs a <code>PortMismatchException</code> with the specified
     * parameters.
     * @param detailMessage
     *          the detail message.
     * @param ibisIdentifier 
     *          identifies the Ibis instance of the target port of
     *          the failed connection attempt.
     * @param receivePortName
     *          the name of the receive port of the failed connection attempt.
     */
    public PortMismatchException(String detailMessage,
            IbisIdentifier ibisIdentifier, String receivePortName) {
        super(detailMessage, ibisIdentifier, receivePortName);
    }
}
