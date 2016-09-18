package simulation;

import graph.Node;

import java.util.ArrayList;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;

import algs.Optimal;
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
		
	public static void main(String[] s) {
		
		performanceOptimal("GEANT");// (4)
		//performanceHeuristicNumReqs("GEANT");
		//performanceHeuristicNumReqs("AS1755");
		//performanceHeuristicNumReqs("AS4755");
	}
	
	public static void performanceOptimal(String networkName) {
		
		int [] numOfReqs = {50, 100, 150, 200, 250};
		
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
		
		for (int sizeI = 0; sizeI < numOfReqs.length; sizeI ++) {
			
			System.out.println("Number of requests in R(t): " + numOfReqs[sizeI]);
			Parameters.maxServersForEachSC = numOfReqs[sizeI];
			Parameters.numReqs = numOfReqs[sizeI];
			
			Initialization.initDataCenters(simulator);
			Initialization.initEdgeWeights(simulator);
			
			for (int round = 0; round < numRound; round ++) {
				
				System.out.println("Round : " + round);
				
				Initialization.initUnicastRequests(simulator, false);
				
				// approximation algorithm. 
				Optimal optimalAlg = new Optimal(simulator);
				long startTime = System.currentTimeMillis();
				optimalAlg.run();				
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				aveTotalCosts[sizeI][0] += (optimalAlg.getTotalCost() / numRound);					
				aveRunningTime[sizeI][0] += (totalTime / numRound);
				
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
