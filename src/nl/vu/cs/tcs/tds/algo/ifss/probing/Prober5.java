package algo.ifss.probing;

import java.io.Serializable;

import ibis.util.ThreadPool;
import tds.main.TDS;
import tds.performance.PerformanceLogger;
import tds.td.correct2.network.Network5;
import tds.td.correct2.node.NodeRunner5;
import tds.td.correct2.node.NodeState5;

public class Prober5{
    private final int totalNodes;
    private final int mynode;
    private final Network5 network;
    
    private final NodeRunner5 nodeRunner;
    private boolean holdsToken = false;
    private boolean waitNodeRunner = false;
    
    public Prober5(int mynode, int totalNodes, Network5 network, NodeRunner5 nodeRunner) {
        this.nodeRunner = nodeRunner;
        this.totalNodes = totalNodes;
        this.mynode = mynode;
        this.network = network;
        
        if(mynode == 0) {
            ProbeMessage5 token = new ProbeMessage5(mynode, totalNodes);
            PerformanceLogger.instance().addTokenBits(5, token.copy());
            network.sendFirstProbeMessage(0, token);
        }
    }
    
    public synchronized void receiveFirstMessage(ProbeMessage5 token) {
        writeString("Starting Probing!!!");
        this.waitUntilPassive();
        long start = System.nanoTime();
        token.incCount(nodeRunner.getState().getCount());
        nodeRunner.setBlack(nodeRunner.furthest(nodeRunner.getBlack(), token.getBlack()));
        
        //Don't check for termination here. Its impossible
        
        //propagate token!
        token.setSender(0);
        network.sendProbeMessage((mynode + 1) % totalNodes, token);
        nodeRunner.setCount(0);
        nodeRunner.setBlack(mynode);
        nodeRunner.incSeq();
        long end = System.nanoTime();
        PerformanceLogger.instance().addProcTime(5, end - start);
        
    }
    
    
    public synchronized void receiveMessage(ProbeMessage5 token) {
        
        this.waitUntilPassive();
        long start = System.nanoTime();
        writeString("Handling Token");
        token.incCount(nodeRunner.getState().getCount());
        
        //writeString("new count_t= " + token.getCount());
       
        nodeRunner.setBlack(nodeRunner.furthest(nodeRunner.getBlack(), token.getBlack()));
        
        //writeString("new black_t= " + token.getBlack());
        
        
        if((token.getCount() == 0) && (nodeRunner.getBlack() == nodeRunner.getId())){
        //if(token.getCount() == 0 && state.getBlack() == mynode){
            writeString("TERMINATION DETECTED");
            writeString("PROBER 5: Termination detected "
                    + (System.currentTimeMillis() - network.getLastPassive())
                    + " milliseconds after last node became passive.");
            
            long end = System.nanoTime();
            PerformanceLogger.instance().addProcTime(5, end - start);
            TDS.instance().announce(5);
        }else{
            writeString("INCONSISTENT SNAPSHOT");
            propagate(token);
        }
        
        long end = System.nanoTime();
        PerformanceLogger.instance().addProcTime(5, end - start);
    }
    
    private void propagate(ProbeMessage5 token) {
        token.setBlack(nodeRunner.furthest(nodeRunner.getState().getBlack(), (mynode + 1) % totalNodes));
        token.setSender(mynode);
        network.sendProbeMessage((mynode + 1) % totalNodes, token);
        nodeRunner.setCount(0);
        nodeRunner.setBlack(mynode);
        nodeRunner.incSeq();
        
        PerformanceLogger.instance().incTokens(5);
        PerformanceLogger.instance().addTokenBits(5, token.copy());
        
    }
    
    
    
    private void waitUntilPassive() {
        //this.waitNodeRunner = !this.nodeRunner.isPassive();
        while(!this.nodeRunner.isPassive()) {
            //synchronized(nodeRunner){
                writeString("PROBE waiting node for passive");
                try { wait(); } catch(InterruptedException e) {} //maybe remove break
            //}
        }
    }



    public synchronized void nodeRunnerStopped() {
        this.waitNodeRunner = false;
        notifyAll();
    }
    
    private void writeString(String s) {
        TDS.writeString(5, " Node " + mynode + ": \t" + s);
    }
}
