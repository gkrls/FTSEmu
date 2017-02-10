package nl.vu.cs.tcs.tds.performance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import tds.td.original.probing.*;
import tds.td.correct2.probing.*;
import tds.td.ft2.probing.*;

import com.opencsv.CSVWriter;




public class PerformanceLogger {
	
	private static final String CSV_FILE = "results.csv";
	
	private Performance  tds1, tds2, tds3, tds4, tds5, tds6;
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
		tds4 = new Performance(4);
		tds5 = new Performance(5);
		tds6 = new Performance(6);
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
            case 4: tds4.timeout(); break;
            case 5: tds5.timeout(); break;
            case 6: tds6.timeout(); break;
            default: break;
        }
    }
	
	public void incTokens(int version){
		switch(version){
			case 1: tds1.incTokens(); break;
			case 2: tds2.incTokens(); break;
			case 3: tds3.incTokens(); break;
			case 4: tds4.incTokens(); break;
			case 5: tds5.incTokens(); break;
			case 6: tds6.incTokens(); break;
			default: break;
		}
	}
	
	
	public int getTokens(int version){
		switch(version){
			case 1: return tds1.getTotalTokens();
			case 2: return tds2.getTotalTokens();
			case 3: return tds3.getTotalTokens();
			case 4: return tds4.getTotalTokens();
			case 5: return tds5.getTotalTokens();
			case 6: return tds6.getTotalTokens();
			default: return -1;
		}
	}
	
	public synchronized void setTokensUpToTerm(int version){
		switch(version){
			case 1: tds1.setTokensUpToTerm(); break;
			case 2: tds2.setTokensUpToTerm(); break;
			case 3: tds3.setTokensUpToTerm(); break;
			case 4: tds4.setTokensUpToTerm(); break;
			case 5: tds5.setTokensUpToTerm(); break;
			case 6: tds6.setTokensUpToTerm(); break;
			default: return;
		}
	}
	
    public synchronized void setBackupTokensUpToTerm(int version) {
        switch(version) {
            case 4: tds4.setBackupTokensUpToTerm(); break;
            case 6: tds6.setBackupTokensUpToTerm(); break;
            default: return;
        }
    }
    
    //Make sure a copy of the token is passed
    //ConcurrentModificationException is possible otherwise
    
    public synchronized void addTokenBits(int version, Object token) {
        switch(version) {
            case 1: tds1.addBits((ProbeMessage1) token); break;
            case 5: tds5.addBits((ProbeMessage5) token); break;
            case 6: tds6.addBits((ProbeMessage6) token); break;
            default: return;
        }
    }
	
	
	public synchronized void addProcTime(int version, long time){
	       switch(version) {
               case 1: tds1.addProcTime(time); break;
               case 2: tds2.addProcTime(time); break;
               case 3: tds3.addProcTime(time); break;
               case 4: tds4.addProcTime(time); break;
               case 5: tds5.addProcTime(time); break;
               case 6: tds6.addProcTime(time); break;
               default: return;
	       }
	}
	
	public synchronized void incBackupTokens(int version){
        switch(version) {
            case 4: tds4.incBackupTokens(); break; // This line was case 4: tds5.incBackupTokens(); break;
            case 6: tds6.incBackupTokens(); break;
            default: return;
        }
	}
	
//	public void printOneLineResults(){
//		System.out.println("[" + tds1.getTotalTokens() + ", " + tds1.getExtraTokens() + ", " 
//		                        + tds1.meanProcTime() + " -- "
//								+ tds2.getTotalTokens() + ", " + tds2.getExtraTokens() + ", "
//								+ tds2.meanProcTime() + " -- "
//								+ tds3.getTotalTokens() + ", " + tds3.getExtraTokens() + ", "
//								+ tds3.meanProcTime() + " -- "
//								+ tds4.getTotalTokens() + ", " + tds4.getExtraTokens() + ", "
//								+ tds4.getTotalBackupTokens() + ", " + tds4.getExtraBackupTokens() + ", "
//								+ tds4.meanProcTime() + "]");
//	}
	
