package algo.fts.network;

import java.util.HashSet;
import java.util.Random;

import ibis.util.ThreadPool;
import performance.PerformanceLogger;
import util.Options;
import algo.fts.node.FailureDetector;
import algo.fts.node.NodeMessage3;
import algo.fts.node.NodeRunner3;
import algo.fts.probing.ProbeMessage3;
import algo.fts.probing.Prober3;

import static util.Options.*;

public class Network3 {
    
    private final int nnodes;
    private final NodeRunner3[] nodeRunners;
    private final Prober3[] probers;
    private final NodeCrasher nc;
    private final FailureDetector[] fds;
    
    private HashSet<Integer> crashed;
    private int nodeCount;
    private Random random;
    /* volatile maybe not needed */
    private volatile boolean stopAll;
    private int tokenLastVisited;
    
    protected long lastPassive;
    
    
    
    public Network3(int nnodes) {
        this.nnodes = nnodes;
        this.nodeCount = 0;
        this.random = new Random();
        this.nodeRunners = new NodeRunner3[nnodes];
        this.probers = new Prober3[nnodes];
        this.fds = new FailureDetector[nnodes];
        this.nc = new NodeCrasher(this, nnodes);
        this.crashed = new HashSet<Integer>();
        this.stopAll = false;
        this.tokenLastVisited = -1;
    }
    
    public synchronized void tokenLastVisited(int node) {
        this.tokenLastVisited = node;
    }
    
    public synchronized void waitForAllNodes() {
        while(nodeCount < nnodes){
            try { wait(); } catch (InterruptedException e) {}
        }
        
        for(NodeRunner3 r: nodeRunners) r.start();
        nc.start();
    }
    
    public synchronized void registerNode(NodeRunner3 nodeRunner) {
        nodeCount++;
        nodeRunners[nodeRunner.getId()] = nodeRunner;
        fds[nodeRunner.getId()] = new FailureDetector(nodeRunner.getId(), nnodes, this, nodeRunner);
        probers[nodeRunner.getId()] = new Prober3(nodeRunner.getId(), nnodes, this, nodeRunner, fds[nodeRunner.getId()]);
        fds[nodeRunner.getId()].linkWithProber(probers[nodeRunner.getId()]);
        
        nodeRunner.attachProber(probers[nodeRunner.getId()]);
        if(nodeCount == nnodes) { notifyAll(); }
    }
    
    public void sendMessage(final int dest, final NodeMessage3 msg) {
        if(!crashed.contains(dest)) {
            
            /** choose network delay **/
            int d = (int) Math.round(random.nextGaussian() * NETWORK_LATENCY_SD + AVERAGE_NETWORK_LATENCY);
            if (d < AVERAGE_NETWORK_LATENCY_MIN) d = AVERAGE_NETWORK_LATENCY_MIN;
            else if (d > AVERAGE_NETWORK_LATENCY_MAX) d = AVERAGE_NETWORK_LATENCY_MAX;
            final int delay = d;
            
            ThreadPool.createNew(() -> {
                try { Thread.sleep(delay); } catch (InterruptedException e) {}
                nodeRunners[dest].receiveMessage(msg);
            }, "Sender3");
        }
    }
    
    public void sendCrashMessage(int dest, int crashedNode) throws NodeCrasherStopException {
        
        if(stopAll) 
            throw new NodeCrasherStopException();
        
        if(!crashed.contains(dest)) {
            
            if(stopAll) 
                throw new NodeCrasherStopException();
            
            ThreadPool.createNew(() -> {
                try { 
                
                    if(stopAll) return;
                    
                    Thread.sleep( Options.instance().get(CRASH_NOTIFY_INTERVAL)
                            + Options.instance().get(AVERAGE_NETWORK_LATENCY)
                            + random.nextInt( Options.instance().get(CRASH_NOTIFY_INTERVAL) ) ); 
                
                } catch (InterruptedException e){}
                
                if(stopAll) return;
                
                System.out.println("Node " + dest + " learnt crash of" + crashedNode +" !" );
                fds[dest].receiveCrash(crashedNode);
                
            }, "NodeCrasher3");
        }
        
        if(stopAll) 
            throw new NodeCrasherStopException();
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
    
    public void crash(int node) {
        if(nodeRunners[node] != null ){ //temporary solution
            nodeRunners[node].crash();
            this.crashed.add(node);
        }
    }
    
    // When a node becomes passive, this method gets called, to register the time.
    public void registerPassive() {
        ThreadPool.createNew(new Runnable() {
            @Override
            public void run() {
                synchronized (Network3.class) {
                    lastPassive = System.currentTimeMillis();
                    
                    /**
                     * Every time some node becomes passive sets number of tokens and number of backup tokens
                     * until termination to be the number of tokens and backup tokens respectively recorded so far.
                     * 
                     * Meanwhile the Prober keeps increasing the number of tokens (or backup tokens) sent.
                     * Thus in the end we can just subtract the number of tokens up to termination from the
                     * total tokens to find the extra.
                     * 
                     * */
                    
                    PerformanceLogger.instance().setTokensUpToTerm(3);
                    PerformanceLogger.instance().setBackupTokensUpToTerm(3);
                }
                
            }
        }, "PassiveRegister3");
    }
    
    
    public long getLastPassive(){
        return this.lastPassive;
    }

    
    public void killNodes() {
        this.stopAll = true;
        nc.stop();
        for(NodeRunner3 r: nodeRunners) { r.stopRunning(); }
    }


    public void sendFirstProbeMessage(int i, ProbeMessage3 token) {
        ThreadPool.createNew(() -> {
            probers[i].receiveFirstMessage(token);
        }, "ProbeSender3");
        
    }

    public void sendProbeMessage(ProbeMessage3 token, int dest) {
        /** choose delay **/
        int d = (int) Math.round(random.nextGaussian() * NETWORK_LATENCY_SD + AVERAGE_NETWORK_LATENCY);
        if (d < AVERAGE_NETWORK_LATENCY_MIN) d = AVERAGE_NETWORK_LATENCY_MIN;
        else if (d > AVERAGE_NETWORK_LATENCY_MAX) d = AVERAGE_NETWORK_LATENCY_MAX;
        final int delay = d;
        
        ThreadPool.createNew(() -> {
            try { Thread.sleep(delay); } catch(InterruptedException e) {}
            probers[dest].receiveMessage(token);
        }, "ProbeSender3");
    }



    
    
    

}
