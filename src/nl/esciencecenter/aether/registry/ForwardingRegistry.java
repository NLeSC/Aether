package nl.esciencecenter.aether.registry;


import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import nl.esciencecenter.aether.IbisIdentifier;
import nl.esciencecenter.aether.NoSuchPropertyException;

public final class ForwardingRegistry extends nl.esciencecenter.aether.registry.Registry {
    
    private final nl.esciencecenter.aether.registry.Registry target;

    public ForwardingRegistry(nl.esciencecenter.aether.registry.Registry target) {
        this.target = target;
    }

    @Override
    public long getSequenceNumber(String name) throws IOException {
        return target.getSequenceNumber(name);
    }

    @Override
    public void leave() throws IOException {
        target.leave();
    }

    public void assumeDead(nl.esciencecenter.aether.IbisIdentifier ibis) throws IOException {
        target.assumeDead(ibis);
    }

    public nl.esciencecenter.aether.IbisIdentifier elect(String election, long timeoutMillis) throws IOException {
        return target.elect(election, timeoutMillis);
    }

    public nl.esciencecenter.aether.IbisIdentifier elect(String election) throws IOException {
        return target.elect(election);
    }
    
    public nl.esciencecenter.aether.IbisIdentifier getElectionResult(String election) throws IOException {
        return target.getElectionResult(election);
    }

    public nl.esciencecenter.aether.IbisIdentifier getElectionResult(String election, long timeoutMillis) throws IOException {
        return target.getElectionResult(election, timeoutMillis);
    }

    public void maybeDead(nl.esciencecenter.aether.IbisIdentifier ibis) throws IOException {
        target.maybeDead(ibis);
    }

    public void signal(String string, nl.esciencecenter.aether.IbisIdentifier... ibisses) throws IOException {
        target.signal(string, ibisses);
    }

    @Override
    public nl.esciencecenter.aether.impl.IbisIdentifier getIbisIdentifier() {
        return target.getIbisIdentifier();
    }

    public IbisIdentifier[] diedIbises() {
        return target.diedIbises();
    }

    public IbisIdentifier[] joinedIbises() {
        return target.joinedIbises();
    }

    public IbisIdentifier[] leftIbises() {
        return target.leftIbises();
    }

    public String[] receivedSignals() {
        return target.receivedSignals();
    }

    public void disableEvents() {
        target.disableEvents();
    }

    public void enableEvents() {
        target.enableEvents();
    }

    public int getPoolSize() {
        return target.getPoolSize();
    }
    
    public String getPoolName() {
        return target.getPoolName();
    }

    public boolean isClosed() {
        return target.isClosed();
    }
    
    public void waitUntilPoolClosed() {
        target.waitUntilPoolClosed();
    }

    public Map<String, String> managementProperties() {
        return target.managementProperties();
    }

    public String getManagementProperty(String key) throws NoSuchPropertyException {
        return target.getManagementProperty(key);
    }

    public void setManagementProperties(Map<String, String> properties) throws NoSuchPropertyException {
        target.setManagementProperties(properties);
    }

    public void setManagementProperty(String key, String value) throws NoSuchPropertyException {
        target.setManagementProperty(key, value);
    }

    public void printManagementProperties(PrintStream stream) {
        target.printManagementProperties(stream);
    }

    public boolean hasTerminated() {
        return target.hasTerminated();
    }

    public void terminate() throws IOException {
        target.terminate();
    }

    public IbisIdentifier waitUntilTerminated() {
        return target.waitUntilTerminated();
    }

    @Override
    public nl.esciencecenter.aether.impl.IbisIdentifier getRandomPoolMember() {
        return target.getRandomPoolMember();
    }

    public String[] wonElections() {
        return target.wonElections();
    }
  

}
