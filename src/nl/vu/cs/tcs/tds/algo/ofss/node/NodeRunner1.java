package nl.vu.cs.tcs.tds.algo.ofss.node;

import java.util.Random;

import tds.main.Options;
import tds.main.TDS;
import tds.performance.PerformanceLogger;
import tds.td.original.network.Network1;
import tds.td.original.probing.Prober1;
import tds.util.Activity;
import tds.util.ActivityGenerator;
import tds.util.Color;

// A NodeRunner thread simulates a node that is doing some computation,
// is sending some messages, or is passive.
// It can only send messages when it is active, and only becomes
// active again when it receives a message.
public class NodeRunner1 implements Runnable {

    private final int mynode;
    private final int nnodes;
    private boolean mustStop = false;
    private NodeState1 state;
    private final Network1 network;
    private Random random = new Random();
    private boolean started = false;
    public boolean isPassive = true;
    public Prober1 prober;

    public NodeRunner1(int mynode, int nnodes, Network1 network, boolean initiallyActive) {
        this.mynode = mynode;
        this.nnodes = nnodes;
        this.isPassive = !initiallyActive;
        state = new NodeState1(!initiallyActive, mynode, nnodes);
        network.registerNode(this);
        this.network = network;
        Thread t = new Thread(this);
        t.start();
    }
    
    public void attachProber(Prober1 p){
    	this.prober = p;
    }

    public int getId() {
        return mynode;
    }

    public NodeState1 getState() {
        return state.copy();
    }
    
    public void setColor(int color){
    	this.state.setColor(Color.WHITE);
    }
    
    // This method is to be called when the termination detection has detected termination.
    public synchronized void stopRunning() {
        if (!state.isPassive()) {
            // Failure in termination detection?
            TDS.writeString(0, "Got stopRunning message but was not passive!");
        }
        mustStop = true;
        notifyAll();
    }

    private void writeString(String s) {
        TDS.writeString(1, " Node " + mynode + ": \t" + s);
    }

    // Called by the network.
    public synchronized void receiveMessage(NodeMessage1 m) {
        writeString("received message from " + m.sender);
        this.state.decCount();
        this.state.setPassive(false);
        this.isPassive = false;
        //notifyAll();
        this.state.setColor(Color.BLACK);
        notifyAll();
    }
    
    // Sends a message to another node. If you need to modify the contents,
    // modify the NodeMessage.
    private void sendMessage(int node) {
        writeString("send a message to " + node);
        this.state.incCount();
        network.sendMessage(node, new NodeMessage1(mynode));
    }

    @Override
    public void run() {
        writeString("Thread started");
        waitUntilStarted();
        writeString("Cluster started");
        while (!shouldStop()) {
            synchronized (this) {
                while (state.isPassive()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // nothing
                    }
                    if (shouldStop()) {
                        return;
                    }
                }

            }
            writeString("becoming active");
            activity();
            prober.nodeRunnerStopped();
            synchronized(this){
            	//not removing it yet. Have to check if isPassive is checked somewhere!!
            	isPassive = true;
            	notifyAll();
            }
            state.setPassive(true);
            writeString("becoming passive");
            network.registerPassive();
        }
    }

    private synchronized void waitUntilStarted() {
        while (!started) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
    

    private void activity() {
        // Create some random number of activities which either
        // simulate the sending of a number of messages,
        // or doing some computation (a sleep :-)
        // This is just an example. Modify to your own taste.

        writeString("starting activity");
//        Activity activity = ActivityGenerator.instance().getNext(1, this.getId());
//        int nActivities = activity.getNumActivities();
        int level = Options.instance().get(Options.ACTIVITY_LEVEL);
        int nActivities = 1 + random.nextInt(level);
        for (int i = 0; i < nActivities; i++) {
            // Start with some computation
            int timeToSleep = random.nextInt(1000);
            // Sleep between 0 and 1000 milliseconds
            try {
                Thread.sleep(timeToSleep);
            } catch (InterruptedException e) {
                // ignore
            }
            //Then, send a couple of messages. Maybe 0, or else it never stops...
            //Make sure Node 0 sends at least 1 message, to avoid zero activity
            //int nMessages = activity.getNumMessages() + (this.mynode == 0? 1:0);
            int nMessages = random.nextInt(level) + (this.mynode == 0? 1:0);
            //int[] targets = activity.getTargets();
            
            for (int j = 0; j < nMessages; j++) {
               int target = network.selectTarget(mynode);
               sendMessage(target);
            }
        }

    }

    private synchronized boolean shouldStop() {
        return mustStop;
    }

    // Called when all nodes are added to the network.
    public synchronized void start() {
        started = true;
        notifyAll();
    }
    
    public synchronized boolean isPassive(){
    	return state.isPassive();
    }
}
