package algo.fts.probing;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import main.TDS;
import performance.PerformanceLogger;
import algo.fts.network.Network6;
import algo.fts.node.FailureDetector;
import algo.fts.node.NodeRunner6;
import algo.fts.node.NodeState6;

public class Prober6 {
    
    private final int nnodes;
    private final int mynode;
    private final Network6 network;
    private final FailureDetector fd;
    
    private final NodeRunner6 nodeRunner;
    private final boolean holdsToken;
    private volatile boolean waitNodeRunner;

    public Prober6(int id, int nnodes, Network6 network, NodeRunner6 nodeRunner, FailureDetector failureDetector) {
        this.mynode = id;
        this.nnodes = nnodes;
        this.network = network;
        this.nodeRunner = nodeRunner;
        this.fd = failureDetector;
       
        this.holdsToken = false;
        this.waitNodeRunner = false;
        if(mynode == 0) {
            ProbeMessage6 token = new ProbeMessage6(mynode, nnodes);
            token.incSeq();
            PerformanceLogger.instance().addTokenBits(6, token.copy());
            network.sendProbeMessage(token, 0);
        }
    }
    
    public synchronized void receiveFirstMessage(ProbeMessage6 token) {}
    
    public synchronized void receiveMessage(ProbeMessage6 token) {
        if(!this.nodeRunner.isCrashed()){
            writeString("TOKEN: seq: " + token.getSeq() + ", black: " + token.getBlack() + " from: " + token.getSender() + " \t\t NODE: seq: " + nodeRunner.getSeq() );
            if(token.getSeq() == nodeRunner.getState().getSeq() + 1) {
                //token.getCRASHED().removeAll(nodeRunner.getCRASHED()); //difference
                token.removeAll(nodeRunner.getCRASHED());
                nodeRunner.getCRASHED().addAll(token.getCRASHED()); //union
                
                handleToken(token);
                
            }else {
                writeString("Received invalid Seq!");
            }
        }else {
            writeString("I am crashed but was asked to do work. (That's ok)");
        }
    }
    
    private void handleToken(ProbeMessage6 token) {
        this.waitUntilPassive();
        long start = System.nanoTime();
        writeString("Handling Token");
        nodeRunner.setBlack(nodeRunner.furthest(nodeRunner.getBlack(), token.getBlack()));
        nodeRunner.reportRemove(token.getCRASHED());
        
        if(nodeRunner.getBlack() == this.mynode || nodeRunner.getREPORT().isEmpty()) {
            token.setCount(mynode, 0);
            
            HashSet<Integer> s = (HashSet<Integer>) IntStream.range(0, this.nnodes).boxed().collect(Collectors.toSet());
            s.removeAll(nodeRunner.getCRASHED());
            
            for(Integer i: s) { token.incrCountBy(mynode, nodeRunner.getCount(i.intValue())); }
        }
        
        if(nodeRunner.getBlack() == this.mynode) { // Can do a termination check
            
            HashSet<Integer> s = (HashSet<Integer>) IntStream.range(0, this.nnodes).boxed().collect(Collectors.toSet());
            s.removeAll(nodeRunner.getCRASHED());
            int sum = s.stream().mapToInt(i -> token.getCount(i)).sum();
            

            
            if(sum == 0) { // Actual termination
                writeString("TERMINATION DETECTED");
                writeString("PROBER 6: Termination detected "
                        + (System.currentTimeMillis() - network.getLastPassive())
                        + " milliseconds after last node became passive.");
                
                long end = System.nanoTime();
                PerformanceLogger.instance().addProcTime(6, end - start);
                TDS.instance().announce(6); //add 6 in TDS.instance
                return;
            }else {
                writeString("No Term: " + sum);
            }
        }
        
        if(token.getCRASHED().contains(nodeRunner.getNext())) { newSuccessor(); }
        
        if(nodeRunner.getNext() < mynode) token.incSeq();
        
        if(!nodeRunner.getREPORT().isEmpty()) {
            //token.getCRASHED().addAll(nodeRunner.getREPORT());
            token.addAll(nodeRunner.getREPORT());
           
            nodeRunner.crashedAdd(nodeRunner.getREPORT());
            nodeRunner.getREPORT().clear();
            token.setBlack(mynode);
        } else {
            token.setBlack(nodeRunner.furthest(nodeRunner.getBlack(), nodeRunner.getNext()));
        }
        
        token.setSender(this.mynode);
        network.sendProbeMessage(token, nodeRunner.getNext());
        
        nodeRunner.setBlack(mynode);
        
        nodeRunner.incSeq();
        fd.updateLastToken(token.copy());
        
        
        long end = System.nanoTime();
        PerformanceLogger.instance().addProcTime(6, end - start);
        PerformanceLogger.instance().incTokens(6);
        PerformanceLogger.instance().addTokenBits(6, token.copy());
        

    }
    
    public void newSuccessor() {
        long start = System.nanoTime();
        HashSet<Integer> crashedReportUnion = new HashSet<Integer>();
        crashedReportUnion.addAll(nodeRunner.getCRASHED());
        crashedReportUnion.addAll(nodeRunner.getREPORT());
        
        int newNext = (nodeRunner.getNext() + 1) % nnodes;
        while(crashedReportUnion.contains(newNext))
            newNext = (newNext + 1) % nnodes;
        
        nodeRunner.setNext(newNext);
        
        /** I am the only node alive */
        if(nodeRunner.getNext() == mynode) {
            writeString("N-1 crashed. Announce after passive");
            this.waitUntilPassive();
            writeString("PROBER 6: Termination detected "
                    + (System.currentTimeMillis() - network.getLastPassive())
                    + " milliseconds after last node became passive.");
            long end = System.nanoTime();
            PerformanceLogger.instance().addProcTime(6, end - start);
            PerformanceLogger.instance().setTokensUpToTerm(6);
            PerformanceLogger.instance().setBackupTokensUpToTerm(6);
            TDS.instance().announce(6);
            return;
        }
        
        if(nodeRunner.getBlack() != mynode)
            nodeRunner.setBlack(nodeRunner.furthest(nodeRunner.getBlack(), nodeRunner.getNext()));
        
        long end = System.nanoTime();
        PerformanceLogger.instance().addProcTime(6, end - start);
    }

    public void waitUntilPassive() {
        //this.waitNodeRunner = !this.nodeRunner.isPassive();
        while(!this.nodeRunner.isPassive()) {
            writeString("PROBE waiting node for passive");
            try { wait(); } catch(InterruptedException e) {}
        }
        //}
    }
    
    private void writeString(String s) {
        TDS.writeString(6, " Node " + mynode + ": \t" + s);
    }


    public synchronized void nodeRunnerStopped() {
        this.waitNodeRunner = false;
        notifyAll();
    }

}
