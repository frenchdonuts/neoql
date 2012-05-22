package net.ericaro.neoql.tutorial;

import net.ericaro.neoql.RandName;
import net.ericaro.neoql.TableList;
import net.ericaro.neoql.lang.NeoQL;
import net.ericaro.neoql.lang.Script;
import net.ericaro.neoql.tutorial.TutorialModel.Student;
import net.ericaro.neoql.tutorial.TutorialModel.Teacher;

import org.junit.Test;


public class TutorialModelTest {

	
	@Test public void testBug() {
		
		TutorialModel m = new TutorialModel();
		TableList<Student> selected = m.getSelectedStudents();
		for(int i=0;i<20;i++) {
		m.addStudent(RandName.next());
		}
		for(int i=0;i<5;i++) {
			m.addTeacher(RandName.next());
		}
		m.addTeacher("mee");
		Teacher mee = m.database.iterator(NeoQL.select(Teacher.TABLE, Teacher.NAME.is("mee"))).next();
		m.selectTeacher(mee, true);
		System.out.println("mee="+mee);
		
		int i=0;
		mee = m.database.iterator(NeoQL.select(Teacher.TABLE, Teacher.NAME.is("mee"))).next();
		for(Student s: m.database.select(Student.TABLE)) {
			if (i++%2==0) {
				System.out.println("linking "+s+" "+mee);
				m.link(s, mee);
			}
		}
		
		
		System.out.println("selected students: expected ");
		for(Student s: m.database.select(TutorialModel.SELECTED_STUDENTS))
			System.out.println(s);
		
		System.out.println("selected students: actual");
		for(Student s: selected)
			System.out.println(s);
		
		mee = m.database.iterator(NeoQL.select(Teacher.TABLE, Teacher.NAME.is("mee"))).next();
		m.selectTeacher(mee, false);
		System.out.println("unselected mee");
		System.out.println("selected students: expected ");
		for(Student s: m.database.select(TutorialModel.SELECTED_STUDENTS))
			System.out.println(s);
		
		System.out.println("selected students: actual");
		for(Student s: selected)
			System.out.println(s);
		
		mee = m.database.iterator(NeoQL.select(Teacher.TABLE, Teacher.NAME.is("mee"))).next();
		m.selectTeacher(mee, true);
		System.out.println("re select mee");
		System.out.println("selected students: expected ");
		for(Student s: m.database.select(TutorialModel.SELECTED_STUDENTS))
			System.out.println(s);
		
		System.out.println("selected students: actual");
		for(Student s: selected)
			System.out.println(s);
		
		mee = m.database.iterator(NeoQL.select(Teacher.TABLE, Teacher.NAME.is("mee"))).next();
		m.selectTeacher(mee, false);
		
		System.out.println("deselected students: actual");
		for(Student s: selected)
			System.out.println(s);
		
		
	}
}
