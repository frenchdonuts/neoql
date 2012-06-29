package net.ericaro.neoql.simpleeditor;

import net.ericaro.neoql.Singleton;
import net.ericaro.neoql.smarttree.tree.JListNode;
import net.ericaro.neoql.smarttree.tree.JNode;

// node of all teachers
public class JDirectoryNode extends JListNode<Directory, HasName> {

	private EditorModel	editorModel;


	public JDirectoryNode(EditorModel model,  Singleton<Directory> dir) {
		super(dir, model.childsOf(dir));
		editorModel = model;
		
	}


	@Override
	public String toString() {
		return getModel().getName();
	}


	@Override
	protected JNode create(int i, HasName item) {
		// I can use a visitor pattern to let items decide how to create their node
		// or i can add a switch pattern
		// or a type -> node map
		// or a factory pattern
		// everything depends on the kind of tree you want here.
		//In my case, a switch is enough
		if (item instanceof Directory)
			return new JDirectoryNode(editorModel, editorModel.singletonOf((Directory) item));
		if (item instanceof Editable)
			return new JEditableNode(editorModel, (Editable) item);
		return null;
	}

	

	
	
	
	
}
