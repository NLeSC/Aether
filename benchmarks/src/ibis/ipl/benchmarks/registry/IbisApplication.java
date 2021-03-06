package ibis.ipl.benchmarks.registry;

import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.CreationFailedException;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.RegistryEventHandler;
import nl.esciencecenter.aether.util.ThreadPool;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class IbisApplication implements Runnable, RegistryEventHandler {

    private static final Logger logger =
        LoggerFactory.getLogger(IbisApplication.class);

    private final boolean generateEvents;

    private Set<AetherIdentifier> ibisses;

    private boolean stopped = false;

    private final Random random;

    private Aether ibis;

    private final PortType portType;

    IbisApplication(boolean generateEvents, boolean fail)
            throws CreationFailedException, IOException {
        this.generateEvents = generateEvents;

        ibisses = new HashSet<AetherIdentifier>();
        random = new Random();

        portType =
            new PortType(PortType.CONNECTION_ONE_TO_ONE,
                    PortType.SERIALIZATION_OBJECT);

        if (!fail) {
            // register shutdown hook
            try {
                Runtime.getRuntime().addShutdownHook(new Shutdown(this));
            } catch (Exception e) {
                // IGNORE
            }
        }

        ThreadPool.createNew(this, "application");
    }

    private synchronized boolean stopped() {
        return stopped;
    }

    synchronized void end() {
        stopped = true;
        notifyAll();

        if (ibis != null) {
            try {
                ibis.end();
            } catch (IOException e) {
                logger.error("cannot end ibis: " + e);
            }
        }
    }

    public synchronized void joined(AetherIdentifier ident) {
        ibisses.add(ident);
        logger.info("upcall for join of: " + ident);
    }

    public synchronized void left(AetherIdentifier ident) {
        ibisses.remove(ident);
        logger.info("upcall for leave of: " + ident);
    }

    public synchronized void died(AetherIdentifier corpse) {
        ibisses.remove(corpse);
        logger.info("upcall for died of: " + corpse);
    }

    public synchronized void gotSignal(String signal, AetherIdentifier source) {
        logger.info("got string: " + signal + " from " + source);
    }

    public synchronized void electionResult(String electionName,
            AetherIdentifier winner) {
        logger.info("got election result for :\"" + electionName + "\" : "
                + winner);
    }
    
	public void poolClosed() {
        logger.info("pool now closed");
	}

	public void poolTerminated(AetherIdentifier source) {
        logger.info("pool terminated by " + source);
	}

    private static class Shutdown extends Thread {
        private final IbisApplication app;

        Shutdown(IbisApplication app) {
            this.app = app;
        }

        public void run() {
            // System.err.println("shutdown hook triggered");

            app.end();
        }
    }

    public synchronized int nrOfIbisses() {
        return ibisses.size();
    }

    private synchronized AetherIdentifier getRandomIbis() {
        if (ibisses.isEmpty()) {
            return null;
        }

        int element = random.nextInt(ibisses.size());

        for (AetherIdentifier ibis : ibisses) {
            if (element == 0) {
                return ibis;
            }
            element--;
        }

        return null;
    }

    // get random ibisses. May/will contain some duplicates :)
    private synchronized AetherIdentifier[] getRandomIbisses() {
        if (nrOfIbisses() == 0) {
            return new AetherIdentifier[0];
        }

        AetherIdentifier[] result =
            new AetherIdentifier[random.nextInt(nrOfIbisses())];

        for (int i = 0; i < result.length; i++) {
            result[i] = getRandomIbis();
        }

        return result;
    }

    synchronized void doElect(String id) throws IOException {
        ibis.registry().elect(id);
    }

    synchronized void getElectionResult(String id) throws IOException {
        ibis.registry().getElectionResult(id);
    }

    public void run() {
        logger.debug("creating ibis");
        try {
            synchronized (this) {

                Capabilities s =
                    new Capabilities(
                            Capabilities.MEMBERSHIP_UNRELIABLE,
                            Capabilities.ELECTIONS_UNRELIABLE,
                            Capabilities.SIGNALS);

                ibis = AetherFactory.createIbis(s, this, portType);

                logger.debug("ibis created, enabling upcalls");

                ibis.registry().enableEvents();
                logger.debug("upcalls enabled");
            }

        } catch (Exception e) {
            logger.error("cannot start ibis", e);
        }

        // start Ibis, generate events, stop Ibis, repeat
        while (true) {
            try {
                while (true) {
                    if (stopped()) {
                        return;
                    }

                    if (generateEvents) {

                        int nextCase = random.nextInt(6);

                        switch (nextCase) {
                        case 0:
                            logger.debug("signalling random member(s)");
                            AetherIdentifier[] signalList = getRandomIbisses();

                            ibis.registry().signal("ARRG to you all!",
                                signalList);
                            break;
                        case 1:
                            logger.debug("doing elect");
                            ibis.registry().elect("bla");
                            break;
                        case 2:
                            logger.debug("doing getElectionResult");
                            // make sure this election exists
                            ibis.registry().elect("bla");

                            ibis.registry().getElectionResult("bla");
                            break;
                        case 3:
                            logger.debug("doing getElectionResult with timeout");
                            ibis.registry().getElectionResult("bla", 100);
                            logger.debug("done getElectionResult with timeout");
                            break;
                        case 4:
                            logger.debug("doing maybeDead() on random ibis");
                            AetherIdentifier suspect = getRandomIbis();
                            if (suspect != null) {
                                ibis.registry().maybeDead(suspect);
                            }
                            break;
                        case 5:
                            logger.debug("signalling random member");
                            AetherIdentifier signallee = getRandomIbis();

                            if (signallee != null) {
                                ibis.registry().signal("ARRG!", signallee);
                            }
                            break;
                        default:
                            logger.error("unknown case: " + nextCase);
                        }

                        logger.info("done");
                    }
                    synchronized (this) {
                        try {
                            wait(10000);
                        } catch (InterruptedException e) {
                            // IGNORE
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("error in  application", e);
            }
        }

    }



}
