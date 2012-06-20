package net.ericaro.neoql.smarttree;

import java.util.List;

import javax.swing.ListModel;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.SelectTable;
import net.ericaro.neoql.Singleton;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.TableData;
import net.ericaro.neoql.TableList;
import net.ericaro.neoql.smarttree.TreeTesterModel.Teacher;

public class TreeTesterModel {
	
	public static class Teacher {
		
		public static final Column<Teacher, String> NAME = NeoQL.column(Teacher.class, "name");
		public static final Column<Teacher, Boolean> SELECTED   = NeoQL.column(Teacher.class, "selected");
		
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

		public static final Column<Student, String> NAME 		= NeoQL.column(Student.class, "name");
		public static final Column<Student, Teacher> TEACHER    = NeoQL.column(Student.class, "teacher", Teacher.class);
		
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
	private ListModel	availableStudentList;
	private Singleton<Teacher>	editingTeacher;
	
	public TreeTesterModel() {
		super();
		this.database = new Database();
		
		teachers = database.createTable(Teacher.NAME, Teacher.SELECTED);
		students = database.createTable(Student.NAME, Student.TEACHER );
		
//		SelectTable<Teacher> selectedTeachers = NeoQL.where(teachers, Teacher.SELECTED.is(true) );
//		Table<Student> selectedStudents = NeoQL.left(  NeoQL.innerJoin(students, selectedTeachers, Student.TEACHER.joins() ));
		Table<Student> availableStudents = NeoQL.where(students, Student.TEACHER.is((Teacher)null)) ;
		
//		public static final TableDef<Student> STUDENTS = NeoQL.select(Student.TABLE);
//		public static final TableDef<Teacher> TEACHERS = NeoQL.select(Teacher.TABLE);
//		public static final TableDef<Teacher> SELECTED_TEACHERS = NeoQL.select(Teacher.TABLE, Teacher.SELECTED.is(true) );
//		public static final TableDef<Student> SELECTED_STUDENTS = NeoQL.left(  NeoQL.innerJoin(Student.TABLE, SELECTED_TEACHERS, Student.TEACHER.joins() ));
		studentList = NeoQL.listFor( students );
		teacherList = NeoQL.listFor(teachers);
//		selectedTeacherList = NeoQL.listFor(selectedTeachers);
//		selectedStudentList = NeoQL.listFor(selectedStudents);
		availableStudentList = NeoQL.listFor(availableStudents);
		editingTeacher = database.createSingleton(Teacher.class);
	}
	
	// operations
	// this could be in another class if needed
	
	/** add a student in the database
	 * 
	 * @param name
	 */
	public void addStudent(final String name) {
		database.insert(Student.NAME.set(name));
	}
	public void addStudent(final String name, Teacher teacher) {
		database.insert(Student.NAME.set(name), Student.TEACHER.set(teacher));
	}
	
	/** add a teacher in the database
	 * 
	 * @param name
	 * @return 
	 */
	public Teacher addTeacher(final String name) {
		return database.insert(Teacher.NAME.set(name) );
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
	public ListModel getAvailableStudents() {
		return availableStudentList;
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

	public ListModel getStudentsOf(Singleton<Teacher> teacher) {
		return NeoQL.listFor(NeoQL.where(database.get(Student.class) , Student.TEACHER.is(teacher) ));
	}

	public void rename(Teacher selection, String next) {
		database.update(selection, Teacher.NAME.set(next));
	}
	public void rename(Student selection, String next) {
		database.update(selection, Student.NAME.set(next));
	}

	public void select(Teacher selection) {
		database.put(editingTeacher, selection);
	}

	public Singleton<Teacher> getEditingTeacher() {
		return editingTeacher;
	}
	
}