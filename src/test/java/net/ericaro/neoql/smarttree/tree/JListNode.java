package net.ericaro.neoql.smarttree.tree;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import net.ericaro.neoql.Singleton;

public abstract class JListNode<M,I> extends JNode<M>{
	
	private ListModel	children;
	
	private ListDataListener	listener = new ListDataListener() {
		public void intervalAdded(ListDataEvent event) {
			ListModel source = (ListModel) event.getSource();
			for (int i = event.getIndex1(); i >= event.getIndex0(); i--) 
				whenAdded(i, (I) source.getElementAt(i) );
			//fireTableRowsInserted(event.getIndex0(), event.getIndex1());
		}

		public void intervalRemoved(ListDataEvent event) {
			ListModel source = (ListModel) event.getSource();
			for (int i = event.getIndex1(); i >= event.getIndex0(); i--) 
				whenRemoved(i, (I) source.getElementAt(i) );
			//fireTableRowsDeleted(event.getIndex0(), event.getIndex1());
		}

		public void contentsChanged(ListDataEvent event) {
			ListModel source = (ListModel) event.getSource();
			for (int i = event.getIndex1(); i >= event.getIndex0(); i--) 
				whenUpdated(i, (I) source.getElementAt(i) );
		}
	};

	
	
	
	public JListNode() {
		super();
	}

	public JListNode(Singleton<M> item) {
		super(item);
	}

	public void setList(ListModel list) {
		if (this.children != null)
			this.children.removeListDataListener(listener);
		this.children = list;
		if (this.children != null)
			this.children.addListDataListener(listener);
	}
	
	protected void whenAdded(int i, I item) {
		JNode child = create(i, item);
		nodeModel.insertNodeInto(this, child, i);
	}
	
	protected abstract JNode create(int i, I item);
	
	protected void whenRemoved(int i, I item) {
		TreeNode mnode = node.getChildAt(i); // the ith was removed
		nodeModel.removeNodeFromParent((MutableTreeNode) mnode);
	}
	
	protected void whenUpdated(int i, I item) {
		nodeModel.nodeChanged(node.getChildAt(i) );
	}
}
