package algo.fts.probing;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProbeMessage3 implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private HashSet<Integer> CRASHED;
    private int[] count;
    private int sender, seq, black, nnodes;
    
    public ProbeMessage3(int sender, int nnodes) {
        this.black = (sender == 0) ? nnodes - 1 : sender;
        this.seq = 0;
        this.sender = sender;
        this.count = new int[nnodes];
        this.nnodes = nnodes;
        this.CRASHED = new HashSet<Integer>();
        for (int i = 0; i < count.length; i++) count[i] = 0;
    }

    public void setSender(int i) {
        this.sender = i;
        
    }
    
    public int getSender() {
        return this.sender;
    }

    public int getSeq() {
        return this.seq;
    }
    
    public void incSeq() {
        this.seq += 1;
    }

    public HashSet<Integer> getCRASHED() {
        return this.CRASHED;
    }

    public void updateCRASHED(HashSet<Integer> newSet) {
        this.CRASHED = newSet;
    }

    public int getBlack() {
        return this.black;
    }

    public void setCount(int mynode, int i) {
        this.count[mynode] = i;
    }
    
    public void incrCountBy(int node, int val) {
        this.count[node] += val;
    }

    public int getCount(int node) {
        return count[node];
    }
    
    public synchronized void addCrashed(int node) {
        this.CRASHED.add(node);
    }
    
    public synchronized void removeCrashed(int node) {
        this.CRASHED.remove(node);
    }
    
    public synchronized void removeAll(Collection<Integer> c){
        this.CRASHED.removeAll(c);
    }
    
    public synchronized void addAll(Collection<Integer> c){
        this.CRASHED.addAll(c);
    }

    public void setBlack(int newBlack) {
        this.black = newBlack;
        
    }

    public void setSeq(int newSeq) {
        this.seq = newSeq;
        
    }
    
    public synchronized ProbeMessage3 copy() {
        ProbeMessage3 result = new ProbeMessage3(this.sender, this.nnodes);
        for ( int i = 0; i < result.count.length; i++) {
            result.count[i] = this.count[i];
        }
        result.seq = this.seq;
        for( Integer i : this.CRASHED) result.CRASHED.add(i);
        result.black = this.black;
        return result;
        
    }



}
