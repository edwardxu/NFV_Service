package algs.flow;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import system.DataCenter;
import graph.Node;

public final class MCMC {
	// ~ Static fields/initializers
	// ---------------------------------------------

	/**
	 * Default tolerance.
	 */
	public static final double DEFAULT_EPSILON = 0.01;

	// ~ Instance fields
	// --------------------------------------------------------

	private ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> network; // our network
	private double epsilon; // tolerance (DEFAULT_EPSILON or user-defined)
	private double delta;

	private List<Commodity> commodities;// commodities
	
	private double budget = 0;
	
	private double costLength = 0;
	
	//private Map<Integer, RequestAssign> assigns;
	
	//private double budgetTest = -1;
	
	//private Map<Integer, Double> hourlyBudget;

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Constructs <tt>MultiCommodityMinCostFlow</tt> instance to work with <i>a
	 * copy of</i> <tt>network</tt>. Current source and sink are set to
	 * <tt>null</tt>. If <tt>network</tt> is weighted, then capacities are
	 * weights, otherwise all capacities are equal to one.
	 * 
	 * @param network
	 *            network, where maximum flow will be calculated
	 * @param epsilon
	 *            tolerance for comparing doubles
	 * @param commodities
	 * 			  commodities that need to route           
	 * @param delayconstraint 
	 */
	public MCMC(ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> network, double epsilon, List<Commodity> commodities) {
		
		if (network == null) {
			throw new NullPointerException("network is null");
		}
		if (epsilon <= 0) {
			throw new IllegalArgumentException(
					"invalid epsilon (must be positive)");
		}
		if (commodities.isEmpty()) {
			throw new IllegalArgumentException(
					"invalid commodities (must not be empty)");
		}
		
		//this.assigns = new HashMap<Integer, RequestAssign>();

		this.network = network;
		this.epsilon = epsilon;
		this.commodities = commodities;
		
		//this.setBudgetTest(testBudget);
		//this.setHourlyBudget(hourlyBudget);
		
		InitializeBudget();
		InitializeLengths();
	}

	// ~ Methods
	// ----------------------------------------------------------------

	private void InitializeBudget() {
		
		double maxBudget = 0;
		
		for (MinCostFlowEdge edge : this.network.edgeSet()) {
			maxBudget += edge.getCost();
		}
		
		maxBudget *= this.getCommodities().size();
		
		this.setBudget(maxBudget);
		
		for (MinCostFlowEdge e : network.edgeSet()) {
			MinCostFlowEdge edge = (MinCostFlowEdge) e;
			if (edge.getCapacity() < -epsilon) {
				throw new IllegalArgumentException(
						"invalid capacity (must be non-negative)");
			}

			if (network.getEdgeWeight(e) < -epsilon) {
				throw new IllegalArgumentException(
						"invalid capacity (must be non-negative)");
			}	
		}
	}

	// Initialise edge lengths representing the values of potential functions
	private void InitializeLengths() {

		if (!(this.network instanceof ListenableDirectedWeightedGraph)) {
			return;
		}
		
		double edges = this.network.edgeSet().size();
		this.delta = Math.pow(edges/(1.0 - this.epsilon), -1.0/this.epsilon);

		for (MinCostFlowEdge e : this.network.edgeSet()) {
			double length = this.delta / e.getCapacity();
			e.setLength(length);
		}
		this.costLength = this.delta / this.getBudget();
	}

