package algs.basicrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;

import graph.Node;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import simulation.Parameters;
import simulation.SDNRoutingSimulator;
import system.DataCenter;
import system.InternetLink;
import system.Request;
import utils.Pair;

public class Exact {

	private SDNRoutingSimulator simulator = null;
	
	private ArrayList<Request> requests = null; 
	
	private Map<Integer, ArrayList<Request>> requestsType = new HashMap<Integer, ArrayList<Request>>();
	
	private double totalCost = 0d;
	
	private double averageCost = 0d; 
	
	private int numOfAdmittedReqs = 0;
	
	private double budget = 0d;

	private double optimalThroughput;
	
	public Exact (SDNRoutingSimulator sim, ArrayList<Request> requests, double budgetScaleFactor) {
		this.setSimulator(sim);	
		this.setRequests(requests);
		// organize the requests into different types
		for (Request req : requests) {
			if (null == this.requestsType.get(req.getServiceChainType()))
				this.requestsType.put(req.getServiceChainType(), new ArrayList<Request>());
			this.requestsType.get(req.getServiceChainType()).add(req);
		}
		
		//TODO: adjust or refine the budget calculation
		this.budget = (Parameters.maxLinkCost * (this.simulator.getNetwork().vertexSet().size() - 1) + Parameters.maxServiceChainCost) * Parameters.maxPacketRate * budgetScaleFactor * requests.size();
	}

	public void run() {
		solveLP(true);
	}
	
	public Pair<Double> getDelayCost(DataCenter dc, Request req){
		
		SimpleWeightedGraph<Node, InternetLink> originalGraph = simulator.getNetwork();

		Node sourceSwitch = req.getSourceSwitch();
		Node destSwitch = req.getDestinationSwitches().get(0);
		
		DijkstraShortestPath<Node, InternetLink> shortestPathSToDC = new DijkstraShortestPath<Node, InternetLink>(originalGraph, sourceSwitch, dc.getAttachedSwitch());
		double delay1 = Double.MAX_VALUE; 
		double pathCost1 = Double.MAX_VALUE;
		for (int i = 0; i < shortestPathSToDC.getPathEdgeList().size(); i ++) {
			if (0 == i ) {
				delay1 = 0d;
				pathCost1 = 0d;
			}
			delay1 += shortestPathSToDC.getPathEdgeList().get(i).getLinkDelay();
			pathCost1 += originalGraph.getEdgeWeight(shortestPathSToDC.getPathEdgeList().get(i));
		}
		
		DijkstraShortestPath<Node, InternetLink> shortestPathDCToDest = new DijkstraShortestPath<Node, InternetLink>(originalGraph, dc.getAttachedSwitch(), destSwitch);
		double delay2 = Double.MAX_VALUE; 
		double pathCost2 = Double.MAX_VALUE;
		for (int i = 0; i < shortestPathDCToDest.getPathEdgeList().size(); i ++) {
			if (0 == i ) {
				delay2 = 0d;
				pathCost2 = 0d; 
			}
			delay2 += shortestPathDCToDest.getPathEdgeList().get(i).getLinkDelay();
			pathCost2 += originalGraph.getEdgeWeight(shortestPathDCToDest.getPathEdgeList().get(i));
		}
		
		double delay = delay1 + delay2 + dc.getProcessingDelays()[req.getServiceChainType()];
		double cost = pathCost1 + pathCost2 + dc.getCosts()[req.getServiceChainType()];
	
		return new Pair<Double>(delay, cost);
	}
	
