package algo.fts.node;

public class NodeMessage3 {
    
    private int sender, seq;

    public NodeMessage3(int sender, int seq) {
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
