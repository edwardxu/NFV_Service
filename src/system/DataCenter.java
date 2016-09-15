package system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import graph.Node;
import simulation.Parameters;
import simulation.SDNRoutingSimulator;
import utils.RanNum;

public class DataCenter extends Node {
	
	private Map<Integer, HashSet<ServiceChain>> serviceChains = new HashMap<Integer, HashSet<ServiceChain>>();
	
	private Map<ServiceChain, ArrayList<Request>> admittedRequests = new HashMap<ServiceChain, ArrayList<Request>>();
	
	// [type] =  cost
	private double [] costs = new double [Parameters.serviceChainProcessingDelays.length];

	// [type] = processing delay
	private double [] processingDelays = new double [Parameters.serviceChainProcessingDelays.length];	
	
	public DataCenter(double id, String name) {
		super(id, name);
		// initialize costs for different types of service chains, and instances of service chains. 
		for (int i = 0; i < Parameters.serviceChainProcessingDelays.length; i ++) {
			costs[i] = RanNum.getRandomDoubleRange(Parameters.minServiceChainCost, Parameters.maxServiceChainCost);
			double minDelayThisType = Parameters.serviceChainProcessingDelays[i][0];
			double maxDelayThisType = Parameters.serviceChainProcessingDelays[i][1];
			processingDelays[i] = RanNum.getRandomDoubleRange(maxDelayThisType, minDelayThisType);
			
			int numOfInstances = RanNum.getRandomIntRange(Parameters.maxNumOfInstances, Parameters.minNumOfInstances);
			for (int j = 0; j < numOfInstances; j ++){
				double SCID = SDNRoutingSimulator.idAllocator.nextId(); 
				ServiceChain sc = new ServiceChain(SCID, "Service Chain: " + SCID, i);
				if (null == this.getServiceChains().get(i))
					this.getServiceChains().put(i, new HashSet<ServiceChain>());
				this.getServiceChains().get(i).add(sc);
			}
		}
	}
	
	public void admitRequest(Request req, ServiceChain sc) {
				
		double availableRateThisSC = this.getAvailableProcessingRate(sc);

		if (availableRateThisSC < req.getDataRate())
			System.out.println("Error: available computing resource is not enough to admit service chain " + sc.getName());
		else {
			if (null == this.getAdmittedRequests().get(sc))
				this.getAdmittedRequests().put(sc, new ArrayList<Request>());
			this.getAdmittedRequests().get(sc).add(req);
		}
	}
	
	private double getAvailableProcessingRate(ServiceChain sc){
		
		double occupiedProcessingCapacity = 0d; 
		
		for (Request req : this.admittedRequests.get(sc)){
			occupiedProcessingCapacity += req.getDataRate();
		}
		
		return sc.getProcessingCapacity() - occupiedProcessingCapacity;
	}
	
	public void reset() {
		this.setAdmittedRequests(new HashMap<ServiceChain, ArrayList<Request>>());
	}

	public Map<Integer, HashSet<ServiceChain>> getServiceChains() {
		return serviceChains;
	}

	public void setServiceChains(Map<Integer, HashSet<ServiceChain>> serviceChains) {
		this.serviceChains = serviceChains;
	}

	public Map<ServiceChain, ArrayList<Request>> getAdmittedRequests() {
		return admittedRequests;
	}

	public void setAdmittedRequests(Map<ServiceChain, ArrayList<Request>> admittedRequests) {
		this.admittedRequests = admittedRequests;
	}

	public double [] getCosts() {
		return costs;
	}

	public void setCosts(double [] costs) {
		this.costs = costs;
	}

	public double [] getProcessingDelays() {
		return processingDelays;
	}

	public void setProcessingDelays(double [] processingDelays) {
		this.processingDelays = processingDelays;
	}
}
