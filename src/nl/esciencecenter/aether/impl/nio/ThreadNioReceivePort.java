/* $Id: ThreadNioReceivePort.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl.nio;


import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

import nl.esciencecenter.aether.ConnectionClosedException;
import nl.esciencecenter.aether.MessageUpcall;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.ReceivePortConnectUpcall;
import nl.esciencecenter.aether.ReceiveTimedOutException;
import nl.esciencecenter.aether.impl.Aether;
import nl.esciencecenter.aether.impl.SendPortIdentifier;
import nl.esciencecenter.aether.util.Queue;

final class ThreadNioReceivePort extends NioReceivePort {

    private ThreadNioDissipator current = null;

    private Queue readyDissipators;

    private boolean closing = false;

    ThreadNioReceivePort(Aether ibis, PortType type, String name,
            MessageUpcall upcall, ReceivePortConnectUpcall connUpcall,
            Properties props) throws IOException {
        super(ibis, type, name, upcall, connUpcall, props);

        readyDissipators = new Queue();
    }

    synchronized void newConnection(SendPortIdentifier spi, Channel channel)
            throws IOException {

        if (!(channel instanceof ReadableByteChannel)) {
            throw new IOException("wrong channel type on creating connection");
        }

        addConnection(spi, new ThreadNioDissipator(
                ((NioIbis) ibis).sendReceiveThread(),
                (ReadableByteChannel) channel));
    }

    synchronized void errorOnRead(NioDissipator dissipator, Exception cause) {
        dissipator.info.close(cause);
    }

    NioDissipator getReadyDissipator(long deadline) throws IOException {
        ThreadNioDissipator dissipator;

        synchronized (this) {
            if (current != null) {
                if (current.dataLeft()) {
                    readyDissipators.enqueue(current);
                }
                current = null;
            }
        }

        // FIXME: if a connection doesn't close gracefully, we won't notice

        while (true) {

            synchronized (this) {
                if (closing) {
                    if (connections.size() == 0) {
                        throw new ConnectionClosedException();

                    }
                }
            }

            dissipator = (ThreadNioDissipator)
                    readyDissipators.dequeue(deadline);

            if (dissipator == null) {
                synchronized (this) {
                    if (closing) {
                        if (connections.size() == 0) {
                            throw new ConnectionClosedException();
                        }
                    }
                }
                throw new ReceiveTimedOutException("deadline passed while"
                        + " selecting dissipator");
            }

            try {
                if (dissipator.messageWaiting()) {
                    synchronized (this) {
                        current = dissipator;
                        return dissipator;
                    }
                }
            } catch (IOException e) {
                if (dissipator != null) {
                    errorOnRead(dissipator, e);
                }
            }
        }
    }

    void addToReadyList(ThreadNioDissipator dissipator) {
        readyDissipators.enqueue(dissipator);
    }

    synchronized void closing() {
        closing = true;
    }
}
