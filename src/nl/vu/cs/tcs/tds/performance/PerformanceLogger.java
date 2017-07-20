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
import static util.Options.*;



public class PerformanceLogger {
	private static String fileVersionString = "";
	
	private static String CSV_FILE;
	
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
	
	public static PerformanceLogger instance(){ return instance; }
	
	public void timeout(int version) {
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
	
	public synchronized void setInitiallyActive(int ia, int version) {
	    switch(version) {
    	    case 2: tds2.setInitiallyActive(ia);
    	    case 3: tds3.setInitiallyActive(ia);
    	    default:
    	        tds1.setInitiallyActive(1);
	    }
	}
	
	public synchronized int getInitiallyActive(int version) {
	       switch(version) {
           case 2: return tds2.getInitiallyActive();
           case 3: return tds3.getInitiallyActive();
           case 1: return tds1.getInitiallyActive();
           default:
               return -1;
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
    
    public void setNumCrashedNodes(int c) {
        tds3.setCrashedNodes(c);
    }
	
	
    public synchronized void writeToCSV() throws IOException {
        if (Options.instance().get(Options.VERSION) == 0) {
            fileVersionString = "O,I,FT";
        } else {
            String versionString = String.valueOf(Options.instance().get(VERSION));
            if (versionString.contains("1")) fileVersionString += "O,";
            if (versionString.contains("2")) fileVersionString += "I,";
            if (versionString.contains("3")) fileVersionString += "FT,";
            fileVersionString = fileVersionString.substring(0, fileVersionString.length() - 1);
        }
        
        String includeCi = fileVersionString.contains("FT") ? 
                (Options.instance().get(CRASHING_NODES_INTERVAL) == CRASHING_NODES_INTERVAL_GAUSSIAN? "ci-gaus" : "ci-uni") : "";
        
        String algoType = "";
        switch(Options.instance().get(Options.BASIC_ALGO_TYPE)) {
            case Options.BASIC_ALGO_CENTRALIZED: algoType = "single"; break;
            case Options.BASIC_ALGO_DECENTRALIZED_EVEN: algoType = "even"; break;
            case Options.BASIC_ALGO_DECENTRALIZED_RANDOM: algoType = "random"; break;
        }
                
        CSV_FILE = "[ " + fileVersionString + " ]" + "__" +
                Options.instance().get(NUM_OF_NODES) + "-" +
                (Options.instance().get(CRASHING_NODES) == -1? "Rnd" : Options.instance().get(CRASHING_NODES))+ "__" +
                (Options.instance().get(ACTIVITY_STRATEGY) == ACTIVITY_STRATEGY_COMPUTE_SEND? "compute-send": "n-activities") + "__" +
                (Options.instance().get(PROB_DISTRIBUTION) == PROB_DISTRIBUTION_UNIFORM? "dist-uni": "dist-gaus") + "__" + includeCi + "__" +
                (Options.instance().get(AVERAGE_NETWORK_LATENCY)) + "ms__" + algoType + ".csv";
        
        File folder = new File(Options.CSV_FOLDER + "/");
        File f = new File(Options.CSV_FOLDER + "/" + CSV_FILE);
        
        if(!folder.exists()){ folder.mkdirs(); }

        
        String s = tds1.getInitiallyActive() + "#" + tds1.getTotalTokens() + "#" + tds1.getExtraTokens() + "#" + tds1.meanProcTime()  + "#" + "" + "#"
                 + tds2.getInitiallyActive() + "#" + tds2.getTotalTokens() + "#" + tds2.getExtraTokens() + "#" + tds2.meanProcTime()  + "#" + "" + "#"
                 + tds3.getInitiallyActive() + "#" + tds3.getTotalTokens() + "#" + tds3.getExtraTokens() + "#" + tds3.meanProcTime()  + "#" 
                 + tds3.getTotalBackupTokens() + "#" + tds3.hasTimedOut() + "#" + tds3.getNumOfCrashedNodes() + "#" 
                 + (((double)tds3.getNumOfCrashedNodes()) / Options.instance().get(NUM_OF_NODES)) * 100;

        if(!f.exists()) {
            CSVWriter writer = new CSVWriter(new FileWriter(f, true), '\t');
            String[] titles = { "O-FSS init-a", "O-FSS MTC", "O-FSS METC", "O-FSS MPT", " -----",
                                "I-FSS init-a", "I-FSS MTC", "I-FSS METC", "I-FSS MPT", " -----",
                                "FTS init-a", "FTS MTC", "FTS METC", "FTS MPT", "FTS MBTC", "FTS TIMEOUT", "C", "C/N*100"};
            writer.writeNext(titles);
            writer.writeNext(s.split("#"));
            writer.close();
        } else {
            CSVWriter writer = new CSVWriter(new FileWriter(f, true), '\t');
            writer.writeNext(s.split("#"));
            writer.close();
        }
	}
	
	public void clearCSV() throws IOException {
	    File folder = new File(Options.CSV_FOLDER + "/");
	    File f = new File(Options.CSV_FOLDER + "/" + CSV_FILE);
	    if (folder.exists() && f.exists()) {
	        CSVWriter writer = new CSVWriter(new FileWriter(f, false), '\t');
            String[] titles = { "O-FSS init-a", "O-FSS MTC", "O-FSS METC", "O-FSS MPT", " -----",
                    "I-FSS init-a", "I-FSS MTC", "I-FSS METC", "I-FSS MPT", " -----",
                    "FTS init-a", "FTS MTC", "FTS METC", "FTS MPT", "FTS MBTC", "FTS TIMEOUT", "C", "C/N*100"};
	        writer.writeNext(titles);
	        writer.close();
	    }
	}
}
