package nl.vu.cs.tcs.tds.performance;

import java.util.concurrent.ConcurrentLinkedQueue;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.instrument.Instrumentation; 

import tds.main.Options;
import tds.td.correct2.probing.ProbeMessage5;
import tds.td.ft2.probing.ProbeMessage6;
import tds.td.original.probing.ProbeMessage1;
import tds.util.ObjectSizeFetcher;



public class Performance {
    
    
	private int version;
	int totalTokens;
	private int tokensUpToTerm;
	private boolean timeout;
	
	private int totalBackupTokens; //ft only
	private int backupTokensUpToTerm;//ft only
	
	private int tokenBits;
	private ConcurrentLinkedQueue<Long> times; //nano
	
	
	public Performance(int version){
		this.version = version;
		this.timeout = false;
		times = new ConcurrentLinkedQueue<Long>();
	}
	
	public synchronized void incTokens(){
		totalTokens++;
		//System.out.println(totalTokens);
	}
	
	public void timeout() {
	    this.timeout = true;
	}
	
	public boolean hasTimedOut() {
	    return this.timeout;
	}
	
	public int getTotalTokens(){
		return this.totalTokens;
	}
	
	public void setTokensUpToTerm(){
		tokensUpToTerm = totalTokens;
	}
	
	public void setTokensUpToTermReset(){
		tokensUpToTerm = 0;
	}
	
	public int getExtraTokens(){
		return totalTokens - tokensUpToTerm;
	}
	
	public void print(){
		System.out.println("[" + totalTokens + ", " + getExtraTokens() + "]");
	}
	
	
	
	/**
	 * Methods only for the Fault Tolerant Version
	 */
	
	
	public synchronized void incBackupTokens(){
	    this.totalBackupTokens++;
	}
	
	public synchronized void setBackupTokensUpToTerm(){
	    this.backupTokensUpToTerm = this.totalBackupTokens;
	}
	
	public int getTotalBackupTokens(){
	    return this.totalBackupTokens;
	}
	
	public int getExtraBackupTokens(){
	    return this.totalBackupTokens - this.backupTokensUpToTerm;
	}
	
	
	/**
	 * Processing Time
	 */
	
	public void addProcTime(long time){
	    this.times.add(time);
	}
	
	
	public double meanProcTime(){
	    double  mean = this.times.stream().mapToLong(x -> x.longValue()).sum() / Options.instance().get(Options.NUM_OF_NODES);
	    return mean / 1000000;
	}
	
	
	/**
	 * Bit complexity
	 *
	 */
	
    public synchronized void addBits(final ProbeMessage1 token) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(token);
            oos.close();
            int bytes = baos.size();
            
            tokenBits += bytes * 8;
            //System.out.println(tokenBits);
        } catch (IOException e) {
            System.out.println("[DEBUG] " + e.getMessage());
        }  
    }
	
	public synchronized void addBits(ProbeMessage5 token) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(token);
            oos.close();
            int bytes = baos.size();
            tokenBits += bytes * 8;
        } catch (IOException e) {
            System.out.println("[DEBUG] " + e.getMessage());
        }
	}
	
	public synchronized void addBits(final ProbeMessage6 token) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(token);
            oos.close();
            int bytes = baos.size();
            tokenBits += (bytes * 8);
        } catch (IOException e) {
            System.out.println("[DEBUG] " + e.getMessage());
        }
	}
	
    public synchronized int meanTokenBitComplexity() {
        int result = 0;
        try {
            result = tokenBits / (this.getTotalTokens());
        }catch(Exception e) {
            return result;
        }
        return result;
    }
	
	
	
}
