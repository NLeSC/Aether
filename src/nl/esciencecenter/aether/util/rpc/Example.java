package nl.esciencecenter.aether.util.rpc;

import java.util.Date;

import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.AetherIdentifier;


public class Example {

	Capabilities ibisCapabilities = new Capabilities(
			Capabilities.ELECTIONS_STRICT);

	private final Aether myIbis;

	public interface ExampleInterface {
		
		// Converts epoch time to date string.
		public String millisToString(long millis) throws RemoteException, Exception;
	}

	public class ExampleClass implements ExampleInterface {
		public String millisToString(long millis) throws RemoteException, Exception {
			return "rpc example result = " + new Date(millis).toString();
		}
	}

	/**
	 * Constructor. Actually does all the work too :)
	 */
	private Example() throws Exception {
		// Create an ibis instance.
		myIbis = AetherFactory.createIbis(ibisCapabilities, null,
				RPC.rpcPortTypes);

		// Elect a server
		AetherIdentifier server = myIbis.registry().elect("Server");

		// If I am the server, run server, else run client.
		if (server.equals(myIbis.identifier())) {
			server();
		} else {
			client(server);
		}

		// End ibis.
		myIbis.end();
	}

	private void server() throws Exception {

		//create object we want to make remotely accessible
		ExampleClass object = new ExampleClass();

		//make object remotely accessible
		RemoteObject<ExampleInterface> remoteObject = RPC.exportObject(
				ExampleInterface.class, object, "my great object", myIbis);

		//wait for a bit
		Thread.sleep(100000);

		//cleanup, object no longer remotely accessible
		remoteObject.unexport();
	}
	
	private void client(AetherIdentifier server) throws Exception {

		//create proxy to remote object
		ExampleInterface interfaceObject = RPC.createProxy(
				ExampleInterface.class, server, "my great object", myIbis);

		//call remote object, print result
		System.err.println(interfaceObject.millisToString(System.currentTimeMillis()));

	}


	public static void main(String args[]) {
		try {
			new Example();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