//	public void writeToCSV() throws IOException{
//		File f = new File(CSV_FILE);
//		String s = tds1.getTotalTokens() + "#" + tds1.getExtraTokens() + "#" 
//		        + tds1.meanProcTime() + "#"
//				+ tds2.getTotalTokens() + "#" + tds2.getExtraTokens() + "#"
//				+ tds2.meanProcTime() + "#"
//				+ tds3.getTotalTokens() + "#" + tds3.getExtraTokens() + "#"
//				+ tds3.meanProcTime() + "#"
//		        + tds4.getTotalTokens() + "#" + tds4.getExtraTokens() + "#"
//		        + tds4.getTotalBackupTokens() + "#" + tds4.getExtraBackupTokens() + "#"
//		        + tds4.meanProcTime();
//		CSVWriter writer;
//		if(!f.exists()){
//			writer = new CSVWriter(new FileWriter(CSV_FILE, true), '\t');
//			String[] title = {"Orig. All", "Orig. Extra", "Mean proc. @ Pi", "Impr. All", "Impr. Extra", "Mean proc. @ Pi", "Corr. All", "Corr. Extra", "Mean proc. @ Pi", "FT all real", "FT real extra", "FT all backup", "FT backup extra", "Mean proc. @ Pi"};
//			writer.writeNext(title);
//			writer.writeNext(s.split("#"));
//		}else{
//			writer = new CSVWriter(new FileWriter(CSV_FILE, true), '\t');
//			writer.writeNext(s.split("#"));
//		}
//		//this.printOneLineResults();
//		writer.close();
//	}
//	
//	public void clearCSV() throws IOException{
//        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE, false), '\t');
//        String[] title = {"Orig. All", "Orig. Extra", "Mean proc. @ Pi", "Impr. All", "Impr. Extra", "Mean proc. @ Pi", "Corr. All", "Corr. Extra", "Mean proc. @ Pi", "FT all real", "FT real extra", "FT all backup", "FT backup extra", "Mean proc. @ Pi"};
//        writer.writeNext(title);
//        writer.close();
//        
//    }
	
	public synchronized void writeToCSV2() throws IOException { //IPDPS
	    File f = new File(CSV_FILE);
	    String s = tds1.getTotalTokens() + "#" + tds1.getExtraTokens() + "#" + tds1.meanProcTime() + "#" + tds1.meanTokenBitComplexity() + "#" + "" + "#"
	            + tds5.getTotalTokens() + "#" + tds5.getExtraTokens() + "#" + tds5.meanProcTime() + "#" + tds5.meanTokenBitComplexity() + "#" + "" + "#"
	            + tds6.getTotalTokens() + "#" + tds6.getExtraTokens() + "#" + tds6.meanProcTime() + "#" + tds6.meanTokenBitComplexity() + "#" + ""+tds6.getTotalBackupTokens() + "#" + tds6.getExtraBackupTokens() + "#" + tds6.hasTimedOut();
	    CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE, true), '\t');
	    if(!f.exists()) {
	        String[] titles = {"OS MTC", "OS METC", "OS MPT", "OS MBC", "",
	                            "IS MTC", "IS METC", "IS MPT", "IS MBC", "",
	                            "FT MTC", "FT METC", "FT MPT", "FT MBC", "FT MBTC", "FT MEBTC", "TIMEOUT"};
	        writer.writeNext(titles);
	        writer.writeNext(s.split("#"));
	    } else {
	        writer.writeNext(s.split("#"));
	    }
	    
	    writer.close();
	            
	}
	
	public void clearCSV2() throws IOException { //IPDPS
	    CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE, false), '\t');
        String[] titles = {"OS MTC", "OS METC", "OS MPT", "OS MBC", "",
                "IS MTC", "IS METC", "IS MPT", "IS MBC", "",
                "FT MTC", "FT METC", "FT MPT", "FT MBC", "FT MBTC", "FT MEBTC", "TIMEOUT"};
        writer.writeNext(titles);
        writer.close();
	}
	



	

}
