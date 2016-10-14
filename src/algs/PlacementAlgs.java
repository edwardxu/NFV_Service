package algs;

import java.util.ArrayList;

import org.jgrapht.graph.SimpleWeightedGraph;

import graph.Node;
import simulation.Parameters;
import system.InternetLink;
import utils.RanNum;

public class PlacementAlgs {

	public static double TOP_DEGREE = 11111d;
	
	public static double RANDOM = 11112d;
	
	private ArrayList<Node> nodesWithServers = null;
	
	public PlacementAlgs(double placeStrategy, SimpleWeightedGraph<Node, InternetLink> network){
		
		if(placeStrategy == PlacementAlgs.TOP_DEGREE){
			placeServersTopDegrees(network);
		} else if (placeStrategy == PlacementAlgs.RANDOM){
			placeServersRandomly(network);
		}
		
	}
	
	public ArrayList<Node> placeServersTopDegrees(SimpleWeightedGraph<Node, InternetLink> network){
		//TODO place servers in a network
		nodesWithServers = new ArrayList<Node>();
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		for(Node node : network.vertexSet())
			nodes.add(node);
		
		// place servers according to their degrees. 
		for(int i = 0; i < Parameters.K; i ++) {
			
			int maxDegreeNodeIndex = -1; 
			int maxDegree = -1;
			for (int j = 0; j < nodes.size(); j ++) {
				Node node = nodes.get(j);
				int degree = network.degreeOf(node);
				if (maxDegree < degree) {
					maxDegree = degree;
					maxDegreeNodeIndex = j; 
				}
			}
			
			Node maxDegreeNode = nodes.get(maxDegreeNodeIndex);
			nodesWithServers.add(maxDegreeNode);
			nodes.remove(maxDegreeNodeIndex);
		}
		return nodesWithServers;
	}

	public ArrayList<Node> placeServersRandomly(SimpleWeightedGraph<Node, InternetLink> network){
		nodesWithServers = new ArrayList<Node>();
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		for(Node node : network.vertexSet())
			nodes.add(node);
		
		// place servers randomly  
		for(int i = 0; i < Parameters.K; i ++) {
			int nodeIndex = RanNum.getRandomIntRange(nodes.size() - 1, 0);
			Node ranNode = nodes.get(nodeIndex);
			nodesWithServers.add(ranNode);
			nodes.remove(nodeIndex);
		}
		return nodesWithServers;
	}

	public ArrayList<Node> getNodesWithServers() {
		return nodesWithServers;
	}

	public void setNodesWithServers(ArrayList<Node> nodesWithServers) {
		this.nodesWithServers = nodesWithServers;
	}
	
}
