package net.ericaro.neoql.smarttree;

import net.ericaro.neoql.PropertyListener;
import net.ericaro.neoql.Singleton;
import net.ericaro.neoql.smarttree.TreeTesterModel.Student;
import net.ericaro.neoql.smarttree.TreeTesterModel.Teacher;
import net.ericaro.neoql.smarttree.tree.JListNode;
import net.ericaro.neoql.smarttree.tree.JNode;

public class JTeacherNode extends JListNode<Teacher,Student> {

	private TreeTesterModel	tutorialModel;

	public JTeacherNode(TreeTesterModel model, Teacher teacher) {
		super();
		Singleton<Teacher> t = model.database.track(teacher) ;
		this.tutorialModel = model;
		setModel(t);
		setList( model.getStudentsOf(t) );
	}
	
	@Override
	protected JNode create(int i, Student item) {
		return new JStudentNode(tutorialModel, item);
	}
	
}
