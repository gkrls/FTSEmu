package main;

import ibis.util.ThreadPool;
import tds.performance.PerformanceLogger;
import tds.td.ft2.network.Network6;
import tds.td.ft2.node.NodeRunner6;

public class TDSFaultTolerant implements Runnable{
    
    private volatile boolean done;
    private int nnodes;
    private NodeRunner6[] nodeRunners;
    private Network6 network;
    private long maxWait;
    
    
    public TDSFaultTolerant(int nnodes, long maxWait) {
        this.nnodes = nnodes;
        this.done = false;
        this.nodeRunners = new NodeRunner6[nnodes];
        this.network = new Network6(nnodes);
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
                        System.out.println("YES!");
                    }
                    PerformanceLogger.instance().timeout(6);
                    TDS.writeString(0, "NO TERMINATION DETECTED IN " + maxWait + " ms" );
                    this.setDone();
                }, "TimeoutCount6");
                wait();
            }catch(InterruptedException e){}
        }
    }
    

    
    @Override
    public void run() {
        for(int i = 0; i < nnodes; i++) {
            nodeRunners[i] = new NodeRunner6(i, nnodes, network, i == 0);
        }
        
        network.waitForAllNodes();
        this.waitTillDone();
        network.killNodes();
        TDS.instance().setDone(6);
    }
    
    public synchronized void announce() {
        done = true;
        notifyAll();
    }

}
