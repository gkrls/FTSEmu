package algo.ofss.network;


import java.util.Random;

import ibis.util.ThreadPool;
import main.TDS;
import performance.PerformanceLogger;
import algo.ofss.node.NodeMessage1;
import algo.ofss.node.NodeRunner1;
import algo.ofss.probing.ProbeMessage1;
import algo.ofss.probing.Prober1;

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
        }, "MessageSender1");
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
        }, "ProbeSender1");
    }

    // Find a target for a message from the specified node.
    // Here we could implement restrictions in the network topology.
    public int selectTargetUniform(int mynode) {
        if(nnodes == 1) {
            System.out.println("WARNING: nnodes = 1, node 0 about to send msg to itself!");
            return mynode;
        }
        
        for (;;) {
            int dest = random.nextInt(nnodes);
            if(dest != mynode) return dest;
        }
    }
    
    public int selectTargetGaussian(int mynode){
        if(nnodes == 1) {
            System.out.println("WARNING: nnodes = 1, node 0 about to send msg to itself!");
            return mynode;
        }
        
        int dest = -1;
        
        
        for (;;) {
            /* pick a candidate */
            dest = random.nextInt(nnodes);
            
            /* make sure it's not us */
            while (dest == mynode)
                dest = random.nextInt(nnodes);
            
            /* decide if destination */
            if ( random.nextGaussian() >= 0)
                return dest;
        }
    }

    // When a node becomes passive, this method gets called, to register the time.
    public void registerPassive() {
    	
        ThreadPool.createNew(new Runnable() {
            @Override
            public void run() {
                synchronized (Network1.class) {
                    lastPassive = System.currentTimeMillis();
                    
                    /**
                     * Every time some node becomes passive sets number of tokens until termination to be
                     * the number of tokens recorded so far.
                     * 
                     * Meanwhile the Prober keeps increasing the number of tokens sent.
                     * Thus in the end we can just subtract the number of tokens up to termination from the
                     * total tokens to find the extra.
                     * 
                     * */
                    
                    PerformanceLogger.instance().setTokensUpToTerm(1);
                }
                
            }
        }, "PassiveRegister1");
    }
    
    public long getLastPassive(){
    	return this.lastPassive;
    }
}
