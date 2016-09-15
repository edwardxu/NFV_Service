package graph;

public class TreeEdge<T> {
	
	T parent;
	T child;
	
	public TreeEdge(T parent, T child){
		this.parent = parent; 
		this.child = child; 
	}
	
	@Override
	public boolean equals(Object another) {
		if (this == another)
			return true;

		if (!(another instanceof TreeEdge))
			return false;

		if (this.parent.equals(((TreeEdge)another).parent) && this.child.equals(((TreeEdge)another).child))
			return true;
		else
			return false;
	}
}
