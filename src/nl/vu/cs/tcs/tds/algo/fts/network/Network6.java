package algo.fts.network;

import java.util.HashSet;
import java.util.Random;

import ibis.util.ThreadPool;
import performance.PerformanceLogger;
import algo.fts.node.FailureDetector;
import algo.fts.node.NodeMessage6;
import algo.fts.node.NodeRunner6;
import algo.fts.probing.ProbeMessage6;
import algo.fts.probing.Prober6;

public class Network6 {
    
    private final int nnodes;
    private final NodeRunner6[] nodeRunners;
    private final Prober6[] probers;
    private final NodeCrasher2 nc;
    private final FailureDetector[] fds;
    
    private HashSet<Integer> crashed;
    private int nodeCount;
    private Random random;
    private boolean stopAll;
    private int tokenLastVisited;
    
    protected long lastPassive;
    
    
    
    public Network6(int nnodes) {
        this.nnodes = nnodes;
        this.nodeCount = 0;
        this.random = new Random();
        this.nodeRunners = new NodeRunner6[nnodes];
        this.probers = new Prober6[nnodes];
        this.fds = new FailureDetector[nnodes];
        this.nc = new NodeCrasher2(this, nnodes);
        this.crashed = new HashSet<Integer>();
        this.stopAll = false;
        this.tokenLastVisited = -1;
    }
    
    public synchronized void tokenLastVisited(int node) {
        this.tokenLastVisited = node;
    }
    
    public synchronized void waitForAllNodes() {
        while(nodeCount < nnodes){
            try { wait(); } catch (InterruptedException e) {System.out.println("HERE OK");}
        }
        
        for(NodeRunner6 r: nodeRunners) r.start();
        nc.start();
    }
    
    public synchronized void registerNode(NodeRunner6 nodeRunner) {
        nodeCount++;
        nodeRunners[nodeRunner.getId()] = nodeRunner;
        fds[nodeRunner.getId()] = new FailureDetector(nodeRunner.getId(), nnodes, this, nodeRunner);
        probers[nodeRunner.getId()] = new Prober6(nodeRunner.getId(), nnodes, this, nodeRunner, fds[nodeRunner.getId()]);
        fds[nodeRunner.getId()].linkWithProber(probers[nodeRunner.getId()]);
        
        nodeRunner.attachProber(probers[nodeRunner.getId()]);
        if(nodeCount == nnodes) { notifyAll(); }
    }
    
    public void sendMessage(final int dest, final NodeMessage6 msg) {
        if(!crashed.contains(dest)) {
            final int delay = random.nextInt(50);
            ThreadPool.createNew(() -> {
                try { Thread.sleep(delay); } catch (InterruptedException e) {}
                nodeRunners[dest].receiveMessage(msg);
            }, "Sender 6");
        }
    }
    
    public void sendCrashMessage(int dest, int crashedNode) throws NodeCrasherStopException {
        if(stopAll) throw new NodeCrasherStopException();
        if(!crashed.contains(dest)) {
            final int delay = random.nextInt(1000);
            if(stopAll) throw new NodeCrasherStopException();
            ThreadPool.createNew(() -> {
                try { Thread.sleep(delay); } catch (InterruptedException e){}
                if(stopAll) return;
                fds[dest].receiveCrash(crashedNode);
            }, "NodeCrasher6");
        }
        if(stopAll) throw new NodeCrasherStopException();
    }
    
    public int selectTarget(int mynode) {
        if(nnodes == 1) return mynode; //this should never happen
        for(;;) {
            int dest = random.nextInt(nnodes);
            if( dest != mynode) return dest;
        }
    }
    
    public void crash(int node) {
        if(nodeRunners[node] != null ){ //temporary solution
            nodeRunners[node].crash();
            this.crashed.add(node);
        }
    }
    
    // When a node becomes passive, this method gets called, to register the
    // time.
    public void registerPassive() {
        ThreadPool.createNew(new Runnable() {
            @Override
            public void run() {
                synchronized (Network6.class) {
                    lastPassive = System.currentTimeMillis();
                    PerformanceLogger.instance().setTokensUpToTerm(6);
                    PerformanceLogger.instance().setBackupTokensUpToTerm(6);
                }
                
            }
        }, "PassiveRegister6");
    }
    
    public void registerPassive(int node){
        this.crashed.add(node);
    }
    
    public long getLastPassive(){
        return this.lastPassive;
    }

    
    public void killNodes() {
        this.stopAll = true;
        nc.stop();
        for(NodeRunner6 r: nodeRunners) { r.stopRunning(); }
    }


    public void sendFirstProbeMessage(int i, ProbeMessage6 token) {
        ThreadPool.createNew(() -> {
            probers[i].receiveFirstMessage(token);
        }, "ProbeSender6");
        
    }

    public void sendProbeMessage(ProbeMessage6 token, int dest) {
        ThreadPool.createNew(() -> {
            int delay = random.nextInt(50);
            try { Thread.sleep(delay); } catch(InterruptedException e) {}
            probers[dest].receiveMessage(token);
        }, "ProbeSender6");
    }



    
    
    

}
