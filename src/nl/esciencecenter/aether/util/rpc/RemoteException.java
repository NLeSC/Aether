package nl.esciencecenter.aether.util.rpc;

import nl.esciencecenter.aether.AetherIOException;

public class RemoteException extends AetherIOException {

    public RemoteException() {
	super();
    }

    public RemoteException(String message) {
	super(message);
    }

    public RemoteException(Throwable cause) {
	super(cause);
    }

    public RemoteException(String message, Throwable cause) {
	super(message, cause);
    }

}
