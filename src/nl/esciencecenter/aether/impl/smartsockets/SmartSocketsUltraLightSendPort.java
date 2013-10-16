package nl.esciencecenter.aether.impl.smartsockets;

import ibis.smartsockets.hub.servicelink.ServiceLink;
import ibis.smartsockets.util.MalformedAddressException;
import ibis.smartsockets.virtual.VirtualSocketAddress;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import nl.esciencecenter.aether.ConnectionFailedException;
import nl.esciencecenter.aether.ConnectionsFailedException;
import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.NoSuchPropertyException;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.ReceivePortIdentifier;
import nl.esciencecenter.aether.SendPort;
import nl.esciencecenter.aether.SendPortIdentifier;
import nl.esciencecenter.aether.WriteMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartSocketsUltraLightSendPort implements SendPort {

	protected static final Logger logger = 
		LoggerFactory.getLogger("ibis.ipl.impl.smartsockets.SendPort");

	private final PortType type;
	private final String name;
 	final Properties properties;	
	private final SmartSocketsIbis ibis;

	private final SendPortIdentifier sid;
	
	private boolean closed = false;

	private SmartSocketsUltraLightWriteMessage message; 

	private boolean messageInUse = false;

	private final Set<ReceivePortIdentifier> connections = new HashSet<ReceivePortIdentifier>();
	
	private final byte [][] messageToHub;
	
	SmartSocketsUltraLightSendPort(SmartSocketsIbis ibis, PortType type, String name, 
			Properties props) throws IOException {
		
		this.ibis = ibis;
		this.type = type;
		this.name = name;
 		this.properties = props;
		
		sid = new nl.esciencecenter.aether.impl.SendPortIdentifier(name, ibis.ident);

		messageToHub = new byte[2][];		
		messageToHub[0] = ibis.ident.toBytes();
	}	
		
	public synchronized void close() throws IOException {
		closed = true;
		notifyAll();		
	}

	public synchronized void connect(ReceivePortIdentifier receiver) throws ConnectionFailedException {
		connections.add(receiver);
	}

	public void connect(ReceivePortIdentifier receiver, long timeoutMillis, boolean fillTimeout) throws ConnectionFailedException {
		connect(receiver);
	}

	public ReceivePortIdentifier connect(AetherIdentifier ibisIdentifier, String receivePortName) throws ConnectionFailedException {
		ReceivePortIdentifier id = new nl.esciencecenter.aether.impl.ReceivePortIdentifier(receivePortName, (nl.esciencecenter.aether.impl.AetherIdentifier) ibisIdentifier);
		connect(id);
		return id; 
	}

	public ReceivePortIdentifier connect(AetherIdentifier ibisIdentifier, String receivePortName, long timeoutMillis, boolean fillTimeout) throws ConnectionFailedException {
		return connect(ibisIdentifier, receivePortName);
	}

	public void connect(ReceivePortIdentifier[] receivePortIdentifiers) throws ConnectionsFailedException {
		
		LinkedList<ConnectionFailedException> tmp = null;
		LinkedList<ReceivePortIdentifier> success = new LinkedList<ReceivePortIdentifier>();
		
		for (ReceivePortIdentifier id : receivePortIdentifiers) {
			try { 
				connect(id);
				success.add(id);
			} catch (ConnectionFailedException e) {

				if (tmp == null) { 
					tmp = new LinkedList<ConnectionFailedException>();
				}
				
				tmp.add(e);
			}
		}		

		if (tmp != null && tmp.size() > 0) { 
			ConnectionsFailedException c = new ConnectionsFailedException("Failed to connect");
			
			for (ConnectionFailedException ex : tmp) { 
				c.add(ex);
			}			
	
			c.setObtainedConnections(success.toArray(new ReceivePortIdentifier[success.size()]));
			throw c;
		}
	}

	public void connect(ReceivePortIdentifier[] receivePortIdentifiers, long timeoutMillis, boolean fillTimeout) throws ConnectionsFailedException {
		connect(receivePortIdentifiers);
	}

	public ReceivePortIdentifier[] connect(Map<AetherIdentifier, String> ports) throws ConnectionsFailedException {

		ReceivePortIdentifier [] tmp = new ReceivePortIdentifier[ports.size()];
		
		int index = 0;
		
		for (Entry<AetherIdentifier, String> e : ports.entrySet()) { 
			tmp[index++] = new nl.esciencecenter.aether.impl.ReceivePortIdentifier(e.getValue(), (nl.esciencecenter.aether.impl.AetherIdentifier) e.getKey());
		}
		
		connect(tmp);
		return tmp;
	}

	public ReceivePortIdentifier[] connect(Map<AetherIdentifier, String> ports, long timeoutMillis, boolean fillTimeout) throws ConnectionsFailedException {
		return connect(ports);
	}

	public ReceivePortIdentifier[] connectedTo() {
		return connections.toArray(new ReceivePortIdentifier[0]);
	}

	public synchronized void disconnect(ReceivePortIdentifier receiver) throws IOException {
		if (!connections.remove(receiver)) { 
			throw new IOException("Not connected to " + receiver);
		}
	}

	public void disconnect(AetherIdentifier ibisIdentifier, String receivePortName) throws IOException {
		disconnect(new nl.esciencecenter.aether.impl.ReceivePortIdentifier(receivePortName, (nl.esciencecenter.aether.impl.AetherIdentifier) ibisIdentifier));
	}

	public PortType getPortType() {
		return type;
	}

	public SendPortIdentifier identifier() {
		return sid;
	}

	public ReceivePortIdentifier[] lostConnections() {
		return new ReceivePortIdentifier[0];
	}

	public String name() {
		return name;
	}

	public synchronized WriteMessage newMessage() throws IOException {
		
		while (!closed && messageInUse) { 
			try { 
				wait();
			} catch (InterruptedException e) {
				// ignore
			}			
		}

		if (closed) { 
			throw new IOException("Sendport is closed");
		}
		
		messageInUse = true;	
		message = new SmartSocketsUltraLightWriteMessage(this);
		return message;
	}

	public String getManagementProperty(String key) throws NoSuchPropertyException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> managementProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public void printManagementProperties(PrintStream stream) {
		// TODO Auto-generated method stub
		
	}

	public void setManagementProperties(Map<String, String> properties) throws NoSuchPropertyException {
		// TODO Auto-generated method stub
		
	}

	public void setManagementProperty(String key, String value) throws NoSuchPropertyException {
		// TODO Auto-generated method stub
		
	}
	
	private void send(byte [] data) throws UnknownHostException, MalformedAddressException { 
		
		ServiceLink link = ibis.getServiceLink();
		
		if (link == null) {
			if (logger.isDebugEnabled()) { 
				logger.debug("No servicelink available");
			}
			
			return;
		}
		
		// messageToHub[1] = Arrays.copyOfRange(buffer, 0, len);
                // This is Java 6 speak. Modified to equivalent code that
                // is acceptable to Java 1.5. --Ceriel
                messageToHub[1] = new byte[data.length];
                System.arraycopy(data, 0, messageToHub[1], 0, data.length);

		for (ReceivePortIdentifier id : connections) { 		

			nl.esciencecenter.aether.impl.AetherIdentifier dst = (nl.esciencecenter.aether.impl.AetherIdentifier) id.ibisIdentifier();
			VirtualSocketAddress a = VirtualSocketAddress.fromBytes(dst.getImplementationData(), 0);
			
			if (logger.isDebugEnabled()) { 
				logger.debug("Sending message to " + a);
			}
			
			link.send(a.machine(), a.hub(), id.name(), 0xDEADBEEF, messageToHub);  
		}
	}

	public synchronized void finishedMessage(byte[] m) throws IOException {

		// int len = (int) message.bytesWritten();

		try { 
			send(m);
		} catch (Exception e) {
			logger.debug("Failed to send message to " + connections, e);
		}
		
		// message.reset();	No longer needed. --Ceriel
		messageInUse = false;
		notifyAll();
	}
	
/*	
	private void send(ReceivePortIdentifier id, byte [] data) throws UnknownHostException, MalformedAddressException { 
		
		ServiceLink link = ibis.getServiceLink();
		
		if (link != null) {
			ibis.ipl.impl.IbisIdentifier dst = (ibis.ipl.impl.IbisIdentifier) id.ibisIdentifier();
			VirtualSocketAddress a = VirtualSocketAddress.fromBytes(dst.getImplementationData(), 0);

			byte [][] message = new byte[2][];
			
			message[0] = ibis.ident.toBytes();
			message[1] = data;
	
			if (logger.isDebugEnabled()) { 
				logger.debug("Sending message to " + a);
			}
			
			link.send(a.machine(), a.hub(), id.name(), 0xDEADBEEF, message);  
		} else { 
			
			if (logger.isDebugEnabled()) { 
				logger.debug("No servicelink available");
			}
		}
	}
	
		
	public synchronized void finishedMessage() throws IOException {

		int len = (int) message.bytesWritten();
		
		byte [] m = buffer;
		
		if (len < buffer.length) { 
			m = Arrays.copyOfRange(buffer, 0, len);
		}
		
		for (ReceivePortIdentifier id : connections) { 
			try { 
				send(id, m);
			} catch (Exception e) {
				logger.debug("Failed to send message to " + id, e);
			}
		}
		
		message.reset();	
		messageInUse = false;
		notifyAll();
	}
*/


	public synchronized void finishedMessage(IOException exception) throws IOException {
		message.reset();	
		messageInUse = false;
		notifyAll();
	} 	
}
