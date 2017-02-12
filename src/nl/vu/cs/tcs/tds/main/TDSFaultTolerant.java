package main;

import ibis.util.ThreadPool;
import performance.PerformanceLogger;
import algo.fts.network.Network3;
import algo.fts.node.NodeRunner3;

public class TDSFaultTolerant implements Runnable{
    
    private volatile boolean done;
    private int nnodes;
    private NodeRunner3[] nodeRunners;
    private Network3 network;
    private long maxWait;
    
    
    public TDSFaultTolerant(int nnodes, long maxWait) {
        this.nnodes = nnodes;
        this.done = false;
        this.nodeRunners = new NodeRunner3[nnodes];
        this.network = new Network3(nnodes);
        this.maxWait = maxWait;
        
    }
    
    public synchronized void setDone() {
        done = true;
        notifyAll();
    }
    
    private synchronized void waitTillDone(){
        while(!done){
            try{
                ThreadPool.createNew(() -> {
                    try{
                        Thread.sleep(maxWait);
                    }catch(Exception e){
                        
                    }
                    
                    TDS.writeString(-1, " [FTS]\tNO TERMINATION DETECTED IN " + maxWait + " ms" );
                    this.setDone();
                    PerformanceLogger.instance().timeout(3);
                }, "TimeoutCount3");
                wait();
            }catch(InterruptedException e){}
        }
    }
    

    
    @Override
    public void run() {
        for(int i = 0; i < nnodes; i++) {
            nodeRunners[i] = new NodeRunner3(i, nnodes, network, i == 0);
        }
        
        network.waitForAllNodes();
        this.waitTillDone();
        network.killNodes();
        TDS.instance().setDone(3);
    }
    
    public synchronized void announce() {
        done = true;
        notifyAll();
    }

}
