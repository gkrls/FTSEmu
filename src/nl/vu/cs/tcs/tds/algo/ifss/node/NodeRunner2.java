package algo.ifss.node;

import java.util.Random;

import util.Options;
import main.TDS;
import algo.ifss.network.Network2;
import algo.ifss.probing.Prober2;

import static util.Options.*;

public class NodeRunner2 implements Runnable{
    
    private enum ActivityStrategy {
        N_ACTIVITIES,
        SLEEP_SEND
    }
    
    private final int mynode;
    private final int nnodes;
    private final NodeState2 state;
    private final Network2 network;
    private boolean mustStop = false;
    private Random random = new Random();
    private boolean started = false;
    private boolean isPassive = true;
    private Prober2 prober;
    
    
    public NodeRunner2(int mynode, int nnodes, Network2 network, boolean initiallyActive){
        this.mynode = mynode;
        this.nnodes = nnodes;
        this.isPassive = !initiallyActive;
        this.state = new NodeState2(!initiallyActive, mynode, nnodes);
        network.registerNode(this);
        this.network = network;
        Thread t = new Thread(this);
        t.start();
    }
    
    public void attachProber(Prober2 p){ this.prober = p; }
    public int getId(){ return mynode; }
    public NodeState2 getState() { return this.state.copy(); }
    public synchronized int getSeq() { return state.getSeq(); }
    public synchronized void incSeq() { state.incSeq(); }
    public synchronized int getBlack() { return this.state.getBlack(); }
    public synchronized void setBlack(int node) { this.state.setBlack(node); };
    public synchronized void setCount(int c) {this.state.setCount(c);} 
    
    public synchronized void stopRunning() {
        if(!state.isPassive()) {
            TDS.writeString(-2," [I-FSS]\tGot stopRunning message but was not passive!");
        }
        mustStop = true;
        notifyAll();
    }
    
    private void writeString(String s) {
        TDS.writeString(2, " Node " + mynode + ": \t" + s);
    }
    
    public synchronized void receiveMessage(NodeMessage2 m) {
        writeString("received message from " + m.getSenderId());
        this.state.setPassive(false);
        this.isPassive = false;
        notifyAll();
        NodeState2 state = this.state.copy();
        if( (m.getSenderId() < mynode && m.getSeq() == state.getSeq() + 1)  ||
                (m.getSenderId() > mynode && m.getSeq() == state.getSeq())   ){
            this.state.setBlack(furthest(state.getBlack(), m.getSenderId()));
            
        }
        this.state.decCount();
        notifyAll(); //maybe not needed
    }
    
    private void sendMessage(int node) {
        writeString("send a message to " + node);
        network.sendMessage(node, new NodeMessage2(mynode, this.state.getSeq()));
        this.state.incCount();
    }
    
    
    
    public int furthest(int j, int k) {
        return ((mynode <= j && j <= k) || (k < mynode && mynode <= j) || (j <= k && k < mynode))? k : j;
    }
    
    
    @Override
    public void run() {
        writeString("Thread started");
        waitUntilStarted();
        writeString("Node started");
        
        while(!shouldStop()){
            synchronized(this) {
                while(state.isPassive()){
                    try {wait(); }catch(InterruptedException e) {}
                    if(shouldStop()) return;
                }
            }
            
            writeString("becoming active");
            
            activity(Options.instance().get(ACTIVITY_STRATEGY));
            
            prober.nodeRunnerStopped();
            
            synchronized(this){ isPassive = true; notifyAll(); }
            
            state.setPassive(true);
            
            writeString("becoming passive");
            
            network.registerPassive();
        }
    }
    
    private synchronized void waitUntilStarted() {
        while(!started)
            try { wait(); } catch(InterruptedException e){break;}//maybe remove break
    }
    
    private void activity(int strategy) {
        if ( strategy == ACTIVITY_STRATEGY_N_ACTIVITIES)
            activityNActivities();
        else if ( strategy == ACTIVITY_STRATEGY_COMPUTE_SEND)
            activityComputeSend();
        else
            writeString("WARNING: Invalid Activity Strategy");
    }
    
