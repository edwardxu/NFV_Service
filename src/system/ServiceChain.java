package system;

import graph.Node;
import simulation.Parameters;
import utils.RanNum;

public class ServiceChain extends Node {
	
	private int serviceChainType = -1;
		
	private double processingCapacity = 0d;
	
	private double computingResourceDemand = 0d;
	
	public ServiceChain(double id, String name) {
		super(id, name);
	}
	
	public ServiceChain(double id, String name, int serviceChainType){
		super(id, name);
		this.setServiceChainType(serviceChainType);

		double minProcessingCapacity = Parameters.serviceChainProcessingCapacities[serviceChainType][0];
		double maxProcessingCapacity = Parameters.serviceChainProcessingCapacities[serviceChainType][1];
		this.setProcessingCapacity(RanNum.getRandomDoubleRange(maxProcessingCapacity, minProcessingCapacity));
	}
	
	public ServiceChain(double id, String name, int serviceChainType, double computingDem, double processingCapacity){
		super(id, name);
		this.setComputingResourceDemand(computingDem);
		this.setProcessingCapacity(processingCapacity);
		this.setServiceChainType(serviceChainType);
	}
	
	public double getComputingResourceDemand() {
		return computingResourceDemand;
	}

	public void setComputingResourceDemand(double computingResourceDemand) {
		this.computingResourceDemand = computingResourceDemand;
	}

	public double getProcessingCapacity() {
		return processingCapacity;
	}

	public void setProcessingCapacity(double processingCapacity) {
		this.processingCapacity = processingCapacity;
	}

	public int getServiceChainType() {
		return serviceChainType;
	}

	public void setServiceChainType(int serviceChainType) {
		this.serviceChainType = serviceChainType;
	}
}
