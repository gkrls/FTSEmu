package algo.fts.node;

import java.util.HashSet;
import java.util.stream.Collectors;

public class NodeState3 {
    
    private boolean passive;
    private int nodeId, black, seq, nnodes, next;
    private HashSet<Integer> CRASHED, REPORT;
    private int[] count;
    

    public NodeState3(boolean passive, int mynode, int nnodes) {
        this.passive = passive;
        this.nodeId = mynode;
        this.nnodes = nnodes;
        this.seq = 0;
        this.black = this.nodeId;
        this.CRASHED = new HashSet<Integer>();
        this.REPORT = new HashSet<Integer>();
        this.next = (this.nodeId + 1) % nnodes;
        this.count = new int[nnodes];
        for (int i = 0; i < count.length; i++) count[i] = 0;
    }
    
    public synchronized NodeState3 copy() {
        NodeState3 s = new NodeState3(passive, nodeId, nnodes);
        
        s.passive = this.passive;
        s.seq = this.seq;
        s.black = this.black;
        s.next = this.next;
        for (int i = 0; i < s.count.length; i++) s.count[i] = this.count[i];
        s.CRASHED = (HashSet<Integer>) CRASHED.stream().map(Integer::new).collect(Collectors.toSet());
        s.REPORT = (HashSet<Integer>) REPORT.stream().map(Integer::new).collect(Collectors.toSet());
        
        return s;
    }
    
    public synchronized boolean isPassive() { return this.passive; }
    public int getNodeId() { return this.nodeId; }
    public synchronized void incCount(int node) { this.count[node]++; }
    public synchronized void decCount(int node) { this.count[node]--; }
    public synchronized void setCount(int node, int val) { this.count[node] = val; }
    public synchronized int getCount(int node) { return this.count[node]; }
    public synchronized void incSeq() { this.seq ++; }
    public synchronized int getSeq() { return this.seq; }
    public synchronized int getBlack() { return this.black; }
    public synchronized void setBlack(int node) { this.black = node; }
    public synchronized int getNext() { return this.next; }
    public synchronized void setPassive(boolean b) { this.passive = b; }
    public synchronized HashSet<Integer> getCRASHED() { return this.CRASHED; }
    public synchronized HashSet<Integer> getREPORT() { return this.REPORT; }
    public synchronized boolean inCrashed(int node) { return this.CRASHED.contains(node); }
    public synchronized boolean inReport(int node) { return this.REPORT.contains(node); }

    
    public synchronized void waitUntilPassive() {
        while(!passive) try { wait(); } catch (InterruptedException e) {}
    }

    public int getTotalCount() {
        throw new UnsupportedOperationException("NIY");
    }

    public void setNext(int i) {
        this.next = i;
        
    }


}