    private void activityNActivities() {
        int distribution = Options.instance().get(PROB_DISTRIBUTION);
        writeString("starting activity (" + ( distribution ==  PROB_DISTRIBUTION_UNIFORM ? "uniform)" : "gaussian)"));
        
        int level = 0;
        int nActivities = 0;
        
        /* choose how many things to do [0 - 4] */
        if ( distribution == PROB_DISTRIBUTION_UNIFORM) {
            nActivities = random.nextInt(5);
        } else {
            /* gaussian with mean=2, sd=1 */
            nActivities = (int) Math.round(random.nextGaussian() + 2);
        }
        
        while ( level++ < Options.instance().get(Options.ACTIVITY_LEVEL)) {
            while (nActivities-- > 0) {
                boolean compute = random.nextGaussian() > 0;
                if (compute) {
                    int timeToSleep;
                    if(distribution == PROB_DISTRIBUTION_UNIFORM) {
                        timeToSleep = random.nextInt((UNIFORM_COMPUTE_MAX/2 - UNIFORM_COMPUTE_MIN) + UNIFORM_COMPUTE_MIN);
                    } else {
                        
                        do {
                            timeToSleep = (int) Math.round(random.nextGaussian() * GAUSSIAN_COMPUTE_SD/2 + GAUSSIAN_COMPUTE_MU/2);
                        }while(timeToSleep < 0);
                        
                    }
                    
                    try { Thread.sleep(timeToSleep);} catch (InterruptedException e) {}
                } else {
                    /* send one message */
                    if(distribution == PROB_DISTRIBUTION_UNIFORM) {
                        sendMessage(network.selectTargetUniform(mynode));
                    } else {
                        sendMessage(network.selectTargetGaussian(mynode));
                    }
                    
                }
            }
        }
    }
    
    /** 
     * The following method simulates activity at a node after becoming active.
     * The activity consists of first performing some computation and then sending
     * a random number of messages;
     */
    private void activityComputeSend() {
        int distribution = Options.instance().get(PROB_DISTRIBUTION);
        writeString("starting activity (" + ( distribution ==  PROB_DISTRIBUTION_UNIFORM ? "uniform)" : "gaussian)"));
        int level = 0;
        
        while(level++ < Options.instance().get(ACTIVITY_LEVEL)) {
            
            int timeToSleep = -1;
            int numOfMessages = -1;
            
            if ( distribution == PROB_DISTRIBUTION_UNIFORM ) {

                timeToSleep = random.nextInt((UNIFORM_COMPUTE_MAX - UNIFORM_COMPUTE_MIN) + UNIFORM_COMPUTE_MIN);
                numOfMessages = random.nextInt((UNIFORM_MESSAGES_MAX - UNIFORM_MESSAGES_MIN) + UNIFORM_MESSAGES_MIN);
                
            } else {
                
                /* make sure we have non-negative value */
                while ( timeToSleep < 0 ) 
                    timeToSleep = (int) Math.round(random.nextGaussian() * GAUSSIAN_COMPUTE_SD + GAUSSIAN_COMPUTE_MU);
                
                numOfMessages = (int) Math.round(random.nextGaussian() * GAUSSIAN_MESSAGES_SD + GAUSSIAN_MESSAGES_MU);
                
                /* make sure we have non-negative value. 
                 * 
                 * We are a little bit biased towards 0 messages because even if we do:
                 * 
                 *  while ( numOfMessages < 0 )
                 *      numOfMessages = new_gaussian_random
                 *  
                 *  OR
                 *  
                 *  if ( numOfMessages < 0)
                 *      numOfMessages = 1
                 *  
                 *  there's simply too much activity even on 64 node networks so that we don't have termination in 3 minutes!
                 * */
                if ( numOfMessages < 0 ) 
                    numOfMessages = 0;
            }
            
            /* 1) compute */
            try { Thread.sleep(timeToSleep);} catch (InterruptedException e) {}
            
            
            /* 2) send messages */
            if(Options.instance().get(PROB_DISTRIBUTION) == PROB_DISTRIBUTION_UNIFORM) {
                while( numOfMessages-- > 0 ) sendMessage(network.selectTargetUniform(mynode));
            } else {
                while( numOfMessages-- > 0 ) sendMessage(network.selectTargetGaussian(mynode));
            }

        }
    }
    
    private synchronized boolean shouldStop() {
        return mustStop;
    }
    
    // Called when all nodes are added to the network.
    public synchronized void start(){
        started = true;
        notifyAll();
    }
    
    public boolean isStarted() {
        return this.started;
    }
    
    public synchronized boolean isPassive(){
        return state.isPassive();
    }
    
}
