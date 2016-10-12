package algs.flow;

import org.jgrapht.graph.DefaultWeightedEdge;

import graph.Node;


public class MinCostFlowEdge extends DefaultWeightedEdge{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 7286332007574167278L;
	
	private double cost;
	private double capacity;
	
	// potentials 
	private double length;
	private double costLength;	
	
	//private double flows = 0;
	private double flows = 0;
	
	/**
	 * Default constructor 
	 */
	public MinCostFlowEdge(){
		super();
	}
	
	/**
	 * Retrieves the source of this edge. 
	 * 
	 * @return source of this edge
	 */
	public Object getSource() {
		return super.getSource();
	}

	/**
	 * Retrieves the target of this edge.
	 * 
	 * @return target of this edge
	 */
	public Object getTarget() {
		return super.getTarget();
	}
	
	@Override
	public String toString() {
		//return super.toString();
		return "Capacity: " + this.capacity + "; Length:"+ this.length + "; Cost:" + this.cost + "; Flow:" + this.flows;
	}
	
//	@Override
//	public boolean equals(Object another) {
//
//		// Check for self-comparison
//		if (this == another)
//			return true;
//
//		// Use instanceof instead of getClass here for two reasons
//		// 1. if need be, it can match any supertype, and not just one class;
//		// 2. it renders an explict check for "that == null" redundant, since
//		// it does the check for null already - "null instanceof [type]" always
//		// returns false.
//		if (!(another instanceof MinCostFlowEdge))
//			return false;
//
//		Object thisS = this.getSource();
//		Object anotherS = ((MinCostFlowEdge) another).getSource();
//
//		Object thisT = this.getTarget();
//		Object anotherT = ((MinCostFlowEdge) another).getTarget();
//
//		// The algorithm only accepts comparison between identical V types.
//		if ((!(thisS instanceof Node)) && (!(anotherS instanceof Node))
//				&& (!(thisT instanceof Node)) && (!(anotherT instanceof Node)))
//			return false;
//		
//		if((((Node)thisS).getID() == ((Node)anotherS).getID())&&(((Node)thisT).getID() == ((Node)anotherT).getID()))
//			return true;
//		
//		return false;
//	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getCostLength() {
		return costLength;
	}

	public void setCostLength(double costLength) {
		this.costLength = costLength;
	}
	
	public void addFlow(double amount){
		this.flows += amount;
	}
	
	public double getFlows(){
		return this.flows;
	}
	
	public void clearFlows(){
		this.flows = 0;
	}
	
	public void scaleFlow(double scaleFactor){
		this.flows = this.flows / scaleFactor;
	}
}
