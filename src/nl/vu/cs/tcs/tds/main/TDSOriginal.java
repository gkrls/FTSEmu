package main;

import ibis.util.ThreadPool;
import algo.ofss.network.Network1;
import algo.ofss.node.NodeRunner1;

public class TDSOriginal implements Runnable { 
	
	private static boolean done;
	private NodeRunner1[] nodeRunners;
	private Network1 network;
	private int nnodes;
	private long maxWait;
	
	public TDSOriginal(int nnodes, long maxWait) {
		this.nnodes = nnodes;
		this.done = false;
		this.nodeRunners = new NodeRunner1[nnodes];
		this.network = new Network1(nnodes);
		this.maxWait = maxWait;
	}
	
	public synchronized void setDone(){
		done = true;
		notifyAll();
	}
	
	private synchronized void waitTillDone(){
		while(!done){
			try{
				ThreadPool.createNew(() -> {
					try{
						Thread.sleep(maxWait);
					}catch(Exception e){
						//ignore
					}
					TDS.writeString(-1, " [O-FSS]\tNO TERMINATION DETECTED IN " + maxWait + " ms" );
    				this.setDone();
				}, "TimeoutCount_1" );
				wait();
			}catch(InterruptedException e){
				
			}
		}
	}
	
	public void run(){

		for(int i = 0; i < nnodes; i++)
			nodeRunners[i] = new NodeRunner1(i, nnodes, network, i == 0);
		
		network.waitForAllNodes();
		waitTillDone();
		network.killNodes();
		TDS.instance().setDone(1);
	}
	
	public synchronized void announce(){
		done = true;
		notifyAll();
	}
	
}
