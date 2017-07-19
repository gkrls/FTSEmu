package algo.fts.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import ibis.util.ThreadPool;
import util.Options;
import main.TDS;

import static util.Options.*;

public class NodeCrasher {
    
    private Network3 network;
    private int numCrashedNodes;
    private int[] crashedNodes;
    private int nnodes;
    /* maybe volatile is not needed! */
    private volatile boolean stop;
    
    private Random random;
    
    public NodeCrasher(Network3 network, int nnodes) {
        this.stop = false;
        this.network = network;
        this.nnodes = nnodes;
        this.random = new Random();
        numCrashedNodes = Options.instance().get(Options.CRASHING_NODES);
        
        if (numCrashedNodes == Options.CRASHING_NODES_RANDOM) {
            numCrashedNodes = random.nextInt(nnodes);
            
        }
        
        this.crashedNodes = new int[numCrashedNodes];
        for (int i = 0; i < numCrashedNodes; i++) {
            crashedNodes[i] = -1;
        }
        
    }
    
    
    public void start() {
        /** start in a new thread */
        ThreadPool.createNew(() -> {
            go();
        }, "Crasher");
    }
    
    
    public void go() {
        for (int i = 0; i < numCrashedNodes; i++ ) {
            int newCrash = random.nextInt(nnodes);
            
            /* *
             * The nodes will be crashed in the order they are added in this list, 
             * thus the order is uniformly random 
             * */
            while(contains(crashedNodes, newCrash))
                newCrash = random.nextInt(nnodes);
            
            crashedNodes[i] = newCrash;
        }
        
        TDS.writeString(0, " [FTS]\tWill crash " + numCrashedNodes + " nodes: " + Arrays.toString(crashedNodes));
        
        if (Options.instance().get(CRASHING_NODES_INTERVAL) == CRASHING_NODES_INTERVAL_UNIFORM) {
            int delay;
            for (int crashedNode: crashedNodes) {
                
                delay = random.nextInt(UNIFORM_CRASHING_NODES_INTERVAL_MAX);
                
                if (delay < UNIFORM_CRASHING_NODES_INTERVAL_MIN) delay = UNIFORM_CRASHING_NODES_INTERVAL_MIN;
                
                /* wait */
                try { Thread.sleep(delay); } catch (InterruptedException e) {}
                
                /* crash the node */
                network.crash(crashedNode);
                
                /* start notifying nodes in a separate thread simulating failure detection */
                try {notifyNodesRandomly(crashedNode, crashedNodes); } catch (NodeCrasherStopException e) { return; }
                
            }
        } else if (Options.instance().get(CRASHING_NODES_INTERVAL) == CRASHING_NODES_INTERVAL_GAUSSIAN){
            int delay;
            for (int crashedNode: crashedNodes) {
                
                delay = (int) Math.round(random.nextGaussian() * GAUSSIAN_CRASHING_NODES_INTERVAL_SD + GAUSSIAN_CRASHING_NODES_INTERVAL_MU);
                
                if (delay < CRASHING_NODES_INTERVAL_MIN) delay = CRASHING_NODES_INTERVAL_MIN;
                
                /* wait */
                try { Thread.sleep(delay); } catch (InterruptedException e) {}
                
                /* crash the node */
                network.crash(crashedNode);
                
                /* start notifying nodes in a separate thread simulating failure detection */
                try {notifyNodesRandomly(crashedNode, crashedNodes); } catch (NodeCrasherStopException e) { return; }
                
            }
            
        } else { //fixed intervals
            
            int delay = Options.instance().get(CRASHING_NODES_INTERVAL);
            
            for (int crashedNode: crashedNodes) {
                
                /* wait */
                try { Thread.sleep(delay); } catch (InterruptedException e) {}
                
                /* crash the node */
                network.crash(crashedNode);
                
                /* start notifying nodes in a separate thread simulating failure detection */
                try {notifyNodesRandomly(crashedNode, crashedNodes); } catch (NodeCrasherStopException e) { return; }
                
            }
        }
    }


    private void notifyNodesRandomly(int crashedNode, int[] ignoreCrashed) throws NodeCrasherStopException{
        ThreadPool.createNew(() -> {
            Random random = new Random();
            try {
                ArrayList<Integer> notified = new ArrayList<Integer>();
                
                /** do not try to notify the crashed nodes ! */
                for(int n: ignoreCrashed)
                    notified.add(n);
                
                int notifyNext;
                while(notified.size() != nnodes && !shouldStop()) {
                    notifyNext = random.nextInt(nnodes);
                    
                    while ( notified.contains(notifyNext) )
                        notifyNext = random.nextInt(nnodes);
                    
                    notified.add(notifyNext);
                    
                    /** this crash-message simulates that enough time elapsed without a heartbeat message 
                     * the call will take care of waiting */
                    network.sendCrashMessage(notifyNext, crashedNode);
                }
            } catch (NodeCrasherStopException e) { return; }
        }, "CrashNotifier");
        
    }
    
    private boolean contains(int[] array, int value){
        for(int val : array)
            if(val == value)
                return true;
        return false;
    }
    
    private boolean shouldStop() throws NodeCrasherStopException{
        if(stop)
            throw new NodeCrasherStopException();
        return stop;
    }
    
    public synchronized void stop(){
        stop = true;
    }
}