	public ListenableDirectedWeightedGraph<Node, MinCostFlowEdge> calcMinCostFlow() {

		int assCount = 0;
		
		int iterCount = 0;
		while (this.getDualValue() < 1) {

			iterCount ++;
			
			for (Commodity comm : this.commodities) {
				
				comm.setCurrDemand(comm.getDemand());
				
				while ((this.getDualValue() < 1) && (comm.getCurrDemand() > 0)) {

					// update weights of each edge.
					for (MinCostFlowEdge e : this.network.edgeSet()) {
						((ListenableDirectedWeightedGraph<Node, MinCostFlowEdge>) this.network)
								.setEdgeWeight(e, e.getLength() + e.getCost() * this.getCostLength());
						
//						if((e.getSource() instanceof WebPortal)&&(e.getTarget() instanceof DataCenter)){
//							System.out.println(((Node)e.getSource()).getID() + "-->" + ((Node)e.getTarget()).getID()+ ":"+((SimpleDirectedWeightedGraph<Node, MinCostFlowEdge>) this.network).getEdgeWeight(e));
//						}
					}
					
					// Find a shortest path
					DijkstraShortestPath<Node, MinCostFlowEdge> shortestPath = new DijkstraShortestPath<Node, MinCostFlowEdge>(this.network, (Node)comm.getSource(), (Node)comm.getSink());
					List<MinCostFlowEdge> pathEdgesList = shortestPath.getPathEdgeList(); 
					
					double amountCanRoute =  pathEdgesList.get(0).getCapacity();
					for (MinCostFlowEdge edge : pathEdgesList){
						if (amountCanRoute > edge.getCapacity())
							amountCanRoute = edge.getCapacity();
					}
					
					if(amountCanRoute > comm.getCurrDemand()){
						amountCanRoute = comm.getCurrDemand();
					}
					
					double pathUnitCost = 0;
					for (MinCostFlowEdge tempEdge : pathEdgesList){
						pathUnitCost += tempEdge.getCost();
					}
					
					double pathCost = amountCanRoute * pathUnitCost;
					if ( pathCost > this.getBudget()){
						amountCanRoute = amountCanRoute * (1 - (pathCost - this.getBudget())/pathCost); 
					}
					pathCost = amountCanRoute * pathUnitCost;
					
					comm.setCurrDemand(comm.getCurrDemand() - amountCanRoute);
					
					// augment flow and update lengths
					for (MinCostFlowEdge edge : pathEdgesList){
						edge.addFlow(amountCanRoute);
						double newLength = edge.getLength()*(1 + this.epsilon * (amountCanRoute/edge.getCapacity()));
						edge.setLength(newLength);
					}
					double newCostLength = this.getCostLength()*(1 + this.epsilon*(pathCost/this.getBudget()));
					this.setCostLength(newCostLength);
					
				}
			}
		}
		
		//scale down flows
		//first find the maximum overflow edge
		double overflowFactor = 0;
		for(MinCostFlowEdge edge : this.network.edgeSet()){	
			if( (edge.getSource() instanceof DataCenter) && (edge.getTarget() instanceof DataCenter)){
				double flow = edge.getFlows();
				double capacity = edge.getCapacity();
				if (overflowFactor < (flow/capacity)){
					overflowFactor = flow/capacity;
				}
			}
		}
		
		overflowFactor = Math.log((1+this.epsilon)/this.delta)/Math.log(1 + this.epsilon);
		
		if (overflowFactor < iterCount){
			overflowFactor = iterCount;
		}
		
		//double iterTest = 1 + (1/this.epsilon)*(Math.log(this.network.edgeSet().size()/(1+this.epsilon))/Math.log(1 + this.epsilon));
		
		// then scale down the flows of all edges
		if (overflowFactor > 1){
			for(MinCostFlowEdge edge : this.network.edgeSet()){
				double currFlow = edge.getFlows();
				edge.scaleFlow(overflowFactor);
				double scaledFlow = edge.getFlows();
				//System.out.println("Capacity:"+ edge.getCapacity()+"; Before scale down:" + currFlow + "; After scale down:" + scaledFlow);
			}
			
		} 
//		else{
//			for(MinCostFlowEdge edge : this.network.edgeSet()){
//				if((edge.getSource() instanceof WebPortal)&&(edge.getTarget() instanceof DataCenter)){
//					System.out.println("Capacity:"+ edge.getCapacity()+"; Flow:"+edge.getFlows());
//				}
//			}
//		}
		
		//
		
		return this.network;
	}

	private double getDualValue() {

		if (!(this.network instanceof ListenableDirectedWeightedGraph)) {
			return -1;
		}
		
		double length = 0;
		double addLength = 0;

		for (MinCostFlowEdge edge : this.network.edgeSet()) {
			addLength = edge.getCapacity()*edge.getLength();
			length += addLength;
		}
		length += this.getBudget() * this.getCostLength();
		return length;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public List<Commodity> getCommodities() {
		return commodities;
	}

	public void setCommodities(List<Commodity> commodities) {
		this.commodities = commodities;
	}

	public double getBudget() {
		return budget;
	}

	public void setBudget(double budget) {
		this.budget = budget;
	}

	public double getCostLength() {
		return costLength;
	}

	public void setCostLength(double costLength) {
		this.costLength = costLength;
	}
}