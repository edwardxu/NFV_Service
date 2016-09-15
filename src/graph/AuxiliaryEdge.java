package graph;

public class AuxiliaryEdge<V, E> {

	private V parent = null;
	
	private double weigth = -1d;
	
	private AuxiliaryNode<V> source = null;
	
	private AuxiliaryNode<V> target = null;
	
	public AuxiliaryEdge(V par, AuxiliaryNode<V> source, AuxiliaryNode<V> target){
		this.setParent(par); 
		this.setSource(source); 
		this.setTarget(target); 
	}

	public V getParent() {
		return parent;
	}

	public void setParent(V parent) {
		this.parent = parent;
	}

	public double getWeigth() {
		return weigth;
	}

	public void setWeigth(double weigth) {
		this.weigth = weigth;
	}

	public AuxiliaryNode<V> getSource() {
		return source;
	}

	public void setSource(AuxiliaryNode<V> source) {
		this.source = source;
	}

	public AuxiliaryNode<V> getTarget() {
		return target;
	}

	public void setTarget(AuxiliaryNode<V> target) {
		this.target = target;
	}
	
	@Override
	public boolean equals(Object another) {

		// Check for self-comparison
		if (this == another)
			return true;

		// Use instanceof instead of getClass here for two reasons
		// 1. if need be, it can match any supertype, and not just one class;
		// 2. it renders an explict check for "that == null" redundant, since
		// it does the check for null already - "null instanceof [type]" always
		// returns false.
		if (!(another instanceof AuxiliaryEdge))
			return false;

		Object thisS = this.getSource();
		
		@SuppressWarnings("unchecked")
		AuxiliaryEdge<V, E> auxiliaryEdge = (AuxiliaryEdge<V, E>) another;
		Object anotherS = auxiliaryEdge.getSource();

		Object thisT = this.getTarget();
		Object anotherT = auxiliaryEdge.getTarget();

		// The algorithm only accepts comparison between identical V types.
		if ((!(thisS instanceof Node)) && (!(anotherS instanceof Node))
				&& (!(thisT instanceof Node)) && (!(anotherT instanceof Node)))
			return false;
		
		if((((Node)thisS).getID() == ((Node)anotherS).getID())&&(((Node)thisT).getID() == ((Node)anotherT).getID()))
			return true;
		
		return false;
	}
}
