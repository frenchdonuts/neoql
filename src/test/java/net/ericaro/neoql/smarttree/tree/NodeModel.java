package net.ericaro.neoql.smarttree.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public class NodeModel extends JNode{
	public DefaultTreeModel model;
	
	public NodeModel() {
		super();
		model = new DefaultTreeModel(node);
	}

	void insertNodeInto(JNode parent, JNode node, int i) {
		node.setNodeModel(this);
		DefaultMutableTreeNode p = this.node;
		if (parent != null)
			p = parent.getNode() ;
		model.insertNodeInto(node.node,p,  i);
	}

	void removeNodeFromParent(JNode parent) {
		
		DefaultMutableTreeNode p = this.node;
		if (parent != null)
			p = parent.getNode() ;
		model.removeNodeFromParent(p);
	}
	
	
	public void addNode(JNode node) {
		insertNodeInto(this, node, 0);
	}

	
}

