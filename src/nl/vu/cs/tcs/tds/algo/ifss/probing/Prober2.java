package algo.ifss.probing;

import main.TDS;
import performance.PerformanceLogger;
import util.Options.VERSIONS;
import algo.ifss.network.Network2;
import algo.ifss.node.NodeRunner2;

public class Prober2{
    private final int totalNodes;
    private final int mynode;
    private final Network2 network;
    
    private final NodeRunner2 nodeRunner;
    private boolean waitNodeRunner;
    
    public Prober2(int mynode, int totalNodes, Network2 network, NodeRunner2 nodeRunner) {
        this.mynode = mynode;
        this.totalNodes = totalNodes;
        this.network = network;
        this.nodeRunner = nodeRunner;
        this.waitNodeRunner = false;
        
        if(mynode == 0) {
            ProbeMessage2 token = new ProbeMessage2(mynode, totalNodes);
            PerformanceLogger.instance().addTokenBits(VERSIONS.IFSS, token.copy());
            network.sendFirstProbeMessage(0, token);
        }
    }
    
    public synchronized void receiveFirstMessage(ProbeMessage2 token) {
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
        PerformanceLogger.instance().addProcTime(VERSIONS.IFSS, end - start);
        
    }
    
    /**
     * This method implements the ReceiveToken_i procedure of the algorithm
     * 
     * @param token
     */
    public synchronized void receiveMessage(ProbeMessage2 token) {
        
        this.waitUntilPassive();
        long start = System.nanoTime();
        writeString("Handling Token");
        token.incCount(nodeRunner.getState().getCount());
        
        nodeRunner.setBlack(nodeRunner.furthest(nodeRunner.getBlack(), token.getBlack()));
        
        
        if((token.getCount() == 0) && (nodeRunner.getBlack() == nodeRunner.getId())){
            announce(start);
            return;
        }else{
            writeString("INCONSISTENT SNAPSHOT");
            propagate(token);
            long end = System.nanoTime();
            PerformanceLogger.instance().addProcTime(VERSIONS.IFSS, end - start);
        }
    }
    
    private void announce(long start) {
        writeString("Termination detected "
                + (System.currentTimeMillis() - network.getLastPassive())
                + " milliseconds after last node became passive.");
        
        long end = System.nanoTime();
        PerformanceLogger.instance().addProcTime(VERSIONS.IFSS, end - start);
        TDS.instance().announce(VERSIONS.IFSS);
    }
    
    private void propagate(ProbeMessage2 token) {
        token.setBlack(nodeRunner.furthest(nodeRunner.getState().getBlack(), (mynode + 1) % totalNodes));
        token.setSender(mynode);
        network.sendProbeMessage((mynode + 1) % totalNodes, token);
        nodeRunner.setCount(0);
        nodeRunner.setBlack(mynode);
        nodeRunner.incSeq();
        
        PerformanceLogger.instance().incTokens(VERSIONS.IFSS);
        PerformanceLogger.instance().addTokenBits(VERSIONS.IFSS, token.copy());
        /** proc time added by the caller when we return */
        
    }
    
    
    
    private void waitUntilPassive() {
        while(!this.nodeRunner.isPassive()) {
            writeString("PROBE waiting node for passive");
            try { wait(); } catch(InterruptedException e) {}
        }
    }



    public synchronized void nodeRunnerStopped() {
        this.waitNodeRunner = false;
        notifyAll();
    }
    
    private void writeString(String s) {
        TDS.writeString(VERSIONS.IFSS, " Node " + mynode + ": \t" + s);
    }
}
