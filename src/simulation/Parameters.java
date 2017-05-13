package simulation;

public class Parameters {
		
	/*********************************Networks************************************/
	public static int numOfNodes = 100;
		
	public static double maxLinkCost = 0.12 / 16384;// to be reset
	public static double minLinkCost = 0.05 / 16384;// to be reset
	
	public static double maxLinkDelay = 5; //ms
	public static double minLinkDelay = 2; //ms
	
	/*********************************Data Center************************************/
	public static double ServerToNodeRatio = 0.1;//0.1
	
	public static int K = (int) (numOfNodes * ServerToNodeRatio);
	
	public static int K_small = 9; // in geant toplogy.
	
	//public static int maxServersForEachSC = 3; // may vary from 2, 4, 6, 8
	
	/*********************************Requests************************************/
	public static int numReqs = 2000; //20000;
	
	public static int minDelayRequirement = 5; //ms
	public static int maxDelayRequirement = 100; //100 ms
	
	public static double minPacketRate = 400; // packets per second, also the basic packet rate. 
	public static double maxPacketRate = 4000;
	
	public static double maxDestinationPercentage = 0.2;
	public static double minDestinationPercentage = 0.04;
	public static int maxNumDestinationsPerRequest = (int) (maxDestinationPercentage * numOfNodes);
	public static int minNumDestinationsPerRequest = (int) (maxDestinationPercentage * numOfNodes);
	
	/*********************************Service chains************************************/
	//public static int numOfServiceChainTypes = 10;
	
	public static double minServiceChainCost = 0.22 / 16384;// to be reset
	public static double maxServiceChainCost = 0.15 / 16384;// to be reset
	
	public static double [][] serviceChainProcessingDelays = {{0.045, 0.3}, {0.09, 0.6}, {0.135, 0.9}, {0.18, 1.2}, {0.225, 1.5}};// ms
	
	public static double [][] serviceChainProcessingCapacities = {{3000, 20000}, {1500, 10000}, {1100, 5500}, {800, 5000}, {500, 4000}};// packets per second. to be reset. 
		
	public static int maxNumOfInstances = 50;
	public static int minNumOfInstances = 1;
	
	public static String LPOutputFile = ".//logs//lp_solver_log.txt";
}
