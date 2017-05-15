package algs.basicrate;

import java.util.ArrayList;
import java.util.Map;

import simulation.SDNRoutingSimulator;
import system.DataCenter;
import system.Request;
import system.ServiceChain;
import utils.HTriple;

public class OnlineGreedy {

	private SDNRoutingSimulator simulator = null;
	
	private ArrayList<Request> requests = null;//all requests that arrive at the system one by one
		
	private double totalCost = 0d;
	
	private double averageCost = 0d; 
	
	private int numOfAdmittedReqs = 0;
	
	private double totalPktRateOfAdmittedReqs = 0d;

	public OnlineGreedy (SDNRoutingSimulator sim, ArrayList<Request> requests, double budgetScaleFactor) {
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
			DataCenter dcMinCost = null; 
			Double minCost = Double.MAX_VALUE;
			for (DataCenter dc : dcsMeetDelay) {
				if (dc.getAvailableProcessingRate(dummySC, true) >= request.getPacketRate()){
					if (minCost > costsForThisReq.get(dc)){
						minCost = costsForThisReq.get(dc);
						dcMinCost = dc; 
					}	
				}
			}
			
			if (null != dcMinCost){
				// admit this request.
				dcMinCost.admitRequest(request, request.getPacketRate(), dummySC, true);
				this.numOfAdmittedReqs ++;
				this.totalCost += costsForThisReq.get(dcMinCost);
				this.totalPktRateOfAdmittedReqs += request.getPacketRate();
			}
		}
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
