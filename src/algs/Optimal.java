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
	
	private ArrayList<Double> accumulativeCost = null;
	
	private ArrayList<Integer> numOfAdmittedReqs = null;
	
	public Optimal(SDNRoutingSimulator sim) {
		this.simulator = sim;
		// set alpha and beta
		
		this.setAccumulativeCost(new ArrayList<Double>(sim.getMulticastRequests().size()));
		this.setNumOfAdmittedReqs(new ArrayList<Integer>(sim.getMulticastRequests().size()));
	}
	
	public void run() {
		// the optimal algorithm. 
		
		SimpleWeightedGraph<Node, InternetLink> originalGraph = simulator.getNetwork();
		
		double approCost = 0d;
		int numAdmitted = 0; 
			
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

	public ArrayList<Double> getAccumulativeCost() {
		return accumulativeCost;
	}

	public void setAccumulativeCost(ArrayList<Double> accumulativeCost) {
		this.accumulativeCost = accumulativeCost;
	}

	public ArrayList<Integer> getNumOfAdmittedReqs() {
		return numOfAdmittedReqs;
	}

	public void setNumOfAdmittedReqs(ArrayList<Integer> numOfAdmittedReqs) {
		this.numOfAdmittedReqs = numOfAdmittedReqs;
	}
}
