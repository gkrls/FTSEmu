package main;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.PropertyConfigurator;
import print.color.Ansi.Attribute;
import print.color.Ansi.BColor;
import print.color.Ansi.FColor;
import print.color.ColoredPrinter;
import util.Option;
import util.Options;
import performance.PerformanceLogger;

import static util.Options.*;

public class TDS {
	
    private TDSOriginal tds1;
    private TDSImproved tds2;
    private TDSFaultTolerant tds3;
    private static ColoredPrinter cp;
    private static boolean[] done;
    
    private static TDS instance = new TDS();
    
  
    
    private TDS(){
    	cp = new ColoredPrinter.Builder(1, false).build();
    	done = new boolean[3];
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
    		cp.print("[ O-FSS ]", Attribute.BOLD, FColor.WHITE, BColor.BLUE);
    	else if(version == 2)
    	    cp.print("[ I-FSS ]", Attribute.BOLD, FColor.WHITE, BColor.YELLOW);
    	else if(version == 3)
    	    cp.print("[   FTS ]", Attribute.BOLD, FColor.WHITE, BColor.BLACK);
    	else if(version == 0){//warning
    		cp.print("[  INFO ]", Attribute.BOLD, FColor.YELLOW, BColor.GREEN);
    		cp.clear();
    		cp.println(s);
    		return;
    	}else if(version == -1) {
    	    cp.print("[TIMEOUT]", Attribute.BOLD, FColor.WHITE, BColor.RED);
    	    cp.clear();
    	    cp.println(s);
    	    return;
    	}else if(version == -2) {
    	    cp.print("[WARNING]", Attribute.BOLD, FColor.YELLOW, BColor.RED);
    	    cp.clear();
    	    cp.println(s);
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
    
    
    public void announce(int version){
    	if(version == 1)
    		tds1.announce();
    	else if(version == 2)
    		tds2.announce();
    	else if(version == 3)
    		tds3.announce();
    }
    
    public void setDone(int version){
    	synchronized(this){
    		done[version - 1] = true;
        	notifyAll();
    	}

    }
    
    private void waitAllDone(){
    	synchronized(this){
    		while(!(done[0] && done[1] && done[2])){
    			try {
					wait();
				} catch (InterruptedException e) {
					break;
				}
    		}
    	}
    }
    
    private void printSimInfo() {
        for(Option opt: Options.instance().getAll()){
            if(opt.getName().equals("-ci") || opt.getName().equals("-anl"))
                System.out.print(opt.alias() + " ");
            else
                System.out.print(opt.alias() + " \t");
            if(opt.getName().equals("-dist")) {
                switch(Options.instance().get(PROB_DISTRIBUTION)) {
                    case PROB_DISTRIBUTION_UNIFORM: 
                        System.out.println("-- uniform"); break;
                    case PROB_DISTRIBUTION_GAUSSIAN:
                        System.out.println("-- gaussian"); break;
                }
            } else if (opt.getName().equals("-strategy")) {
                switch(Options.instance().get(ACTIVITY_STRATEGY)) {
                    case ACTIVITY_STRATEGY_COMPUTE_SEND: 
                        System.out.println("-- compute-send"); break;
                    case ACTIVITY_STRATEGY_N_ACTIVITIES:
                        System.out.println("-- n-activities"); break;
                }
                
            } else if (opt.getName().equals("-batype")) {
                switch(Options.instance().get(BASIC_ALGO_TYPE)) {
                    case BASIC_ALGO_CENTRALIZED: 
                        System.out.println("-- centralized"); break;
                    case BASIC_ALGO_DECENTRALIZED_EVEN:
                        System.out.println("-- even-nodes-active"); break;
                    case BASIC_ALGO_DECENTRALIZED_RANDOM:
                        System.out.println("-- random-nodes-active"); break;
                }
            } else if(opt.getName().equals("-ci")){
                switch(Options.instance().get(CRASHING_NODES_INTERVAL)) {
                    case CRASHING_NODES_INTERVAL_GAUSSIAN: 
                        System.out.println("-- gaussian"); break;
                    case CRASHING_NODES_INTERVAL_UNIFORM:
                        System.out.println("-- uniform"); break;
                    default:
                        System.out.println("-- " + Options.instance().get(CRASHING_NODES_INTERVAL)); break;
                }
            } else if (opt.getName().equals("-c")) {
                if(Options.instance().get(CRASHING_NODES) == Options.CRASHING_NODES_RANDOM) {
                    System.out.println("\t-- random");
                } else {
                    System.out.println("\t-- " + Options.instance().get(CRASHING_NODES)); break;
                }
            } else if (opt.getName().equals("-anl")) {
                System.out.println("-- " + Options.instance().get(opt.getId()));
            } else {
                System.out.println(" \t-- " + Options.instance().get(opt.getId()));
            }
        }
    }
    
    public void start(){
        printSimInfo();
    	if(Options.instance().get(Options.VERSION) == 0){
    		tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
    		tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
    		tds3 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
    		new Thread(tds1).start();
        	new Thread(tds2).start();
        	new Thread(tds3).start();
    	}else{//ugly but ok for now
    	    done[0] = done[1] = done[2] = true;
    	    ArrayList<Runnable> tds = new ArrayList<Runnable>();
            int version = Options.instance().get(Options.VERSION);
            String versionString = String.valueOf(version);
            
            if(versionString.contains("1")){
                tds1 = new TDSOriginal(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
                done[0] = false;
                new Thread(tds1).start();
            }
            
            if(versionString.contains("2")){
                tds2 = new TDSImproved(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
                done[1] = false;
                new Thread(tds2).start();
            }
            
            if(versionString.contains("3") ){
                tds3 = new TDSFaultTolerant(Options.instance().get(Options.NUM_OF_NODES), Options.instance().get(Options.MAX_WAIT));
                done[2] = false;
                new Thread(tds3).start();
            }
    	}


    	waitAllDone();
    	
    	if(Options.instance().get(Options.FLUSH_CSV) == 1){
    		try {
				PerformanceLogger.instance().clearCSV();
			} catch (IOException e) {
				System.out.println("Error flushing CSV:\n" + e.getMessage());
			}
    	}
    	if(Options.instance().get(Options.CSV) == 1){
        	try {
    			PerformanceLogger.instance().writeToCSV();
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
