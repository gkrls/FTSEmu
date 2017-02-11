package performance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import algo.ofss.probing.*;
import util.Options;
import algo.ifss.probing.*;
import algo.fts.probing.*;

import com.opencsv.CSVWriter;




public class PerformanceLogger {
	
	private static final String CSV_FILE = Options.instance().get(Options.NUM_OF_NODES)+"_"+Options.instance().get(Options.CRASHED_NODES)+".csv";
	
	private Performance  tds1, tds2, tds3;
	private int tokenCycles;
	private int tokensSent;

	private long firstMessageSentTime;
	private long lastMessageSentTime;
	private long terminationTime;

	
	private static PerformanceLogger instance = new PerformanceLogger();
	
	private PerformanceLogger(){
		tds1 = new Performance(1);
		tds2 = new Performance(2);
		tds3 = new Performance(3);
		//nodesReceivingToken.add(Options.DEFAULT_NUM_OF_NODES - 1); //Add first initiator. Has to be fixed when Options is finished
	}
	
	public static PerformanceLogger instance(){
		return instance;
	}
	
	public void timeout(int version){
        switch(version){
            case 1: tds1.timeout(); break;
            case 2: tds2.timeout(); break;
            case 3: tds3.timeout(); break;
            default: break;
        }
    }
	
	public void incTokens(int version){
		switch(version){
			case 1: tds1.incTokens(); break;
			case 2: tds2.incTokens(); break;
			case 3: tds3.incTokens(); break;
			default: break;
		}
	}
	
	
	public int getTokens(int version){
		switch(version){
			case 1: return tds1.getTotalTokens();
			case 2: return tds2.getTotalTokens();
			case 3: return tds3.getTotalTokens();
			default: return -1;
		}
	}
	
	public synchronized void setTokensUpToTerm(int version){
		switch(version){
			case 1: tds1.setTokensUpToTerm(); break;
			case 2: tds2.setTokensUpToTerm(); break;
			case 3: tds3.setTokensUpToTerm(); break;
			default: return;
		}
	}
	
    public synchronized void setBackupTokensUpToTerm(int version) {
        switch(version) {
            case 3: tds3.setBackupTokensUpToTerm(); break;
            default: return;
        }
    }
    
    //Make sure a copy of the token is passed
    //ConcurrentModificationException is possible otherwise
    
    public synchronized void addTokenBits(int version, Object token) {
        switch(version) {
            case 1: tds1.addBits((ProbeMessage1) token); break;
            case 5: tds2.addBits((ProbeMessage2) token); break;
            case 6: tds3.addBits((ProbeMessage3) token); break;
            default: return;
        }
    }
	
	
	public synchronized void addProcTime(int version, long time){
	       switch(version) {
               case 1: tds1.addProcTime(time); break;
               case 2: tds2.addProcTime(time); break;
               case 3: tds3.addProcTime(time); break;
               default: return;
	       }
	}
	
	public synchronized void incBackupTokens(int version){
        switch(version) {
            case 3: tds3.incBackupTokens(); break; // This line was case 4: tds5.incBackupTokens(); break;
            default: return;
        }
	}
	
	
	public synchronized void writeToCSV() throws IOException {
	    File f = new File("csv/"+CSV_FILE);
	    //f.getParentFile().mkdirs();
	    String s = tds1.getTotalTokens() + "#" + tds1.getExtraTokens() + "#" + tds1.meanProcTime() + "#" + tds1.meanTokenBitComplexity() + "#" + "" + "#"
	             + tds2.getTotalTokens() + "#" + tds2.getExtraTokens() + "#" + tds2.meanProcTime() + "#" + tds2.meanTokenBitComplexity() + "#" + "" + "#"
	             + tds3.getTotalTokens() + "#" + tds3.getExtraTokens() + "#" + tds3.meanProcTime() + "#" + tds3.meanTokenBitComplexity() + "#" + ""+tds3.getTotalBackupTokens() + "#" + tds3.getExtraBackupTokens() + "#" + tds3.hasTimedOut();
	    CSVWriter writer = new CSVWriter(new FileWriter(f, true), '\t');
	    if(!f.exists()) {
	        f.getParentFile().mkdirs();
	        System.out.println("HERE!");
	        String[] titles = {"OS MTC", "OS METC", "OS MPT", "OS MBC", "",
	                            "IS MTC", "IS METC", "IS MPT", "IS MBC", "",
	                            "FT MTC", "FT METC", "FT MPT", "FT MBC", "FT MBTC", "FT MEBTC", "TIMEOUT"};
	        writer.writeNext(titles);
	        writer.writeNext(s.split("#"));
	    } else {
	        System.out.println("HERE!!!");
	        writer.writeNext(s.split("#"));
	    }
	    
	    writer.close();
	            
	}
	
	public void clearCSV() throws IOException {
	    File f = new File("csv/"+CSV_FILE);
        f.getParentFile().mkdirs();
	    CSVWriter writer = new CSVWriter(new FileWriter(f, false), '\t');
        String[] titles = {"OS MTC", "OS METC", "OS MPT", "OS MBC", "",
                "IS MTC", "IS METC", "IS MPT", "IS MBC", "",
                "FT MTC", "FT METC", "FT MPT", "FT MBC", "FT MBTC", "FT MEBTC", "TIMEOUT"};
        writer.writeNext(titles);
        writer.close();
	}
	



	

}
