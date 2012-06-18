package net.ericaro.neoql.smarttree.tree;

import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

// basic controller
public class JNode<M>  extends JLabel {
	protected M model;
	protected DefaultMutableTreeNode node;
	protected NodeModel	nodeModel;
	
	
	public JNode(M item) {
		this();
		setModel(item);
	}
	
	public JNode() {
		super();
		this.node = new DefaultMutableTreeNode() {
			public String toString() {
				return JNode.this.toString();
			}
		};
		node.setUserObject(this);
	}



	public void setModel(M m) {
		this.model= m;
	}

	DefaultMutableTreeNode getNode() {return node;}
	
	void setNodeModel(NodeModel nodeModel) {
		this.nodeModel = nodeModel;
	}
	
}
