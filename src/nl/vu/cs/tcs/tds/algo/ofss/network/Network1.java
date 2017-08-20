package algo.ofss.network;


import static util.Options.AVERAGE_NETWORK_LATENCY;
import static util.Options.AVERAGE_NETWORK_LATENCY_MAX;
import static util.Options.AVERAGE_NETWORK_LATENCY_MIN;
import static util.Options.NETWORK_LATENCY_SD;

import java.util.Random;

import ibis.util.ThreadPool;
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
    private Random random;
    protected long lastPassive;
    
    public Network1(int nnodes) {
        this.nnodes = nnodes;
        this.nodeRunners = new NodeRunner1[nnodes];
        this.probers = new Prober1[nnodes];
        this.random = new Random();
        this.random.setSeed(System.currentTimeMillis() + Double.doubleToLongBits(Math.random()) + this.hashCode());
    }
    

    public synchronized void waitForAllNodes() {
        while (nodeCount < nnodes) {
            try { wait(); } catch (InterruptedException e) { /* ignore */ }
        }
        
        /* start the nodes */
        for (NodeRunner1 r : nodeRunners) r.start();
    }
    
    /**
     * Once a node is created this method is called to let the network learn <br/>
     * about it and attach a prober to it. <br/><br/>
     * 
     * Called by the NodeRunner constructor like this: <br/>
     * <code>
     *    network.registerNode(this);
     * </code> 
     * 
     * @param nodeRunner
     */
    public synchronized void registerNode(NodeRunner1 nodeRunner) {
        nodeCount++;
        nodeRunners[nodeRunner.getId()] = nodeRunner;
        
        probers[nodeRunner.getId()] = new Prober1(nodeRunner.getId(), nnodes, this, nodeRunner);
        nodeRunner.attachProber(probers[nodeRunner.getId()]);
        
        /* We registered the last node. Let waitForAllNodes() start them all now */
        if (nodeCount == nnodes) {
            notifyAll();
        }
        
    }

    /**
     * Send a basic message with random delay. <br/>
     * The delay has Gaussian distribution with mean {@code Options.AVERAGE_NETWORK_LATENCY} <br/>
     * and Standard Deviation {@code Options.NETWORK_LATENCY_SD} <br/>
     * Execute in separate thread to not delay the sender with it.
     * 
     * @param destination
     * @param nodeMessage
     */
    public void sendMessage(final int destination, final NodeMessage1 nodeMessage) {
        /* choose network delay */
        int d = (int) Math.round(random.nextGaussian() * NETWORK_LATENCY_SD + AVERAGE_NETWORK_LATENCY);
        if (d < AVERAGE_NETWORK_LATENCY_MIN) d = AVERAGE_NETWORK_LATENCY_MIN;
        else if (d > AVERAGE_NETWORK_LATENCY_MAX) d = AVERAGE_NETWORK_LATENCY_MAX;
        final int delay = d;
        
        ThreadPool.createNew(() -> {
            try { Thread.sleep(delay); } catch (InterruptedException e) { /* ignore */ }
            nodeRunners[destination].receiveMessage(nodeMessage);
        }, "BasicMessageSender1");
    }



    /**
     * Send a control message (in this case the token) with random delay. <br/>
     * The delay has Gaussian distribution with mean {@code Options.AVERAGE_NETWORK_LATENCY} <br/>
     * and Standard Deviation {@code Options.NETWORK_LATENCY_SD} <br/>
     * Execute in separate thread to not delay the sender with it.
     * 
     * @param destination
     * @param nodeMessage
     */
    public void sendProbeMessage(final int destination, final ProbeMessage1 probeMessage) {
        /* choose network delay */
        int d = (int) Math.round(random.nextGaussian() * NETWORK_LATENCY_SD + AVERAGE_NETWORK_LATENCY);
        if (d < AVERAGE_NETWORK_LATENCY_MIN) d = AVERAGE_NETWORK_LATENCY_MIN;
        else if (d > AVERAGE_NETWORK_LATENCY_MAX) d = AVERAGE_NETWORK_LATENCY_MAX;
        final int delay = d;
        
        ThreadPool.createNew(() -> {
            try { Thread.sleep(delay); } catch (InterruptedException e) { /* ignore */}
            probers[destination].receiveMessage(probeMessage);
        }, "ProbeSender1");
    }
    
    // To be called when termination is detected.
    public void killNodes() {
        for (NodeRunner1 r : nodeRunners) {
            r.stopRunning();
        }
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
