package util;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class Options {
	
	public static final int NUM_OF_NODES = -1;
	public static final int LOG = -2;
	public static final int ACTIVITY_LEVEL = -3;
	public static final int MAX_WAIT = -4;
	public static final int USAGE = -5;
	public static final int VERBOSE = -6;
	public static final int CSV = -7;
	public static final int FLUSH_CSV = -8;
	public static final int VERSION = -9;
	public static final int CRASHED_NODES = -10;
	public static final int PROB_DISTRIBUTION = -11;
	
	public static final int PROB_DISTRIBUTION_UNIFORM = 1;
	public static final int PROB_DISTRIBUTION_GAUSSIAN = 2;
	
	public static final int DEFAULT_NUM_OF_NODES = 4;
	public static final int MAXIMUM_NUM_OF_NODES = 1024;
	public static final int DEFAULT_ACTIVITY_LEVEL = 1;
	public static final int MAXIMUM_ACTIVITY_LEVEL = 2;
	public static final int DEFAULT_MAX_WAIT = 100000;
	public static final int DEFAULT_LOG = 0;
	public static final int DEFAULT_VERBOSE = 0;
	public static final int DEFAULT_USAGE = 0;
	public static final int DEFAULT_CSV = 0;
	public static final int DEFAULT_CSV_FLUSH = 0;
	public static final int DEFAULT_VERSION = 0;
	private static final int[] ALLOWED_VERSIONS = {1,2,3, 12, 13, 21, 31, 23, 32, 123, 132, 213, 231, 312, 321};
	public static final int DEFAULT_CRUSHED_NODES = 0;
	public static final int DEFAULT_PROB_DISTRIBUTION = PROB_DISTRIBUTION_UNIFORM;
	
	
	
	
	
	private static ArrayList<Option> opts; 
	private Options(){init();}
	public static Options instance = new Options();
	
	public void init(){
		opts = new ArrayList<Option>();
		opts.add(new Option(Options.NUM_OF_NODES, "-n", true, DEFAULT_NUM_OF_NODES));
		opts.add(new Option(Options.LOG, "-log", false, DEFAULT_LOG));
		opts.add(new Option(Options.MAX_WAIT, "-w", true, DEFAULT_MAX_WAIT));
		opts.add(new Option(Options.ACTIVITY_LEVEL, "-l", true, DEFAULT_ACTIVITY_LEVEL));
		opts.add(new Option(Options.USAGE, "-h", false, DEFAULT_USAGE));
		opts.add(new Option(Options.VERBOSE, "-v", false, DEFAULT_VERBOSE));
		opts.add(new Option(Options.CSV, "-csv", false, DEFAULT_CSV));
		opts.add(new Option(Options.FLUSH_CSV, "-f", false, DEFAULT_CSV_FLUSH));
		opts.add(new Option(Options.VERSION, "-ver", true, DEFAULT_VERSION));
		opts.add(new Option(Options.CRASHED_NODES, "-c", true, DEFAULT_CRUSHED_NODES));
		opts.add(new Option(Options.PROB_DISTRIBUTION, "-dist", false, DEFAULT_PROB_DISTRIBUTION));
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
		return -10;
	}
	
	private Option getOptByName(String name){
		for(Option opt: opts)
			if(opt.getName().equals(name))
				return opt;
		return null;
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
		if(option == Options.CRASHED_NODES){
		    if(value < 0 || value > Options.instance().get(Options.NUM_OF_NODES) - 1){
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
		if(option == Options.CRASHED_NODES){
		    getOptByName("-c").setValue(value);
		}
		if(option == Options.PROB_DISTRIBUTION) {
		    getOptByName("-dist").setValue(value);
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
		    return Options.CRASHED_NODES;
		if(opt.equals("-dist"))
		    return Options.PROB_DISTRIBUTION;
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
				case Options.CRASHED_NODES:
				    try{
				        this.setOption(Options.CRASHED_NODES, Integer.parseInt(args[i+1]));
				    }catch(Exception e){
				        System.out.println("Invalid value for option '-c'");
				        System.exit(1);
				    }
				    i++;
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
			}
			
		}
	}
	
	
	private void printUsage(){
		System.out.println("USAGE: Todo!!!");
	}
	
	
}
