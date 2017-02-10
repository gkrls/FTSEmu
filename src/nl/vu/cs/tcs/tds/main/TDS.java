package nl.vu.cs.tcs.tds.main;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.PropertyConfigurator;
import print.color.Ansi.Attribute;
import print.color.Ansi.BColor;
import print.color.Ansi.FColor;
import print.color.ColoredPrinter;
import tds.performance.PerformanceLogger;
// TDS: Termination Detection Simulator
public class TDS {
	
    private TDSOriginal tds1;
    private TDSImproved tds2;
    private TDSCorrect tds3;
    private TDSFaultTolerant tds4;
    private TDSCorrect2 tds5;
    private TDSFaultTolerant2 tds6;
    private static ColoredPrinter cp;
    private static boolean[] done;
    
    private static TDS instance = new TDS();
    
  
    
    private TDS(){
    	cp = new ColoredPrinter.Builder(1, false).build();
    	done = new boolean[6];
    }
    
    public static synchronized TDS instance(){
    	return instance;
    }
    
    /**
     * Uncomment when working on eclipse or generally when output is not
     * printed in terminal.
     */
//    public static synchronized void writeString(int version, String s){
//    	System.out.println("[" + version + "] " + s);
//    }
    
    /**
     * Uncomment to create the jar
     */
    public static synchronized void writeString(int version,String s) {
    	if(Options.instance().get(Options.VERBOSE) == 0) return;
    	if(version==1)
    		cp.print("[" + version + "]", Attribute.BOLD, FColor.WHITE, BColor.BLUE);
    	else if(version == 2)
    		cp.print("[" + version + "]", Attribute.BOLD, FColor.WHITE, BColor.GREEN);
    	else if(version == 3)
    		cp.print("[" + version + "]", Attribute.BOLD, FColor.WHITE, BColor.MAGENTA);
    	else if(version == 4)
    		cp.print("[" + version + "]", Attribute.BOLD, FColor.WHITE, BColor.CYAN);
    	else if(version == 5)
    	    cp.print("[" + version + "]", Attribute.BOLD, FColor.WHITE, BColor.YELLOW);
    	else if(version == 6)
    	    cp.print("[" + version + "]", Attribute.BOLD, FColor.WHITE, BColor.BLACK);
    	else if(version == 0){//warning
    		cp.print(s, Attribute.NONE, FColor.YELLOW, BColor.RED);
    		cp.clear();
    		cp.print("\n");
    		return;
    	}
    	if(s.contains("Termination")){
    		if(s.charAt(9) == '\t'){
    			cp.print(s.substring(0,10), Attribute.BOLD, FColor.RED, BColor.YELLOW);
    			cp.clear();
    			cp.print(s.substring(10));
    		}else{
    			cp.print(s.substring(0,11), Attribute.BOLD, FColor.RED, BColor.YELLOW);
    			cp.clear();
    			cp.print(s.substring(11));
    		}
    		
    		cp.print("\n");
    	}else{
    		cp.clear();
        	cp.println(s);
    	}
    	
    }
    
    public static void configlog4j(){
    	String log4jConfPath = "log4j.properties";
    	PropertyConfigurator.configure(log4jConfPath);
    }
    
//    public synchronized void waitTillDone(long limit){
//    	while(!done){
//    		try{
//    			ThreadPool.createNew(() -> {
//    				try {
//						Thread.sleep(limit);
//					} catch (Exception e) {
//						//ignore
//					}
//    				TDS.writeString(0, "NO TERMINATION DETECTED IN " + limit + " ms" );
//    				TDS.instance().announce(0);
//    			}, "TimeoutCount");
//    			wait();
//    		}catch (InterruptedException e){
//    			TDS.writeString(0, "DONE!");
//    		}
//    	}
//    }
    
    public void announce(int version){
    	if(version == 1)
    		tds1.announce();
    	else if(version == 2)
    		tds2.announce();
    	else if(version == 3)
    		tds3.announce();
    	else if(version == 4)
    		tds4.announce();
    	else if(version == 5)
    	    tds5.announce();
    	else if(version == 6)
    	    tds6.announce();
    }
    
    public void setDone(int version){
    	synchronized(this){
    		done[version - 1] = true;
        	notifyAll();
    	}

    }
    
    private void waitAllDone(){
    	synchronized(this){
    		while(!(done[0] && done[1] && done[2] && done[3] && done[4] && done[5])){
    			try {
					wait();
				} catch (InterruptedException e) {
					break;
				}
    		}
    		
    	}
    }
    
