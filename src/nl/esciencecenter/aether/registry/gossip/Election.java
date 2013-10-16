package nl.esciencecenter.aether.registry.gossip;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.esciencecenter.aether.impl.AetherIdentifier;

public class Election {
    
    private final String name;
    private SortedSet<AetherIdentifier> candidates;
    
    Election(String name) {
        this.name = name;
        
        candidates = new TreeSet<AetherIdentifier>(new AetherComparator());
    }
    
    Election(DataInputStream in) throws IOException {
        name = in.readUTF();
        candidates = new TreeSet<AetherIdentifier>(new AetherComparator());
        
        int nrOfCandidates = in.readInt();
        
        if (nrOfCandidates < 0) {
            throw new IOException("negative candidate list value");
        }
        
        for(int i = 0; i < nrOfCandidates; i++) {
            candidates.add(new AetherIdentifier(in));
        }
    }
    
    synchronized void writeTo(DataOutputStream out) throws IOException {
        out.writeUTF(name);
        out.writeInt(candidates.size());
        for(AetherIdentifier candidate: candidates) {
            candidate.writeTo(out);
        }
    }

    synchronized void merge(Election other) {
        for(AetherIdentifier candidate: other.candidates) {
            candidates.add(candidate);
        }
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized int nrOfCandidates() {
        return candidates.size();
    }
    
    public synchronized AetherIdentifier getWinner() {
        if (candidates.isEmpty()) {
            return null;
        }
        
        //use sorting function of set to determine winner
        return candidates.first();
    }
    
    public synchronized AetherIdentifier[] getCandidates() {
        if (candidates.isEmpty()) {
            return new AetherIdentifier[0];
        }
        
        //use sorting function of set to determine winner
        return candidates.toArray(new AetherIdentifier[0]);
    }
    
    public synchronized void addCandidate(AetherIdentifier candidate) {
        candidates.add(candidate);
    }
    
    public synchronized String toString() {
        String result = name + " candidates: ";
        
        for(AetherIdentifier candidate: candidates) {
            result += candidate;
        }
        
        return result;
    }
    
    
    
}
