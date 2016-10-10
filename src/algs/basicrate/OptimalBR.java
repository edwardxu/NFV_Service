package algs.basicrate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.SimpleWeightedGraph;

import graph.Node;
import simulation.Parameters;
import simulation.SDNRoutingSimulator;
import system.DataCenter;
import system.InternetLink;
import system.Request;
import system.ServiceChain;
import system.Switch;

public class OptimalBR {

	private SDNRoutingSimulator simulator = null;
	
	private ArrayList<Request> requests = null;
	
	private double totalCost = 0d;
	
	private double averageCost = 0d;
	
	private int numOfAdmittedReqs = 0;
	
	public OptimalBR(SDNRoutingSimulator sim, ArrayList<Request> requests) {
		
		if (sim == null || requests == null || requests.isEmpty())
			throw new IllegalArgumentException("Simulator, request list should not be null or empty!");
		
		this.simulator = sim;	
		this.requests = requests;
		
		// check the input of this algorithm
		for (Request req : this.requests){
			if (req.getPacketRate() != Parameters.minPacketRate) {
				throw new IllegalArgumentException("The packet rate of each request must equal to the basicPacketRate!");
			}
		}
	}
	
	public void run() {
		// the optimal algorithm.
		SimpleWeightedGraph<Node, InternetLink> originalGraph = simulator.getNetwork();
				
		ArrayList<Node> XSet = new ArrayList<Node>();
		ArrayList<Node> YSet = new ArrayList<Node>();
		ArrayList<Node> dummyNodesXSet = new ArrayList<Node>();
		ArrayList<Node> dummyNodesYSet = new ArrayList<Node>();

		SimpleWeightedGraph<Node, InternetLink> bipartiteGraph = constructAuxiliaryGraph(this.requests, 
				originalGraph, this.simulator.getSwitchesAttachedDataCenters(), Parameters.minPacketRate, XSet, YSet, dummyNodesXSet, dummyNodesYSet);
		
		KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, InternetLink> perfectMatching = new KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, InternetLink>(bipartiteGraph, XSet, YSet);
		Set<InternetLink> matching = perfectMatching.getMatching();
		
		for (InternetLink auEdge : matching) {
			Node edgeSource = bipartiteGraph.getEdgeSource(auEdge);
			Node edgeTarget = bipartiteGraph.getEdgeTarget(auEdge);
			
			double edgeWeight = bipartiteGraph.getEdgeWeight(auEdge);
			
			if (edgeWeight == Double.MAX_VALUE || dummyNodesXSet.contains(edgeSource) || dummyNodesXSet.contains(edgeTarget) || dummyNodesYSet.contains(edgeSource) || dummyNodesYSet.contains(edgeTarget))
				continue;
			
			// an admission of the request 
			Request admittedReq = null; 
			ServiceChain vSC = null; 
			if (edgeSource instanceof Request) {
				admittedReq = (Request) edgeSource;
				vSC = (ServiceChain) edgeTarget; 
			} else {
				admittedReq = (Request) edgeTarget;
				vSC = (ServiceChain) edgeSource; 
			}
			
			DataCenter dc = vSC.getParent().getHomeDataCenter(); 
			dc.admitRequest(admittedReq, admittedReq.getPacketRate(), vSC.getParent(), true);
			
			numOfAdmittedReqs ++;
			totalCost += bipartiteGraph.getEdgeWeight(auEdge);
		}
		
		this.averageCost = this.totalCost / this.numOfAdmittedReqs;
	
	}
	
