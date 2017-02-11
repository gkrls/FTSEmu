package algo.ifss.probing;

import java.io.Serializable;

public class ProbeMessage2 implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int sender;
    private int nnodes;
    private int count;
    private int black;

    public ProbeMessage2(int sender, int nnodes) {
        this.sender = sender;
        this.nnodes = nnodes;
        this.count = 0;
        this.black = nnodes - 1;
    }

    public void setCount(int count) {
        this.count = count;
        
    }

    public void setBlack(int i) {
        this.black = i;
        
    }

    public void incCount(int count) {
        this.count = this.count + count;
        
    }

    public int getBlack() {
        return this.black;
    }

    public int getCount() {
        return this.count;
    }
    
    public int getSender() {
        return this.sender;
    }
    
    public void setSender(int sender){
        this.sender = sender;
    }
    
    public synchronized ProbeMessage2 copy() {
        ProbeMessage2 result = new ProbeMessage2(this.sender, this.nnodes);
        result.count = this.count;
        result.black = this.black;
        return result;
    }

}
