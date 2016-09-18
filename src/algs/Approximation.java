package algs;

import java.util.ArrayList;

import simulation.Initialization;
import simulation.SDNRoutingSimulator;
import system.DataCenter;
import system.Request;
import system.Switch;

public class Approximation {
	private SDNRoutingSimulator simulator = null;
	private double totalCost = 0d;
	private double averageCost = 0d; 
	private int numOfAdmittedReqs = 0;
	
	public Approximation(SDNRoutingSimulator sim) {
		this.simulator = sim;
	}
	
	public void run() {
		// the approximation algorithm.
		ArrayList<Request> virtualRequests = Initialization.generateVirtualRequests(this.simulator.getUnicastRequests());
		
		Optimal optimalAlg = new Optimal(this.simulator, virtualRequests);
		optimalAlg.run();
		
		// TODO: adjust the solution to make it feasible. 
		for (Switch sw : this.simulator.getSwitchesAttachedDataCenters()){
			DataCenter dc = sw.getAttachedDataCenter();
		}
	}
	
	public SDNRoutingSimulator getSimulator() {
		return simulator;
	}
	public void setSimulator(SDNRoutingSimulator simulator) {
		this.simulator = simulator;
	}
	public double getAverageCost() {
		return averageCost;
	}
	public void setAverageCost(double averageCost) {
		this.averageCost = averageCost;
	}
	public double getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
	public int getNumOfAdmittedReqs() {
		return numOfAdmittedReqs;
	}
	public void setNumOfAdmittedReqs(int numOfAdmittedReqs) {
		this.numOfAdmittedReqs = numOfAdmittedReqs;
	}
}
