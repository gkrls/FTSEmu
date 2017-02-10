package nl.vu.cs.tcs.tds.main;

import ibis.util.ThreadPool;
import tds.td.correct2.network.Network5;
import tds.td.correct2.node.NodeRunner5;

public class TDSCorrect2 implements Runnable{
    
    private static boolean done;
    private int nnodes;
    private NodeRunner5[] nodeRunners;
    private Network5 network;
    private long maxWait;
    
    
    public TDSCorrect2(int nnodes, long maxWait) {
        this.nnodes = nnodes;
        this.done = false;
        this.nodeRunners = new NodeRunner5[nnodes];
        this.network = new Network5(nnodes);
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
                    TDS.writeString(0, "NO TERMINATION DETECTED IN " + maxWait + " ms");
                    this.setDone();
                }, "TimeoutCount_5");
                wait();
            }catch (InterruptedException e){}
        }
    }
    
    public void run() {
        for ( int i = 0; i < nnodes; i++ ) {
            // Here choose who starts as active
            nodeRunners[i] = new NodeRunner5(i, nnodes, network, i % 2 == 0 || i % 5 == 0); 
        }
        
        network.waitForAllNodes();
        waitTillDone();
        network.killNodes();
        TDS.instance().setDone(5);
    }
    
    public synchronized void announce() {
        done = true;
        notifyAll();
    }
}
