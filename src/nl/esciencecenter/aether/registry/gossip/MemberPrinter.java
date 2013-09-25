package nl.esciencecenter.aether.registry.gossip;

import nl.esciencecenter.aether.util.ThreadPool;

public class MemberPrinter implements Runnable {
    
    public static final int INTERVAL = 10000;
    
    private final MemberSet pool;
    
    MemberPrinter(MemberSet pool) {
        this.pool = pool;
        
        ThreadPool.createNew(this, "member printer");
    }
    
    public synchronized void run() {
        while (pool.isAlive()) {
            System.out.println("*************************************");
            pool.printMembers();
            System.out.println("*************************************");
            
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                //IGNORE
            }
        }
    }


}
