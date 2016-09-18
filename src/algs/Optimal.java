package algs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import graph.Node;
import simulation.SDNRoutingSimulator;
import system.InternetLink;
import system.Request;
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
		
		double cost = 0d;
		int numAdmitted = 0;
		
		for (Request req : this.simulator.getUnicastRequests()){
			// admit this request or not
			
			
		}
		
	}
	
	private SimpleDirectedWeightedGraph<Node, InternetLink> constructAuxiliaryGraph(
			SimpleWeightedGraph<Node, InternetLink> originalGraph, 
			Request request, 
			ArrayList<Switch> serversSwitches, 
			Switch source, 
			Switch virtual_source, 
			Set<InternetLink> linksToReserveBandwidth, 
			Set<Node> terminals, 
			Set<Node> terminalsInAuGraph
			) {
		
		SimpleDirectedWeightedGraph<Node, InternetLink> auxiliaryGraph = new SimpleDirectedWeightedGraph<Node, InternetLink>(InternetLink.class);
		auxiliaryGraph.addVertex(virtual_source);

		Map<Switch, ArrayList<Switch>> switchToVirtualSwitches = new HashMap<Switch, ArrayList<Switch>>();
		
		
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
