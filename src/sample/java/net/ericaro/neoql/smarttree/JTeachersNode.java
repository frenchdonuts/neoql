package net.ericaro.neoql.smarttree;

import net.ericaro.neoql.smarttree.TreeTesterModel.Teacher;
import net.ericaro.neoql.smarttree.tree.JListNode;
import net.ericaro.neoql.smarttree.tree.JNode;

// node of all teachers
public class JTeachersNode extends JListNode<TreeTesterModel, Teacher> {

	
	
	
	TreeTesterModel	tutorialModel;

	public JTeachersNode(TreeTesterModel tutorialModel) {
		super();
		this.tutorialModel = tutorialModel;
		setList(tutorialModel.getTeachers());
	}

	@Override
	protected JNode create(int i, Teacher item) {
		return new JTeacherNode(tutorialModel, item );
	}

	@Override
	public String toString() {
		return "teachers";
	}

	

	
	
	
	
}
