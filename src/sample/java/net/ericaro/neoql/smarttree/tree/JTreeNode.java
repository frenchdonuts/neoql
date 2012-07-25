package net.ericaro.neoql.smarttree.tree;

import javax.swing.JTree;

public class JTreeNode extends JTree{

	NodeModel nodeModel;
	
	
	public JTreeNode() {
		super();
		setNodeModel(new NodeModel());
//		setCellRenderer(new TreeCellRenderer() {
//			@Override
//			public Component getTreeCellRendererComponent(JTree tree, Object node, boolean selected, boolean hasFocus, boolean arg4, int arg5, boolean arg6) {
//				Object userObject = ( (DefaultMutableTreeNode)node).getUserObject();
//				if (JNode.class.isAssignableFrom(userObject.getClass() ))
//					return (JNode) userObject;
//				else return new JLabel("fake");
//				
//			}
//		});
	}


	public NodeModel getNodeModel() {
		return nodeModel;
	}


	public void setNodeModel(NodeModel model) {
		this.nodeModel = model;
		this.setModel(model.treeModel);
	}


	public void addNode(JNode node) {
		nodeModel.addNode(node);
	}
	
	
	
	

	
	
	
	
}
