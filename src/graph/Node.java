/* -------------------------
 * Node.java
 * -------------------------
 *
 * Original Author:  Zichuan Xu.
 *
 * The node class V need to have four parameters: id, x location, y location, rest energy and current energy.
 *
 * Changes
 * -------
 * 2-Dec-2011 : Initial revision (GB);
 *
 */

package graph;

public class Node {
	
	/**
	 * 
	 */
	private double ID;

	private String name;

	transient private NodeType nodeType;
	
	private int x; 
	
	private int y;
	
	private double weight;

	public Node(double id, String name) {
		this.ID = id;
		this.name = name;
	}
	
	public Node(double id, String name, int x, int y) {
		this.ID = id;
		this.name = name;
		this.x = x;
		this.y = y;
	}

	public double getID() {
		return ID;
	}

	public void setID(int id) {
		this.ID = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType type) {
		this.nodeType = type;
	}

	@Override
	public boolean equals(Object another) {
		if (this == another)
			return true;

		if (!(another instanceof Node))
			return false;

		if (this.ID == ((Node) another).getID())
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		return this.ID + this.name;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

}
