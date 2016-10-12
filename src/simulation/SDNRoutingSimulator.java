package simulation;

import graph.Node;

import java.util.ArrayList;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;

import algs.basicrate.ApproSplittableSpecialBR;
import algs.basicrate.ApproUnSplittableSpecialBR;
import algs.basicrate.Greedy;
import algs.basicrate.OptimalBR;
import system.InternetLink;
import system.Request;
import system.Switch;
import utils.IdAllocator;

public class SDNRoutingSimulator {
	
	public static IdAllocator idAllocator = new IdAllocator();
	
	private SimpleWeightedGraph<Node, InternetLink> network;

	private ArrayList<Switch> switchesAttachedDataCenters = null;
	
	private ArrayList<Switch> switches = null;
	
	private ArrayList<Request> multicastRequests = null;
	
	private ArrayList<Request> unicastRequests = null;
		
	private FloydWarshallShortestPaths<Node, InternetLink> allPairShortestPath = null;
	
	public SDNRoutingSimulator() {
		this.setSwitches(new ArrayList<Switch>());
		this.setSwitchesAttachedDataCenters(new ArrayList<Switch>());
		this.setMulticastRequests(new ArrayList<Request>());
		this.setUnicastRequests(new ArrayList<Request>());
	}
		
	public static void main(String[] args) {
		
		String alg = args[0];
		String network = args[1];
		String evaType = args[2]; 
		
		if (evaType.equals("PER")){
			if (alg.equals("OPT")){
				switch (network) {
				case "ALL":
					performanceOptimalNetworkSizesBR();
					break; 
				case "GEANT":
					performanceOptimalNumReqsBR("GEANT");
					break; 
				case "AS4755":
					performanceOptimalNumReqsBR("AS4755");
					break;
				case "AS1755":
					performanceOptimalNumReqsBR("AS1755");
					break;
				 default:
			          System.out.println("Unknown argument: " + evaType + " " + alg + " " + network);
			          System.exit(1);
				}
			
			} else if (alg.equals("APP-S")){
				switch (network) {
				case "ALL":
					performanceApproSplittableNetworkSizesBR();
					break; 
				case "GEANT":
					performanceApproSplittableNumReqsBR("GEANT");
					break; 
				case "AS4755":
					performanceApproSplittableNumReqsBR("AS4755");
					break;
				case "AS1755":
					performanceApproSplittableNumReqsBR("AS1755");
					break;
				 default:
			          System.out.println("Unknown argument: " + evaType + " " + alg + " " + network);
			          System.exit(1);
				}
			} else if (alg.equals("APP-US")){
				switch (network) {
				case "ALL":
					performanceApproUnSplittableNetworkSizesBR();
					break; 
				case "GEANT":
					performanceApproUnSplittableNumReqsBR("GEANT");
					break; 
				case "AS4755":
					performanceApproUnSplittableNumReqsBR("AS4755");
					break;
				case "AS1755":
					performanceApproUnSplittableNumReqsBR("AS1755");
					break;
				 default:
			          System.out.println("Unknown argument: " + evaType + " " + alg + " " + network);
			          System.exit(1);
				}
			}
		
		} else if (evaType.equals("IMPACT-RATIO")) {
			switch (alg) {
			case "OPT":
				impactOfSwitchToDCRatioOptimalBR();
				break; 
			case "APP-S":
				impactOfSwitchToDCRatioSplittableBR();
				break; 
			case "APP-US":
				impactOfSwitchToDCRatioUnSplittableBR();
				break;
			 default:
		          System.out.println("Unknown argument: " + evaType + " " + alg + " " + network);
		          System.exit(1);
			}
		} else if (evaType.equals("IMPACT-RHO")) {
			switch (alg) {
			case "OPT":
				impactOfMinRhoOptimalBR();
				break; 
			case "APP-S":
				impactOfMinRhoSplittableBR();
				break; 
			case "APP-US":
				impactOfMinRhoUnSplittableBR();
				break;
			 default:
		          System.out.println("Unknown argument: " + evaType + " " + alg + " " + network);
		          System.exit(1);
			}
		}		
		// first set of experiments. 
		//performanceOptimalNetworkSizesBR();
		//performanceOptimalNumReqsBR("GEANT");
		//performanceOptimalNumReqsBR("AS4755");
		//performanceOptimalNumReqsBR("AS1755");
		
		// second set of experiments. 
		//performanceApproSplittableNetworkSizesBR();
		//performanceApproSplittableNumReqsBR("GEANT");
		//performanceApproSplittableNumReqsBR("AS4755");
		//performanceApproSplittableNumReqsBR("AS1755");
		
		// third set of experiments.
		//performanceApproUnSplittableNetworkSizesBR();
		//performanceApproUnSplittableNumReqsBR("GEANT");
		//performanceApproUnSplittableNumReqsBR("AS4755");
		//performanceApproUnSplittableNumReqsBR("AS1755");
		
		// fourth set of experiments
		//impactOfSwitchToDCRatioOptimalBR();
		//impactOfSwitchToDCRatioSplittableBR();
		//impactOfSwitchToDCRatioUnSplittableBR();
		
		// fifth set of experiments
		//impactOfMinRhoOptimalBR();
		//impactOfMinRhoSplittableBR();
		//impactOfMinRhoUnSplittableBR();
		
		
		//performanceApproUnSplittableBRNumReqs("GEANT");
		//performanceHeuristicNumReqs("GEANT");
		//performanceHeuristicNumReqs("AS1755");
		//performanceHeuristicNumReqs("AS4755");
	}
	
