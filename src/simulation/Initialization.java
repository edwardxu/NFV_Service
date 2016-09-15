package simulation;

import graph.Node;
import algs.PlacementAlgs;

import java.util.ArrayList;

import org.jgrapht.graph.SimpleWeightedGraph;

import system.InternetLink;
import system.Request;
import system.DataCenter;
import system.ServiceChain;
import system.Switch;
import system.generate.NetworkGenerator;
import utils.RanNum;

// for some initialization and updating functions. 
public class Initialization {
	
	public static void initNetwork (
			SDNRoutingSimulator simulator, // initizalize for which simulator
			int generateType, //0: generate
			int size, // the number of data centers.
			boolean specialCase,
			String netIndexPostFix
			) {

		simulator.setNetwork(new SimpleWeightedGraph<Node, InternetLink>(
				InternetLink.class));

		NetworkGenerator<Node, InternetLink> networkGenerator = new NetworkGenerator<Node, InternetLink>();
		networkGenerator.setGenerateType(generateType);
		networkGenerator.setSize(size);
		networkGenerator.setSpecialCase(specialCase);
		networkGenerator.setNetworkIndexPostFix(netIndexPostFix);
		networkGenerator.generateGraph(simulator.getNetwork(), null, null);
		
		for (Node node : simulator.getNetwork().vertexSet()){
			if (!simulator.getSwitches().contains(node))
				simulator.getSwitches().add((Switch) node);
		}
		//ConnectivityInspector<Node, InternetLink> connect = new ConnectivityInspector<Node, InternetLink>(simulator.getNetwork());
		//System.out.println(connect.isGraphConnected());
	}
	
	public static void initEdgeWeights(SDNRoutingSimulator simulator) {
		SimpleWeightedGraph<Node, InternetLink> network = simulator.getNetwork();
		
		for (InternetLink il : network.edgeSet()) {
			il.setLinkCost(RanNum.getRandomDoubleRange(Parameters.maxLinkCost, Parameters.minLinkCost));
			network.setEdgeWeight(il, il.getLinkCost());
		}
	}
	
	public static void initDataCenters(SDNRoutingSimulator simulator) {
		
		if (!simulator.getSwitchesAttachedServers().isEmpty())
			simulator.getSwitchesAttachedServers().clear();
		
		SimpleWeightedGraph<Node, InternetLink> network = simulator.getNetwork();
		// put servers at the nodes with the top-K degrees. 
		PlacementAlgs placeServers = new PlacementAlgs(PlacementAlgs.TOP_DEGREE, network);	
		ArrayList<Node> switchesWithServers = placeServers.getNodesWithServers();
		
		for (Node node : switchesWithServers) {
			Switch sw = (Switch) node;
			// initialize the server randomly. 
			DataCenter server = new DataCenter(simulator.idAllocator.nextId(), "Server", RanNum.getRandomDoubleRange(Parameters.MaxCPUCapPerServer, Parameters.MinCPUCapPerServer));
			sw.setAttachedServer(server);
			
			simulator.getSwitchesAttachedServers().add(sw);
		}
	}
	
	public static void initMulticastRequests(SDNRoutingSimulator simulator, boolean realdata) {
		ArrayList<Request> reqs = simulator.getMulticastRequests();
		
		if (!reqs.isEmpty())
			reqs.clear();
		
		for(int i = 0; i < Parameters.numReqs; i ++) {
			// initialize each multicast request.	
			if(!realdata){
				// Step 1: random select destination switches for this request.
				int numOfDestinations = -1;
				if (Parameters.maxNumDestinationsPerRequest == Parameters.minNumDestinationsPerRequest)
					numOfDestinations = Parameters.maxNumDestinationsPerRequest;
				else 
					numOfDestinations = RanNum.getRandomIntRange(Parameters.maxNumDestinationsPerRequest, Parameters.minNumDestinationsPerRequest);
				
				ArrayList<Integer> destinationIndexes = RanNum.getDistinctInts(simulator.getSwitches().size(), 0, numOfDestinations);
				// Step 2: random select source switch for this request. 
				int sourceIndex = RanNum.getRandomIntRange(simulator.getSwitches().size(), 0);
				while(destinationIndexes.contains(sourceIndex))
					sourceIndex = RanNum.getRandomIntRange(simulator.getSwitches().size(), 0);
				
				Switch sourceSwitch = simulator.getSwitches().get(sourceIndex);
				ArrayList<Switch> desSwitches = new ArrayList<Switch>();
				for (Integer desSwitchIndex : destinationIndexes)
					desSwitches.add(simulator.getSwitches().get(desSwitchIndex));
				
				// Step 3: initialize the service chain of this multicast request. 
				double scID = SDNRoutingSimulator.idAllocator.nextId();
				ServiceChain serviceChain = new ServiceChain(scID, "Switch: " + scID, RanNum.getRandomDoubleRange(Parameters.maxComputingDemandsSC, Parameters.minComputingDemandsSC));
				
				// Step 4L randomly generate the bandwidth resource demand of this request
				double bandwidthDemands = RanNum.getRandomDoubleRange(Parameters.maxBandwidthDemands, Parameters.minBandwidthDemands);
				Request multicastReq = new Request(SDNRoutingSimulator.idAllocator.nextId(), sourceSwitch, desSwitches, bandwidthDemands, serviceChain);
				reqs.add(multicastReq);
			} else {
				// TODO initialize each multicast request from real data. 
			}
		}
	}
	
}
