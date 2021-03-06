/* $Id: SendPortConnectionInfo.java 12944 2011-01-25 21:38:46Z ceriel $ */

package nl.esciencecenter.aether.impl;

/**
 * Abstract class for implementation-dependent connection info for sendports.
 */
public abstract class SendPortConnectionInfo {
    /** Identifies the receiveport side of the connection. */
    public final ReceivePortIdentifier target;

    /** The sendport of the connection. */
    public final SendPort port;

    /**
     * Constructs a <code>SendPortConnectionInfo</code> with the specified parameters.
     * @param port the sendport.
     * @param target identifies the receiveport.
     */
    protected SendPortConnectionInfo(SendPort port, ReceivePortIdentifier target) {
        this.port = port;
        this.target = target;
    }
    
    public String connectionType() {
        return "unknown";
    }

    /**
     * Should close this particular connection.
     */
    public abstract void closeConnection();
}
