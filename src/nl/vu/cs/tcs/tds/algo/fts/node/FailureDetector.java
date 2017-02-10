package nl.vu.cs.tcs.tds.algo.fts.node;

import java.util.HashSet;

import tds.main.TDS;
import tds.performance.PerformanceLogger;
import tds.td.ft2.network.Network6;
import tds.td.ft2.probing.ProbeMessage6;
import tds.td.ft2.probing.Prober6;

public class FailureDetector {
    
    private int mynode, nnodes;
    private Network6 network;
    private NodeRunner6 nodeRunner;
    private boolean waitNodeRunner;
    private ProbeMessage6 lastToken;
    private Prober6 prober;

    public FailureDetector(int id, int nnodes, Network6 network, NodeRunner6 nodeRunner) {
        this.mynode = id;
        this.nnodes = nnodes;
        this.network = network;
        this.nodeRunner = nodeRunner;
        
        this.lastToken = new ProbeMessage6(mynode, nnodes);
        lastToken.setBlack(id);
    }
    
    public void linkWithProber(Prober6 prober){this.prober = prober;}

    
    /** Called to indicate that node "learns" of someone's crash
     * 
     * @param the newly crashed node
     * 
     * */
    public synchronized void receiveCrash(int crashedNode) {
        
        long start = System.nanoTime();
        
        
        HashSet<Integer> crashedReportUnion = new HashSet<Integer>();
        crashedReportUnion.addAll(nodeRunner.getCRASHED());
        crashedReportUnion.addAll(nodeRunner.getREPORT());
        
        if(!crashedReportUnion.contains(crashedNode)) {
            nodeRunner.writeString(crashedNode + " crashed");
            nodeRunner.getREPORT().add(crashedNode);
            nodeRunner.writeString(crashedNode + " " + nodeRunner.getNext());
            if(crashedNode == nodeRunner.getNext()) {
                prober.newSuccessor();
                writeString("New successor: " + nodeRunner.getNext());
                
                synchronized (this) {
                    if(nodeRunner.getSeq() > 0 || nodeRunner.getNext() < mynode) { 
                        lastToken.getCRASHED().addAll(nodeRunner.getREPORT());
                        lastToken.setBlack(mynode);
                        if(nodeRunner.getNext() < mynode) 
                            lastToken.setSeq(nodeRunner.getSeq() + 1);
                        nodeRunner.writeString("Sending BACKUP! token");
                        lastToken.setSender(this.mynode);
                        
                        
                        network.sendProbeMessage(lastToken, nodeRunner.getNext());
                        PerformanceLogger.instance().incTokens(6);
                        PerformanceLogger.instance().incBackupTokens(6);
                        PerformanceLogger.instance().addTokenBits(6, lastToken.copy());
                        
                        
                    }
                }

            }
        }else {
            nodeRunner.writeString("already knows that " + crashedNode + " crashed");
        }
        
        long end = System.nanoTime();
        PerformanceLogger.instance().addProcTime(6, end - start);
    }

    private void writeString(String string) {
        TDS.writeString(6, " Node " + mynode + ": \t" + string);
    }
    
    public void updateLastToken(ProbeMessage6 token) {
        this.lastToken = token;
    }
}