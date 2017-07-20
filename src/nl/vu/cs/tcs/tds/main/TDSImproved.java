package main;

import ibis.util.ThreadPool;
import performance.PerformanceLogger;
import util.Options;

import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

import algo.ifss.network.Network2;
import algo.ifss.node.NodeRunner2;

import static util.Options.*;

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
            
            TDS.writeString(0, " [IFSS ]\tInitially Active: " + initiallyActiveCount+ " (random): " + initiallyActiveList.toString());
            
            //System.out.println("RANDOM(" + initiallyActiveCount +"): " + initiallyActiveList.toString() );
            PerformanceLogger.instance().setInitiallyActive(initiallyActiveCount, 2);
            for ( int i = 0; i < nnodes; i++ ) {
                // Here choose who starts as active
                nodeRunners[i] = new NodeRunner2(i, nnodes, network, initiallyActiveList.contains(i)); 
            }

        } else if (Options.instance().get(BASIC_ALGO_TYPE) == BASIC_ALGO_DECENTRALIZED_EVEN){
   
            PerformanceLogger.instance().setInitiallyActive(nnodes % 2 == 0? nnodes / 2 : ((int) nnodes / 2) + 1, 2);
            
            TDS.writeString(0, " [IFSS ]\tInitially Active: " + PerformanceLogger.instance().getInitiallyActive(2) + " (even)");
            
            
            for ( int i = 0; i < nnodes; i++ ) {
                // Here choose who starts as active
                nodeRunners[i] = new NodeRunner2(i, nnodes, network, i % 2 == 0); 
            }
        } else {
            
            TDS.writeString(0, " [IFSS ]\tInitially Active: 1 (single)");
            
            PerformanceLogger.instance().setInitiallyActive(1, 2);
            
            for ( int i = 0; i < nnodes; i++ ) {
                // Here choose who starts as active
                nodeRunners[i] = new NodeRunner2(i, nnodes, network, i == 0); 
            }
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
