package net.ericaro.neoql.smarttree;

import net.ericaro.neoql.smarttree.TreeTesterModel.Student;
import net.ericaro.neoql.smarttree.tree.JNode;

public class JStudentNode extends JNode<Student> {


	private TreeTesterModel	tutorialModel;

	public JStudentNode(TreeTesterModel model, Student item) {
		super(item);
		tutorialModel = model;
	}
}
