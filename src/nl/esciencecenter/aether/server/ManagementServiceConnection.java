package nl.esciencecenter.aether.server;

import nl.esciencecenter.aether.IbisIdentifier;
import nl.esciencecenter.aether.support.Connection;
import nl.esciencecenter.aether.support.management.AttributeDescription;
import ibis.smartsockets.virtual.VirtualSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

class ManagementServiceConnection implements ManagementServiceInterface {

    public static final int TIMEOUT = 10000;

    private VirtualSocketAddress address;
    private VirtualSocketFactory socketFactory;

    ManagementServiceConnection(VirtualSocketAddress address,
            VirtualSocketFactory socketFactory) {
        this.address = address;
        this.socketFactory = socketFactory;
    }

    // Java 1.5 Does not allow @Override for interface methods
    //    @Override
    public Object[] getAttributes(IbisIdentifier ibis,
            AttributeDescription... descriptions) throws Exception {
        Connection connection = new Connection(address, TIMEOUT, true,
                socketFactory);
        try {

            connection.out().writeByte(ServerConnectionProtocol.MAGIC_BYTE);
            connection.out().writeByte(
                    ServerConnectionProtocol.OPCODE_MANAGEMENT_GET_ATTRIBUTES);
            connection.writeObject(ibis);
            connection.writeObject(descriptions);
            connection.getAndCheckReply();

            return (Object[]) connection.readObject();
        } finally {
            connection.close();
        }
    }

}
