package nl.vu.cs.tcs.tds.algo.fts.node;

public class NodeMessage6 {
    
    private int sender, seq;

    public NodeMessage6(int sender, int seq) {
        this.sender = sender;
        this.seq = seq;
    }

    public int getSenderId() {
        return this.sender;
    }
    
    public int getSeq() {
        return this.seq;
    }

}
