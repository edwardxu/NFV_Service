package simulation;

import graph.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;

import algs.basicrate.ApproSplittableSpecialBR;
import algs.basicrate.ApproUnSplittableSpecialBR;
import algs.basicrate.Exact;
import algs.basicrate.Greedy;
import algs.basicrate.GreedySplittable;
import algs.basicrate.Online;
import algs.basicrate.OnlineGreedy;
import algs.basicrate.OptimalBR;
import system.DataCenter;
import system.InternetLink;
import system.Request;
import system.ServiceChain;
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
	
	public static final Logger logger = LogManager.getLogger(SDNRoutingSimulator.class);
	
	private static final ExecutorService threadPool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

	public SDNRoutingSimulator() {
		this.setSwitches(new ArrayList<Switch>());
		this.setSwitchesAttachedDataCenters(new ArrayList<Switch>());
		this.setMulticastRequests(new ArrayList<Request>());
		this.setUnicastRequests(new ArrayList<Request>());
	}
		
	public static void main(String[] args) {
		
		ArrayList<Runnable> listOfTasks = new ArrayList<>();
		
	    for (String arg : args) {
			switch (arg) {
			case "POA":
				listOfTasks.add(new Thread(() -> performanceOptimalNetworkSizesBR(), "PER-OPT-ALL"));
				break;
			case "POG":
				listOfTasks.add(new Thread(() -> performanceOptimalSDCRatioBR("GEANT"), "PER-OPT-GEANT"));
				break;
			case "PO4755":
				listOfTasks.add(new Thread(() -> performanceOptimalSDCRatioBR("AS4755"), "PER-OPT-AS4755"));
				break;
			case "PO1755":
				listOfTasks.add(new Thread(() -> performanceOptimalSDCRatioBR("AS1755"), "PER-OPT-AS1755"));
				break;
			case "PASA":
				listOfTasks.add(new Thread(() -> performanceApproSplittableNetworkSizesBR(), "PER-APP-SPLITTABLE-ALL"));
				break;
			case "PASG":
				listOfTasks.add(new Thread(() -> performanceApproSplittableSDCRatioBR("GEANT"), "PER-APP-SPLITTABLE-GEANT"));
				break;
			case "PAS4755":
				listOfTasks.add(new Thread(() -> performanceApproSplittableSDCRatioBR("AS4755"), "PER-APP-SPLITTABLE-AS4755"));
				break;
			case "PAS1755":
				listOfTasks.add(new Thread(() -> performanceApproSplittableSDCRatioBR("AS1755"), "PER-APP-SPLITTABLE-AS1755"));
				break;
			case "PAUA":
				listOfTasks.add(new Thread(() -> performanceApproUnSplittableNetworkSizesBR(), "PER-APP-UNSPLITTABLE-ALL"));
				break;
			case "PAUG":
				listOfTasks.add(new Thread(() -> performanceApproUnSplittableSDCRatioBR("GEANT"), "PER-APP-UNSPLITTABLE-GEANT"));
				break;
			case "PAU4755":
				listOfTasks.add(new Thread(() -> performanceApproUnSplittableSDCRatioBR("AS4755"), "PER-APP-UNSPLITTABLE-AS4755"));
				break;
			case "PAU1755":
				listOfTasks.add(new Thread(() -> performanceApproUnSplittableSDCRatioBR("AS1755"), "PER-APP-UNSPLITTABLE-AS1755"));
				break;
			case "IRO":
				listOfTasks.add(new Thread(() -> impactOfMinRhoOptimalBR(), "IMPACT-RHO-OPT"));
				break; 
			case "IRAS":
				listOfTasks.add(new Thread(() -> impactOfMinRhoSplittableBR(), "IMPACT-RHO-APP-SPLITTABLE"));
				break; 
			case "IRAU":
				listOfTasks.add(new Thread(() -> impactOfMinRhoUnSplittableBR(), "IMPACT-RHO-APP-UNSPLITTABLE"));
				break;
			case "IDO":
				listOfTasks.add(new Thread(() -> impactOfDCNumOptimalBR(), "IMPACT-RATIO-OPT"));
				break; 
			case "IDAS":
				listOfTasks.add(new Thread(() -> impactOfDCNumSplittableBR(), "IMPACT-RATIO-APP-SPLITTABLE"));
				break; 
			case "IDAU":
				listOfTasks.add(new Thread(() -> impactOfDCNumUnSplittableBR(), "IMPACT-RATIO-APP-UNSPLITTABLE"));
				break;
			case "POnlineA":
				listOfTasks.add(new Thread(() -> performanceOnlineNetworkSizesBR(), "PER-ONLINE-ALL"));
				break;
			case "POnlineG":
				listOfTasks.add(new Thread(() -> performanceOnlineNumReqsBR("GEANT"), "PER-ONLINE-GEANT"));
				break;
			case "POnline4755":
				listOfTasks.add(new Thread(() -> performanceOnlineNumReqsBR("AS4755"), "PER-ONLINE-AS4755"));
				break;
			case "POnline1755":
				listOfTasks.add(new Thread(() -> performanceOnlineNumReqsBR("AS1755"), "PER-ONLINE-AS1755"));
			default:
				System.out.println("Unknown argument: " + arg);
				System.exit(1);
			}
	    }

	    listOfTasks.forEach(threadPool::execute);

	    threadPool.shutdown();
	    try {
	      threadPool.awaitTermination(1L, TimeUnit.DAYS);
	    } catch (InterruptedException ie) {
	      ie.printStackTrace();
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
		
		ThreadContext.put("threadName", "PER-OPT-ALL");
		int [] networkSizes = {50, 100, 150, 200, 250};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [networkSizes.length][numAlgs];
		double [][] aveRunningTime = new double [networkSizes.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [networkSizes.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [networkSizes.length][numAlgs];
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		double numRound = 2;
		
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("Number of nodes: " + networkSizes[sizeI]);
			Parameters.numOfNodes = networkSizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (optimalAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);

				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}
	
	public static void performanceOptimalNumReqsBR(String networkName) {
		
		//int [] numOfReqs = {350, 400, 450, 500, 550, 600};
		int [] numOfReqs = {400, 450, 500, 1000, 1500, 2000};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [numOfReqs.length][numAlgs];
		double [][] aveRunningTime = new double [numOfReqs.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [numOfReqs.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [numOfReqs.length][numAlgs];

		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		double numRound = 2;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
			ThreadContext.put("threadName", "PER-OPT-GEANT");
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
			ThreadContext.put("threadName", "PER-OPT-AS1755");
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
			ThreadContext.put("threadName", "PER-OPT-AS4755");
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);

		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("Number of requests in R(t): " + numOfReqs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			Parameters.numReqs = numOfReqs[sizeI];
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
				
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (optimalAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);

				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}
	
	public static void performanceOptimalSDCRatioBR(String networkName) {
		
		double [] switchToDCRatios = {5, 10, 15, 20};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [switchToDCRatios.length][numAlgs];
		double [][] aveRunningTime = new double [switchToDCRatios.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [switchToDCRatios.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [switchToDCRatios.length][numAlgs];
		
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		double numRound = 2;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
			ThreadContext.put("threadName", "PER-OPT-GEANT");
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
			ThreadContext.put("threadName", "PER-OPT-AS1755");
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
			ThreadContext.put("threadName", "PER-OPT-AS4755");
		}

		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {			
			Parameters.ServerToNodeRatio = 1 / switchToDCRatios[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			Initialization.initDataCenters(simulator, true);
			
			SDNRoutingSimulator.logger.info("Switch To DC Ratios: " + switchToDCRatios[sizeI] + " Number of DCs " + Parameters.K);

			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
				
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (optimalAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}
	
	public static void performanceApproSplittableNetworkSizesBR() {
		
		ThreadContext.put("threadName", "PER-APP-SPLITTABLE-ALL");
		int [] networkSizes = {50, 100, 150, 200, 250};
		
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [networkSizes.length][numAlgs];
		double [][] aveRunningTime = new double [networkSizes.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [networkSizes.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [networkSizes.length][numAlgs];
		
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d;
			}
		}
				
		double numRound = 10;
		
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("Number of nodes: " + networkSizes[sizeI]);
			Parameters.numOfNodes = networkSizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				GreedySplittable greedyAlg = new GreedySplittable(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);

				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}
	
	public static void performanceApproSplittableSDCRatioBR(String networkName) {
		
		//double [] switchToDCRatios = {4, 6, 8, 10};
		double [] switchToDCRatios = {5, 10, 15, 20};

		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [switchToDCRatios.length][numAlgs];
		double [][] aveRunningTime = new double [switchToDCRatios.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [switchToDCRatios.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [switchToDCRatios.length][numAlgs];

		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		//Parameters.numReqs = 500;
		double numRound = 5;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
			ThreadContext.put("threadName", "PER-APP-SPLITTABLE-GEANT");
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
			ThreadContext.put("threadName", "PER-APP-SPLITTABLE-AS1755");
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
			ThreadContext.put("threadName", "PER-APP-SPLITTABLE-AS4755");
		}
		
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			Parameters.ServerToNodeRatio = 1 / switchToDCRatios[sizeI]; 
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			Initialization.initDataCenters(simulator, true);
			SDNRoutingSimulator.logger.info("Switch To DC Ratios: " + switchToDCRatios[sizeI] + " Number of DCs " + Parameters.K);
			
			for (int round = 0; round < numRound; round ++) {
				SDNRoutingSimulator.logger.info("Round : " + round);
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound); 
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				GreedySplittable greedyAlg = new GreedySplittable(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run(true);
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
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
		double [][] aveTotalPktRateOfAdmitted = new double [numOfReqs.length][numAlgs];
		
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		//Parameters.numReqs = 500;
		double numRound = 2;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
			ThreadContext.put("threadName", "PER-APP-SPLITTABLE-GEANT");
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
			ThreadContext.put("threadName", "PER-APP-SPLITTABLE-AS1755");
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
			ThreadContext.put("threadName", "PER-APP-SPLITTABLE-AS4755");
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			SDNRoutingSimulator.logger.info("Number of requests in R(t): " + numOfReqs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			Parameters.numReqs = numOfReqs[sizeI];
			
			for (int round = 0; round < numRound; round ++) {
				SDNRoutingSimulator.logger.info("Round : " + round);
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);

				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}
	
	public static void performanceApproUnSplittableNetworkSizesBR() {
		
		
		ThreadContext.put("threadName", "PER-APP-UNSPLITTABLE-ALL");
		
		int [] networkSizes = {50, 100, 150, 200, 250};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [networkSizes.length][numAlgs];
		double [][] aveRunningTime = new double [networkSizes.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [networkSizes.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [networkSizes.length][numAlgs];

		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		double numRound = 2;
		
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {		
			SDNRoutingSimulator.logger.info("Number of nodes: " + networkSizes[sizeI]);
			Parameters.numOfNodes = networkSizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
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
		double [][] aveTotalPktRateOfAdmitted = new double [numOfReqs.length][numAlgs];

		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		double numRound = 2;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
			Parameters.numReqs = 300; 
			ThreadContext.put("threadName", "PER-APP-UNSPLITTABLE-GEANT");
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
			Parameters.numReqs = 500; 
			ThreadContext.put("threadName", "PER-APP-UNSPLITTABLE-AS1755");
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
			Parameters.numReqs = 500; 
			ThreadContext.put("threadName", "PER-APP-UNSPLITTABLE-AS4755");
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
		
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			SDNRoutingSimulator.logger.info("Number of requests in R(t): " + numOfReqs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			Parameters.numReqs = numOfReqs[sizeI];
			
			for (int round = 0; round < numRound; round ++) {
				SDNRoutingSimulator.logger.info("Round : " + round);
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound); 
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}

	public static void performanceApproUnSplittableSDCRatioBR(String networkName) {
		
		//double [] switchToDCRatios = {4, 6, 8, 10};
		double [] switchToDCRatios = {5, 10, 15, 20};
		
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [switchToDCRatios.length][numAlgs];
		double [][] aveRunningTime = new double [switchToDCRatios.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [switchToDCRatios.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [switchToDCRatios.length][numAlgs];

		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		double numRound = 2;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
			Parameters.numReqs = 300; 
			ThreadContext.put("threadName", "PER-APP-UNSPLITTABLE-GEANT");
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
			Parameters.numReqs = 500; 
			ThreadContext.put("threadName", "PER-APP-UNSPLITTABLE-AS1755");
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
			Parameters.numReqs = 500; 
			ThreadContext.put("threadName", "PER-APP-UNSPLITTABLE-AS4755");
		}
		
		
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			
			Parameters.ServerToNodeRatio = 1 / switchToDCRatios[sizeI]; 
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			Initialization.initDataCenters(simulator, true);
			SDNRoutingSimulator.logger.info("Switch To DC Ratios: " + switchToDCRatios[sizeI] + " Number of DCs " + Parameters.K);
						
			for (int round = 0; round < numRound; round ++) {
				SDNRoutingSimulator.logger.info("Round : " + round);
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < switchToDCRatios.length; sizeI ++) {
			String out = switchToDCRatios[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}
	
	public static void performanceOnlineNetworkSizesBR() {
		
		ThreadContext.put("threadName", "PER-ONLINE-ALL");
		int [] networkSizes = {50, 100, 150, 200, 250};
		int numAlgs = 3;
		
		double [][] aveTotalCosts = new double [networkSizes.length][numAlgs];
		double [][] aveRunningTime = new double [networkSizes.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [networkSizes.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [networkSizes.length][numAlgs];
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		double numRound = 2;
		
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("Number of nodes: " + networkSizes[sizeI]);
			Parameters.numOfNodes = networkSizes[sizeI];
			Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
				SDNRoutingSimulator simulator = new SDNRoutingSimulator();
				String postFix = "";
				if (round > 0) postFix = "-" + round;
				
				Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, postFix);
				Initialization.initDataCenters(simulator, true);
				Initialization.initEdgeWeights(simulator);
				
				Initialization.initUnicastRequests(simulator, false, true, true);
				
				// optimal solution for the problem with identical data rates. 
				Exact optimalAlg = new Exact(simulator, simulator.getUnicastRequests(), 1d);
				long startTime = System.currentTimeMillis();
				optimalAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (optimalAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (optimalAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][0] += (optimalAlg.getOptimalThroughput() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				Online onlineAlg = new Online(simulator, simulator.getUnicastRequests(), 1d);
				startTime = System.currentTimeMillis();
				onlineAlg.run();
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (onlineAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (onlineAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][1] += (onlineAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				OnlineGreedy greedyAlg = new OnlineGreedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run();
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][2] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][2] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][2] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][2] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < networkSizes.length; sizeI ++) {
			String out = networkSizes[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}

	public static void performanceOnlineNumReqsBR(String networkName) {
		
		//int [] numOfReqs = {350, 400, 450, 500, 550, 600};
		int [] numOfReqs = {400, 450, 500, 1000, 1500, 2000};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [numOfReqs.length][numAlgs];
		double [][] aveRunningTime = new double [numOfReqs.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [numOfReqs.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [numOfReqs.length][numAlgs];

		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		double numRound = 2;
		//changeNumOfNodes(network_sizes[sizeI]);
		if (networkName.equals("GEANT")) {
			Parameters.numOfNodes = 40;
			ThreadContext.put("threadName", "PER-OnlineGreedy-GEANT");
		} else if (networkName.equals("AS1755")) {
			Parameters.numOfNodes = 172;
			ThreadContext.put("threadName", "PER-OnlineGreedy-AS1755");
		} else if (networkName.equals("AS4755")) {
			Parameters.numOfNodes = 121;
			ThreadContext.put("threadName", "PER-OnlineGreedy-AS4755");
		}
		
		Parameters.K = (int) (Parameters.numOfNodes * Parameters.ServerToNodeRatio);

		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, networkName);
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("Number of requests in R(t): " + numOfReqs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			Parameters.numReqs = numOfReqs[sizeI];
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
				
				Initialization.initUnicastRequests(simulator, false, true, true);
				
				// optimal solution for the problem with identical data rates. 
				Exact optimalAlg = new Exact(simulator, simulator.getUnicastRequests(), 1d);
				long startTime = System.currentTimeMillis();
				optimalAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (optimalAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (optimalAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][0] += (optimalAlg.getOptimalThroughput() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				Online onlineAlg = new Online(simulator, simulator.getUnicastRequests(), 1d);
				startTime = System.currentTimeMillis();
				onlineAlg.run();
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][1] += (onlineAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][1] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][1] += (onlineAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][1] += (onlineAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
				
				// optimal solution for the problem with identical data rates. 
				OnlineGreedy greedyAlg = new OnlineGreedy(simulator, simulator.getUnicastRequests());
				startTime = System.currentTimeMillis();
				greedyAlg.run();
				endTime   = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][2] += (greedyAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][2] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][2] += (greedyAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][2] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);

				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Average cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			String out = numOfReqs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}

	
	public static void impactOfDCNumOptimalBR() {
		
		ThreadContext.put("threadName", "IMPACT-RATIO-OPT");
		double [] numOfDCs = {10, 15, 20, 25, 30};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [numOfDCs.length][numAlgs];
		double [][] aveRunningTime = new double [numOfDCs.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [numOfDCs.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [numOfDCs.length][numAlgs];

		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		double numRound = 1;
		
		//Parameters.minDelayRequirement = 30; //ms
		
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("V/DC: " + numOfDCs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.ServerToNodeRatio = Parameters.numOfNodes / numOfDCs[sizeI];					
			Parameters.K = (int) numOfDCs[sizeI];
			
			SDNRoutingSimulator simulator = new SDNRoutingSimulator();
			Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, "");
			Initialization.initEdgeWeights(simulator);
			Initialization.initUnicastRequests(simulator, false, true, true);
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
				Initialization.initDataCenters(simulator, true);
				// optimal solution for the problem with identical data rates. 
				OptimalBR optimalAlg = new OptimalBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				optimalAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (optimalAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (optimalAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][0] += (optimalAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}

	
	public static void impactOfDCNumSplittableBR() {
		
		ThreadContext.put("threadName", "IMPACT-RATIO-APP-SPLITTABLE");
		
		double [] numOfDCs = {10, 15, 20, 25, 30};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [numOfDCs.length][numAlgs];
		double [][] aveRunningTime = new double [numOfDCs.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [numOfDCs.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [numOfDCs.length][numAlgs];

		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		double numRound = 2;
		
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("V/DC: " + numOfDCs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.ServerToNodeRatio = Parameters.numOfNodes / numOfDCs[sizeI];					
			Parameters.K = (int) numOfDCs[sizeI];
			
			SDNRoutingSimulator simulator = new SDNRoutingSimulator();
			Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, "");
			Initialization.initEdgeWeights(simulator);
			Initialization.initUnicastRequests(simulator, false, true, false);
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
				
				Initialization.initDataCenters(simulator, true);
				
				// optimal solution for the problem with identical data rates. 
				ApproSplittableSpecialBR approAlg = new ApproSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (approAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}

	public static void impactOfDCNumUnSplittableBR() {
		
		ThreadContext.put("threadName", "IMPACT-RATIO-APP-UNSPLITTABLE");
		
		double [] numOfDCs = {10, 15, 20, 25, 30};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [numOfDCs.length][numAlgs];
		double [][] aveRunningTime = new double [numOfDCs.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [numOfDCs.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [numOfDCs.length][numAlgs];

		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d;
			}
		}
		
		//Parameters.numReqs = 500;
		
		double numRound = 2;
		
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("V/DC: " + numOfDCs[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.ServerToNodeRatio = Parameters.numOfNodes / numOfDCs[sizeI];					
			Parameters.K = (int) numOfDCs[sizeI];
			
			SDNRoutingSimulator simulator = new SDNRoutingSimulator();
			Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, "");
			Initialization.initEdgeWeights(simulator);
			Initialization.initUnicastRequests(simulator, false, true, false);

			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
				Initialization.initDataCenters(simulator, true);
				
				// optimal solution for the problem with identical data rates. 
				ApproUnSplittableSpecialBR approAlg = new ApproUnSplittableSpecialBR(simulator, simulator.getUnicastRequests());
				long startTime = System.currentTimeMillis();
				approAlg.run();			
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (approAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				aveNumOfAdmitted[sizeI][0] += (approAlg.getNumOfAdmittedReqs() / numRound);
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < numOfDCs.length; sizeI ++) {
			String out = numOfDCs[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
		
	}
	
	public static void impactOfMinRhoOptimalBR() {
		
		ThreadContext.put("threadName", "IMPACT-RHO-OPT");
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		double [] minRhos = {4, 6, 8, 10};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [minRhos.length][numAlgs];
		double [][] aveRunningTime = new double [minRhos.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [minRhos.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [minRhos.length][numAlgs];

		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		double numRound = 2;
		
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, "");
		
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("Rho_Max/Rho_Min: " + minRhos[sizeI]);
			Parameters.minPacketRate = Parameters.maxPacketRate / minRhos[sizeI]; 
			
			for (Switch swDC : simulator.getSwitchesAttachedDataCenters()) {
				DataCenter dc = swDC.getAttachedDataCenter();
				for (Entry<Integer, HashSet<ServiceChain>> entry : dc.getServiceChains().entrySet()){
					for (ServiceChain sc : entry.getValue()){
						sc.setProcessingCapacity(Parameters.minPacketRate);
					}
				}
			}
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
				
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (optAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}

	
	public static void impactOfMinRhoSplittableBR() {
		
		ThreadContext.put("threadName", "IMPACT-RHO-APP-SPLITTABLE");
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		double [] minRhos = {4, 6, 8, 10};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [minRhos.length][numAlgs];
		double [][] aveRunningTime = new double [minRhos.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [minRhos.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [minRhos.length][numAlgs];

		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		double numRound = 2;
		
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, "");
		
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("Rho_Max/Rho_Min: " + minRhos[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.minPacketRate = Parameters.maxPacketRate / minRhos[sizeI]; 
			
			for (Switch swDC : simulator.getSwitchesAttachedDataCenters()) {
				DataCenter dc = swDC.getAttachedDataCenter();
				for (Entry<Integer, HashSet<ServiceChain>> entry : dc.getServiceChains().entrySet()){
					for (ServiceChain sc : entry.getValue()){
						sc.setProcessingCapacity(Parameters.minPacketRate);
					}
				}
			}
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
				
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
	}
	
	
	public static void impactOfMinRhoUnSplittableBR() {
		
		ThreadContext.put("threadName", "IMPACT-RHO-APP-UNSPLITTABLE");
		//int [] numOfReqs = {150, 200, 250, 300, 350};
		double [] minRhos = {4, 6, 8, 10};
		int numAlgs = 2;
		
		double [][] aveTotalCosts = new double [minRhos.length][numAlgs];
		double [][] aveRunningTime = new double [minRhos.length][numAlgs];
		double [][] aveNumOfAdmitted = new double [minRhos.length][numAlgs];
		double [][] aveTotalPktRateOfAdmitted = new double [minRhos.length][numAlgs];

		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			for (int j = 0; j < numAlgs; j ++) {
				aveTotalCosts[sizeI][j] = 0d;
				aveRunningTime[sizeI][j] = 0d;
				aveNumOfAdmitted[sizeI][j] = 0d;
				aveTotalPktRateOfAdmitted[sizeI][j] = 0d; 
			}
		}
		
		double numRound = 2;
		
		SDNRoutingSimulator simulator = new SDNRoutingSimulator();
		Initialization.initNetwork(simulator, 0, Parameters.numOfNodes, false, "");
		
		Initialization.initDataCenters(simulator, true);
		Initialization.initEdgeWeights(simulator);
		
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			
			SDNRoutingSimulator.logger.info("Rho_Max/Rho_Min: " + minRhos[sizeI]);
			//Parameters.maxServersForEachSC = numOfReqs[sizeI];
			
			Parameters.minPacketRate = Parameters.maxPacketRate / minRhos[sizeI]; 
			for (Switch swDC : simulator.getSwitchesAttachedDataCenters()) {
				DataCenter dc = swDC.getAttachedDataCenter();
				for (Entry<Integer, HashSet<ServiceChain>> entry : dc.getServiceChains().entrySet()){
					for (ServiceChain sc : entry.getValue()){
						sc.setProcessingCapacity(Parameters.minPacketRate);
					}
				}
			}
			
			for (int round = 0; round < numRound; round ++) {
				
				SDNRoutingSimulator.logger.info("Round : " + round);
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
				aveTotalPktRateOfAdmitted[sizeI][0] += (approAlg.getTotalPktRateOfAdmittedReqs() / numRound);
				
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
				aveTotalPktRateOfAdmitted[sizeI][1] += (greedyAlg.getTotalPktRateOfAdmittedReqs() / numRound);

				
				// reset 
				for (Switch sw : simulator.getSwitches())
					sw.reset();
				
				for (InternetLink il : simulator.getNetwork().edgeSet())
					il.reset();
			}
		}
		
		SDNRoutingSimulator.logger.info("Num of requests admitted------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveNumOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Throughput------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalPktRateOfAdmitted[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Total cost---------------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveTotalCosts[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		SDNRoutingSimulator.logger.info("Running time--------------------------");
		for (int sizeI = 0; sizeI < minRhos.length; sizeI ++) {
			String out = minRhos[sizeI] + " ";
			for (int j = 0; j < numAlgs; j ++)
				out += aveRunningTime[sizeI][j] + " ";
			
			SDNRoutingSimulator.logger.info(out);
		}
		
		ThreadContext.remove("threadName");
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
