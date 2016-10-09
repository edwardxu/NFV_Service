package algs.basicrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import utils.Pair;

public class ApproUnSplittableSpecialBR {
	private SDNRoutingSimulator simulator = null;
	
	private ArrayList<Request> requests = null;
	
	private double totalCost = 0d;
	
	private double averageCost = 0d;
	
	private int numOfAdmittedReqs = 0;
	
	private double epsilon = 0.05;
	
	public ApproUnSplittableSpecialBR(SDNRoutingSimulator sim, ArrayList<Request> requests) {
		
		if (sim == null || requests == null || requests.isEmpty())
			throw new IllegalArgumentException("Simulator, request list should not be null or empty!");
		
		this.simulator = sim;	
		this.requests = requests;
		
		// check the input of this algorithm
		for (Request req : this.requests) {
			if (req.getPacketRate() != Parameters.minPacketRate) {
				throw new IllegalArgumentException("The packet rate of each request must equal to the basicPacketRate!");
			}
		}
	}
	
	public void run() {
		
		SimpleWeightedGraph<Node, InternetLink> originalGraph = simulator.getNetwork();
		ArrayList<Commodity> commodities = new ArrayList<Commodity>();
		ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> flowNetwork = ApproSplittableSpecialBR.constructAuxiliaryGraph(this.simulator, this.requests, originalGraph, this.simulator.getSwitchesAttachedDataCenters(), commodities);
		// call the mcmc algorithm. 
		MCMC flowAlg = new MCMC(flowNetwork, this.epsilon, commodities);
		
		ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> afterFlowNetwork = flowAlg.calcMinCostFlow();
		
		//Set<Request> admittedReqs = new HashSet<Request>();
		Map<Request, Map<DataCenter, Pair<Double>>> admittedReqs = new HashMap<Request, Map<DataCenter, Pair<Double>>>();
		
		for (MinCostFlowEdge edge : afterFlowNetwork.edgeSet()) {
			if ((edge.getSource() instanceof Request) && (edge.getTarget() instanceof ServiceChain)) {
				if (edge.getFlows() <= 0)
					continue;
				
				Request req = (Request) edge.getSource();
				ServiceChain scNode = (ServiceChain) edge.getTarget();
				DataCenter dc = scNode.getParent().getHomeDataCenter();
				double admittedPacketRate = edge.getFlows() * Parameters.minPacketRate;
				
				double unitCost = edge.getCost() + dc.getCosts()[req.getServiceChainType()]; 
				
				if (null == admittedReqs.get(req))
					admittedReqs.put(req, new HashMap<DataCenter, Pair<Double>>());
				
				if (null == admittedReqs.get(req).get(dc))
					admittedReqs.get(req).put(dc, new Pair<Double>(admittedPacketRate, unitCost));
				else 
					admittedReqs.get(req).put(dc, new Pair<Double>(admittedReqs.get(req).get(dc).getA() + admittedPacketRate, unitCost));
				
				//totalCost += (edge.getCost() + dc.getCosts()[req.getServiceChainType()]) * admittedPacketRate;
			}
		}
		// adjust assignment. 
		for (Entry<Request, Map<DataCenter, Pair<Double>>> entry : admittedReqs.entrySet()){
			
			Request req = entry.getKey(); 
			DataCenter dcWithMostTraffic = null; 
			double maxTrafficDC = -1d; 
			for (Entry<DataCenter, Pair<Double>> entry2 : entry.getValue().entrySet()) {
				if (entry2.getValue().getA() > maxTrafficDC) {
					maxTrafficDC = entry2.getValue().getA();
					dcWithMostTraffic = entry2.getKey();
				}
			}
			dcWithMostTraffic.admitRequest(req, req.getPacketRate(), null, true);
			totalCost += req.getPacketRate() * entry.getValue().get(dcWithMostTraffic).getB();
		}
		
		this.averageCost = totalCost / admittedReqs.size();
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

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	} 
	

}
