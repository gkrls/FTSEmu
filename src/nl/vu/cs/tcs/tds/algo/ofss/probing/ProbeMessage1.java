package nl.vu.cs.tcs.tds.algo.ofss.probing;

import java.io.Serializable;

import tds.td.original.node.NodeRunner1;
import tds.td.original.node.NodeState1;
import tds.util.Color;

public class ProbeMessage1 implements Serializable{
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int sender;
	private int initiator;
	private int color;
	private int count;
	private int nnodes;
	
	public ProbeMessage1(int sender, int nnodes) {
		this.sender = sender;
		this.color = Color.WHITE;
		this.count = 0;
		this.nnodes = nnodes;
		this.initiator = nnodes - 1;
	}
	
	public int getSender(){
		return this.sender;
	}
	
	public boolean consistentSnapshot(NodeRunner1 node){
		NodeState1 state = node.getState();
		return (this.count + state.getCount() == 0)
			&& (this.color == Color.WHITE)
			&& (state.getColor() == Color.WHITE);
	}
	
	public int getInitiator(){
		return this.initiator;
	}
	
	public void addToCount(int value){
		this.count += value;
	}
	
	public int getCount(){
		return this.count;
	}
	
	public void zeroCount(){
		this.count = 0;
	}
	
	public void setColor(int color){
		this.color = color;
	}
	
	public synchronized ProbeMessage1 copy() {
	    ProbeMessage1 result = new ProbeMessage1(this.sender, this.nnodes);
	    result.sender = this.sender;
	    result.initiator = this.initiator;
	    result.color = this.color;
	    result.count = this.count;
	    result.nnodes = this.nnodes;
	    return result;
	}
	
	
}
