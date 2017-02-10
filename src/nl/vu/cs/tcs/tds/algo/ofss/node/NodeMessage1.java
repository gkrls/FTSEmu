package nl.vu.cs.tcs.tds.algo.ofss.node;

public class NodeMessage1 {
	
	public int sender;
	
	public NodeMessage1(int sender){
		this.sender = sender;
	}
	
	public int getSenderId(){
		return this.sender;
	}

}
