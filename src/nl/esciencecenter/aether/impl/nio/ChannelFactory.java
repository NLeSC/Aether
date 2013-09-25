/* $Id: ChannelFactory.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;

import nl.esciencecenter.aether.impl.ReceivePortIdentifier;

/**
 * Creates and recycles/destroys writeable and readable channels. Any
 * implementation should also handle incoming connections in the run() method
 * and register them with the receiveport.
 */
public interface ChannelFactory extends Runnable {

    /**
     * Tries to connect to sendport to the given receiveport for tileoutMillis
     * milliseconds and returns the writechannel if it succeeded. A timeout of 0
     * means try forever.
     * 
     * @return a new Channel connected to "rpi".
     */
    public Channel connect(NioSendPort spi,
            ReceivePortIdentifier rpi, long timeoutMillis)
            throws IOException;

    /**
     * Stops the factory. It will kill off any threads it made.
     */
    public void quit() throws IOException;

    /**
     * Returns the socket address that this factory is listening to.
     */
    public InetSocketAddress getAddress();
}
