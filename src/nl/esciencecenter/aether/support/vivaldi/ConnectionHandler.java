package nl.esciencecenter.aether.support.vivaldi;

import ibis.smartsockets.virtual.VirtualServerSocket;

import java.io.IOException;

import nl.esciencecenter.aether.support.Connection;
import nl.esciencecenter.aether.util.ThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionHandler implements Runnable {

    private static final Logger logger = LoggerFactory
            .getLogger(ConnectionHandler.class);

    private final VirtualServerSocket serverSocket;
    private final VivaldiClient vivaldi;

    ConnectionHandler(VirtualServerSocket socket, VivaldiClient vivaldi)
            throws IOException {

        this.serverSocket = socket;
        this.vivaldi = vivaldi;

        ThreadPool.createNew(this, "Vivaldi Connection handler");
    }

    public void run() {
        while (true) {
            try {
                Connection connection = new Connection(serverSocket);
                vivaldi.handleConnection(connection);
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    return;
                } else {
                    logger.error("Accept failed, waiting a second, will retry",
                            e);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        // IGNORE
                    }
                }
            }

        }
    }
}
