package algs.basicrate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import system.Switch;
import utils.Pair;

public class ApproUnSplittableSpecialBR {
	
	private SDNRoutingSimulator simulator = null;
	
	private ArrayList<Request> requests = null;
	
	private double totalCost = 0d;
	
	private double averageCost = 0d;
	
	private int numOfAdmittedReqs = 0;
	
	private double totalPktRateOfAdmittedReqs = 0d;
	
	private double epsilon = 0.07;
	
	public ApproUnSplittableSpecialBR(SDNRoutingSimulator sim, ArrayList<Request> requests) {
		
		if (sim == null || requests == null || requests.isEmpty())
			throw new IllegalArgumentException("Simulator, request list should not be null or empty!");
		
		this.simulator = sim;
		this.requests = requests;
		
		//Collections.sort(this.requests, Request.RequestPacketRateComparator);
		
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
		
//		ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> flowNetwork = ApproSplittableSpecialBR.constructAuxiliaryGraph(this.simulator, 
//				this.requests, originalGraph, 
//				this.simulator.getSwitchesAttachedDataCenters(), 
//				commodities, 
//				this.simulator.getSwitchesAttachedDataCenters().size());
		ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> flowNetwork = ApproSplittableSpecialBR.constructAuxiliaryGraph(this.simulator,
				this.requests, originalGraph,
				this.simulator.getSwitchesAttachedDataCenters(),
				commodities,
				1d);
		// call the MCMC algorithm. 
		MCMC flowAlg = new MCMC(flowNetwork, this.epsilon, commodities);
		
		ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> afterFlowNetwork = flowAlg.calcMinCostFlow();
		
		//Set<Request> admittedReqs = new HashSet<Request>();
		ArrayList<Request> reqsList = new ArrayList<Request>();
		Map<Request, Map<DataCenter, Pair<Double>>> admittedReqs = new HashMap<Request, Map<DataCenter, Pair<Double>>>();
		
		for (MinCostFlowEdge edge : afterFlowNetwork.edgeSet()) {
			if ((edge.getSource() instanceof Request) && (edge.getTarget() instanceof ServiceChain)) {
				if (edge.getFlows() <= 0)
					continue;
				
				Request req = (Request) edge.getSource();
				if (!reqsList.contains(req))
					reqsList.add(req);
				
				ServiceChain scNode = (ServiceChain) edge.getTarget();
				DataCenter dc = scNode.getParent().getHomeDataCenter();
				double admittedPacketRate = edge.getFlows() * Parameters.minPacketRate;
				
				double unitCost = edge.getCost() / Parameters.minPacketRate + dc.getCosts()[req.getServiceChainType()]; 
				
				if (null == admittedReqs.get(req))
					admittedReqs.put(req, new HashMap<DataCenter, Pair<Double>>());
				
				if (null == admittedReqs.get(req).get(dc))
					admittedReqs.get(req).put(dc, new Pair<Double>(admittedPacketRate, unitCost));
				else 
					admittedReqs.get(req).put(dc, new Pair<Double>(admittedReqs.get(req).get(dc).getA() + admittedPacketRate, unitCost));
				
				//totalCost += (edge.getCost() + dc.getCosts()[req.getServiceChainType()]) * admittedPacketRate;
			}
		}
		
		Collections.sort(reqsList, Request.RequestPacketRateComparator);
		
		int numAdmitted = 0;
		// adjust assignment. 
		for (Request req : reqsList) {
			
			DataCenter dcWithMostTraffic = null; 
			double maxTrafficDC = -1d; 
			for (Entry<DataCenter, Pair<Double>> entry2 : admittedReqs.get(req).entrySet()) {
				if (entry2.getValue().getA() > maxTrafficDC) {
					maxTrafficDC = entry2.getValue().getA();
					dcWithMostTraffic = entry2.getKey();
				}
			}
			
			if (req.getPacketRate() > dcWithMostTraffic.getAvailableProcessingRate((ServiceChain) dcWithMostTraffic.getServiceChains().get(req.getServiceChainType()).toArray()[0], true)){
				continue; 
			}
			
			numAdmitted ++; 
			
			dcWithMostTraffic.admitRequest(req, req.getPacketRate(), (ServiceChain) dcWithMostTraffic.getServiceChains().get(req.getServiceChainType()).toArray()[0], true);
			totalCost += req.getPacketRate() * admittedReqs.get(req).get(dcWithMostTraffic).getB();
			
			this.totalPktRateOfAdmittedReqs += req.getPacketRate();
		}
		
		this.numOfAdmittedReqs = numAdmitted;
		this.averageCost = totalCost / this.numOfAdmittedReqs;
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

	public double getTotalPktRateOfAdmittedReqs() {
		return totalPktRateOfAdmittedReqs;
	}

	public void setTotalPktRateOfAdmittedReqs(double totalPktRateOfAdmittedReqs) {
		this.totalPktRateOfAdmittedReqs = totalPktRateOfAdmittedReqs;
	} 
	

}