	private SimpleWeightedGraph<Node, InternetLink> constructAuxiliaryGraph(
			ArrayList<Request> requests, 
			SimpleWeightedGraph<Node, InternetLink> originalGraph, 
			ArrayList<Switch> switchesWithDCs, 
			double identicalDataRate, 
			ArrayList<Node> sourceNodes, // requests or virtual requests
			ArrayList<Node> targetNodes, // virtual switches
			ArrayList<Node> dummySourceNodes,
			ArrayList<Node> dummyTargetNodes
			) {
		
		SimpleWeightedGraph<Node, InternetLink> auxiliaryGraph = new SimpleWeightedGraph<Node, InternetLink>(InternetLink.class);
		
		//Map<Switch, ArrayList<Switch>> switchToVirtualSwitches = new HashMap<Switch, ArrayList<Switch>>();
		
		for (Switch sw : switchesWithDCs) {
			DataCenter dc = sw.getAttachedDataCenter();
			
			for (Entry<Integer, HashSet<ServiceChain>> entry : dc.getServiceChains().entrySet()) {
				for (ServiceChain sc : entry.getValue()) {
					double processingCapacity = sc.getProcessingCapacity();
					while (processingCapacity >= identicalDataRate) {
						// create a virtual sc with "identicalDataRate"
						ServiceChain vSC = new ServiceChain(SDNRoutingSimulator.idAllocator.nextId(), "Virtual SC Instance", sc, identicalDataRate);
						auxiliaryGraph.addVertex(vSC);
						
						targetNodes.add(vSC);
						processingCapacity -= identicalDataRate;
					}
				}
			}
		}
		
		for (Request req : requests) {
			sourceNodes.add(req);
			auxiliaryGraph.addVertex(req);
		}
		
		if (sourceNodes.size() > targetNodes.size()) {
			// add dummy target Nodes
			int numDummiesToAdd = sourceNodes.size() - targetNodes.size(); 
			for (int i = 0; i < numDummiesToAdd; i ++) {
				ServiceChain vSC = new ServiceChain(SDNRoutingSimulator.idAllocator.nextId(), "Dummy SC Instance", null, identicalDataRate);
				auxiliaryGraph.addVertex(vSC);
				dummyTargetNodes.add(vSC);
				targetNodes.add(vSC);
			}
		} else {
			// add dummy source Nodes
			int numDummiesToAdd = targetNodes.size() - sourceNodes.size(); 
			for (int i = 0; i < numDummiesToAdd; i ++) {
				Request dummyRequest = new Request();
				auxiliaryGraph.addVertex(dummyRequest);
				dummySourceNodes.add(dummyRequest);
				sourceNodes.add(dummyRequest);
			}
		}
		
		// add edges from source node to target node
		double maxCost = -1d; 
		for (Node sNode : sourceNodes) {
			
			Request req = (Request) sNode;
			
			for (Node tNode : targetNodes) {
				ServiceChain vSC = (ServiceChain) tNode;
				
				if (!dummySourceNodes.contains(sNode) && !dummyTargetNodes.contains(tNode)) {
					
					if ((req.getServiceChainType() != vSC.getParent().getServiceChainType() )){
						InternetLink auEdge = auxiliaryGraph.addEdge(sNode, tNode);
						auxiliaryGraph.setEdgeWeight(auEdge, Double.MAX_VALUE);
						continue; 
					}
					
					// check whether the delay requirement is met. 
					Node sourceSwitch = req.getSourceSwitch();
					Node destSwitch = req.getDestinationSwitches().get(0);
					
					double delay1 = 0d; 
					double pathCost1 = 0d; 
					if (!sourceSwitch.equals(vSC.getSwitchHomeDataCenter())) {
						DijkstraShortestPath<Node, InternetLink> shortestPathSToDC = new DijkstraShortestPath<Node, InternetLink>(originalGraph, sourceSwitch, vSC.getParent().getHomeDataCenter().getAttachedSwitch());
						delay1 = Double.MAX_VALUE; 
						pathCost1 = Double.MAX_VALUE;
						for (int i = 0; i < shortestPathSToDC.getPathEdgeList().size(); i ++) {
							if (0 == i ) {
								delay1 = 0d;
								pathCost1 = 0d;
							}
							delay1 += shortestPathSToDC.getPathEdgeList().get(i).getLinkDelay();
							pathCost1 += originalGraph.getEdgeWeight(shortestPathSToDC.getPathEdgeList().get(i));
						}
					}
					
					double delay2 = 0d; 
					double pathCost2 = 0d;
					if (!destSwitch.equals(vSC.getSwitchHomeDataCenter())) {
						DijkstraShortestPath<Node, InternetLink> shortestPathDCToDest = new DijkstraShortestPath<Node, InternetLink>(originalGraph, vSC.getParent().getHomeDataCenter().getAttachedSwitch(), destSwitch);
						delay2 = Double.MAX_VALUE; 
						pathCost2 = Double.MAX_VALUE;
						for (int i = 0; i < shortestPathDCToDest.getPathEdgeList().size(); i ++) {
							if (0 == i ) {
								delay2 = 0d;
								pathCost2 = 0d; 
							}
							delay2 += shortestPathDCToDest.getPathEdgeList().get(i).getLinkDelay();
							pathCost2 += originalGraph.getEdgeWeight(shortestPathDCToDest.getPathEdgeList().get(i));
						}
					}
					
					double delay = delay1 + delay2 + vSC.getParent().getHomeDataCenter().getProcessingDelays()[vSC.getParent().getServiceChainType()];
					if (delay < req.getDelayRequirement()) {
						// add an edge in the auxiliary graph. 
						double cost = req.getPacketRate() * (pathCost1 + pathCost2 + vSC.getParent().getHomeDataCenter().getCosts()[vSC.getParent().getServiceChainType()]);
						InternetLink auEdge = auxiliaryGraph.addEdge(sNode, tNode);
						auxiliaryGraph.setEdgeWeight(auEdge, cost);
						if (cost > maxCost)
							maxCost = cost; 
					} else {
						InternetLink auEdge = auxiliaryGraph.addEdge(sNode, tNode);
						auxiliaryGraph.setEdgeWeight(auEdge, Double.MAX_VALUE);
					}
				}
			}
		}
		
		for (Node dummySNode : dummySourceNodes) {
			Request req = (Request) dummySNode;
			
			for (Node tNode : targetNodes) {
				ServiceChain sc = (ServiceChain) tNode;
				InternetLink edge = auxiliaryGraph.addEdge(req, sc);
				auxiliaryGraph.setEdgeWeight(edge, Double.MAX_VALUE); //maxCost * 10);
			}
		}
		
		for (Node sNode : sourceNodes){
			Request req = (Request) sNode;
			for (Node tNode : dummyTargetNodes) {
				ServiceChain sc = (ServiceChain) tNode;
				InternetLink edge = auxiliaryGraph.addEdge(req, sc);
				auxiliaryGraph.setEdgeWeight(edge, Double.MAX_VALUE); //maxCost * 10);
			}
		}
		
		return auxiliaryGraph; 
	}

	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	public double getAverageCost() {
		return averageCost;
	}

	public void setAverageCost(double averageCost) {
		this.averageCost = averageCost;
	}

	public int getNumOfAdmittedReqs() {
		return numOfAdmittedReqs;
	}

	public void setNumOfAdmittedReqs(int numOfAdmittedReqs) {
		this.numOfAdmittedReqs = numOfAdmittedReqs;
	}
}
