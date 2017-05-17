package algs.basicrate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import algs.flow.Commodity;
import algs.flow.MCMC;
import algs.flow.MinCostFlowEdge;
import graph.Node;
import simulation.Parameters;
import simulation.SDNRoutingSimulator;
import system.DataCenter;
import system.InternetLink;
import system.Request;
import system.ServiceChain;
import system.Switch;

public class ApproSplittableSpecialBR {

	private SDNRoutingSimulator simulator = null;
	
	private ArrayList<Request> requests = null;
	
	private double totalCost = 0d;
	
	private double averageCost = 0d;
	
	private int numOfAdmittedReqs = 0;
	
	private double totalPktRateOfAdmittedReqs = 0d;
	
	private double epsilon = 0.05; 
	
	public ApproSplittableSpecialBR(SDNRoutingSimulator sim, ArrayList<Request> requests) {
		
		if (sim == null || requests == null || requests.isEmpty())
			throw new IllegalArgumentException("Simulator, request list should not be null or empty!");
		
		this.simulator = sim;
		this.requests = requests;
		
		// enforce the assumption for the approximation algorihtms
		double totalPacketRates = 0d;
		for (Switch swDC : this.simulator.getSwitchesAttachedDataCenters()) {
			DataCenter dc = swDC.getAttachedDataCenter(); 
			for (Entry<Integer, HashSet<ServiceChain>> entry : dc.getServiceChains().entrySet()){
				for (ServiceChain sc : entry.getValue()){
					totalPacketRates += sc.getProcessingCapacity();
				}				
			}
		}
		
		ArrayList<Request> newRequests = new ArrayList<Request>();
		double totalPacketRatesRequests = 0d;
		for (Request req : this.requests) {
			if (totalPacketRatesRequests + req.getPacketRate() < totalPacketRates){
				newRequests.add(req);
				totalPacketRatesRequests += req.getPacketRate(); 
			} else {
				continue; 
			}
		}
		
		this.simulator.setUnicastRequests(newRequests);
		this.requests = this.simulator.getUnicastRequests();
	}
	
	public void run() {
		
		SimpleWeightedGraph<Node, InternetLink> originalGraph = simulator.getNetwork();
		ArrayList<Commodity> commodities = new ArrayList<Commodity>();
		ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> flowNetwork = ApproSplittableSpecialBR.constructAuxiliaryGraph(this.simulator, this.requests, originalGraph, this.simulator.getSwitchesAttachedDataCenters(), commodities, 1d);
		// call the mcmc algorithm. 
		MCMC flowAlg = new MCMC(flowNetwork, this.epsilon, commodities);
		
		ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> afterFlowNetwork = flowAlg.calcMinCostFlow();
		
		Set<Request> admittedReqs = new HashSet<Request>();
		for (MinCostFlowEdge edge : afterFlowNetwork.edgeSet()) {
			
			if ((edge.getSource() instanceof Request) && (edge.getTarget() instanceof ServiceChain)) {
				if (edge.getFlows() <= 0)
					continue;
				
				Request req = (Request) edge.getSource();
				ServiceChain scNode = (ServiceChain) edge.getTarget();
				DataCenter dc = scNode.getParent().getHomeDataCenter();
				
				double admittedPacketRate = edge.getFlows() * Parameters.minPacketRate;
				dc.admitRequest(req, admittedPacketRate, scNode.getParent(), true);
				
				admittedReqs.add(req);
				
				this.totalCost += (edge.getCost() + dc.getCosts()[req.getServiceChainType()]) * edge.getFlows();
				//this.totalPktRateOfAdmittedReqs += admittedPacketRate;
			}
		}
		
		this.numOfAdmittedReqs = admittedReqs.size(); 
		for (Request req : admittedReqs)
			this.totalPktRateOfAdmittedReqs += req.getPacketRate();
		
		this.averageCost = totalCost / this.numOfAdmittedReqs;
	}
	
