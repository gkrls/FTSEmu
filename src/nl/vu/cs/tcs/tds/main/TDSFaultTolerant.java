package main;

import ibis.util.ThreadPool;
import performance.PerformanceLogger;
import util.Options;

import static util.Options.BASIC_ALGO_DECENTRALIZED_EVEN;
import static util.Options.BASIC_ALGO_DECENTRALIZED_RANDOM;
import static util.Options.BASIC_ALGO_TYPE;

import java.util.ArrayList;
import java.util.Random;

import algo.fts.network.Network3;
import algo.fts.node.NodeRunner3;
import algo.ifss.node.NodeRunner2;

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

        if(Options.instance().get(BASIC_ALGO_TYPE) == BASIC_ALGO_DECENTRALIZED_RANDOM) {
            
            Random random = new Random();
            //choose how many to activate
            int initiallyActiveCount = random.nextInt(nnodes);
            ArrayList<Integer> initiallyActiveList = new ArrayList<Integer>();
            for ( int i = 0; i < initiallyActiveCount; i++ ) {
                Integer newActive = random.nextInt(nnodes);
                while(initiallyActiveList.contains(newActive)) 
                    newActive = random.nextInt(nnodes);
                initiallyActiveList.add(newActive);
            }
            
            TDS.writeString(0, " [ FTS ]\tInitially Active: " + initiallyActiveCount+ " (random): " + initiallyActiveList.toString());
            
            PerformanceLogger.instance().setInitiallyActive(initiallyActiveCount, 2);
            for ( int i = 0; i < nnodes; i++ ) {
                // Here choose who starts as active
                nodeRunners[i] = new NodeRunner3(i, nnodes, network, initiallyActiveList.contains(i)); 
            }

        } else if (Options.instance().get(BASIC_ALGO_TYPE) == BASIC_ALGO_DECENTRALIZED_EVEN){
            
            TDS.writeString(0, " [ FTS ]\tInitially Active: " + PerformanceLogger.instance().getInitiallyActive(2) + " (even)");
            
            PerformanceLogger.instance().setInitiallyActive(nnodes % 2 == 0? nnodes / 2 : ((int) nnodes / 2) + 1, 2);
            for ( int i = 0; i < nnodes; i++ ) {
                // Here choose who starts as active
                nodeRunners[i] = new NodeRunner3(i, nnodes, network, i % 2 == 0); 
            }
        } else {
            
            TDS.writeString(0, " [ FTS ]\tInitially Active: 1 (single)");
            
            PerformanceLogger.instance().setInitiallyActive(1, 2);
            for ( int i = 0; i < nnodes; i++ ) {
                // Here choose who starts as active
                nodeRunners[i] = new NodeRunner3(i, nnodes, network, i == 0); 
            }
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
