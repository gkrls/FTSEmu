package nl.vu.cs.tcs.tds.main;

import ibis.util.ThreadPool;
import tds.td.correct.network.Network3;
import tds.td.correct.node.NodeRunner3;


public class TDSCorrect implements Runnable{
	private static boolean done;
	private int nnodes;
	private NodeRunner3[] nodeRunners;
	private Network3 network;
	private long maxWait;
	
	public TDSCorrect(int nnodes, long maxWait){
		this.nnodes = nnodes;
		this.done = false;
		this.nodeRunners = new NodeRunner3[nnodes];
		this.network = new Network3(nnodes);
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
					TDS.writeString(0, "NO TERMINATION DETECTED IN " + maxWait + " ms" );
    				this.setDone();
				}, "TimeoutCount_3");
				wait();
			}catch(InterruptedException e){}
		}
	}
	
	public void run(){
		for(int i = 0; i < nnodes; i++)
			nodeRunners[i] = new NodeRunner3(i, nnodes, network, i == 0);
		network.waitForAllNodes();
		waitTillDone();
		network.killNodes();
		TDS.instance().setDone(3);
	}
	
	public synchronized void announce(){
		done = true;
		notifyAll();
	}
}
