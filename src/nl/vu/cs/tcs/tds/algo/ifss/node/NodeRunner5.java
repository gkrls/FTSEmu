package algo.ifss.node;

import java.util.Random;

import tds.main.Options;
import tds.main.TDS;
import tds.td.correct2.network.Network5;
import tds.td.correct2.probing.Prober5;

public class NodeRunner5 implements Runnable{
    
    private final int mynode;
    private final int nnodes;
    private final NodeState5 state;
    private final Network5 network;
    private boolean mustStop = false;
    private Random random = new Random();
    private boolean started = false;
    private boolean isPassive = true;
    private Prober5 prober;
    
    
    public NodeRunner5(int mynode, int nnodes, Network5 network, boolean initiallyActive){
        this.mynode = mynode;
        this.nnodes = nnodes;
        this.isPassive = !initiallyActive;
        this.state = new NodeState5(!initiallyActive, mynode, nnodes);
        network.registerNode(this);
        this.network = network;
        Thread t = new Thread(this);
        t.start();
    }
    
    public void attachProber(Prober5 p){ this.prober = p; }
    public int getId(){ return mynode; }
    public NodeState5 getState() { return this.state.copy(); }
    public synchronized int getSeq() { return state.getSeq(); }
    public synchronized void incSeq() { state.incSeq(); }
    public synchronized int getBlack() { return this.state.getBlack(); }
    public synchronized void setBlack(int node) { this.state.setBlack(node); };
    public synchronized void setCount(int c) {this.state.setCount(c);} 
    
    public synchronized void stopRunning() {
        if(!state.isPassive()) {
            TDS.writeString(0,"Got stopRunning message but was not passive!");
        }
        mustStop = true;
        notifyAll();
    }
    
    private void writeString(String s) {
        TDS.writeString(5, " Node " + mynode + ": \t" + s);
    }
    
    public synchronized void receiveMessage(NodeMessage5 m) {
        writeString("received message from " + m.getSenderId());
        this.state.setPassive(false);
        this.isPassive = false;
        notifyAll();
        NodeState5 state = this.state.copy();
        if( (m.getSenderId() < mynode && m.getSeq() == state.getSeq() + 1)  ||
                (m.getSenderId() > mynode && m.getSeq() == state.getSeq())   ){
            this.state.setBlack(furthest(state.getBlack(), m.getSenderId()));
            
        }
        this.state.decCount();
        notifyAll(); //maybe not needed
    }
    
    private void sendMessage(int node) {
        writeString("send a message to " + node);
        network.sendMessage(node, new NodeMessage5(mynode, this.state.getSeq()));
        this.state.incCount();
    }
    
    
    
    public int furthest(int j, int k) {
        return ((mynode <= j && j <= k) || (k < mynode && mynode <= j) || (j <= k && k < mynode))? k : j;
    }
    
    
    @Override
    public void run() {
        writeString("Thread started");
        waitUntilStarted();
        writeString("Cluster started");
        
        while(!shouldStop()){
            synchronized(this) {
                while(state.isPassive()){
                    try {wait(); }catch(InterruptedException e) {}
                    if(shouldStop()) return;
                }
            }
            
            writeString("becoming active");
            activity();
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
    
    private void activity() {
        writeString("starting activity");
        int level = Options.instance().get(Options.ACTIVITY_LEVEL);
        int nActivities = 1 + random.nextInt(level);
        for(int i = 0; i < nActivities; i++) {
            int timeToSleep = random.nextInt(1000); //computation lol
            try { Thread.sleep(timeToSleep); } catch(InterruptedException e) {}
            
            int nMessages = random.nextInt(level) + (this.mynode == 0? 1 : 0);
            
            for (int j = 0; j < nMessages; j++)
                sendMessage(network.selectTarget(mynode));
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
