package algo.fts.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import ibis.util.ThreadPool;
import util.Options;
import main.TDS;
import performance.PerformanceLogger;

import static util.Options.*;

/**
 * A NodeCrasher is invoked at the beginning of the simulation. It's job is to:
 * 
 * 1) Select a number of nodes to crash (if -c without parameters is passed) or crash 
 *    the number of nodes n passed in -c
 * 2) Create a list of nodes that will crash and crash them one at a time
 * 3) For each about-to-crash node choose some number of ms to wait before crashing it. This
 *    value depends on the value passed to -ci arg
 *       - If 1 the interval is uniformly random between UNIFORM_CRASHING_NODES_INTERVAL_MIN 
 *         and UNIFORM_CRASHING_NODES_INTERVAL_MAX
 *       - If 2 the interval is under Gaussian distribution with mean GAUSSIAN_CRASHING_NODES_INTERVAL_MU
 *         and standard deviation GAUSSIAN_CRASHING_NODES_INTERVAL_SD
 *       - If x the interval is has a fix value x
 * 4) After the wait a random node is notified. So far there's no strategy on notification. The choise
 *    of a node is uniformly random
 *     
 * @author gkarlos
 *
 */
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
        this.random.setSeed(System.currentTimeMillis() + Double.doubleToLongBits(Math.random()) + this.hashCode());
        
        if (Options.instance().get(Options.CRASHING_NODES) == Options.CRASHING_NODES_RANDOM) {
            TDS.writeString(0, " [ FTS ]\tCrashing nodes in random (uniform in [0-N])");
            this.numCrashedNodes = random.nextInt(nnodes);
        } else {
            
            int low = Options.instance().get(Options.CRASHING_NODES_LOW);
            int high = Options.instance().get(Options.CRASHING_NODES_HIGH);
            
            if ( low == high ) {
                /* exact amount */
                this.numCrashedNodes = low; 
            } else {
                /* make a uniform choice in that range */
                this.numCrashedNodes = low + random.nextInt(high - low + 1);

            }
            
            TDS.writeString(0, " [ FTS ]\tCrashing nodes in Interval: [" + low + "," + high + "] (uniform choice)");
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
    
    private void chooseNodesToCrash() {
        for (int i = 0; i < numCrashedNodes; i++ ) {
            
            /* we will crash the node with this id */
            int newCrash = random.nextInt(nnodes);
            
            /* *
             * The nodes will be crashed in the order they are added in this list, 
             * thus the order is uniformly random 
             * */
            while(contains(crashedNodes, newCrash))
                newCrash = random.nextInt(nnodes);
            
            crashedNodes[i] = newCrash;
        }

    }
    
    public void go() {
        
        chooseNodesToCrash();
        
        TDS.writeString(0, " [ FTS ]\tWill crash " + numCrashedNodes + " nodes: " + Arrays.toString(crashedNodes));
        
        PerformanceLogger.instance().setNumCrashedNodes(numCrashedNodes);
        
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

    /**
     * This method spawns a thread and attempts to notify all nodes 
     * in the network (expect the one that just crashed) about the crash.
     * 
     * There's no notification strategy option (for now).
     * The next node to notify is randomly chosen from the pool of active nodes.
     * 
     * @param crashedNode
     * @param ignoreCrashed
     * @throws NodeCrasherStopException
     */
    private void notifyNodesRandomly(int crashedNode, int[] ignoreCrashed) throws NodeCrasherStopException{
        ThreadPool.createNew(() -> {
            
            //Random random = new Random();
            //this.random.setSeed(System.currentTimeMillis() + Double.doubleToLongBits(Math.random()) + this.hashCode() ^ 7);
            
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
