package net.ericaro.neoql.tutorial;

import java.util.List;

import javax.swing.ListModel;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.TableData;
import net.ericaro.neoql.swing.SwingQL;
import net.ericaro.neoql.swing.TableList;
import net.ericaro.neoql.tables.SelectTable;

public class TutorialModel {
	
	public static class Teacher {
		
		public static final Column<Teacher, String> NAME = NeoQL.column(Teacher.class, "name", String.class, false);
		public static final Column<Teacher, Boolean> SELECTED   = NeoQL.column(Teacher.class, "selected", Boolean.class, false);
		
		private String name;
		private boolean selected = false;
		
		public Teacher() {}
		public Teacher(String name) {
			super();
			this.name = name;
		}
		String getName() {
			return name;
		}

		public boolean isSelected() {
			return selected;
		}

		@Override
		public String toString() {
			return "Teacher [name=" + name + ", selected=" + selected + "]";
		}
	}

	public static class Student {

		public static final Column<Student, String> NAME 		= NeoQL.column(Student.class, "name", String.class, false);
		public static final Column<Student, Teacher> TEACHER    = NeoQL.column(Student.class, "teacher", Teacher.class, true);
		
		private String name;
		private Teacher teacher;
		
		public Student() {}
		
		public Student(String name) {
			super();
			this.name = name;
		}

		String getName() { // for external use
			return name;
		}
		
		public Teacher getTeacher() { // for external use only (no necessary)
			return teacher;
		}
		
		@Override
		public String toString() {
			return "Student [name=" + name + ", teacher's name=" + (teacher == null ? "''" : teacher.name+(teacher.selected?"*":" ")) + "]";
		}
	}
	
	
	
	// instance definition
	Database database;
	private TableList<Student> studentList;
	private TableList<Student> teacherList;
	private TableList<Student> selectedTeacherList;
	private TableList<Student> selectedStudentList;
	private TableData<Teacher> teachers;
	private TableData<Student> students;
	
	public TutorialModel() {
		super();
		this.database = new Database();
		
		teachers = database.createTable(Teacher.NAME, Teacher.SELECTED);
		students = database.createTable(Student.NAME, Student.TEACHER );
		
		SelectTable<Teacher> selectedTeachers = NeoQL.where(teachers, Teacher.SELECTED.is(true) );
		Table<Student> selectedStudents = NeoQL.left(  NeoQL.innerJoin(students, selectedTeachers, Student.TEACHER.joins() ));
		
//		public static final TableDef<Student> STUDENTS = NeoQL.select(Student.TABLE);
//		public static final TableDef<Teacher> TEACHERS = NeoQL.select(Teacher.TABLE);
//		public static final TableDef<Teacher> SELECTED_TEACHERS = NeoQL.select(Teacher.TABLE, Teacher.SELECTED.is(true) );
//		public static final TableDef<Student> SELECTED_STUDENTS = NeoQL.left(  NeoQL.innerJoin(Student.TABLE, SELECTED_TEACHERS, Student.TEACHER.joins() ));
		studentList = SwingQL.listFor( students );
		teacherList = SwingQL.listFor(teachers);
		selectedTeacherList = SwingQL.listFor(selectedTeachers);
		selectedStudentList = SwingQL.listFor(selectedStudents);
	}
	
	// operations
	// this could be in another class if needed
	
	/** add a student in the database
	 * 
	 * @param name
	 */
	public void addStudent(final String name) {
		database.insert(Student.NAME.set(name));
//		database.execute(new Script() {{
//			insertInto(Student.TABLE).set(Student.NAME, name);
//		}});
	}
	
	/** add a teacher in the database
	 * 
	 * @param name
	 */
	public void addTeacher(final String name) {
		database.insert(Teacher.NAME.set(name) );
//			database.execute(new Script() {{
//				insertInto(Teacher.TABLE).set(Teacher.NAME, name);
//			}});
	}
	
	/** Connect a Student with its teacher
	 * 
	 * @param student
	 * @param teacher
	 */
	public void link(final Student student, final Teacher teacher) {
		database.update(student, Student.TEACHER.set(teacher) );
	}
	
	public void selectTeacher(final Teacher teacher, final boolean selected) {
		database.update(teacher, Teacher.SELECTED.set(selected) );
	}
	
	public Iterable<Teacher> teachers(){
		return NeoQL.select(teachers);
	}
	
	public ListModel getStudents() {
		return studentList;
	}
	public ListModel getTeachers() {
		return teacherList;
	}
	public ListModel getSelectedTeachers() {
		return selectedTeacherList;
	}
	public ListModel getSelectedStudents() {
		return selectedStudentList;
	}

	public void select(List<Teacher> selected) {
		try {
			database.stage(); // use transaction mode to avoid concurrent modification
			for(Teacher t: teachers())
				selectTeacher(t, selected.contains(t) );
			database.commit();
		} finally {
			database.unstage() ;
		}
		
	}
	
	
	
	
	
	
}
