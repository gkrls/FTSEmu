package algo.ifss.network;


import java.util.Random;

import ibis.util.ThreadPool;
import tds.performance.PerformanceLogger;
import tds.td.correct.network.Network3;
import tds.td.correct2.node.NodeMessage5;
import tds.td.correct2.node.NodeRunner5;
import tds.td.correct2.probing.ProbeMessage5;
import tds.td.correct2.probing.Prober5;

public class Network5 {
    
    private final int nnodes;
    private final NodeRunner5[] nodeRunners;
    private final Prober5[] probers;
    private Random random;
    private int nodeCount;
    protected long lastPassive;
    
    
    public Network5(int nnodes){
        this.nnodes = nnodes;
        nodeCount = 0;
        random = new Random();
        nodeRunners = new NodeRunner5[nnodes];
        probers = new Prober5[nnodes];
    }
    
    public synchronized void waitForAllNodes() {
        while(nodeCount < nnodes) {
            try {
                wait();
            } catch (InterruptedException e){
                break;
            }
        }
        
        for(NodeRunner5 r: nodeRunners) {
            r.start();
        }
    }
    
    public synchronized void registerNode(NodeRunner5 nodeRunner) {
        nodeRunners[nodeRunner.getId()] = nodeRunner;
        probers[nodeRunner.getId()] = new Prober5(nodeRunner.getId(), nnodes, this, nodeRunner);
        nodeRunner.attachProber(probers[nodeRunner.getId()]);
        nodeCount++;
        if(nodeCount == nnodes) {
            notifyAll();
        }
    }
    
    // Send message with random delay. Execute in separate thread to not delay the sender with it.
    public void sendMessage(final int dest, final NodeMessage5 nodeMessage) {
        ThreadPool.createNew(() -> {
            int delay = random.nextInt(50);
            try { Thread.sleep(delay); } catch (InterruptedException e){}
            nodeRunners[dest].receiveMessage(nodeMessage);
        }, "Sender5");
    }
    //To be called when termination is detected
    public void killNodes() {
        for(NodeRunner5 nr : nodeRunners )
            nr.stopRunning();
    }
    
    
    // Send the token and receive it after random delay. Execute in seperate thread to not delay the sender.
    public void sendProbeMessage(final int dest, final ProbeMessage5 probeMessage) {
        ThreadPool.createNew(() -> {
            int delay = random.nextInt(50); //Maybe increase that delay as token is larger
            try { Thread.sleep(delay); } catch (InterruptedException e){}
            probers[dest].receiveMessage(probeMessage);
        }, "ProbeSender5");
    }
    
    public void sendFirstProbeMessage(final int dest, final ProbeMessage5 probeMessage) {
        ThreadPool.createNew(() -> {
            //int delay = random.nextInt(50); //Maybe increase that delay as token is larger
            //try { Thread.sleep(delay); } catch (InterruptedException e){}
            probers[dest].receiveFirstMessage(probeMessage);
        }, "ProbeSender5");
    }
    
    public int selectTarget(int mynode) {
        if(nnodes == 1) {
            System.out.println("WARNING: nnodes = 1, node 0 about to send msg to itself!");
            return mynode;
        }
        
        for(;;){
            int dest = random.nextInt(nnodes);
            if(dest != mynode) return dest;
        }
    }
    
    
    public void registerPassive() {
        ThreadPool.createNew(() -> {
            synchronized (Network3.class) {
                lastPassive = System.currentTimeMillis();
                PerformanceLogger.instance().setTokensUpToTerm(5);
                //PerformanceLogger.instance().setBackupTokensUpToTerm(5);
            }
        }, "PassiveRegister5");
    }

    public long getLastPassive() {
        return this.lastPassive;
    }


}
