package graph;

import java.util.ArrayList;

public class Path<T> {

	private ArrayList<T> edges = null;
	
	public Path(ArrayList<T> eds){
		this.setEdges(eds);
	}

	public ArrayList<T> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<T> edges) {
		this.edges = edges;
	}
}
