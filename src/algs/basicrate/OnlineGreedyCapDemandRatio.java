package algs.basicrate;

import java.util.ArrayList;
import java.util.Map;

import simulation.SDNRoutingSimulator;
import system.DataCenter;
import system.Request;
import system.ServiceChain;
import utils.HTriple;

public class OnlineGreedyCapDemandRatio {

	private SDNRoutingSimulator simulator = null;
	
	private ArrayList<Request> requests = null;//all requests that arrive at the system one by one
		
	private double totalCost = 0d;
	
	private double averageCost = 0d; 
	
	private int numOfAdmittedReqs = 0;
	
	private double totalPktRateOfAdmittedReqs = 0d;

	public OnlineGreedyCapDemandRatio (SDNRoutingSimulator sim, ArrayList<Request> requests) {
		this.setSimulator(sim);	
		this.setRequests(requests);	
	}
	
	// greedy approach that processes requests greedily without resource reservation
	public void run() {
		// online algorithm based on primal-dual approach.
		for (Request request : this.getRequests()) {
			// dummy service chain instance for this request. 
			ServiceChain dummySC = new ServiceChain(SDNRoutingSimulator.idAllocator.nextId(), "Dummy SC", request.getServiceChainType(), true);
			// find the data center that can meet the delay requirement of this request. 
			HTriple<ArrayList<DataCenter>, Map<DataCenter, Double>, Map<DataCenter, Double>> retTripleDCListDelays = Online.getDataCentersMeetDelayRequirement(this.getSimulator(), request);
			ArrayList<DataCenter> dcsMeetDelay = retTripleDCListDelays.getA();
			//Map<DataCenter, Double> delaysForThisReq = retTripleDCListDelays.getB();
			Map<DataCenter, Double> costsForThisReq = retTripleDCListDelays.getC();
			
			// admit this request into the data center that achieves the minimum cost. 
			DataCenter dcMinCapDemandRatio = null; 
			Double minDemandCapRatio = Double.MAX_VALUE;
			for (DataCenter dc : dcsMeetDelay) {
				
				double capDemandRatio = dc.getAvailableProcessingRate(dummySC, true);
				
				if (minDemandCapRatio > capDemandRatio) {
					minDemandCapRatio = capDemandRatio;
					dcMinCapDemandRatio = dc; 
				}
			}
			
			if (null != dcMinCapDemandRatio) {
				// admit this request.
				if (dcMinCapDemandRatio.admitRequest(request, request.getPacketRate(), dummySC, true)) {
					this.numOfAdmittedReqs ++;
					this.totalCost += costsForThisReq.get(dcMinCapDemandRatio);
					this.totalPktRateOfAdmittedReqs += request.getPacketRate();
				}
			}
		}
		
		this.averageCost = this.totalCost / this.numOfAdmittedReqs;
		
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