    public void start(){
    	if(Options.instance().get(Options.VERSION) == 0){
    		tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
    		tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
    		tds3 = new TDSCorrect(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
    		tds4 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
        	tds5 = new TDSCorrect2(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
        	tds6 = new TDSFaultTolerant2(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
    		new Thread(tds1).start();
        	new Thread(tds2).start();
        	new Thread(tds3).start();
        	new Thread(tds4).start();
        	new Thread(tds6).start();
    	}else{//ugly but ok for now
    	    done[0] = done[1] = done[2] = done[3] = done[4] = done[5] = true;
    	    ArrayList<Runnable> tds = new ArrayList<Runnable>();
            int version = Options.instance().get(Options.VERSION);
            String versionString = String.valueOf(version);
            
            if(versionString.contains("1")){
                //Do nothing now!
            }
            
            if(versionString.contains("5")){
                tds5 = new TDSCorrect2(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
                tds.add(tds5);
                done[4] = false;
            }
            
            if(versionString.contains("6") ){
                tds6 = new TDSFaultTolerant2(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
                tds.add(tds6);
                done[5] = false;
            }
            
            for(Runnable r: tds)
                new Thread(r).start();
    	}

    	    
//    	    if(version == 1){
//                done[1] = true; done[2] = true; done[3] = true; done[4] = true; done[5] = true;
//                tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds1).start();
//    	    }
//    	    if(version == 2){
//                done[0] = true; done[2] = true; done[3] = true; done[4] = true; done[5] = true;
//                tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds2).start();
//    	    }
//    	    if(version == 3){
//                done[0] = true; done[1] = true; done[3] = true; done[4] = true; done[5] = true;
//                tds3 = new TDSCorrect(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds3).start();
//    	    }
//    	    if(version == 4){
//                done[0] = true; done[1] = true; done[2] = true; done[4] = true; done[5] = true;
//                tds4 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds4).start(); 
//    	    }
//    	    if(version == 5){
//    	        done[0] = done[1] = done[2] = done[3] = done[5] = true;
//    	        tds5 = new TDSCorrect2(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//    	        new Thread(tds5).start();
//    	    }
//    	    
//    	    if(version == 6) {
//                done[0] = done[1] = done[2] = done[3] = true; done[4] = true; 
//                tds6 = new TDSFaultTolerant2(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds6).start();
//    	    }
//    	    
//    	    if(version == 12 || version == 21){
//                done[2] = true; done[3] = true;
//                tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds1).start();
//                new Thread(tds2).start();
//    	    }
//    	    if(version == 13 || version == 31){
//                done[1] = true; done[3] = true;
//                tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds3 = new TDSCorrect(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds1).start();
//                new Thread(tds3).start();
//    	        
//    	    }
//    	    if(version == 14 || version == 41){
//                done[1] = true; done[2] = true;
//                tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds4 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds1).start();
//                new Thread(tds4).start();
//    	    }
//    	    if(version == 23 || version == 32){
//                done[0] = true; done[3] = true;
//                tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds3 = new TDSCorrect(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds2).start();
//                new Thread(tds3).start();
//    	    }
//    	    if(version == 24 || version == 42){
//                done[0] = true; done[2] = true;
//                tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds4 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds2).start();
//                new Thread(tds4).start();
//    	    }
//    	    if(version == 34 || version == 43){
//                done[0] = true; done[1] = true;
//                tds3 = new TDSCorrect(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds4 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds3).start();
//                new Thread(tds4).start();
//    	    }
//    	    if(version == 123 || version == 213 || version == 231 || version == 312 || version == 321 || version == 132){
//                done[3] = true;
//                tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds3 = new TDSCorrect(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds1).start();
//                new Thread(tds2).start();
//                new Thread(tds3).start();
//    	    }
//    	    if(version == 124 || version == 142 || version == 214 || version == 241 || version == 412 || version == 421){
//                done[2] = true;
//                tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds4 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds1).start();
//                new Thread(tds2).start();
//                new Thread(tds4).start();
//    	    }
//    	    if(version == 134 || version == 143 || version == 314 || version == 341 || version == 431 || version == 413){
//                done[1] = true;
//                tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds3 = new TDSCorrect(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds4 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds1).start();
//                new Thread(tds3).start();
//                new Thread(tds4).start();
//    	    }
//    	    if(version == 234 || version == 243 || version == 324 || version == 342 || version == 423 || version == 432){
//                done[0] = true;
//                tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds3 = new TDSCorrect(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds4 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds2).start();
//                new Thread(tds3).start();
//                new Thread(tds4).start();
//    	    }
//    	    if(version == 156) {
//    	        done[1] = done[2] = done[3] = true;
//                tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds5 = new TDSCorrect2(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                tds6 = new TDSFaultTolerant2(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
//                new Thread(tds1).start();
//                new Thread(tds5).start();
//                new Thread(tds6).start();
//    	    }
//    	}
//
//
    	waitAllDone();
    	if(Options.instance().get(Options.FLUSH_CSV) == 1){
    		try {
				PerformanceLogger.instance().clearCSV2();
			} catch (IOException e) {
				System.out.println("Error flushing CSV:\n" + e.getMessage());
			}
    	}
    	if(Options.instance().get(Options.CSV) == 1){
        	try {
    			PerformanceLogger.instance().writeToCSV2();
    		} catch (IOException e) {
    			System.out.println("Error writting CSV:\n" + e.getMessage());
    		}
    	}

    }
    
    public static void main(String[] args) {
    	
    	configlog4j();
    	Options.instance().parse(args);
    	TDS.instance.start();
    }
    

}
