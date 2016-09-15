package graph;

import java.util.LinkedList;
import java.util.List;

public class Tree<T> {
	
	private TreeNode<T> root;

    public Tree(T rootData) {
        root = new TreeNode<T>(rootData);
        root.children = new LinkedList<TreeNode<T>>();
        root.level = 0; 
    }
    
    public boolean isEmpty() {
    	if (root == null)
    		return true;
    	else 
    		return false;
    }
    
    public TreeNode<T> getRoot() {
    	return root;
    }
    
    public void setRoot(TreeNode<T> r){
    	this.root = r;
    }
    
    public boolean isAncestor(TreeNode<T> node1, TreeNode<T> node2){
    	
    	boolean isAncestor = false; 
    	
    	List<TreeNode<T>> children = node1.children;
    	for (TreeNode<T> child : children) {
    		if (child.equals(node2)) {
    			isAncestor = true;
    		}
    	}
    	
    	if (isAncestor)
    		return true;
    	else {
    		for (TreeNode<T> child : children) {
        		if(isAncestor(child, node2)){
        			isAncestor = true;
        			break;
        		}
        	}
    		
    		if (isAncestor)
        		return true;
    		else 
    			return false;
    	}
    }
}