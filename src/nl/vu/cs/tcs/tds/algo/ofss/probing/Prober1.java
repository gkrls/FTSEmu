package nl.vu.cs.tcs.tds.algo.ofss.probing;

import tds.main.TDS;
import tds.performance.PerformanceLogger;
import tds.td.original.network.Network1;
import tds.td.original.node.NodeRunner1;
import tds.td.original.node.NodeState1;
import tds.util.Color;

public class Prober1 {
	
	private final int totalNodes;
	private final int mynode;
	private final Network1 network;
	private final NodeRunner1 nodeRunner;
	private boolean waitNodeRunner;
	
	public Prober1(int mynode, int totalNodes, Network1 network, NodeRunner1 nodeRunner){
		this.nodeRunner = nodeRunner;
		this.totalNodes = totalNodes;
		this.mynode = mynode;
		this.network = network;
		if(mynode == 0){
			ProbeMessage1 probeMessage = new ProbeMessage1(mynode, totalNodes);
			PerformanceLogger.instance().incTokens(1);
			network.sendProbeMessage(0, probeMessage);
		}
	}
	
    private void writeString(String s) {
        TDS.writeString(1," Node " + mynode + ": \t" + s);
    }
	
	public synchronized void receiveMessage(ProbeMessage1 probeMessage){
		this.waitUntilPassive();
		long start = System.nanoTime();
		if(nodeRunner.getId() == totalNodes - 1){
			if(probeMessage.consistentSnapshot(nodeRunner)){
				writeString("TERMINATION DETECTED");
	            writeString("PROBER 1: Termination detected "
	                    + (System.currentTimeMillis() - network.getLastPassive())
	                    + " milliseconds after last node became passive.");
	            long end = System.nanoTime();
	            PerformanceLogger.instance().addProcTime(1, end - start);
				TDS.instance().announce(1);
			}
			else
				retransmit(probeMessage);
		}else{
			propagate(probeMessage);
		}
		long end = System.nanoTime();
		PerformanceLogger.instance().addProcTime(1, end - start);
	}
	
	private void retransmit(ProbeMessage1 probeMessage){
		probeMessage.zeroCount();
		probeMessage.setColor(Color.WHITE);
		nodeRunner.setColor(Color.WHITE);
		
		PerformanceLogger.instance().incTokens(1);
		PerformanceLogger.instance().addTokenBits(1, probeMessage.copy());
		network.sendProbeMessage(0, probeMessage);
	}
	
	private void propagate(ProbeMessage1 probeMessage){
		probeMessage.addToCount(nodeRunner.getState().getCount());
		if(nodeRunner.getState().getColor() == Color.BLACK)
			probeMessage.setColor(Color.BLACK);
		nodeRunner.setColor(Color.WHITE);
		
		PerformanceLogger.instance().incTokens(1);
		PerformanceLogger.instance().addTokenBits(1, probeMessage.copy());
		network.sendProbeMessage(mynode == (totalNodes - 1)? 0: mynode + 1, probeMessage);
	}
	
	

	public synchronized void nodeRunnerStopped() {
		this.waitNodeRunner = false;
		notifyAll();
	}
	
    private void waitUntilPassive(){
    	this.waitNodeRunner = !this.nodeRunner.isPassive();
    	while(waitNodeRunner){
    		writeString("PROBE waiting node for passive");
    		try {
				wait();
			} catch (InterruptedException e) {
				//ignore
			}
    	}
    }

}
