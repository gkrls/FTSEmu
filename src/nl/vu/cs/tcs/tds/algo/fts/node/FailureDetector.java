package algo.fts.node;

import java.util.HashSet;

import main.TDS;
import performance.PerformanceLogger;
import algo.fts.network.Network3;
import algo.fts.probing.ProbeMessage3;
import algo.fts.probing.Prober3;

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
            nodeRunner.writeString(crashedNode + " crashed");
            nodeRunner.getREPORT().add(crashedNode);
            //nodeRunner.writeString(crashedNode + " " + nodeRunner.getNext());
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
                        PerformanceLogger.instance().incTokens(3);
                        PerformanceLogger.instance().incBackupTokens(3);
                        PerformanceLogger.instance().addTokenBits(3, lastToken.copy());
                        
                        
                    }
                }

            }
        }else {
            nodeRunner.writeString("already knows that " + crashedNode + " crashed");
        }
        
        long end = System.nanoTime();
        PerformanceLogger.instance().addProcTime(3, end - start);
    }

    private void writeString(String string) {
        TDS.writeString(3, " Node " + mynode + ": \t" + string);
    }
    
    public void updateLastToken(ProbeMessage3 token) {
        this.lastToken = token;
    }
}