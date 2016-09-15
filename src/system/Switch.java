package system;

import graph.Node;
import graph.NodeInitialParameters;

public class Switch extends Node implements Comparable<Switch>{

	private DataCenter attachedDataCenter = null;
	
	// used in the construction of auxiliary graphs
	private Switch parent = null;
	
	public Switch(NodeInitialParameters ni){
		super(ni.id, ni.name);
	}
	
	public Switch(double id, String name) {
		super(id, name);
	}

	public DataCenter getAttachedDataCenter() {
		return attachedDataCenter;
	}

	public void setAttachedServer(DataCenter attachedDataCenter) {
		this.attachedDataCenter = attachedDataCenter;
	}
	
	public Switch getParent() {
		return parent;
	}

	public void setParent(Switch parent) {
		this.parent = parent;
	}

	public void reset(){
		if (this.attachedDataCenter != null)
			this.attachedDataCenter.reset();
	}

	@Override
	public int compareTo(Switch o) {
		// TODO Auto-generated method stub
		return (int) (this.getID() - o.getID());
	}
}