	public static ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> constructAuxiliaryGraph(
			SDNRoutingSimulator simulator, 
			ArrayList<Request> requests, 
			SimpleWeightedGraph<Node, InternetLink> originalGraph, 
			ArrayList<Switch> switchesWithDCs, 
			ArrayList<Commodity> commodities, 
			double networkCapacityScaleDownRatio
			) {
		
		ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> auxiliaryGraph = new ListenableDirectedWeightedGraph<Node, MinCostFlowEdge>(MinCostFlowEdge.class);
		
		Node virtualSource = new Node(SDNRoutingSimulator.idAllocator.nextId(), "Virtual Source");
		auxiliaryGraph.addVertex(virtualSource);
		Node virtualSink = new Node(SDNRoutingSimulator.idAllocator.nextId(), "Virtual Sink");
		auxiliaryGraph.addVertex(virtualSink);
		
		// request nodes
		for (Request req : requests) {
			auxiliaryGraph.addVertex(req);
			double demand = req.getPacketRate() / Parameters.minPacketRate;
			Commodity comm = new Commodity(SDNRoutingSimulator.idAllocator.nextId(), req, virtualSink, demand);
			commodities.add(comm);
		}
		
		// service chain nodes
		ArrayList<ServiceChain> scNodes = new ArrayList<ServiceChain>();
		
		for (Switch swDC : simulator.getSwitchesAttachedDataCenters()) {
			DataCenter dc = swDC.getAttachedDataCenter();
			auxiliaryGraph.addVertex(dc);
			
			for (int type = 0; type < Parameters.serviceChainProcessingDelays.length ; type ++){
				
				if (dc.getServiceChains().get(type).isEmpty())
					continue; 
				
				Object [] scs = dc.getServiceChains().get(type).toArray();
				int capa = (int) (scs.length / networkCapacityScaleDownRatio);
				if (0 == capa)
					continue; 
				
				ServiceChain serviceChainNode = new ServiceChain(SDNRoutingSimulator.idAllocator.nextId(), "Service Chain Node", (ServiceChain) scs[0], dc.getServiceChains().get(type).size());
				auxiliaryGraph.addVertex(serviceChainNode);
				scNodes.add(serviceChainNode);
			}
		}
		
		// add edges and edge costs capacities
		for (Request req : requests) {
			
			Switch sourceSwitch = req.getSourceSwitch();
			Switch destSwitch = req.getDestinationSwitches().get(0);
			
			double demand = req.getPacketRate() / Parameters.minPacketRate;
			
			MinCostFlowEdge edge = auxiliaryGraph.addEdge(virtualSource, req);
			//capacity
			edge.setCapacity(demand);
			edge.setCost(0d);
			
			for (ServiceChain scNode : scNodes) {
				if (req.getServiceChainType() == scNode.getParent().getServiceChainType()) {
					// check whether the delay can be met by these instances of service chain 
					DataCenter dc = scNode.getParent().getHomeDataCenter();
					
					double delay1 = 0d; 
					double pathCost1 = 0d; 
					if (!sourceSwitch.equals(dc.getAttachedSwitch())) {
						DijkstraShortestPath<Node, InternetLink> shortestPathSToDC = new DijkstraShortestPath<Node, InternetLink>(originalGraph, sourceSwitch, dc.getAttachedSwitch());
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
					if (!destSwitch.equals(dc.getAttachedSwitch())) {
						DijkstraShortestPath<Node, InternetLink> shortestPathDCToDest = new DijkstraShortestPath<Node, InternetLink>(originalGraph, dc.getAttachedSwitch(), destSwitch);
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
					
					double delay = delay1 + delay2 + dc.getProcessingDelays()[req.getServiceChainType()];
					if (delay < req.getDelayRequirement()) {
						// add an edge in the auxiliary graph. 
						double cost = Parameters.minPacketRate * (pathCost1 + pathCost2); // + dc.getCosts()[req.getServiceChainType()]);
						MinCostFlowEdge auEdge = auxiliaryGraph.addEdge(req, scNode);
						auEdge.setCost(cost);
						auEdge.setCapacity(demand);
					}
				}
			}
		}
		
		int totalCap = 0;
		for (ServiceChain scNode : scNodes) {
			DataCenter dc = scNode.getParent().getHomeDataCenter();
			int scType = scNode.getParent().getServiceChainType();
			double cost = dc.getCosts()[scType];
			
			int capa = (int) (dc.getServiceChains().get(scType).size() / networkCapacityScaleDownRatio);
			
			MinCostFlowEdge auEdge = auxiliaryGraph.addEdge(scNode, dc);
			auEdge.setCost(cost);
			//int capa = (int) (dc.getServiceChains().get(scType).size() / networkCapacityScaleDownRatio);
			auEdge.setCapacity(capa);
			totalCap += auEdge.getCapacity();
		}
		
		for (Switch swDC : simulator.getSwitchesAttachedDataCenters()){
			DataCenter dc = swDC.getAttachedDataCenter();
			
			MinCostFlowEdge auEdge = auxiliaryGraph.addEdge(dc, virtualSink);
			auEdge.setCost(0d);
			auEdge.setCapacity(totalCap);
		}
		
		return auxiliaryGraph;
	}

	public SDNRoutingSimulator getSimulator() {
		return simulator;
	}

	public void setSimulator(SDNRoutingSimulator simulator) {
		this.simulator = simulator;
	}

	public ArrayList<Request> getRequests() {
		return requests;
	}

	public void setRequests(ArrayList<Request> requests) {
		this.requests = requests;
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

	public double getTotalPktRateOfAdmittedReqs() {
		return totalPktRateOfAdmittedReqs;
	}

	public void setTotalPktRateOfAdmittedReqs(double totalPktRateOfAdmittedReqs) {
		this.totalPktRateOfAdmittedReqs = totalPktRateOfAdmittedReqs;
	}
	
	
}
