package nl.esciencecenter.aether.registry.central;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import nl.esciencecenter.aether.impl.AetherIdentifier;

public interface MemberSet {

    public int size();

    public void add(Member member);

    public Member remove(AetherIdentifier identifier);

    public boolean contains(AetherIdentifier identifier);

    public boolean contains(Member member);

    public Member get(AetherIdentifier identifier);

    //return a member from what identifier.name() returns (!= identifier.getID())
    public Member get(String name);
    
    public int getMinimumTime();

    public Member getLeastRecentlySeen();

    public Member get(int index);

    public Member getRandom();

    public Member[] getRandom(int size);

    public Member[] asArray();

    public void init(DataInputStream in) throws IOException;

    public void writeTo(DataOutputStream out) throws IOException;

    public List<Event> getJoinEvents();
    
    public Member[] getChildren(AetherIdentifier ibis);

    public Member[] getRootChildren();
}
