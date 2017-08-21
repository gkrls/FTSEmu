package algo.fts.node;

import java.util.HashSet;

import main.TDS;
import performance.PerformanceLogger;
import util.Options.VERSIONS;
import algo.fts.network.Network3;
import algo.fts.probing.ProbeMessage3;
import algo.fts.probing.Prober3;

/**
 * The FailureDetector for a node is invoked after the NodeCrasher informs that node
 * of another node's crash
 * 
 * It has to be linked with a NodeRunner and does work in the receiveCrash() method
 * 
 * @author gkarlos
 *
 */
public class FailureDetector {
    
    private int mynode, nnodes;
    private Network3 network;
    private NodeRunner3 nodeRunner;
    private boolean waitNodeRunner;
    private ProbeMessage3 lastToken;
    private Prober3 prober;

    public FailureDetector(int id, int nnodes, Network3 network, NodeRunner3 nodeRunner) {
        this.mynode = id;
        this.nnodes = nnodes;
        this.network = network;
        this.nodeRunner = nodeRunner;
        
        this.lastToken = new ProbeMessage3(mynode, nnodes);
        lastToken.setBlack(id);
    }
    
    public void linkWithProber(Prober3 prober){this.prober = prober;}

    
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
            //nodeRunner.writeString(crashedNode + " crashed");
            
            nodeRunner.getREPORT().add(crashedNode);

            if(crashedNode == nodeRunner.getNext()) {
                prober.newSuccessor();
                
                //writeString("New successor: " + nodeRunner.getNext());
                
                synchronized (this) {
                    if(nodeRunner.getSeq() > 0 || nodeRunner.getNext() < mynode) { 
                        lastToken.getCRASHED().addAll(nodeRunner.getREPORT());
                        lastToken.setBlack(mynode);
                        if(nodeRunner.getNext() < mynode) 
                            lastToken.setSeq(nodeRunner.getSeq() + 1);
                        
                        //nodeRunner.writeString("Sending BACKUP! token");
                        
                        lastToken.setSender(this.mynode);
                        
                        network.sendProbeMessage(lastToken, nodeRunner.getNext());
                        PerformanceLogger.instance().incTokens(VERSIONS.FTS);
                        PerformanceLogger.instance().incBackupTokens(VERSIONS.FTS);
                        PerformanceLogger.instance().addTokenBits(VERSIONS.FTS, lastToken.copy());
                        
                        
                    }
                }

            }
        }else {
            nodeRunner.writeString("already knows that " + crashedNode + " crashed");
        }
        
        long end = System.nanoTime();
        PerformanceLogger.instance().addProcTime(VERSIONS.FTS, end - start);
    }

    private void writeString(String string) {
        TDS.writeString(VERSIONS.FTS, " Node " + mynode + ": \t" + string);
    }
    
    public void updateLastToken(ProbeMessage3 token) {
        this.lastToken = token;
    }
}