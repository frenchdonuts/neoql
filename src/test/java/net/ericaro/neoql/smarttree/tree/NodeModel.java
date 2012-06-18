package net.ericaro.neoql.smarttree.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class NodeModel<M> extends JNode<M>{
	DefaultTreeModel treeModel;
	
	public NodeModel() {
		super();
		treeModel = new DefaultTreeModel(node);
	}

	void insertNodeInto(JNode parent, JNode node, int i) {
		node.setNodeModel(this);
		DefaultMutableTreeNode p = this.node;
		if (parent != null)
			p = parent.getNode() ;
		treeModel.insertNodeInto(node.node,p,  i);
	}

	void removeNodeFromParent(JNode parent) {
		
		DefaultMutableTreeNode p = this.node;
		if (parent != null)
			p = parent.getNode() ;
		treeModel.removeNodeFromParent(p);
	}
	
	
	public void addNode(JNode node) {
		insertNodeInto(this, node, 0);
	}

	void removeNodeFromParent(MutableTreeNode node) {
		treeModel.removeNodeFromParent(node);
	}

	void nodeChanged(TreeNode node) {
		treeModel.nodeChanged(node);
	}
	
	

	
}

