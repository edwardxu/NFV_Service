package simulation;

import graph.Node;

import java.util.ArrayList;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;

//import algs.Heuristic;
import system.InternetLink;
import system.Request;
import system.Switch;
import utils.IdAllocator;

public class SDNRoutingSimulator {
	
	public static IdAllocator idAllocator = new IdAllocator();
	
	private SimpleWeightedGraph<Node, InternetLink> network;

	private ArrayList<Switch> switchesAttachedServers = null;
	
	private ArrayList<Switch> switches = null;
	
	private ArrayList<Request> multicastRequests = null;
		
	private FloydWarshallShortestPaths<Node, InternetLink> allPairShortestPath = null;
	
	public SDNRoutingSimulator() {
		this.setSwitches(new ArrayList<Switch>());
		this.setSwitchesAttachedServers(new ArrayList<Switch>());
		this.setMulticastRequests(new ArrayList<Request>());
	}
		
	public static void main(String[] s) {
		
		//ImpactOfNetworkSizesApproximation(0.2); // (1)
		//ImpactOfNetworkSizesApproximation(0.2);
		
		//performanceApproximation("GEANT"); // (2)
		//performanceApproximationPercentage("GEANT");
		
		//performanceApproximation("AS1755");// (2)
		//performanceApproximationPercentage("AS1755");
		
		//performanceApproximation("AS4755");// (2)
		//performanceApproximationPercentage("AS4755");
		
		//ImpactOfNetworkSizesHeuristic(); // (3)
		//ImpactOfNumReqsHeuristic();
		//performanceHeuristic("GEANT"); // (4)
		performanceHeuristic("AS1755");// (4)
		//performanceHeuristic("AS4755");// (4)
		//performanceHeuristicNumReqs("GEANT");
		//performanceHeuristicNumReqs("AS1755");
		//performanceHeuristicNumReqs("AS4755");
		
		
		//ImpactOfNetworkSizesOnline(); // (5)
		//ImpactOfNumReqsOnline();
		//performanceOnline("GEANT"); // (6)
		//performanceOnline("AS1755");// (6)
		//performanceOnline("AS4755");// (6)
		//performanceOnlineNumReqs("GEANT");
		//performanceOnlineNumReqs("AS1755");
		//performanceOnlineNumReqs("AS4755");
		
		//ImpactOfAlphaOnline(); // (7)
		//ImpactOfBetaOnline();  // (8)
		
		
		
		// unit tests
		//ImpactOfNetworkSizesApproximationBenchmark();
	}
	
