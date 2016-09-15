package system;

import java.util.ArrayList;

import graph.Path;
import graph.Tree;

public class Route {
		
	private DataCenter server = null;
	
	private ArrayList<Path<InternetLink>> pathsToServer = null;
	
	private ArrayList<Path<InternetLink>> pathsServerToDest = null; 
	
	private ArrayList<Tree<Switch>> treesToDestinations = null;
	
	public Route(Request req){
		this.setPathsToServer(new ArrayList<Path<InternetLink>>());
		this.setPathsServerToDest(new ArrayList<Path<InternetLink>>());
		this.setTreesToDestinations(new ArrayList<Tree<Switch>>());
	}

	public DataCenter getServer() {
		return server;
	}

	public void setServer(DataCenter server) {
		this.server = server;
	}

	public ArrayList<Path<InternetLink>> getPathsToServer() {
		return pathsToServer;
	}

	public void setPathsToServer(ArrayList<Path<InternetLink>> pathsToServer) {
		this.pathsToServer = pathsToServer;
	}

	public ArrayList<Tree<Switch>> getTreesToDestinations() {
		return treesToDestinations;
	}

	public void setTreesToDestinations(ArrayList<Tree<Switch>> treesToDestinations) {
		this.treesToDestinations = treesToDestinations;
	}

	public ArrayList<Path<InternetLink>> getPathsServerToDest() {
		return pathsServerToDest;
	}

	public void setPathsServerToDest(ArrayList<Path<InternetLink>> pathsServerToDest) {
		this.pathsServerToDest = pathsServerToDest;
	}
}
