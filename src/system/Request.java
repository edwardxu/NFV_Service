package system;

import java.util.ArrayList;
import java.util.Collections;

public class Request {
	
	private double ID;
	
	private Switch sourceSwitch = null;
	
	// multicast request
	private ArrayList<Switch> destinationSwitches = null;
	
	private int serviceChainType = -1;
	
	private double delayRequirement = 0d;//
	
	private double dataRate = 0d;
		
	public Request(double id, Switch sourceSwitch, Switch destinationSwitch, ArrayList<Switch> destinationSwitches, double dataRate, int serviceChainType, double delayRequirement){
		this.setID(id);
		this.setSourceSwitch(sourceSwitch);
		this.setDestinationSwitches(destinationSwitches);
		this.setDataRate(dataRate); 
		this.setServiceChainType(serviceChainType);
		this.setDelayRequirement(delayRequirement);
		// sort destination nodes in to increasing order of their IDs. 
		Collections.sort(this.getDestinationSwitches());
	}

	public Switch getSourceSwitch() {
		return sourceSwitch;
	}

	public void setSourceSwitch(Switch sourceSwitch) {
		this.sourceSwitch = sourceSwitch;
	}

	public ArrayList<Switch> getDestinationSwitches() {
		return destinationSwitches;
	}

	public void setDestinationSwitches(ArrayList<Switch> destinationSwitches) {
		this.destinationSwitches = destinationSwitches;
	}
	
//	@Override
//	public boolean equals(Object another) {
//		if (this == another)
//			return true;
//
//		if (!(another instanceof MulticastRequest))
//			return false;
//
//		if (!this.getSourceSwitch().equals(((MulticastRequest) another).getSourceSwitch()))
//			return false;
//		
//		if (this.getDestinationSwitches().size() != ((MulticastRequest) another).getDestinationSwitches().size())
//			return false;
//		
//		// check destination switches, whether they are the same. 
//		for (int i = 0; i < this.getDestinationSwitches().size(); i ++){
//			if (!this.getDestinationSwitches().get(i).equals(((MulticastRequest) another).destinationSwitches.get(i)))
//				return false;
//		}
//		
//		return true;
//	}
	
	@Override
	public boolean equals(Object another) {
		if (this == another)
			return true;

		if (!(another instanceof Request))
			return false;

		if (this.getID() == ((Request) another).getID())
			return true;
		else 
			return false;
	}

	public double getID() {
		return ID;
	}

	public void setID(double iD) {
		ID = iD;
	}

	public int getServiceChainType() {
		return serviceChainType;
	}

	public void setServiceChainType(int serviceChainType) {
		this.serviceChainType = serviceChainType;
	}

	public double getDelayRequirement() {
		return delayRequirement;
	}

	public void setDelayRequirement(double delayRequirement) {
		this.delayRequirement = delayRequirement;
	}

	public double getDataRate() {
		return dataRate;
	}

	public void setDataRate(double dataRate) {
		this.dataRate = dataRate;
	}
}
