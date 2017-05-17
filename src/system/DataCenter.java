package system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import graph.Node;
import simulation.Parameters;
import simulation.SDNRoutingSimulator;
import utils.RanNum;

public class DataCenter extends Node {
	
	private Map<Integer, HashSet<ServiceChain>> serviceChains = new HashMap<Integer, HashSet<ServiceChain>>();
	
	private Map<ServiceChain, ArrayList<Request>> admittedRequests = new HashMap<ServiceChain, ArrayList<Request>>();
	
	// request and its admitted packet rate. 
	private Map<Request, Double> admittedRequestsBR = new HashMap<Request, Double>();
	
	// [type] =  cost
	private double [] costs = new double [Parameters.serviceChainProcessingDelays.length];

	// [type] = processing delay
	private double [] processingDelays = new double [Parameters.serviceChainProcessingDelays.length];	
	
	private Switch attachedSwitch = null; 
	
	public DataCenter(double id, String name, boolean serviceChainsWithBasicRate) {
		super(id, name);
		// initialize costs for different types of service chains, and instances of service chains. 
		for (int i = 0; i < Parameters.serviceChainProcessingDelays.length; i ++) {
			costs[i] = RanNum.getRandomDoubleRange(Parameters.minServiceChainCost, Parameters.maxServiceChainCost);
			double minDelayThisType = Parameters.serviceChainProcessingDelays[i][0];
			double maxDelayThisType = Parameters.serviceChainProcessingDelays[i][1];
			processingDelays[i] = RanNum.getRandomDoubleRange(maxDelayThisType, minDelayThisType);
			
			int numOfInstances = RanNum.getRandomIntRange(Parameters.maxNumOfInstances, Parameters.minNumOfInstances);
			for (int j = 0; j < numOfInstances; j ++) {
				double SCID = SDNRoutingSimulator.idAllocator.nextId(); 
				ServiceChain sc = new ServiceChain(SCID, "Service Chain: " + SCID, i, serviceChainsWithBasicRate);
				sc.setHomeDataCenter(this);
				sc.setSwitchHomeDataCenter(this.getAttachedSwitch());
				
				if (null == this.getServiceChains().get(i))
					this.getServiceChains().put(i, new HashSet<ServiceChain>());
				this.getServiceChains().get(i).add(sc);
			}
		}
	}
	
	public boolean admitRequest(Request req, double admittedPacketRateReq, ServiceChain sc, boolean basicRate) {
		
		if (!basicRate) {
			double availableRateThisSC = this.getAvailableProcessingRate(sc, basicRate);
			if (availableRateThisSC < req.getPacketRate()) {
				SDNRoutingSimulator.logger.info("Error: available computing resource is not enough to admit service chain " + sc.getName());
				return false; 
			}
			else {
				if (null == this.getAdmittedRequests().get(sc))
					this.getAdmittedRequests().put(sc, new ArrayList<Request>());
				this.getAdmittedRequests().get(sc).add(req);
			}
		} else {
			double availableRateThisTypeSC = this.getAvailableProcessingRate(sc, basicRate);
			if (availableRateThisTypeSC < admittedPacketRateReq){
				//SDNRoutingSimulator.logger.info("Error: available computing resource is not enough to admit service chain " + sc.getName());
				return false; 
			} else {
				if (null == this.getAdmittedRequestsBR().get(req))
					this.getAdmittedRequestsBR().put(req, 0d);
				
				if (this.getAdmittedRequestsBR().get(req) + admittedPacketRateReq > req.getPacketRate()) {
					this.getAdmittedRequestsBR().put(req, req.getPacketRate());
				} else {
					this.getAdmittedRequestsBR().put(req, this.getAdmittedRequestsBR().get(req) + admittedPacketRateReq);
				}
			}
		}
		
		return true; 
	}
	
	public void removeRequest(Request req, boolean basicRate){
		if (!basicRate) {
			
			for (Entry<ServiceChain, ArrayList<Request>> entry : this.getAdmittedRequests().entrySet()){
				for (Iterator<Request> iter = entry.getValue().iterator(); iter.hasNext(); ){
					Request request = iter.next(); 
					if (req.equals(request)){
						iter.remove();
						break; 
					}
					
				}
			}
		} else {
			this.getAdmittedRequestsBR().remove(req);
		}
	}
	
	public double getAvailableProcessingRate(ServiceChain sc, boolean basicRate) {
		
		double occupiedProcessingCapacity = 0d;
		if (!basicRate) {
			for (Request req : this.admittedRequests.get(sc)){
				occupiedProcessingCapacity += req.getPacketRate();
			}
			return sc.getProcessingCapacity() - occupiedProcessingCapacity;
		} else {	
			double thisTypeSCProcessingCapacity = 0d; 
			for (ServiceChain scThisType : this.serviceChains.get(sc.getServiceChainType()))
				thisTypeSCProcessingCapacity += scThisType.getProcessingCapacity();
			
			for (Entry<Request, Double> entry : this.admittedRequestsBR.entrySet()) {
				if (entry.getKey().getServiceChainType() == sc.getServiceChainType()) {
					occupiedProcessingCapacity += entry.getValue();
				}
			}
			return thisTypeSCProcessingCapacity - occupiedProcessingCapacity;
		}
	}
	
	public double getProcessingRateCapacityType(Integer type){
		double thisTypeSCProcessingCapacity = 0d; 
		for (ServiceChain scThisType : this.serviceChains.get(type))
			thisTypeSCProcessingCapacity += scThisType.getProcessingCapacity();
		return thisTypeSCProcessingCapacity;
	}
	
	public void reset() {
		this.setAdmittedRequests(new HashMap<ServiceChain, ArrayList<Request>>());
		this.setAdmittedRequestsBR(new HashMap<Request, Double>());
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

	public Switch getAttachedSwitch() {
		return attachedSwitch;
	}

	public void setAttachedSwitch(Switch attachedSwitch) {
		this.attachedSwitch = attachedSwitch;
	}

	public Map<Request, Double> getAdmittedRequestsBR() {
		return admittedRequestsBR;
	}

	public void setAdmittedRequestsBR(Map<Request, Double> admittedRequestsBR) {
		this.admittedRequestsBR = admittedRequestsBR;
	}
}
