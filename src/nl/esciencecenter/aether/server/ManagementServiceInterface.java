package nl.esciencecenter.aether.server;

import nl.esciencecenter.aether.IbisIdentifier;
import nl.esciencecenter.aether.support.management.AttributeDescription;

/**
 * Interface to the management service. Mostly for getting management info from the
 * server.
 * 
 * @ibis.experimental
 */
public interface ManagementServiceInterface {

    /**
     * Obtains the attributes from the specified Ibis instance, one object per attribute
     * description.
     * @param ibis the Ibis instance to obtain attributes from.
     * @param descriptions the attribute descriptions.
     * @return the attributes.
     * @throws Exception is thrown in case of trouble.
     */
    public abstract Object[] getAttributes(IbisIdentifier ibis,
            AttributeDescription... descriptions) throws Exception;

}