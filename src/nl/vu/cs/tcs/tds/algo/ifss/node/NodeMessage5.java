package algo.ifss.node;

import java.io.Serializable;

public class NodeMessage5 implements Serializable {
    public int sender;
    public int seq;
    
    public NodeMessage5(int sender, int seq){
        this.sender = sender;
        this.seq = seq;
    }
    
    public int getSeq(){
        return this.seq;
    }
    
    public int getSenderId(){
        return this.sender;
    }

}
