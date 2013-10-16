package nl.esciencecenter.aether.util.rpc;

import java.io.IOException;

public class RemoteException extends IOException {

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
