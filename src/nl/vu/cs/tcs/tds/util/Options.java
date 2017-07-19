package util;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class Options {
    
    public static final int OPTION_ERR = -12345;
	
	public static final int NUM_OF_NODES = -1;
	public static final int LOG = -2;
	public static final int ACTIVITY_LEVEL = -3;
	public static final int MAX_WAIT = -4;
	public static final int USAGE = -5;
	public static final int VERBOSE = -6;
	public static final int CSV = -7;
	public static final int FLUSH_CSV = -8;
	public static final int VERSION = -9;
	public static final int CRASHING_NODES = -10;
	public static final int PROB_DISTRIBUTION = -11;
	public static final int ACTIVITY_STRATEGY = -12;
	public static final int BASIC_ALGO_TYPE = -13;
	public static final int CRASHING_NODES_INTERVAL = -14;
	public static final int CRASH_NOTIFY_INTERVAL = -15;
	public static final int AVERAGE_NETWORK_LATENCY = -16;
	
	public static final int PROB_DISTRIBUTION_UNIFORM = 1;
	public static final int PROB_DISTRIBUTION_GAUSSIAN = 2;
	
	public static final int ACTIVITY_STRATEGY_COMPUTE_SEND = 1;
	public static final int ACTIVITY_STRATEGY_N_ACTIVITIES = 2;
	
	public static final int BASIC_ALGO_CENTRALIZED = 1;
	/* half the network (nodes with even ids) is initially active */
	public static final int BASIC_ALGO_DECENTRALIZED_EVEN = 2;
	/* a random number of nodes (in range 1-N) are initially active */
	public static final int BASIC_ALGO_DECENTRALIZED_RANDOM = 3;
	
	
	public static final int CRASHING_NODES_NONE = 0;
	public static final int CRASHING_NODES_RANDOM = -1;
	
	public static final int CRASHING_NODES_INTERVAL_UNIFORM = 1;
	public static final int CRASHING_NODES_INTERVAL_GAUSSIAN = 2;

	
	/** The following options are not user definable (for now)! **/
	public static final int MINIMUM_ACTIVITY_LEVEL = 1;
	public static final int MAXIMUM_ACTIVITY_LEVEL = 2;
	
	public static final int MINIMUM_NUM_OF_NODES = 2;
	public static final int MAXIMUM_NUM_OF_NODES = 1024;
	
    public static final int UNIFORM_COMPUTE_MAX = 2000;
    public static final int UNIFORM_COMPUTE_MIN = 1;
    public static final int UNIFORM_MESSAGES_MAX = 3;
    public static final int UNIFORM_MESSAGES_MIN = 0;
    
    public static final int GAUSSIAN_COMPUTE_MU = 1000;
    public static final int GAUSSIAN_COMPUTE_SD = 200;
    public static final int GAUSSIAN_MESSAGES_MU = 1;
    public static final int GAUSSIAN_MESSAGES_SD = 1;
    
    public static final int CRASHING_NODES_INTERVAL_MIN = 200;
    public static final int CRASHING_NODES_INTERVAL_MAX = 3000;
    
    public static final int UNIFORM_CRASHING_NODES_INTERVAL_MAX = 2000;
    public static final int UNIFORM_CRASHING_NODES_INTERVAL_MIN = CRASHING_NODES_INTERVAL_MIN;
    
    public static final int GAUSSIAN_CRASHING_NODES_INTERVAL_MU = 1000;
    public static final int GAUSSIAN_CRASHING_NODES_INTERVAL_SD = 200;
    
    public static final int AVERAGE_NETWORK_LATENCY_MIN = 20;
    public static final int AVERAGE_NETWORK_LATENCY_MAX = 100;
    public static final int NETWORK_LATENCY_SD = 10;
    /*************************************************************/
	
    
	public static final int DEFAULT_NUM_OF_NODES = 2;
	public static final int DEFAULT_ACTIVITY_LEVEL = MINIMUM_ACTIVITY_LEVEL;
	
	public static final int DEFAULT_MAX_WAIT = 100000;
	public static final int DEFAULT_LOG = 0;
	public static final int DEFAULT_VERBOSE = 0;
	public static final int DEFAULT_USAGE = 0;
	public static final int DEFAULT_CSV = 0;
	public static final int DEFAULT_CSV_FLUSH = 0;
	public static final int DEFAULT_VERSION = 0;
	private static final int[] ALLOWED_VERSIONS = {1,2,3, 12, 13, 21, 31, 23, 32, 123, 132, 213, 231, 312, 321};
	public static final int DEFAULT_CRASHED_NODES = CRASHING_NODES_NONE;
	public static final int DEFAULT_PROB_DISTRIBUTION = PROB_DISTRIBUTION_UNIFORM;
	public static final int DEFAULT_ACTIVITY_STRATEGY = ACTIVITY_STRATEGY_COMPUTE_SEND;
	public static final int DEFAULT_BASIC_ALGO_TYPE = BASIC_ALGO_DECENTRALIZED_EVEN;
	public static final int DEFAULT_CRASHED_NODES_INTERVAL = CRASHING_NODES_INTERVAL_UNIFORM;
	public static final int DEFAULT_AVERAGE_NETWORK_LATENCY = (AVERAGE_NETWORK_LATENCY_MIN + AVERAGE_NETWORK_LATENCY_MAX) / 2;
	
	public static final int DEFAULT_CRASH_NOTIFY_INTERVAL = 3 * AVERAGE_NETWORK_LATENCY_MAX;
	
	

	
	
	
	
	
	private static ArrayList<Option> opts; 
	private Options(){init();}
	private static Options instance = new Options();
	
	public void init(){
		opts = new ArrayList<Option>();
		opts.add(new Option(Options.NUM_OF_NODES, "-n", true, DEFAULT_NUM_OF_NODES, "network-size"));
		opts.add(new Option(Options.LOG, "-log", false, DEFAULT_LOG, "enable-logging"));
		opts.add(new Option(Options.MAX_WAIT, "-w", true, DEFAULT_MAX_WAIT, "max-wait"));
		opts.add(new Option(Options.ACTIVITY_LEVEL, "-l", true, DEFAULT_ACTIVITY_LEVEL, "activity-level"));
		opts.add(new Option(Options.USAGE, "-h", false, DEFAULT_USAGE, "print-usage"));
		opts.add(new Option(Options.VERBOSE, "-v", false, DEFAULT_VERBOSE, "verbose"));
		opts.add(new Option(Options.CSV, "-csv", false, DEFAULT_CSV, "write-to-csv"));
		opts.add(new Option(Options.FLUSH_CSV, "-f", false, DEFAULT_CSV_FLUSH, "flush-csv"));
		opts.add(new Option(Options.VERSION, "-ver", true, DEFAULT_VERSION, "version"));
		opts.add(new Option(Options.CRASHING_NODES, "-c", true, DEFAULT_CRASHED_NODES, "crashing-nodes"));
		opts.add(new Option(Options.PROB_DISTRIBUTION, "-dist", false, DEFAULT_PROB_DISTRIBUTION, "probability-distr"));
		opts.add(new Option(Options.ACTIVITY_STRATEGY, "-strategy", false, DEFAULT_ACTIVITY_STRATEGY, "activity-strategy"));
		opts.add(new Option(Options.BASIC_ALGO_TYPE, "-batype", false, DEFAULT_BASIC_ALGO_TYPE, "basic-algo-type"));
		opts.add(new Option(Options.CRASHING_NODES_INTERVAL, "-ci", false, DEFAULT_CRASHED_NODES_INTERVAL, "crashing-nodes-interval"));
		opts.add(new Option(Options.AVERAGE_NETWORK_LATENCY, "-anl", false, DEFAULT_AVERAGE_NETWORK_LATENCY, "average-network-latency"));
		opts.add(new Option(Options.CRASH_NOTIFY_INTERVAL, "-cni", false, DEFAULT_CRASH_NOTIFY_INTERVAL, "crash-notify-interval"));
	}
	
	public static Options instance(){
		return instance;
	}
	
	public static  void printOptions(){
		for(Option opt: opts)
			System.out.println(opt.toString());
	}
	
	public int get(int option){
		for(Option opt: opts)
			if(opt.getId() == option)
				return opt.value();
		return OPTION_ERR;
	}
	
	private Option getOptByName(String name){
		for(Option opt: opts)
			if(opt.getName().equals(name))
				return opt;
		return null;
	}
	
	public ArrayList<Option> getAll() {
	    return instance.opts;
	}
	
	private void rangeCheck(int option, int value){
		if(option == Options.NUM_OF_NODES){
			if(value < Options.DEFAULT_NUM_OF_NODES || value > Options.MAXIMUM_NUM_OF_NODES){
				System.out.println("Number of nodes should be between " + Options.DEFAULT_NUM_OF_NODES + " and " + Options.MAXIMUM_NUM_OF_NODES );
				System.exit(1);
			}
		}
		if(option == Options.ACTIVITY_LEVEL){
			if(value < Options.DEFAULT_ACTIVITY_LEVEL || value > Options.MAXIMUM_ACTIVITY_LEVEL){
				System.out.println("Activity level should be between " + Options.DEFAULT_ACTIVITY_LEVEL +" and " + Options.MAXIMUM_ACTIVITY_LEVEL);
				System.exit(1);
			}
		}
		if(option == Options.MAX_WAIT){
			if(value <= 0){
				System.out.println("Wait time cannot be <= 0");
				System.exit(1);
			}
		}
		if(option == Options.VERSION){
			if(!contains(Options.ALLOWED_VERSIONS, value)){ //this is working but bad -> fix
				System.out.println("Use version 1,2 or 3, any combination of them, or leave empty to run all 3 versions in parallel");
				System.exit(1);
			}
		}
		if(option == Options.CRASHING_NODES){
		    if((value < 0 && value != Options.CRASHING_NODES_RANDOM) || value > Options.instance().get(Options.NUM_OF_NODES) - 1){
		        System.out.println("Cannot crash less than 0 or more than n-1 nodes");
                System.exit(1);
		    }
		        
		}
		if(option == Options.PROB_DISTRIBUTION) {
		    if(value != Options.PROB_DISTRIBUTION_GAUSSIAN && value != Options.PROB_DISTRIBUTION_UNIFORM) {
		        System.out.println("Use 1 (Uniform) or 2(Gaussian) distribution.");
		        System.exit(1);
		    }
		}
		if(option == Options.ACTIVITY_STRATEGY) {
		    if(value != Options.ACTIVITY_STRATEGY_N_ACTIVITIES && value != Options.ACTIVITY_STRATEGY_COMPUTE_SEND) {
		        System.out.println("Use 1 (sleep-send) or 2 (n activities) for activity strategy");
		        System.exit(1);
		    }
		}
		if(option == Options.BASIC_ALGO_TYPE) {
		    if(value != BASIC_ALGO_CENTRALIZED && value != BASIC_ALGO_DECENTRALIZED_EVEN && value != BASIC_ALGO_DECENTRALIZED_RANDOM) {
		        System.out.println("Use 1 (centralize) or 2 (decentralized) for basic algorithm type");
		        System.exit(1);
		    }
		}
		if(option == Options.CRASHING_NODES_INTERVAL) {
		    if(value != CRASHING_NODES_INTERVAL_GAUSSIAN && value != CRASHING_NODES_INTERVAL_UNIFORM && (value < CRASHING_NODES_INTERVAL_MIN || value > CRASHING_NODES_INTERVAL_MAX)){
	              System.out.println("Use 1 (uniform), 2 (gaussian) or " + CRASHING_NODES_INTERVAL_MIN + " <= i <= " + CRASHING_NODES_INTERVAL_MAX +"(fixed) for crashing nodes interval");
	              System.exit(1);
		    }
		}
		if(option == Options.AVERAGE_NETWORK_LATENCY) {
		    if(value < AVERAGE_NETWORK_LATENCY_MIN || value > AVERAGE_NETWORK_LATENCY_MAX) {
		        System.out.println("Use " + AVERAGE_NETWORK_LATENCY_MIN + " <= l <= " + AVERAGE_NETWORK_LATENCY_MAX + " for average network latency");
		        System.exit(1);
		    }
		}
		
	}
	
	private boolean contains(int[] array, int val){
	    return IntStream.of(array).anyMatch(x -> x == val);
	}
	
	private void setOption(int option, int value){
		rangeCheck(option, value);
		if(option == Options.NUM_OF_NODES)
			getOptByName("-n").setValue(value);
		
		if(option == Options.ACTIVITY_LEVEL)
			getOptByName("-l").setValue(value);
		
		if(option == Options.MAX_WAIT)
			getOptByName("-w").setValue(value);
		
		if(option == Options.LOG)
			getOptByName("-log").setValue(value);
		
		if(option == Options.USAGE)
			getOptByName("-h").setValue(value);
		
		if(option == Options.VERBOSE)
			getOptByName("-v").setValue(value);
		
		if(option == Options.CSV)
			getOptByName("-csv").setValue(value);
		
		if(option == Options.FLUSH_CSV)
			getOptByName("-f").setValue(value);
		if(option == Options.VERSION)
			getOptByName("-ver").setValue(value);
		if(option == Options.CRASHING_NODES){
		    getOptByName("-c").setValue(value);
		}
		if(option == Options.PROB_DISTRIBUTION) {
		    getOptByName("-dist").setValue(value);
		}
		if(option == Options.ACTIVITY_STRATEGY) {
		    getOptByName("-strategy").setValue(value);
		}
		if(option == Options.BASIC_ALGO_TYPE) {
		    getOptByName("-batype").setValue(value);
		}
		if(option == Options.CRASHING_NODES_INTERVAL){
		    getOptByName("-ci").setValue(value);
		}
		if(option == Options.AVERAGE_NETWORK_LATENCY) {
		    getOptByName("-anl").setValue(value);
		}
		
	}
	
	private int parseOptionName(String opt){
		if(opt.equals("-n"))
			return Options.NUM_OF_NODES;
		if(opt.equals("-log"))
			return Options.LOG;
		if(opt.equals("-w"))
			return Options.MAX_WAIT;
		if(opt.equals("-l"))
			return Options.ACTIVITY_LEVEL;
		if(opt.equals("-h"))
			return Options.USAGE;
		if(opt.equals("-v"))
			return Options.VERBOSE;
		if(opt.equals("-csv"))
			return Options.CSV;	
		if(opt.equals("-f"))
			return Options.FLUSH_CSV;
		if(opt.equals("-ver"))
			return Options.VERSION;
		if(opt.equals("-c"))
		    return Options.CRASHING_NODES;
		if(opt.equals("-dist"))
		    return Options.PROB_DISTRIBUTION;
		if(opt.equals("-strategy"))
		    return Options.ACTIVITY_STRATEGY;
		if(opt.equals("-batype"))
		    return Options.BASIC_ALGO_TYPE;
		if(opt.equals("-ci"))
		    return Options.CRASHING_NODES_INTERVAL;
		if(opt.equals("-anl"))
		    return Options.AVERAGE_NETWORK_LATENCY;
		throw new IllegalArgumentException("Invalid option: '" + opt + "'");	
	}

	public void parse(String[] args){
		for(int i = 0; i < args.length; i++){
			int opt = 0;
			try{
				opt = parseOptionName(args[i]);
			}catch(IllegalArgumentException e){
				System.out.println(e.getMessage());
				System.out.println("Use -h for usage");
				System.exit(1);
			}
			switch(opt){
				case Options.LOG:
					this.setOption(Options.LOG, 1); break;
				case Options.CSV:
					this.setOption(Options.CSV, 1);break;
				case Options.FLUSH_CSV:
					this.setOption(Options.FLUSH_CSV, 1); break;
				case Options.USAGE:
					this.setOption(Options.USAGE, 1);
					printUsage();
					if(args.length == 1) System.exit(1);break;
				case Options.VERBOSE:
					this.setOption(Options.VERBOSE, 1); break;
				case Options.NUM_OF_NODES:
					try{
						this.setOption(Options.NUM_OF_NODES, Integer.parseInt(args[i+1]));
					}catch(Exception e){
						System.out.println("Invalid value for option '-n'");
						System.exit(1);
					}
					i++;
					break;
				case Options.MAX_WAIT:
					try{
						this.setOption(Options.MAX_WAIT, Integer.parseInt(args[i+1]));
					}catch(Exception e){
						System.out.println("Invalid value for option '-w'");
						System.exit(1);
					}
					i++;
					break;
				case Options.ACTIVITY_LEVEL:
					try{
						this.setOption(Options.ACTIVITY_LEVEL, Integer.parseInt(args[i+1]));
					}catch(Exception e){
						System.out.println("Invalid value for option '-l'");
						System.exit(1);
					}
					
					i++;
					break;
				case Options.VERSION:
					try{
						this.setOption(Options.VERSION, Integer.parseInt(args[i+1]));
					}catch(Exception e){
						System.out.println("Invalid value for option '-ver'");
						System.exit(1);
					}
					i++;
					break;
				case Options.CRASHING_NODES:
				    try{
				        this.setOption(Options.CRASHING_NODES, Integer.parseInt(args[i+1]));
				        i++;
				    }catch(Exception e){
				        //-c flag without arg
				        this.setOption(Options.CRASHING_NODES, Options.CRASHING_NODES_RANDOM);
				    }
				    
				    break;
				case Options.PROB_DISTRIBUTION:
				    try {
				        this.setOption(Options.PROB_DISTRIBUTION, Integer.parseInt(args[i+1]));
				    }catch(Exception e) {
				        System.out.println("Invalid value for '-dist'");
				        System.exit(1);
				    }
				    i++;
				    break;
				case Options.ACTIVITY_STRATEGY:
				    try {
				        this.setOption(Options.ACTIVITY_STRATEGY, Integer.parseInt(args[i+1]));
				    }catch(Exception e) {
				        System.out.println("Invalid value for '-strategy'");
				        System.exit(1);
				    }
				    i++;
				    break;
                case Options.BASIC_ALGO_TYPE:
                    try {
                        this.setOption(Options.BASIC_ALGO_TYPE, Integer.parseInt(args[i+1]));
                    }catch(Exception e) {
                        System.out.println("Invalid value for '-batype'");
                        System.exit(1);
                    }
                    i++;
                    break;
                case Options.CRASHING_NODES_INTERVAL:
                    try {
                        this.setOption(Options.CRASHING_NODES_INTERVAL,  Integer.parseInt(args[i+1]));
                    } catch(Exception e) {
                        System.out.println("Invalid value for '-ci'");
                        System.exit(1);
                    }
                    i++;
                    break;
                case Options.AVERAGE_NETWORK_LATENCY:
                    try {
                        this.setOption(Options.AVERAGE_NETWORK_LATENCY, Integer.parseInt(args[i+1]));
                    } catch(Exception e) {
                        System.out.println("Invalid value for '-avl'");
                        System.exit(1);
                    }
                    i++;
                    break;
			}
			
		}
	}
	
	
	private void printUsage(){
		System.out.println("USAGE: Todo!!!");
	}
	
	
}