	public static void performanceApproximation(String networkName) {
		
		int [] maxNumServers = {2, 3, 4, 5};// make sure total server number is larger than 8. 
		
		double [][] aveCost = new double [maxNumServers.length][4];
		double [][] aveRunningTime = new double [maxNumServers.length][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveCost[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
			}
		}
		
		int numRound = 1;
		
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT"))
			Parameters.numOfNodes = 40;
		else if (networkName.equals("AS1755")){
			Parameters.numOfNodes = 172;
		} else if (networkName.equals("AS4755")){
			Parameters.numOfNodes = 121;
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
		Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);

		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			
			System.out.println("Max number of servers for each SC: " + maxNumServers[sizeI]);
			Parameters.maxServersForEachSC = maxNumServers[sizeI];
			
			Initialization.initServers(simulator);
			Initialization.initEdgeWeights(simulator);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				Initialization.initMulticastRequests(simulator, false);
				
				// approximation algorithm. 
				ApproMulti approAlg = new ApproMulti(simulator);
				long startTime = System.currentTimeMillis();
				approAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveCost[sizeI][0] += (approAlg.getCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// approximation algorithm. 
				ApproximationBenchmark approBenchmarkAlg = new ApproximationBenchmark(simulator);
				startTime = System.currentTimeMillis();
				approBenchmarkAlg.run();				
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveCost[sizeI][1] += (approBenchmarkAlg.getCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			String out = maxNumServers[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveCost[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			String out = maxNumServers[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void performanceApproximationPercentage(String networkName) {
		
		double [] maxDestinationPercentages = {0.05, 0.1, 0.15, 0.2};// make sure total server number is larger than 8. 
		
		double [][] aveCost = new double [maxDestinationPercentages.length][4];
		double [][] aveRunningTime = new double [maxDestinationPercentages.length][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		for (int sizeI = 0; sizeI < maxDestinationPercentages.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveCost[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
			}
		}
		
		int numRound = 1;
		
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT"))
			Parameters.numOfNodes = 40;
		else if (networkName.equals("AS1755")){
			Parameters.numOfNodes = 172;
		} else if (networkName.equals("AS4755")){
			Parameters.numOfNodes = 121;
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		
		for (int sizeI = 0; sizeI < maxDestinationPercentages.length; sizeI ++) {
			
			System.out.println("Max percentages: " + maxDestinationPercentages[sizeI]);
			Parameters.maxDestinationPercentage = maxDestinationPercentages[sizeI];
			Parameters.maxNumDestinationsPerRequest = (int) (Parameters.maxDestinationPercentage * Parameters.numOfNodes);
			Parameters.minNumDestinationsPerRequest = (int) (Parameters.minDestinationPercentage * Parameters.numOfNodes);
			
			Initialization.initServers(simulator);
			Initialization.initEdgeWeights(simulator);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				Initialization.initMulticastRequests(simulator, false);
				
				// approximation algorithm. 
				ApproMulti approAlg = new ApproMulti(simulator);
				long startTime = System.currentTimeMillis();
				approAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveCost[sizeI][0] += (approAlg.getCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// approximation algorithm. 
				ApproximationBenchmark approBenchmarkAlg = new ApproximationBenchmark(simulator);
				startTime = System.currentTimeMillis();
				approBenchmarkAlg.run();				
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveCost[sizeI][1] += (approBenchmarkAlg.getCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < maxDestinationPercentages.length; sizeI ++) {
			String out = maxDestinationPercentages[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveCost[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < maxDestinationPercentages.length; sizeI ++) {
			String out = maxDestinationPercentages[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	
	public static void performanceHeuristic(String networkName) {
		
		int [] maxNumServers = {2, 3, 4, 5};// make sure total server number is larger than 8. 
		
		double [][][] aveCost = new double [maxNumServers.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [maxNumServers.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 5;
		
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT"))
			Parameters.numOfNodes = 40;
		else if (networkName.equals("AS1755")){
			Parameters.numOfNodes = 172;
		} else if (networkName.equals("AS4755")){
			Parameters.numOfNodes = 121;
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
		Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);

		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			
			System.out.println("Max number of servers for each SC: " + maxNumServers[sizeI]);
			Parameters.maxServersForEachSC = maxNumServers[sizeI];
			
			Initialization.initServers(simulator);
			Initialization.initEdgeWeights(simulator);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				OnlineHeu heuAlg = new OnlineHeu(simulator);
				long startTime = System.currentTimeMillis();
				heuAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (heuAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (heuAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// online benchmark;
				
				// online algorithm. 
				OnlineBenchmark heuBenchmarkAlg = new OnlineBenchmark(simulator);
				startTime = System.currentTimeMillis();
				heuBenchmarkAlg.run();
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][1] += (heuBenchmarkAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][1] += (heuBenchmarkAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
//			String out = maxNumServers[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][Parameters.numReqs - 1][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Num reqs admitted--------------------------");
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			String out = maxNumServers[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[sizeI][Parameters.numReqs - 1][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void performanceHeuristicNumReqs(String networkName) {
		
		int [] maxNumServers = {3};// make sure total server number is larger than 8. 
		
		double [][][] aveCost = new double [maxNumServers.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [maxNumServers.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 5;
		
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT"))
			Parameters.numOfNodes = 40;
		else if (networkName.equals("AS1755")){
			Parameters.numOfNodes = 172;
		} else if (networkName.equals("AS4755")){
			Parameters.numOfNodes = 121;
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
		Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);

		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			
			System.out.println("Max number of servers for each SC: " + maxNumServers[sizeI]);
			Parameters.maxServersForEachSC = maxNumServers[sizeI];
			
			Initialization.initServers(simulator);
			Initialization.initEdgeWeights(simulator);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				OnlineHeu heuAlg = new OnlineHeu(simulator);
				long startTime = System.currentTimeMillis();
				heuAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (heuAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (heuAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// online benchmark;
				
				// online algorithm. 
				OnlineBenchmark heuBenchmarkAlg = new OnlineBenchmark(simulator);
				startTime = System.currentTimeMillis();
				heuBenchmarkAlg.run();
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][1] += (heuBenchmarkAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][1] += (heuBenchmarkAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
//			String out = maxNumServers[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][Parameters.numReqs - 1][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Num reqs admitted--------------------------");
		for (int t = 0; t < Parameters.numReqs; t ++) {
			
			if ((t % 50) != 0)
				continue;
			
			String out = t + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[0][t][j] + " ";
			
			System.out.println(out);
		}
	}

	public static void performanceOnlineNumReqs(String networkName) {
		
		int [] maxNumServers = {3};// make sure total server number is larger than 8. 
		
		double [][][] aveCost = new double [maxNumServers.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [maxNumServers.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 5;
		
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT"))
			Parameters.numOfNodes = 40;
		else if (networkName.equals("AS1755")){
			Parameters.numOfNodes = 172;
		} else if (networkName.equals("AS4755")){
			Parameters.numOfNodes = 121;
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
		Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);

		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			
			System.out.println("Max number of servers for each SC: " + maxNumServers[sizeI]);
			Parameters.maxServersForEachSC = maxNumServers[sizeI];
			
			Initialization.initServers(simulator);
			Initialization.initEdgeWeights(simulator);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				Online onlineAlg = new Online(simulator, 1, 1);
				long startTime = System.currentTimeMillis();
				onlineAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (onlineAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (onlineAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// online benchmark;
				
				// online algorithm. 
				OnlineBenchmark heuBenchmarkAlg = new OnlineBenchmark(simulator);
				startTime = System.currentTimeMillis();
				heuBenchmarkAlg.run();
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][1] += (heuBenchmarkAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][1] += (heuBenchmarkAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
//			String out = maxNumServers[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][Parameters.numReqs - 1][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Num reqs admitted--------------------------");
		for (int t = 0; t < Parameters.numReqs; t ++) {
			
			if ((t % 50) != 0)
				continue;
			
			String out = t + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[0][t][j] + " ";
			
			System.out.println(out);
		}
	}

	

	public static void performanceOnline(String networkName) {
		
		int [] maxNumServers = {2, 3, 4, 5};// make sure total server number is larger than 8. 
		
		double [][][] aveCost = new double [maxNumServers.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [maxNumServers.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 5;
		
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT"))
			Parameters.numOfNodes = 40;
		else if (networkName.equals("AS1755")){
			Parameters.numOfNodes = 172;
		} else if (networkName.equals("AS4755")){
			Parameters.numOfNodes = 121;
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
		Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);

		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			
			System.out.println("Max number of servers for each SC: " + maxNumServers[sizeI]);
			Parameters.maxServersForEachSC = maxNumServers[sizeI];
			
			Initialization.initServers(simulator);
			Initialization.initEdgeWeights(simulator);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				Online onlineAlg = new Online(simulator, 1, 1);
				long startTime = System.currentTimeMillis();
				onlineAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (onlineAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (onlineAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// online benchmark;
				OnlineBenchmark heuBenchmarkAlg = new OnlineBenchmark(simulator);
				startTime = System.currentTimeMillis();
				heuBenchmarkAlg.run();				
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][1] += (heuBenchmarkAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][1] += (heuBenchmarkAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
//			String out = maxNumServers[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Num of requests admitted--------------------------");
		for (int sizeI = 0; sizeI < maxNumServers.length; sizeI ++) {
			String out = maxNumServers[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[sizeI][Parameters.numReqs - 1][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void ImpactOfNetworkSizesApproximation(double maxDesPercentage) {
				
		int [] network_sizes = {50, 100, 150, 200};
//		int [] network_sizes = {100};
//		int [] network_sizes = {100};
		
		double [][] aveCost = new double [network_sizes.length][4];
		double [][] aveRunningTime = new double [network_sizes.length][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 3;
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveCost[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
			}
		}
		
		int numRound = 1;
		
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			
			System.out.println("Network size: " + network_sizes[sizeI]);
			
			//changeNumOfNodes(network_sizes[sizeI]);
			Parameters.numOfNodes = network_sizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			Parameters.maxDestinationPercentage = maxDesPercentage;
			
			Parameters.maxNumDestinationsPerRequest = (int) (Parameters.maxDestinationPercentage * Parameters.numOfNodes);
			Parameters.minNumDestinationsPerRequest = (int) (Parameters.minDestinationPercentage * Parameters.numOfNodes);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Topology : " + round);
				
				String netPostFix = "";
				if (round > 0)
					netPostFix = "-" + round;
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, netPostFix);
				Initialization.initServers(simulator);
				Initialization.initEdgeWeights(simulator);
				Initialization.initMulticastRequests(simulator, false);
				
				// approximation algorithm. 
				ApproMulti approAlg = new ApproMulti(simulator);
				long startTime = System.currentTimeMillis();
				approAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveCost[sizeI][0] += (approAlg.getCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// approximation algorithm with capacities. 
				ApproximationCap approAlgCap = new ApproximationCap(simulator);
				startTime = System.currentTimeMillis();
				approAlgCap.run();			
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveCost[sizeI][1] += (approAlgCap.getCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// approximation algorithm. 
				ApproximationBenchmark approBenchmarkAlg = new ApproximationBenchmark(simulator);
				startTime = System.currentTimeMillis();
				approBenchmarkAlg.run();
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveCost[sizeI][2] += (approBenchmarkAlg.getCost() / numRound);					
				aveRunningTime[sizeI][2] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
//				// approximation algorithm. 
//				ApproximationBenchmark2 shortest = new ApproximationBenchmark2(simulator);
//				startTime = System.currentTimeMillis();
//				shortest.run();
//				endTime   = System.currentTimeMillis();
//				totalTime = endTime - startTime;
//				
//				aveCost[sizeI][3] += (shortest.getCost() / numRound);					
//				aveRunningTime[sizeI][3] += (totalTime / numRound);
				
			}
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			String out = network_sizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveCost[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			String out = network_sizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void ImpactOfNetworkSizesOnline() {
		
		int [] network_sizes = {50, 100, 150, 200};
		//int [] network_sizes = {100};
		
		double [][][] aveCost = new double [network_sizes.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 5;
		
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			
			System.out.println("Network size: " + network_sizes[sizeI]);
			
			//changeNumOfNodes(network_sizes[sizeI]);
			Parameters.numOfNodes = network_sizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
			Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Topology : " + round);
				
				String netPostFix = "";
				if (round > 0)
					netPostFix = "-" + round;
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, netPostFix);
				Initialization.initServers(simulator);
				Initialization.initEdgeWeights(simulator);
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				Online onlineAlg = new Online(simulator, 1, 1);
				long startTime = System.currentTimeMillis();
				onlineAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (onlineAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (onlineAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// online benchmark;
				
				OnlineBenchmark onlineBenchmarkAlg = new OnlineBenchmark(simulator);
				startTime = System.currentTimeMillis();
				onlineBenchmarkAlg.run();				
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][1] += (onlineBenchmarkAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][1] += (onlineBenchmarkAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
//			String out = network_sizes[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][Parameters.numReqs - 1][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Num of requests admitted-------------------");
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			String out = network_sizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[sizeI][Parameters.numReqs - 1][j] + " ";
			
			System.out.println(out);
		}
	}
	
	public static void ImpactOfNumReqsOnline() {
		
		//int [] network_sizes = {50, 100, 150, 200};
		int [] network_sizes = {100};
		
		double [][][] aveCost = new double [network_sizes.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 5;
		
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			
			System.out.println("Network size: " + network_sizes[sizeI]);
			
			//changeNumOfNodes(network_sizes[sizeI]);
			Parameters.numOfNodes = network_sizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
			Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Topology : " + round);
				
				String netPostFix = "";
				if (round > 0)
					netPostFix = "-" + round;
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, netPostFix);
				Initialization.initServers(simulator);
				Initialization.initEdgeWeights(simulator);
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				Online onlineAlg = new Online(simulator, 1, 1);
				long startTime = System.currentTimeMillis();
				onlineAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (onlineAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (onlineAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// online benchmark;
				
				OnlineBenchmark onlineBenchmarkAlg = new OnlineBenchmark(simulator);
				startTime = System.currentTimeMillis();
				onlineBenchmarkAlg.run();				
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][1] += (onlineBenchmarkAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][1] += (onlineBenchmarkAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
//			String out = network_sizes[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][Parameters.numReqs - 1][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Num of requests admitted-------------------");
		for (int t = 0; t < Parameters.numReqs; t ++){
			if ( (t % 50) != 0)
				continue;
			String out = t + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[0][t][j] + " ";
			
			System.out.println(out);
		}
	}
	
	
	public static void ImpactOfNetworkSizesHeuristic() {
		
		int [] network_sizes = {50, 100, 150, 200};
		//int [] network_sizes = {50};
		
		double [][][] aveCost = new double [network_sizes.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 2;
		
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			
			System.out.println("Network size: " + network_sizes[sizeI]);
			
			Parameters.numOfNodes = network_sizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
			Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Topology : " + round);
				
				String netPostFix = "";
				if (round > 0)
					netPostFix = "-" + round;
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, netPostFix);
				Initialization.initServers(simulator);
				Initialization.initEdgeWeights(simulator);
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				OnlineHeu heuAlg = new OnlineHeu(simulator);
				long startTime = System.currentTimeMillis();
				heuAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (heuAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (heuAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// online benchmark;
				
				// online algorithm. 
				OnlineBenchmark heuBenchmarkAlg = new OnlineBenchmark(simulator);
				startTime = System.currentTimeMillis();
				heuBenchmarkAlg.run();				
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][1] += (heuBenchmarkAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][1] += (heuBenchmarkAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
//			String out = network_sizes[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][Parameters.numReqs - 1][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Num of requests admitted-------------------");
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			String out = network_sizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[sizeI][Parameters.numReqs - 1][j] + " ";
			
			System.out.println(out);
		}
	}

	public static void ImpactOfNumReqsHeuristic() {
		
		//int [] network_sizes = {50, 100, 150, 200};
		int [] network_sizes = {100};
		
		double [][][] aveCost = new double [network_sizes.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 5;
		
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			
			System.out.println("Network size: " + network_sizes[sizeI]);
			
			//changeNumOfNodes(network_sizes[sizeI]);
			Parameters.numOfNodes = network_sizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
			Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Topology : " + round);
				
				String netPostFix = "";
				if (round > 0)
					netPostFix = "-" + round;
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, netPostFix);
				Initialization.initServers(simulator);
				Initialization.initEdgeWeights(simulator);
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				OnlineHeu onlineAlg = new OnlineHeu(simulator);
				long startTime = System.currentTimeMillis();
				onlineAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (onlineAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (onlineAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// online benchmark;
				
				OnlineBenchmark onlineBenchmarkAlg = new OnlineBenchmark(simulator);
				startTime = System.currentTimeMillis();
				onlineBenchmarkAlg.run();				
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][1] += (onlineBenchmarkAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][1] += (onlineBenchmarkAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
//			String out = network_sizes[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][Parameters.numReqs - 1][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Num of requests admitted-------------------");
		for (int t = 0; t < Parameters.numReqs; t ++){
			if ( (t % 50) != 0)
				continue;
			String out = t + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[0][t][j] + " ";
			
			System.out.println(out);
		}
	}
	
	
	public static void ImpactOfAlphaOnline() {
		
		//int [] network_sizes = {50, 100, 150, 200};
		int [] alphaPowers = {1, 2, 3, 4, 5};
		
		double [][][] aveCost = new double [alphaPowers.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [alphaPowers.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		for (int sizeI = 0; sizeI < alphaPowers.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 5;
		
		for (int sizeI = 0; sizeI < alphaPowers.length; sizeI ++) {
			
			System.out.println("Alpha: " + alphaPowers[sizeI]);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Topology : " + round);
				
				String netPostFix = "";
				if (round > 0)
					netPostFix = "-" + round;
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, netPostFix);
				Initialization.initServers(simulator);
				Initialization.initEdgeWeights(simulator);
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				Online onlineAlg = new Online(simulator, alphaPowers[sizeI], 1);
				long startTime = System.currentTimeMillis();
				onlineAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (onlineAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (onlineAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < alphaPowers.length; sizeI ++) {
//			String out = alphaPowers[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][Parameters.numReqs - 1][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Number of requests admitted-------------------");
		for (int sizeI = 0; sizeI < alphaPowers.length; sizeI ++) {
			String out = alphaPowers[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[sizeI][Parameters.numReqs - 1][j] + " ";
			
			System.out.println(out);
		}
	}

	public static void ImpactOfBetaOnline() {
		
		//int [] network_sizes = {50, 100, 150, 200};
		int [] betaPowers = {1, 2, 3, 4, 5};
		
		double [][][] aveCost = new double [betaPowers.length][Parameters.numReqs][4];
		double [][][] numReqsAdmitted = new double [betaPowers.length][Parameters.numReqs][4];
		//double [][][] aveRunningTime = new double [network_sizes.length][Parameters.numReqs][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		for (int sizeI = 0; sizeI < betaPowers.length; sizeI ++) {
			for (int k = 0; k < Parameters.numReqs; k ++){
				for (int j = 0; j < numAlgs; j ++) {
					aveCost[sizeI][k][j] = 0d;
					numReqsAdmitted[sizeI][k][j] = 0;
					//aveRunningTime[sizeI][k][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
				}
			}
		}
		
		int numRound = 5;
		
		for (int sizeI = 0; sizeI < betaPowers.length; sizeI ++) {
			
			System.out.println("Alpha: " + betaPowers[sizeI]);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Topology : " + round);
				
				String netPostFix = "";
				if (round > 0)
					netPostFix = "-" + round;
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, netPostFix);
				Initialization.initServers(simulator);
				Initialization.initEdgeWeights(simulator);
				Initialization.initMulticastRequests(simulator, false);
				
				// online algorithm. 
				Online onlineAlg = new Online(simulator, 1, betaPowers[sizeI]);
				long startTime = System.currentTimeMillis();
				onlineAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				for (int k = 0; k < Parameters.numReqs; k ++) {
					aveCost[sizeI][k][0] += (onlineAlg.getAccumulativeCost().get(k) / numRound);
					numReqsAdmitted[sizeI][k][0] += (onlineAlg.getNumOfAdmittedReqs().get(k) / numRound);
				}
				
				//aveRunningTime[sizeI][0] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
			}
		}
		
//		System.out.println("Average cost---------------------------------");
//		for (int sizeI = 0; sizeI < betaPowers.length; sizeI ++) {
//			String out = betaPowers[sizeI] + " ";
//			for (int j = 0; j < numAlgs; j ++)
//				out += aveCost[sizeI][Parameters.numReqs - 1][j] + " ";
//			
//			System.out.println(out);
//		}
		
		System.out.println("Num of requests admitted-------------------");
		for (int sizeI = 0; sizeI < betaPowers.length; sizeI ++) {
			String out = betaPowers[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += numReqsAdmitted[sizeI][Parameters.numReqs - 1][j] + " ";
			
			System.out.println(out);
		}
	}
	
	
	/**********unit tests******************/
	public static void ImpactOfNetworkSizesApproximationBenchmark() {
		
//		int [] network_sizes = {50, 100, 150, 200};
		int [] network_sizes = {100};
		
		double [][] aveCost = new double [network_sizes.length][4];
		double [][] aveRunningTime = new double [network_sizes.length][4];
		//double [][] aveRatios = new double [network_sizes.length][4];
		
		int numAlgs = 2;
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveCost[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				//aveRatios[sizeI][j] = 0d;
			}
		}
		
		int numRound = 1;
		
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			
			System.out.println("Network size: " + network_sizes[sizeI]);
			
			//changeNumOfNodes(network_sizes[sizeI]);
			Parameters.numOfNodes = network_sizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			Parameters.maxNumDestinationsPerRequest = (int) (0.2 * Parameters.numOfNodes);
			Parameters.minNumDestinationsPerRequest = (int) (0.1 * Parameters.numOfNodes);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Topology : " + round);
				
				String netPostFix = "";
				if (round > 0)
					netPostFix = "-" + round;
				
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, netPostFix);
				Initialization.initServers(simulator);
				Initialization.initEdgeWeights(simulator);
				Initialization.initMulticastRequests(simulator, false);
				
				// approximation algorithm. 
				ApproximationBenchmark approBenchmarkAlg = new ApproximationBenchmark(simulator);
				long startTime = System.currentTimeMillis();
				approBenchmarkAlg.run();
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveCost[sizeI][1] += (approBenchmarkAlg.getCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		System.out.println("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			String out = network_sizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveCost[sizeI][j] + " ";
			
			System.out.println(out);
		}
		
		System.out.println("Running time--------------------------");
		for (int sizeI = 0; sizeI < network_sizes.length; sizeI ++) {
			String out = network_sizes[sizeI] + " ";
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

	public ArrayList<Switch> getSwitchesAttachedServers() {
		return switchesAttachedServers;
	}

	public void setSwitchesAttachedServers(ArrayList<Switch> switchesAttachedServers) {
		this.switchesAttachedServers = switchesAttachedServers;
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
}
