package algo.fts.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import ibis.util.ThreadPool;
import util.Options;
import main.TDS;

public class NodeCrasher {
    
    private Network3 network;
    private int numCrashedNodes;
    private int[] crashedNodes;
    private int nnodes;
    private boolean stop;
    
    private Random random;
    
    public NodeCrasher(Network3 network, int nnodes) {
        this.stop = false;
        this.network = network;
        this.nnodes = nnodes;
        numCrashedNodes = Options.instance().get(Options.CRASHED_NODES);
        this.crashedNodes = new int[numCrashedNodes];
				for (int i = 0; i < numCrashedNodes; i++) {
					crashedNodes[i] = -1;
				}
        this.random = new Random();
    }
    
    
    public void start() {
        ThreadPool.createNew(() -> {
            go();
        }, "Crasher");
    }
    
    
    public void go() {
        for (int i = 0; i < numCrashedNodes; i++ ) {
            int newCrash = random.nextInt(nnodes);
            while(contains(crashedNodes, newCrash))
                newCrash = random.nextInt(nnodes);
            crashedNodes[i] = newCrash;
        }
        
        TDS.writeString(0, " [FTS]\tWill crash nodes: " + Arrays.toString(crashedNodes));
    
        for(int crashedNode : crashedNodes) {
            int delay = random.nextInt(2000);
            try { Thread.sleep(delay); } catch (InterruptedException e) {}
            network.crash(crashedNode);
            try { notifyNodesRandomly(crashedNode, crashedNodes); } catch (NodeCrasherStopException e) { return; }
            
        }
    }


    private void notifyNodesRandomly(int crashedNode, int[] ignoreCrashed) throws NodeCrasherStopException{
        ThreadPool.createNew(() -> {
            try {
                ArrayList<Integer> notified = new ArrayList<Integer>();
                for(int n: ignoreCrashed)
                    notified.add(n);
                while(notified.size() != nnodes && !shouldStop()) {
                    int notifyNext = random.nextInt(nnodes);
                    while(notified.contains(notifyNext))
                        notifyNext = random.nextInt(nnodes);
                    notified.add(notifyNext);
                    int delay = random.nextInt(1000);
                    try { Thread.sleep(delay); } catch (InterruptedException e) {}
                    network.sendCrashMessage(notifyNext, crashedNode);
                }
            } catch (NodeCrasherStopException e) {
                return;
            }
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
