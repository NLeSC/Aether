package nl.esciencecenter.aether.registry;


import java.security.AccessControlException;

import nl.esciencecenter.aether.Credentials;

/**
 * @ibis.experimental
 * @author rkemp
 */
public interface ControlPolicy {

    /**
     * @ibis.experimental
     * @param credentials credentials provided by the ibis instance
     * @throws AccessControlException if the user is not authorized to join the pool
     */
    public void onJoin(Credentials credentials) throws AccessControlException;

}
