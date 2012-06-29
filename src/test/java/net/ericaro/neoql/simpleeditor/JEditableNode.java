package net.ericaro.neoql.simpleeditor;

import net.ericaro.neoql.Singleton;
import net.ericaro.neoql.smarttree.tree.JNode;

public class JEditableNode extends JNode<Editable> {

	private EditorModel	editorModel;


	public JEditableNode(EditorModel editorModel, Editable item) {
		super(editorModel.singletonOf(item));
		this.editorModel = editorModel;
	}


	@Override
	public String toString() {
		return getModel().getName();
	}
	
	
	
}
