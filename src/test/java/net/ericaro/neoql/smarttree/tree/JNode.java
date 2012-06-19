package net.ericaro.neoql.smarttree.tree;

import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

import net.ericaro.neoql.PropertyListener;
import net.ericaro.neoql.Singleton;
import net.ericaro.neoql.smarttree.TreeTesterModel;
import net.ericaro.neoql.smarttree.TreeTesterModel.Teacher;

// basic controller
public class JNode<M> {
	protected Singleton<M> model;
	protected DefaultMutableTreeNode node;
	protected NodeModel	nodeModel;
	private PropertyListener<M> propertyListener = new PropertyListener<M>() {
		public void updated(M oldValue, M newValue) {
			nodeModel.nodeChanged(node);
		}
	};
	
	
	public JNode(Singleton<M> item) {
		this();
		node.setAllowsChildren(false);
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



	public void setModel(Singleton<M> m) {
		if (this.model !=null)
			model.removePropertyListener(propertyListener);
		this.model= m;
		if (this.model !=null)
			model.addPropertyListener(propertyListener);
	}
	
	public M getModel() {return model.get();}

	DefaultMutableTreeNode getNode() {return node;}
	
	void setNodeModel(NodeModel nodeModel) {
		this.nodeModel = nodeModel;
	}

	public String toString() {
		if (model == null) return "";
		return String.valueOf( getModel() );
	}
	
}
