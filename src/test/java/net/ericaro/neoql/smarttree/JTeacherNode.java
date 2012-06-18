package net.ericaro.neoql.smarttree;

import net.ericaro.neoql.PropertyListener;
import net.ericaro.neoql.Singleton;
import net.ericaro.neoql.smarttree.TreeTesterModel.Student;
import net.ericaro.neoql.smarttree.TreeTesterModel.Teacher;
import net.ericaro.neoql.smarttree.tree.JListNode;
import net.ericaro.neoql.smarttree.tree.JNode;

public class JTeacherNode extends JListNode<Teacher,Student> {

	private Singleton<Teacher>	teacher;
	private TreeTesterModel	tutorialModel;

	public JTeacherNode(TreeTesterModel model, Teacher teacher) {
		super();
		//this.teacher = model.database.track(teacher);
		this.tutorialModel = model;
//		this.teacher.addPropertyListener(new PropertyListener<TreeTesterModel.Teacher>() {
//			public void updated(Teacher oldValue, Teacher newValue) {
//				setModel(newValue);
//			}
//		});
		setModel(teacher);
		setList( model.getStudentsOf(teacher) );
	}
	
	@Override
	protected JNode create(int i, Student item) {
		return new JStudentNode(tutorialModel, item);
	}

	
	
	
}
