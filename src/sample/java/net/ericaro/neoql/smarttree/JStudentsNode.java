package net.ericaro.neoql.smarttree;

import net.ericaro.neoql.smarttree.TreeTesterModel.Student;
import net.ericaro.neoql.smarttree.TreeTesterModel.Teacher;
import net.ericaro.neoql.smarttree.tree.JListNode;
import net.ericaro.neoql.smarttree.tree.JNode;

// node of all students
public class JStudentsNode extends JListNode<TreeTesterModel, Student> {

	
	
	
	TreeTesterModel	tutorialModel;

	public JStudentsNode(TreeTesterModel tutorialModel) {
		super();
		this.tutorialModel = tutorialModel;
		setList(tutorialModel.getStudents());
	}

	@Override
	protected JNode create(int i, Student item) {
		return new JStudentNode(tutorialModel, item );
	}

	@Override
	public String toString() {
		return "students";
	}

	
	
	
	
}
