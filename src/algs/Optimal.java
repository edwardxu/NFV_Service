package algs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.SimpleWeightedGraph;

import graph.Node;
import simulation.SDNRoutingSimulator;
import system.DataCenter;
import system.InternetLink;
import system.Request;
import system.ServiceChain;
import system.Switch;

public class Optimal {

	private SDNRoutingSimulator simulator = null;
	
	private double totalCost = 0d;
	
	private double averageCost = 0d; 
	
	private int numOfAdmittedReqs = 0;
	
	public Optimal(SDNRoutingSimulator sim) {
		this.simulator = sim;		
	}
	
	public void run() {
		// the optimal algorithm.
		SimpleWeightedGraph<Node, InternetLink> originalGraph = simulator.getNetwork();
				
		ArrayList<Node> XSet = new ArrayList<Node>();
		ArrayList<Node> YSet = new ArrayList<Node>();
		ArrayList<Node> dummyNodesXSet = new ArrayList<Node>();
		ArrayList<Node> dummyNodesYSet = new ArrayList<Node>();

		SimpleWeightedGraph<Node, InternetLink> bipartiteGraph = constructAuxiliaryGraph(this.simulator.getUnicastRequests(), 
				originalGraph, this.simulator.getSwitchesAttachedDataCenters(), this.simulator.getUnicastRequests().get(0).getDataRate(), XSet, YSet, dummyNodesXSet, dummyNodesYSet);
		
		KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, InternetLink> perfectMatching = new KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, InternetLink>(bipartiteGraph, XSet, YSet);
		Set<InternetLink> matching = perfectMatching.getMatching();
		
		for (InternetLink auEdge : matching){
			Node edgeSource = bipartiteGraph.getEdgeSource(auEdge);
			Node edgeTarget = bipartiteGraph.getEdgeTarget(auEdge);
			
			if (dummyNodesXSet.contains(edgeSource) || dummyNodesXSet.contains(edgeTarget) || dummyNodesYSet.contains(edgeSource) || dummyNodesYSet.contains(edgeTarget))
				continue;
			// an admission of the request 
			Request admittedReq = null; 
			ServiceChain sc = null; 
			if (edgeSource instanceof Request) {
				admittedReq = (Request) edgeSource;
				sc = (ServiceChain) edgeTarget; 
			}
			else {
				admittedReq = (Request) edgeTarget;
				sc = (ServiceChain) edgeSource; 
			}
			
			DataCenter dc = sc.getHomeDataCenter(); 
			if (null == dc.getAdmittedRequests().get(sc))
				dc.getAdmittedRequests().put(sc, new ArrayList<Request>());
			dc.getAdmittedRequests().get(sc).add(admittedReq);
			
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
		
		Map<Switch, ArrayList<Switch>> switchToVirtualSwitches = new HashMap<Switch, ArrayList<Switch>>();
		
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
		
		for (Request req : requests){
			sourceNodes.add(req);
			auxiliaryGraph.addVertex(req);
		}
		
		if (sourceNodes.size() > targetNodes.size()){
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
				ServiceChain sc = (ServiceChain) tNode;
				
				if (!dummySourceNodes.contains(sNode) && !dummyTargetNodes.contains(tNode)) {
					// check whether the delay requirement is met. 
					Node sourceSwitch = req.getSourceSwitch();
					Node destSwitch = req.getDestinationSwitches().get(0);
					
					DijkstraShortestPath<Node, InternetLink> shortestPathSToDC = new DijkstraShortestPath<Node, InternetLink>(originalGraph, sourceSwitch, sc.getHomeDataCenter());
					double delay1 = Double.MAX_VALUE; 
					double pathCost1 = Double.MAX_VALUE;
					for (int i = 0; i < shortestPathSToDC.getPathEdgeList().size(); i ++) {
						if (0 == i ) {
							delay1 = 0d;
							pathCost1 = 0d;
						}
						delay1 += shortestPathSToDC.getPathEdgeList().get(i).getLinkDelay();
						pathCost1 += originalGraph.getEdgeWeight(shortestPathSToDC.getPathEdgeList().get(i));
					}
					
					DijkstraShortestPath<Node, InternetLink> shortestPathDCToDest = new DijkstraShortestPath<Node, InternetLink>(originalGraph, sc.getHomeDataCenter(), destSwitch);
					double delay2 = Double.MAX_VALUE; 
					double pathCost2 = Double.MAX_VALUE;
					for (int i = 0; i < shortestPathDCToDest.getPathEdgeList().size(); i ++) {
						if (0 == i ) {
							delay2 = 0d;
							pathCost2 = 0d; 
						}
						delay2 += shortestPathDCToDest.getPathEdgeList().get(i).getLinkDelay();
						pathCost2 += originalGraph.getEdgeWeight(shortestPathDCToDest.getPathEdgeList().get(i));
					}
					
					double delay = delay1 + delay2 + sc.getHomeDataCenter().getProcessingDelays()[sc.getServiceChainType()];
					if (delay < req.getDelayRequirement()) {
						// add an edge in the auxiliary graph. 
						double cost = req.getDataRate() * (pathCost1 + pathCost2 + sc.getHomeDataCenter().getCosts()[sc.getServiceChainType()]);
						InternetLink auEdge = auxiliaryGraph.addEdge(sNode, tNode);
						auxiliaryGraph.setEdgeWeight(auEdge, cost);
						if (cost > maxCost)
							maxCost = cost; 
					}
				}
			}
		}
		
		for (Node dummySNode : dummySourceNodes) {
			Request req = (Request) dummySNode;
			
			for (Node tNode : targetNodes) {
				ServiceChain sc = (ServiceChain) tNode;
				InternetLink edge = auxiliaryGraph.addEdge(req, sc);
				auxiliaryGraph.setEdgeWeight(edge, maxCost * 10);
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
