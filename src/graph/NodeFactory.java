/* -------------------------
 * NodeFactory.java
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

import java.lang.reflect.Constructor;
import org.jgrapht.VertexFactory;

public class NodeFactory<V> implements VertexFactory<V> {
	// ~ Instance fields
	// --------------------------------------------------------

	public static int nodesProduced = 0;
	
	private NodeInitialParameters ni = null;
	
	private final Class<? extends V> vertexClass;

	/*
	 * In mode 0(g == null), nodes are generated one by one. In mode 1(g !=
	 * null), the vertices are generated from a existing graph.
	 */

	// ~ Constructors
	// -----------------------------------------------------------

	public NodeFactory(Class<? extends V> vertexClass, NodeInitialParameters ni) {
		this.vertexClass = vertexClass;
		this.ni = ni;
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * @see VertexFactory#createVertex()
	 */
	public V createVertex() {
		try {
			Constructor con = this.vertexClass
					.getConstructor(new Class[] { NodeInitialParameters.class });
			if (null != con) {
				return (V) con.newInstance(ni);
			} else {
				return this.vertexClass.newInstance();
			}
		} catch (Exception e) {
			throw new RuntimeException("Vertex factory failed", e);
		}

	}
}