	public static void performanceOptimalNetworkSizesBR() {
		
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		int [] networkSizes = {50, 100, 150, 200, 250};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [networkSizes.length][numAlgs];
		double [][] aveRunningTime = new double [networkSizes.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [networkSizes.length][numAlgs];
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		int numRound = 5;
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {			
			System.out.println("Number of nodes: " + networkSizes[sizeI]);
			Parameters.numOfNodes = networkSizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, true);
				
				// optimal solution for the problem with identical data rates. 
				OptimalBR optimalAlg = new OptimalBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				optimalAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (optimalAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (optimalAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void performanceOptimalNumReqsBR(String networkName) {
		
		//int [] numOfReqs = {350, 400, 450, 500, 550, 600};
		int [] numOfReqs = {400, 450, 500, 1000, 1500, 2000};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [numOfReqs.length][numAlgs];
		double [][] aveRunningTime = new double [numOfReqs.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [numOfReqs.length][numAlgs];
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		int numRound = 5;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);

		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			
			System.out.println("Number of requests in R(t): " + numOfReqs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			Parameters.numReqs = numOfReqs[sizeI];
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				Initialization.initUnicastRequests(simulator, false, true, true);
				
				// optimal solution for the problem with identical data rates. 
				OptimalBR optimalAlg = new OptimalBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				optimalAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (optimalAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (optimalAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void performanceApproSplittableNetworkSizesBR() {
		
		int [] networkSizes = {50, 100, 150, 200, 250};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [networkSizes.length][numAlgs];
		double [][] aveRunningTime = new double [networkSizes.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [networkSizes.length][numAlgs];
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		Parameters.numReqs = 500; 
		
		int numRound = 5;
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {			
			System.out.println("Number of nodes: " + networkSizes[sizeI]);
			Parameters.numOfNodes = networkSizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, false);
				
				ApproSplittableSpecialBR approAlg = new ApproSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (approAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	
	public static void performanceApproSplittableNumReqsBR(String networkName) {
		
		//int [] numOfReqs = {150, 200, 250, 300, 350};
//		int [] numOfReqs = {500, 600, 700, 800, 900, 1000};
		//int [] numOfReqs = {1500, 1700, 1900, 2100};
		int [] numOfReqs = {400, 450, 500, 1000, 1500, 2000};

		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [numOfReqs.length][numAlgs];
		double [][] aveRunningTime = new double [numOfReqs.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [numOfReqs.length][numAlgs];
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		Parameters.numReqs = 500; 
		
		int numRound = 5;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			System.out.println("Number of requests in R(t): " + numOfReqs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			Parameters.numReqs = numOfReqs[sizeI];
			
			for (int round = 0; round < numRound; round ++) {
				System.out.println("Round : " + round);
				Initialization.initUnicastRequests(simulator, false, true, false);
				
				// optimal solution for the problem with identical data rates. 
				ApproSplittableSpecialBR approAlg = new ApproSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void performanceApproUnSplittableNetworkSizesBR() {
		
		int [] networkSizes = {100, 150, 200, 250};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [networkSizes.length][numAlgs];
		double [][] aveRunningTime = new double [networkSizes.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [networkSizes.length][numAlgs];
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		Parameters.numReqs = 500; 
		
		int numRound = 5;
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {		
			System.out.println("Number of nodes: " + networkSizes[sizeI]);
			Parameters.numOfNodes = networkSizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, false);
				
				ApproUnSplittableSpecialBR approAlg = new ApproUnSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (approAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void performanceApproUnSplittableNumReqsBR(String networkName) {
		
		//int [] numOfReqs = {150, 200, 250, 300, 350};
//		int [] numOfReqs = {500, 600, 700, 800, 900, 1000};
		//int [] numOfReqs = {1500, 1700, 1900, 2100};
		int [] numOfReqs = {400, 450, 500, 1000, 1500, 2000};
		
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [numOfReqs.length][numAlgs];
		double [][] aveRunningTime = new double [numOfReqs.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [numOfReqs.length][numAlgs];
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		
		
		int numRound = 5;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
			Parameters.numReqs = 300; 
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
			Parameters.numReqs = 500; 
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
			Parameters.numReqs = 500; 
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			System.out.println("Number of requests in R(t): " + numOfReqs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			Parameters.numReqs = numOfReqs[sizeI];
			
			for (int round = 0; round < numRound; round ++) {
				System.out.println("Round : " + round);
				Initialization.initUnicastRequests(simulator, false, true, false);
				
				// optimal solution for the problem with identical data rates. 
				ApproUnSplittableSpecialBR approAlg = new ApproUnSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
			}
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void impactOfSwitchToDCRatioOptimalBR() {
		
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		double [] switchToDCRatios = {5, 6, 7, 8, 9, 10};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [switchToDCRatios.length][numAlgs];
		double [][] aveRunningTime = new double [switchToDCRatios.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [switchToDCRatios.length][numAlgs];
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		int numRound = 3;
		
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			
			System.out.println("V/DC: " + switchToDCRatios[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.ServerToNodeRatio = 1 / switchToDCRatios[sizeI];					
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);

			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, true);
				
				// optimal solution for the problem with identical data rates. 
				OptimalBR optimalAlg = new OptimalBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				optimalAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (optimalAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (optimalAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}

	
	public static void impactOfSwitchToDCRatioSplittableBR() {
		
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		double [] switchToDCRatios = {5, 6, 7, 8, 9, 10};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [switchToDCRatios.length][numAlgs];
		double [][] aveRunningTime = new double [switchToDCRatios.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [switchToDCRatios.length][numAlgs];
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		int numRound = 3;
		
		Parameters.numReqs = 500; 
		
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			
			System.out.println("V/DC: " + switchToDCRatios[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.ServerToNodeRatio = 1 / switchToDCRatios[sizeI];					
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);

			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, false);
				
				// optimal solution for the problem with identical data rates. 
				ApproSplittableSpecialBR approAlg = new ApproSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (approAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}

	public static void impactOfSwitchToDCRatioUnSplittableBR() {
		
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		double [] switchToDCRatios = {5, 6, 7, 8, 9, 10};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [switchToDCRatios.length][numAlgs];
		double [][] aveRunningTime = new double [switchToDCRatios.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [switchToDCRatios.length][numAlgs];
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		Parameters.numReqs = 500; 
		
		int numRound = 3;
		
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			
			System.out.println("V/DC: " + switchToDCRatios[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.ServerToNodeRatio = 1 / switchToDCRatios[sizeI];					
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);

			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, false);
				
				// optimal solution for the problem with identical data rates. 
				ApproUnSplittableSpecialBR approAlg = new ApproUnSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (approAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void impactOfMinRhoOptimalBR() {
		
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		double [] minRhos = {4, 6, 8, 10};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [minRhos.length][numAlgs];
		double [][] aveRunningTime = new double [minRhos.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [minRhos.length][numAlgs];
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		int numRound = 3;
		
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			
			System.out.println("Rho_Max/Rho_Min: " + minRhos[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.minPacketRate = Parameters.maxPacketRate / minRhos[sizeI]; 
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, true);
				
				// optimal solution for the problem with identical data rates. 
				OptimalBR optAlg = new OptimalBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				optAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (optAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (optAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}

	
	public static void impactOfMinRhoSplittableBR() {
		
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		double [] minRhos = {4, 6, 8, 10};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [minRhos.length][numAlgs];
		double [][] aveRunningTime = new double [minRhos.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [minRhos.length][numAlgs];
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		int numRound = 3;
		
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			
			System.out.println("Rho_Max/Rho_Min: " + minRhos[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.minPacketRate = Parameters.maxPacketRate / minRhos[sizeI]; 
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, false);
				
				// optimal solution for the problem with identical data rates. 
				ApproSplittableSpecialBR approAlg = new ApproSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (approAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	
	public static void impactOfMinRhoUnSplittableBR() {
		
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		double [] minRhos = {4, 6, 8, 10};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [minRhos.length][numAlgs];
		double [][] aveRunningTime = new double [minRhos.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [minRhos.length][numAlgs];
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		int numRound = 3;
		
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			
			System.out.println("Rho_Max/Rho_Min: " + minRhos[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.minPacketRate = Parameters.maxPacketRate / minRhos[sizeI]; 
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, false);
				
				// optimal solution for the problem with identical data rates. 
				ApproUnSplittableSpecialBR approAlg = new ApproUnSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (approAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Greedy greedyAlg = new Greedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}

	
	
	
	public SimpleWeightedGraph<Node, InternetLink> getNetwork() {
		return network;
	}

	public void setNetwork(SimpleWeightedGraph<Node, InternetLink> network) {
		this.network = network;
	}
	
	public double getShortestPath(Node n1, Node n2) {
		
		if (n1.equals(n2))
			return 0d;
		
		if (null == this.allPairShortestPath){
			this.allPairShortestPath = new FloydWarshallShortestPaths<Node, InternetLink>(this.getNetwork());
		}
		double shortestPathLength = this.allPairShortestPath.shortestDistance(n1, n2);
		return shortestPathLength;
	}

	public ArrayList<Switch> getSwitches() {
		return switches;
	}

	public void setSwitches(ArrayList<Switch> switches) {
		this.switches = switches;
	}

	public ArrayList<Request> getMulticastRequests() {
		return multicastRequests;
	}

	public void setMulticastRequests(ArrayList<Request> multicastRequests) {
		this.multicastRequests = multicastRequests;
	}

	public ArrayList<Switch> getSwitchesAttachedDataCenters() {
		return switchesAttachedDataCenters;
	}

	public void setSwitchesAttachedDataCenters(ArrayList<Switch> switchesAttachedDataCenters) {
		this.switchesAttachedDataCenters = switchesAttachedDataCenters;
	}

	public ArrayList<Request> getUnicastRequests() {
		return unicastRequests;
	}

	public void setUnicastRequests(ArrayList<Request> unicastRequests) {
		this.unicastRequests = unicastRequests;
	}
}
