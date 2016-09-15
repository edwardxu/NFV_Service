package graph;

public class AuxiliaryNode<T> extends Node {
	
	private double weight = -1d;
	
	private T parent = null;
	
	public AuxiliaryNode(double id, String name, T par) {
		super(id, name);
		this.setParent(par);
	}

	public T getParent() {
		return parent;
	}

	public void setParent(T parent) {
		this.parent = parent;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
