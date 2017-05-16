package algs.basicrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;

import graph.Node;
import simulation.SDNRoutingSimulator;
import system.DataCenter;
import system.InternetLink;
import system.Request;
import system.ServiceChain;
import system.Switch;

public class GreedySplittable {

	private SDNRoutingSimulator simulator = null;

	private ArrayList<Request> requests = null;

	private double totalCost = 0d;

	private double averageCost = 0d;

	private int numOfAdmittedReqs = 0;
	
	private double totalPktRateOfAdmittedReqs = 0d;

	public GreedySplittable(SDNRoutingSimulator sim, ArrayList<Request> requests) {
		if (sim == null || requests == null || requests.isEmpty())
			throw new IllegalArgumentException("Simulator, request list should not be null or empty!");

		this.simulator = sim;
		this.requests = requests;
	}

	public void run(boolean basicRate) {
		// greedily select a data center that has the most available resource
		// for each request.
		Set<Request> admittedReqs = new HashSet<Request>();

		Map<Request, Double> toBeAdmittedReqs = new HashMap<Request, Double>();
		for (Request req : this.requests)
			toBeAdmittedReqs.put(req, req.getPacketRate());

		Map<Request, Map<DataCenter, Double>> preAdmissionsPktRate = new HashMap<Request, Map<DataCenter, Double>>();
		Map<Request, Map<DataCenter, Double>> preAdmissionsCost = new HashMap<Request, Map<DataCenter, Double>>();

		while (!toBeAdmittedReqs.isEmpty()) {

			Iterator<Entry<Request, Double>> iter = toBeAdmittedReqs.entrySet().iterator();
			
			while (iter.hasNext()) {

				Entry<Request, Double> entry = iter.next();
				Request req = entry.getKey();
				double packetRate = entry.getValue();

				Switch sourceSwitch = req.getSourceSwitch();
				Switch destSwitch = req.getDestinationSwitches().get(0);

				DataCenter dcWithMinCost = null;
				double minCost = Double.MAX_VALUE;
				double minCostDCAvail = 0d;
				for (Switch swDC : this.simulator.getSwitchesAttachedDataCenters()) {

					DataCenter dc = swDC.getAttachedDataCenter();
					double avail = dc.getAvailableProcessingRate(
							(ServiceChain) dc.getServiceChains().get(req.getServiceChainType()).toArray()[0], true);

					if (avail <= 0)
						continue;

					double delay1 = 0d;
					double pathCost1 = 0d;
					if (!sourceSwitch.equals(swDC)) {
						DijkstraShortestPath<Node, InternetLink> shortestPathSToDC = new DijkstraShortestPath<Node, InternetLink>(
								this.simulator.getNetwork(), sourceSwitch, swDC);
						delay1 = Double.MAX_VALUE;
						pathCost1 = Double.MAX_VALUE;
						for (int i = 0; i < shortestPathSToDC.getPathEdgeList().size(); i++) {
							if (0 == i) {
								delay1 = 0d;
								pathCost1 = 0d;
							}
							delay1 += shortestPathSToDC.getPathEdgeList().get(i).getLinkDelay();
							pathCost1 += this.simulator.getNetwork()
									.getEdgeWeight(shortestPathSToDC.getPathEdgeList().get(i));
						}
					}

					double delay2 = 0d;
					double pathCost2 = 0d;
					if (!destSwitch.equals(swDC)) {
						DijkstraShortestPath<Node, InternetLink> shortestPathDCToDest = new DijkstraShortestPath<Node, InternetLink>(
								this.simulator.getNetwork(), swDC, destSwitch);
						delay2 = Double.MAX_VALUE;
						pathCost2 = Double.MAX_VALUE;
						for (int i = 0; i < shortestPathDCToDest.getPathEdgeList().size(); i++) {
							if (0 == i) {
								delay2 = 0d;
								pathCost2 = 0d;
							}
							delay2 += shortestPathDCToDest.getPathEdgeList().get(i).getLinkDelay();
							pathCost2 += this.simulator.getNetwork()
									.getEdgeWeight(shortestPathDCToDest.getPathEdgeList().get(i));
						}
					}

					double delay = delay1 + delay2 + dc.getProcessingDelays()[req.getServiceChainType()];
					double costThisDC = pathCost1 + pathCost2 + dc.getCosts()[req.getServiceChainType()];

					if (delay <= req.getDelayRequirement()) {
						if (minCost > costThisDC) {
							minCost = costThisDC;
							dcWithMinCost = dc;
							minCostDCAvail = avail;
						}
					}
				}

				if (null == dcWithMinCost) {
					// reject this request.
					// check whether this request is partially admitted in other
					// data centers.
					if (null != preAdmissionsCost.get(req)) {
						for (Entry<DataCenter, Double> entryAd : preAdmissionsCost.get(req).entrySet()) {
							entryAd.getKey().removeRequest(req, true);
							this.totalCost -= entryAd.getValue();
							this.totalPktRateOfAdmittedReqs -= preAdmissionsPktRate.get(req).get(entryAd.getKey());
						}
					}
					admittedReqs.remove(req);
					preAdmissionsCost.remove(req);
					preAdmissionsPktRate.remove(req);

					iter.remove();
					continue;
				} else {
					double admittedPacketRate = (packetRate > minCostDCAvail) ? minCostDCAvail : packetRate;
					dcWithMinCost.admitRequest(req, admittedPacketRate,
							(ServiceChain) dcWithMinCost.getServiceChains().get(req.getServiceChainType()).toArray()[0],
							true);
					admittedReqs.add(req);
					// calculate the cost of implementing this request;
					this.totalCost += (minCost * admittedPacketRate);
					this.totalPktRateOfAdmittedReqs += admittedPacketRate; 

					// preAdmissions.
					if (null == preAdmissionsCost.get(req)) {
						preAdmissionsCost.put(req, new HashMap<DataCenter, Double>());
						preAdmissionsPktRate.put(req, new HashMap<DataCenter, Double>());
					}
					preAdmissionsCost.get(req).put(dcWithMinCost, (minCost * admittedPacketRate));
					preAdmissionsPktRate.get(req).put(dcWithMinCost, admittedPacketRate);
					
					if (0 < packetRate - admittedPacketRate) {
						toBeAdmittedReqs.put(req, packetRate - admittedPacketRate);
					} else {
						iter.remove();
					}
				}
			}
		}

		this.numOfAdmittedReqs = admittedReqs.size();
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
