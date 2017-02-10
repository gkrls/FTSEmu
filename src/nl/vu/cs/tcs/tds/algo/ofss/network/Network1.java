package nl.vu.cs.tcs.tds.algo.ofss.network;


import java.util.Random;

import ibis.util.ThreadPool;
import tds.main.TDS;
import tds.performance.PerformanceLogger;
import tds.td.original.node.NodeMessage1;
import tds.td.original.node.NodeRunner1;
import tds.td.original.probing.ProbeMessage1;
import tds.td.original.probing.Prober1;

public class Network1 {

    private final int nnodes;
    private final NodeRunner1[] nodeRunners;
    private final Prober1[] probers;
    private int nodeCount = 0;
    private Random random = new Random();
    protected long lastPassive;
    
    private int tokenLastVisited;

    public Network1(int nnodes) {
        this.nnodes = nnodes;
        nodeRunners = new NodeRunner1[nnodes];
        probers = new Prober1[nnodes];
        this.tokenLastVisited = -1;
    }
    
    public synchronized int tokenLastVisited(){
    	return this.tokenLastVisited;
    }
    

    public synchronized void waitForAllNodes() {
        while (nodeCount < nnodes) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        for (NodeRunner1 r : nodeRunners) {
            r.start();
        }
    }

    public synchronized void registerNode(NodeRunner1 nodeRunner) {
        nodeCount++;
        nodeRunners[nodeRunner.getId()] = nodeRunner;
        
        probers[nodeRunner.getId()] = new Prober1(nodeRunner.getId(), nnodes, this, nodeRunner);
        nodeRunner.attachProber(probers[nodeRunner.getId()]);
        if (nodeCount == nnodes) {
            notifyAll();
        }
    }

    // Send message with random delay. Execute in separate thread to not delay the sender with it.
    public void sendMessage(final int destination, final NodeMessage1 nodeMessage) {
        final int delay = random.nextInt(50);
        //PerformanceLogger.instance().addMessage(nodeMessage, nodeRunners[destination]);
        ThreadPool.createNew(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // ignore
                }
                nodeRunners[destination].receiveMessage(nodeMessage);
            }
        }, "Sender");
    }

    // To be called when termination is detected.
    public void killNodes() {
        for (NodeRunner1 r : nodeRunners) {
            r.stopRunning();
        }
    }

    // Send message with random delay. Execute in separate thread to not delay the sender with it.
    public void sendProbeMessage(final int destination, final ProbeMessage1 probeMessage) {
        final int delay = random.nextInt(50);
        ThreadPool.createNew(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // ignore
                }
                probers[destination].receiveMessage(probeMessage);
            }
        }, "Sender");
    }

    // Find a target for a message from the specified node.
    // Here, you could implement restrictions in the network topology.
    public int selectTarget(int mynode) {
        if (nnodes == 1) {
            return mynode;
        }
        for (;;) {
            int dest = random.nextInt(nnodes);
            if (dest != mynode) {
                return dest;
            }
        }
    }

    // When a node becomes passive, this method gets called, to register the
    // time.
    public void registerPassive() {
    	
        ThreadPool.createNew(new Runnable() {
            @Override
            public void run() {
                synchronized (Network1.class) {
                    lastPassive = System.currentTimeMillis();
                    PerformanceLogger.instance().setTokensUpToTerm(1);
                }
                
            }
        }, "PassiveRegister");
    }
    
    public long getLastPassive(){
    	return this.lastPassive;
    }
}
