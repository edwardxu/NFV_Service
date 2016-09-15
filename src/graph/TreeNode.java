package graph;

import java.util.LinkedList;
import java.util.List;

public class TreeNode<T> {
	
	T data;
    TreeNode<T> parent;
    List<TreeNode<T>> children;
    int level = 0;

    public TreeNode(T data) {
        this.setData(data);
        this.children = new LinkedList<TreeNode<T>>();
    }

    public TreeNode<T> addChild(T child) {
        TreeNode<T> childNode = new TreeNode<T>(child);
        childNode.parent = this;
        childNode.level = this.level + 1;
        this.children.add(childNode);
        return childNode;
    }
    
    public TreeNode<T> getParent() {
    	return this.parent;
    }

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public boolean equals(Object another) {
		if (this == another)
			return true;

		if (!(another instanceof TreeNode))
			return false;

		if (this.getData().equals(( (TreeNode<?>)another).data))
			return true;
		else
			return false;
	}
}
