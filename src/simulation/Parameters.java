package simulation;

public class Parameters {
		
	/*********************************Networks************************************/
	public static int numOfNodes = 100;
	
	public static double maxLinkCapacity = 10000;// 10000 Mbps
	public static double minLinkCapacity = 1000; // 1000 Mbps
	
	public static double maxLinkCost = 1;// to be reset
	public static double minLinkCost = 1;// to be reset
	
	public static double maxLinkDelay = 5; //ms to be reset
	public static double minLinkDelay = 1; //ms to be reset. 
	
	/*********************************Server************************************/
	public static double ServerToNodeRatio = 0.1;//0.1
	
	public static int K = (int) (numOfNodes * ServerToNodeRatio);
	
	public static int K_small = 9; // in geant toplogy. 
	
	public static int maxServersForEachSC = 3; // may vary from 2, 4, 6, 8
	
	/*********************************Requests************************************/
	public static int numReqs = 1000; //20000;
	
	public static int minDelayRequirement = 10; //ms
	public static int maxDelayRequirement = 50; //ms
	
	public static double minDataRate = 50; // packets per second
	public static double maxDataRate = 200;
	
	public static double maxDestinationPercentage = 0.2;
	public static double minDestinationPercentage = 0.04;
	public static int maxNumDestinationsPerRequest = (int) (maxDestinationPercentage * numOfNodes);
	public static int minNumDestinationsPerRequest = (int) (maxDestinationPercentage * numOfNodes);	
	
	/*********************************Service chains************************************/
	//public static int numOfServiceChainTypes = 10;
	
	public static double minServiceChainCost = 0.1;// to be reset
	public static double maxServiceChainCost = 1;// to be reset
	
	public static double [][] serviceChainProcessingDelays = {{2, 3}, {4, 5}, {6, 9}, {10, 12}, {12, 15}};// ms
	
	public static double [][] serviceChainProcessingCapacities = {{200, 300}, {400, 500}, {600, 900}, {1000, 1200}, {1200, 1500}};// packets per second. to be reset. 
	
	public static int maxNumOfInstances = 100; 
	public static int minNumOfInstances = 10; 
}
