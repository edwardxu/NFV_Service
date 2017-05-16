package simulation;

import graph.Node;
import algs.PlacementAlgs;

import java.util.ArrayList;

import org.jgrapht.graph.SimpleWeightedGraph;

import system.InternetLink;
import system.Request;
import system.DataCenter;
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
	
	public static void initDataCenters(SDNRoutingSimulator simulator, boolean serviceChainsWithBasicRate) {
		
		if (!simulator.getSwitchesAttachedDataCenters().isEmpty())
			simulator.getSwitchesAttachedDataCenters().clear();
		
		SimpleWeightedGraph<Node, InternetLink> network = simulator.getNetwork();
		// put servers at the nodes with the top-K degrees. 
		PlacementAlgs placeDataCenters = new PlacementAlgs(PlacementAlgs.RANDOM, network);
		ArrayList<Node> switchesWithDCs = placeDataCenters.getNodesWithServers();
		
		for (Node node : switchesWithDCs) {
			Switch sw = (Switch) node;
			// initialize the server randomly. 
			DataCenter dc = new DataCenter(SDNRoutingSimulator.idAllocator.nextId(), "Data Center", serviceChainsWithBasicRate);
			dc.setAttachedSwitch(sw);
			sw.setAttachedServer(dc);
			simulator.getSwitchesAttachedDataCenters().add(sw);
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
				
				// Step 3: generate the service chain type, data rate, delay requirement of this request
				int SCType = RanNum.getRandomIntRange(Parameters.serviceChainProcessingDelays.length - 1, 0);
				double dataRate = RanNum.getRandomDoubleRange(Parameters.maxPacketRate, Parameters.minPacketRate);
				double delayRequirement = RanNum.getRandomDoubleRange(Parameters.maxPacketRate, Parameters.minPacketRate);
				
				// Step 4L randomly generate the bandwidth resource demand of this request
				Request multicastReq = new Request(sourceSwitch, desSwitches, dataRate, SCType, delayRequirement);
				reqs.add(multicastReq);
			} else {
				// TODO initialize each multicast request from real data. 
			}
		}
	}
	
	public static void initUnicastRequests(
			SDNRoutingSimulator simulator, 
			boolean realdata, 
			boolean underBasicPacketRate, 
			boolean underIdenticalPacketRate) {
		
		ArrayList<Request> reqs = simulator.getUnicastRequests();
		
		if (!reqs.isEmpty())
			reqs.clear();

		double identicalDataRate = 0d; 
		if (underBasicPacketRate && underIdenticalPacketRate)
			identicalDataRate = Parameters.minPacketRate;//RanNum.getRandomDoubleRange(Parameters.maxPacketRate, Parameters.minPacketRate);
		else if (!underBasicPacketRate && underIdenticalPacketRate)
			identicalDataRate = RanNum.getRandomDoubleRange(Parameters.maxPacketRate, Parameters.minPacketRate);

		for (int i = 0; i < Parameters.numReqs; i ++) {
			// initialize each multicast request.	
			if(!realdata){
				// Step 1: random select destination switches for this request.
				int numOfDestinations = 1;
				ArrayList<Integer> destinationIndexes = RanNum.getDistinctInts(simulator.getSwitches().size(), 0, numOfDestinations);
				// Step 2: random select source switch for this request. 
				int sourceIndex = RanNum.getRandomIntRange(simulator.getSwitches().size(), 0);
				while(destinationIndexes.contains(sourceIndex))
					sourceIndex = RanNum.getRandomIntRange(simulator.getSwitches().size(), 0);
				
				Switch sourceSwitch = simulator.getSwitches().get(sourceIndex);
				ArrayList<Switch> desSwitches = new ArrayList<Switch>();
				for (Integer desSwitchIndex : destinationIndexes)
					desSwitches.add(simulator.getSwitches().get(desSwitchIndex));
				
				// Step 3: generate the service chain type, data rate, delay requirement of this request
				int SCType = RanNum.getRandomIntRange(Parameters.serviceChainProcessingDelays.length, 0);
				double dataRate = 0d;
				
				if (underIdenticalPacketRate)
					dataRate = identicalDataRate;
				else {
					if (underBasicPacketRate){
						dataRate = Parameters.minPacketRate * RanNum.getRandomIntRange((int) (Parameters.maxPacketRate/Parameters.minPacketRate), 1);
					} else {
						dataRate = RanNum.getRandomDoubleRange(Parameters.maxPacketRate, Parameters.minPacketRate);
					}
				}
				
				double delayRequirement = RanNum.getRandomDoubleRange(Parameters.maxDelayRequirement, Parameters.minDelayRequirement);
				
				// Step 4L randomly generate the bandwidth resource demand of this request
				Request unicastReq = new Request(sourceSwitch, desSwitches, dataRate, SCType, delayRequirement);
				reqs.add(unicastReq);
			} else {
				// TODO initialize each unicast request from real data. 
			}
		}
	}
	
	/**
	 * transform each request into several virtual requests with each having an identical data rate. 
	 * */
	public static ArrayList<Request> generateVirtualRequests(ArrayList<Request> requests){
		ArrayList<Request> virtualRequests = new ArrayList<Request>();
		// the minimum data rate should be "Parameters.minDataRate"
		for (Request req : requests) {
			int numOfVirtualRequests = (int) (req.getPacketRate()/Parameters.minPacketRate);
			for (int i = 0; i < numOfVirtualRequests; i ++){
				Request vReq = new Request(req, Parameters.minPacketRate);
				virtualRequests.add(vReq);
			}
		}		
		return virtualRequests;
	}
}
