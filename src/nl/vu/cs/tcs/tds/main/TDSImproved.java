package main;

import ibis.util.ThreadPool;
import algo.ifss.network.Network2;
import algo.ifss.node.NodeRunner2;

public class TDSImproved implements Runnable{
    
    private static boolean done;
    private int nnodes;
    private NodeRunner2[] nodeRunners;
    private Network2 network;
    private long maxWait;
    
    
    public TDSImproved(int nnodes, long maxWait) {
        this.nnodes = nnodes;
        this.done = false;
        this.nodeRunners = new NodeRunner2[nnodes];
        this.network = new Network2(nnodes);
        this.maxWait = maxWait;
    }
    
    public synchronized void setDone() {
        done = true;
        notifyAll();
    }
    
    private synchronized void waitTillDone() {
        while(!done) {
            try {
                ThreadPool.createNew(() -> {
                    try {
                        Thread.sleep(maxWait);
                    } catch (Exception e) {}
                    
                    TDS.writeString(-1, " [I-FSS]\tNO TERMINATION DETECTED IN " + maxWait + " ms");
                    this.setDone();
                }, "TimeoutCount_2");
                wait();
            }catch (InterruptedException e){}
        }
    }
    
    public void run() {
        for ( int i = 0; i < nnodes; i++ ) {
            // Here choose who starts as active
            nodeRunners[i] = new NodeRunner2(i, nnodes, network, i % 2 == 0 || i % 5 == 0); 
        }
        
        network.waitForAllNodes();
        waitTillDone();
        network.killNodes();
        TDS.instance().setDone(2);
    }
    
    public synchronized void announce() {
        done = true;
        notifyAll();
    }
}
