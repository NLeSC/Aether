package nl.esciencecenter.aether.support;

import ibis.smartsockets.SmartSocketsProperties;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.virtual.InitializationException;
import ibis.smartsockets.virtual.VirtualSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.aether.ConfigurationException;
import nl.esciencecenter.aether.AetherProperties;
import nl.esciencecenter.aether.server.ServerProperties;
import nl.esciencecenter.aether.util.TypedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to create a VirtualSocketFactory, and get the address of
 * the server.
 */
public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static DirectSocketAddress createServerAddress(String serverString,
            int defaultPort) throws ConfigurationException {

        if (serverString == null) {
            throw new ConfigurationException("serverString undefined");
        }

        // maybe it is a DirectSocketAddress?
        try {
            return DirectSocketAddress.getByAddress(serverString);
        } catch (Throwable e) {
            // IGNORE
        }

        Throwable throwable = null;
        // or only a host address
        try {
            return DirectSocketAddress.getByAddress(serverString, defaultPort);
        } catch (Throwable e) {
            throwable = e;
            // IGNORE
        }

        throw new ConfigurationException(
                "could not create server address from given string: "
                        + serverString, throwable);
    }

    private static Map<String, Client> clients = new HashMap<String, Client>();

    /**
     * Returns a named client. Creates one if needed.
     * 
     * @param name
     *            name of the client
     * @param properties
     *            properties used when client is created
     * @param port
     *            local port to bound client to used when client is created (0
     *            for any free port)
     * @return the client with the specified name
     * @throws ConfigurationException
     *             if the client cannot be created due to invalid settings
     * @throws IOException
     *             if the client cannot be created due to a SmartSockets error
     */
    public static synchronized Client getOrCreateClient(String name,
            Properties properties, int port) throws ConfigurationException,
            IOException {
        Client result = clients.get(name);

        if (result == null) {
            result = new Client(properties, port);
            clients.put(name, result);
        }
        return result;
    }

    private final DirectSocketAddress serverMachine;

    private final VirtualSocketFactory factory;

    private Client(Properties properties, int port)
            throws ConfigurationException, IOException {
        TypedProperties typedProperties = ServerProperties
                .getHardcodedProperties();
        typedProperties.addProperties(properties);

        String serverAddressString = typedProperties
                .getProperty(AetherProperties.SERVER_ADDRESS);

        if (serverAddressString == null || serverAddressString.equals("")) {
            serverMachine = null;
        } else {
            serverMachine = createServerAddress(serverAddressString,
                    typedProperties.getIntProperty(ServerProperties.PORT,
                            ServerProperties.DEFAULT_PORT));
        }

        String hubs = typedProperties.getProperty(AetherProperties.HUB_ADDRESSES);

        // did the server also start a hub?
        boolean serverIsHub = typedProperties
                .getBooleanProperty(AetherProperties.SERVER_IS_HUB);

        if (serverMachine != null && serverIsHub) {
            // add server to hub addresses
            if (hubs == null || hubs.equals("")) {
                hubs = serverMachine.toString();
            } else {
                hubs = hubs + "," + serverMachine.toString();
            }
        }

        Properties smartProperties = new Properties(typedProperties);

        if (port > 0) {
            smartProperties.put(SmartSocketsProperties.PORT_RANGE, Integer
                    .toString(port));
        }

        if (hubs != null) {
            smartProperties.put(SmartSocketsProperties.HUB_ADDRESSES, hubs);
        }

        try {
            factory = VirtualSocketFactory.createSocketFactory(smartProperties,
                    true);
        } catch (InitializationException e) {
            throw new IOException(e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("client factory running on " + factory.getLocalHost());
        }

    }

    /**
     * Returns the VirtualSocketFactory.
     * @return the socket factory.
     */
    public VirtualSocketFactory getFactory() {
        return factory;
    }

    /**
     * Get the address of a service running on a given port.
     * 
     * @param port
     *            the port the service is running on
     * @throws ConfigurationException
     *             if the server address is unknown.
     */
    public VirtualSocketAddress getServiceAddress(int port)
            throws ConfigurationException {

        if (serverMachine == null) {
            throw new ConfigurationException(
                    "cannot get address of server, server address property \""
                            + AetherProperties.SERVER_ADDRESS + "\" undefined");
        }

        return new VirtualSocketAddress(serverMachine, port, serverMachine,
                null);
    }
}
