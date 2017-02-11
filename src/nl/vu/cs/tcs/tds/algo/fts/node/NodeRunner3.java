package algo.fts.node;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import util.Options;
import main.TDS;
import performance.PerformanceLogger;
import algo.fts.network.Network3;
import algo.fts.probing.Prober3;

public class NodeRunner3 implements Runnable{
    
    private final int mynode;
    private final int nnodes;
    private final NodeState3 state;
    private final Network3 network;
    private boolean mustStop = false;
    private Random random = new Random();
    private boolean started = false;
    private boolean isPassive = false;
    private Prober3 prober;
    private boolean crashed = false;
    private Thread t;
    
    
    public NodeRunner3(int mynode, int nnodes, Network3 network, boolean initiallyActive) {
        this.mynode = mynode;
        this.nnodes = nnodes;
        this.isPassive = !initiallyActive;
        this.state = new NodeState3(!initiallyActive, mynode, nnodes);
        network.registerNode(this);
        this.network = network;
        t = new Thread(this);
        t.start();
    }
    
    public void attachProber(Prober3 p) { this.prober = p; }
    public int getId() { return mynode; }
    public NodeState3 getState() { return this.state.copy(); }
    
    public synchronized int getSeq() { return state.getSeq(); }
    public synchronized void incSeq() { state.incSeq(); }
    public synchronized int getBlack() { return this.state.getBlack(); }
    public synchronized void setBlack(int node) { this.state.setBlack(node); }
    public synchronized void setCount(int node, int count) { this.state.setCount(node, count); }
    public synchronized int getNext() { return this.state.getNext(); }
    public synchronized boolean isCrashed() { return this.crashed; }

    void writeString(String s) { TDS.writeString(3, " Node " + mynode + ": \t" + s); }
    
    private synchronized boolean shouldStop() { return mustStop; }
    public synchronized void start() {started = true; notifyAll(); }
    public boolean isStarted() { return this.started; }
    public synchronized boolean isPassive(){ return state.isPassive(); }
    
    public synchronized void stopRunning() {
        if(!state.isPassive())
            TDS.writeString(-2," [FTS]\tGot stopRunning message but was not passive!");
        mustStop = true;
        notifyAll();
    }
    
    private void sendMessage(int node) {
        writeString("send a message to " + node);
        if(state.inCrashed(node) || state.inReport(node)) return;
        network.sendMessage(node, new NodeMessage3(mynode, this.state.getSeq()));
        this.state.incCount(node);
    }
    
    public synchronized void receiveMessage(NodeMessage3 m) {
        long start = System.nanoTime();
        if(!crashed) {
            if(!state.inCrashed(m.getSenderId())){
                writeString("received message from " + m.getSenderId());
                
                activate();
                notifyAll();
                if( (m.getSenderId() < mynode && m.getSeq() == state.getSeq() + 1) || 
                        (m.getSenderId() > mynode && m.getSeq() == state.getSeq())){
                    this.state.setBlack(furthest(state.getBlack(), m.getSenderId()));
                }
                this.state.decCount(m.getSenderId());
                notifyAll();
            }else {
                writeString("received message from crashed node " + m.getSenderId() + " . Ignoring it!");
            }
        }
        long end = System.nanoTime();
        PerformanceLogger.instance().addProcTime(3, end - start);
    }
    
    private synchronized void activate() {
        this.state.setPassive(false);
        this.isPassive = false;
        notifyAll();
    }
    
    public int furthest(int j, int k) {
        return ((mynode <= j && j <= k) || (k < mynode && mynode <= j) || (j <= k && k < mynode))? k : j;
    }
    
    
    @Override
    public void run() {
        writeString("Starting...");
        waitUntilStarted();
        writeString("Started");
        
        while(!shouldStop()){
            //writeString("ACTIVE");
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
        writeString("NOT ACTIVE");
        
    }
    
    private void activity() {
        writeString("starting activity");
        int level = Options.instance().get(Options.ACTIVITY_LEVEL);
        int nActivities = 1 + random.nextInt(level);
        for(int i = 0; i < nActivities; i++) {
            int timeToSleep = random.nextInt(1000); //computation
            try { Thread.sleep(timeToSleep); } catch(InterruptedException e) {}
            
            int nMessages = random.nextInt(level) + (this.mynode == 0? 1 : 0);
            
            for (int j = 0; j < nMessages; j++)
                sendMessage(network.selectTarget(mynode));
        }
    }
    
    private synchronized void waitUntilStarted() {
        while(!started) try {wait(); } catch(InterruptedException e){}//maybe remove break
    }

    public void crash() {
        writeString("I CRASHED");
        this.state.setPassive(true);
        this.mustStop = true;
        this.crashed = true;
        this.network.registerPassive(mynode);
        //notifyAll();
        
    }
    
    public int getTotalCount() {
        return this.state.getTotalCount();
    }
    public HashSet<Integer> getCRASHED() {
        return this.state.getCRASHED();
    }

    public HashSet<Integer> getREPORT() {
        return this.state.getREPORT();
        
    }

    public int getCount(int node) {
        return state.getCount(node);
    }

    public void setNext(int i) {
        this.state.setNext(i);
        
    }
    
    public synchronized void reportRemove(Collection<Integer> c) {
        state.getREPORT().removeAll(c);
    }
    
    public synchronized void crashedAdd(Collection<Integer> c) {
        state.getCRASHED().addAll(c);
    }

}
