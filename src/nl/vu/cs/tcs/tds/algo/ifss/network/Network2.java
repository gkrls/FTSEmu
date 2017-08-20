package algo.ifss.network;


import static util.Options.AVERAGE_NETWORK_LATENCY;
import static util.Options.AVERAGE_NETWORK_LATENCY_MAX;
import static util.Options.AVERAGE_NETWORK_LATENCY_MIN;
import static util.Options.NETWORK_LATENCY_SD;

import java.util.Random;

import ibis.util.ThreadPool;
import performance.PerformanceLogger;
import algo.ifss.node.NodeMessage2;
import algo.ifss.node.NodeRunner2;
import algo.ifss.probing.ProbeMessage2;
import algo.ifss.probing.Prober2;

public class Network2 {
    
    private final int nnodes;
    private final NodeRunner2[] nodeRunners;
    private final Prober2[] probers;
    private Random random;
    private int nodeCount;
    protected long lastPassive;
    
    
    public Network2(int nnodes){
        this.nnodes = nnodes;
        nodeCount = 0;
        random = new Random();
        this.random.setSeed(System.currentTimeMillis() + Double.doubleToLongBits(Math.random()) + this.hashCode());
        nodeRunners = new NodeRunner2[nnodes];
        probers = new Prober2[nnodes];
    }
    
    public synchronized void waitForAllNodes() {
        while(nodeCount < nnodes) {
            try {
                wait();
            } catch (InterruptedException e){
                break;
            }
        }
        
        for(NodeRunner2 r: nodeRunners) {
            r.start();
        }
    }
    
    public synchronized void registerNode(NodeRunner2 nodeRunner) {
        nodeRunners[nodeRunner.getId()] = nodeRunner;
        probers[nodeRunner.getId()] = new Prober2(nodeRunner.getId(), nnodes, this, nodeRunner);
        nodeRunner.attachProber(probers[nodeRunner.getId()]);
        nodeCount++;
        if(nodeCount == nnodes) {
            notifyAll();
        }
    }
    
    // Send message with random delay. Execute in separate thread to not delay the sender with it.
    public void sendMessage(final int dest, final NodeMessage2 nodeMessage) {
        /** choose network delay **/
        int d = (int) Math.round(random.nextGaussian() * NETWORK_LATENCY_SD + AVERAGE_NETWORK_LATENCY);
        if (d < AVERAGE_NETWORK_LATENCY_MIN) d = AVERAGE_NETWORK_LATENCY_MIN;
        else if (d > AVERAGE_NETWORK_LATENCY_MAX) d = AVERAGE_NETWORK_LATENCY_MAX;
        final int delay = d;
        
        ThreadPool.createNew(() -> {
            try { Thread.sleep(delay); } catch (InterruptedException e){}
            nodeRunners[dest].receiveMessage(nodeMessage);
        }, "Sender2");
    }
    //To be called when termination is detected
    public void killNodes() {
        for(NodeRunner2 nr : nodeRunners )
            nr.stopRunning();
    }
    
    
    // Send the token and receive it after random delay. Execute in seperate thread to not delay the sender.
    public void sendProbeMessage(final int dest, final ProbeMessage2 probeMessage) {
        /** choose network delay **/
        int d = (int) Math.round(random.nextGaussian() * NETWORK_LATENCY_SD + AVERAGE_NETWORK_LATENCY);
        if (d < AVERAGE_NETWORK_LATENCY_MIN) d = AVERAGE_NETWORK_LATENCY_MIN;
        else if (d > AVERAGE_NETWORK_LATENCY_MAX) d = AVERAGE_NETWORK_LATENCY_MAX;
        final int delay = d;
        
        ThreadPool.createNew(() -> {
            try { Thread.sleep(delay); } catch (InterruptedException e){}
            probers[dest].receiveMessage(probeMessage);
        }, "ProbeSender2");
    }
    
    public void sendFirstProbeMessage(final int dest, final ProbeMessage2 probeMessage) {
        ThreadPool.createNew(() -> {
            probers[dest].receiveFirstMessage(probeMessage);
        }, "ProbeSender2");
    }
    
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
    
    
    public void registerPassive() {
        ThreadPool.createNew(() -> {
            synchronized (Network2.class) {
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
                
                PerformanceLogger.instance().setTokensUpToTerm(2);
            }
        }, "PassiveRegister2");
    }

    public long getLastPassive() {
        return this.lastPassive;
    }


}
