package nl.esciencecenter.aether.support.management;

import java.io.IOException;

import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.io.Conversion;
import nl.esciencecenter.aether.server.ManagementServiceInterface;
import nl.esciencecenter.aether.support.Connection;
import nl.esciencecenter.aether.util.TypedProperties;

import ibis.smartsockets.virtual.VirtualSocketFactory;

public class ManagementService implements nl.esciencecenter.aether.server.Service,
		ManagementServiceInterface {

	private static final int CONNECT_TIMEOUT = 10000;
	private final VirtualSocketFactory factory;

	public ManagementService(TypedProperties properties,
			VirtualSocketFactory factory) {
		this.factory = factory;
	}

	public void end(long deadline) {
		// NOTHING
	}

	public String getServiceName() {
		return "management";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ibis.ipl.management.ManagementServerInterface#getAttributes(ibis.ipl.
	 * IbisIdentifier, ibis.ipl.management.AttributeDescription)
	 */
	public Object[] getAttributes(AetherIdentifier ibis,
			AttributeDescription... descriptions) throws IOException {
		nl.esciencecenter.aether.impl.AetherIdentifier identifier;
		try {
			identifier = (nl.esciencecenter.aether.impl.AetherIdentifier) ibis;
		} catch (ClassCastException e) {
			throw new IOException(
					"cannot cast given identifier to implementation identifier: " + e);
		}

		Connection connection = new Connection(identifier, CONNECT_TIMEOUT,
				false, factory, Protocol.VIRTUAL_PORT);
		connection.out().writeByte(Protocol.MAGIC_BYTE);
		connection.out().writeByte(Protocol.OPCODE_GET_MONITOR_INFO);

		connection.out().writeInt(descriptions.length);
		for (int i = 0; i < descriptions.length; i++) {
			connection.out().writeUTF(descriptions[i].getBeanName());
			connection.out().writeUTF(descriptions[i].getAttribute());
		}

		connection.getAndCheckReply();

		int length = connection.in().readInt();
		if (length < 0) {
			connection.close();
			throw new IOException("End of Stream on reading from connection");
		}

		byte[] resultBytes = new byte[length];

		connection.in().readFully(resultBytes);

		Object[] reply;
		try {
			reply = (Object[]) Conversion.byte2object(resultBytes);
		} catch (ClassNotFoundException e) {
			throw new IOException("Cannot cast result " + e);
		}

		connection.close();

		return reply;
	}

	public String toString() {
		return "Management service on virtual port " + Protocol.VIRTUAL_PORT;
	}

}