	public void solveLP(boolean relax) {  
		
		try {
			int K = Parameters.serviceChainProcessingDelays.length;
			
			int J = this.getRequests().size();
			int Js [] = new int [K];
			for (int k = 0; k < K; k ++) {
				Js[k] = this.getRequestsType().get(k).size();
			}
			int D = this.getSimulator().getSwitchesAttachedDataCenters().size();// number of data centers.
			
			//variables x_{ij}, for any k. 
			int consSize = D * this.getRequests().size();
			LpSolve solver = LpSolve.makeLp(0, consSize);
			
			// constraint (1)
			for (int k = 0; k < K; k ++) {
				for (int j = 0; j < Js[k]; j ++) {
					double [] cons_1 = new double[consSize];
					
					int preReqNum = 0;
					if (k >= 1) {
						for (int kk = 0; kk < k; kk++) {
							preReqNum += Js[kk];
						}
					}
					
					int j_original = preReqNum + j; 
					for (int d = 0; d < D; d ++) {
						cons_1 [d * J + j_original] = 1;  
					}
					solver.addConstraint(cons_1, LpSolve.LE, 1d);
				}
			}
			
			// constraint (2)
			for (int d = 0; d < D; d ++) {
				for (int k = 0; k < K; k ++) {
					double dcCapacity = this.getSimulator().getSwitchesAttachedDataCenters().get(d).getAttachedDataCenter().getProcessingRateCapacityType(k);
					double [] cons_2 = new double[consSize];
					int preReqNum = 0; 
					if (k >= 1) {
						for (int kk = 0; kk < k; kk++) {
							preReqNum += Js[kk];
						}
					}
					
					for (int j = 0; j < Js[k]; j ++) {
						cons_2[d * J + preReqNum + j] = this.getRequestsType().get(k).get(j).getPacketRate();
					}
					
					solver.addConstraint(cons_2, LpSolve.LE, dcCapacity);
				}
			}
			
			// constraint (3) and (4)
			double [] cons_4 = new double[consSize];
			for (int d = 0; d < D; d ++) {
				for (int j = 0; j < J; j ++) {
					double [] cons_3 = new double[consSize];
					Pair<Double> delayCostPair = this.getDelayCost(this.getSimulator().getSwitchesAttachedDataCenters().get(d).getAttachedDataCenter(), this.getRequests().get(j)); 
					cons_3[d * J + j] = delayCostPair.getA();// delay
					solver.addConstraint(cons_3, LpSolve.LE, this.getRequests().get(j).getDelayRequirement());
					
					cons_4[d * J + j] = delayCostPair.getB(); // cost
				}
			}
			
			solver.addConstraint(cons_4, LpSolve.LE, this.budget);
			
//			for (int i = 1; i <= consSize; i ++)
//				solver.setInt(i, true);
			
			double [] objs = new double[consSize];
			
			for (int d = 0; d < D; d ++) {
				for (int j = 0; j < J; j ++) {
					objs[d * J + j] = this.getRequests().get(j).getPacketRate(); 
				}
			}
			
			solver.setOutputfile(Parameters.LPOutputFile);
			
			solver.setObjFn(objs);
			solver.setPresolve(1, 50000);
			solver.setScaling(1);//;set_scaling(solver, 1);
			solver.setMaxim();
			
			solver.solve();
			
			this.setOptimalThroughput(solver.getObjective());
			
			// number of admitted requests
			double [] variables = solver.getPtrVariables();
			for (int ii = 0; ii < variables.length; ii ++ )
				this.numOfAdmittedReqs += variables[ii];
			
			// cost of implementing admitted requests
			for (int d = 0; d < D; d ++) {
				for (int j = 0; j < J; j ++) {
					if ((d * J + j < variables.length ) && (variables[d * J + j] > 0)) {
						Pair<Double> delayCostPair = this.getDelayCost(this.getSimulator().getSwitchesAttachedDataCenters().get(d).getAttachedDataCenter(), this.getRequests().get(j)); 
						Double cost = delayCostPair.getB();
						if (cost != Double.MAX_VALUE && cost != Double.POSITIVE_INFINITY)
							this.totalCost += variables[d * J + j] * this.getRequests().get(j).getPacketRate() * cost; 
					}
				}
			}
			
			this.averageCost = this.totalCost / this.numOfAdmittedReqs;
			// print solution
			//System.out.println("Lower bound of optimal cost : " + this.optimalCostLowerBound);
			//double[] vars = solver.getPtrVariables();
			// delete the problem and free memory
			solver.deleteLp();

		} catch (LpSolveException e) {
			e.printStackTrace();
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

	public Map<Integer, ArrayList<Request>> getRequestsType() {
		return requestsType;
	}

	public void setRequestsType(Map<Integer, ArrayList<Request>> requestsType) {
		this.requestsType = requestsType;
	}

	public double getOptimalThroughput() {
		return optimalThroughput;
	}

	public void setOptimalThroughput(double optimalThroughput) {
		this.optimalThroughput = optimalThroughput;
	}
	
	
}
