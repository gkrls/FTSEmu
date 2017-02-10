package nl.vu.cs.tcs.tds.main;

import ibis.util.ThreadPool;
import tds.td.ft.network.Network4;
import tds.td.ft.node.NodeRunner4;

public class TDSFaultTolerant implements Runnable{
	private volatile boolean done;
	private int nnodes;
	private NodeRunner4[] nodeRunners;
	private Network4 network;
	private long maxWait;
	
	public TDSFaultTolerant(int nnodes, long maxWait){
		this.nnodes = nnodes;
		done = false;
		this.nodeRunners = new NodeRunner4[nnodes];
		this.network = new Network4(nnodes);
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
			nodeRunners[i] = new NodeRunner4(i, nnodes, network, i == 0);
		network.waitForAllNodes();
		waitTillDone();
		network.killNodes();
		TDS.instance().setDone(4);
	}
	
	public synchronized void announce(){
		done = true;
		notifyAll();
	}

}
