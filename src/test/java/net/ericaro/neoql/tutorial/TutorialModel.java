package net.ericaro.neoql.tutorial;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.TableList;
import net.ericaro.neoql.lang.ClassTableDef;
import net.ericaro.neoql.lang.NeoQL;
import net.ericaro.neoql.lang.Script;
import net.ericaro.neoql.system.Column;
import net.ericaro.neoql.system.TableDef;

public class TutorialModel {
	
	public static class Teacher {
		public static final ClassTableDef<Teacher> TABLE = new ClassTableDef<Teacher>(Teacher.class);
		
		public static final Column<Teacher, String> NAME = TABLE.addColumn("name");
		public static final Column<Teacher, Boolean> SELECTED   = TABLE.addColumn("selected");
		
		private String name;
		private boolean selected;

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
		public static final ClassTableDef<Student> TABLE = new ClassTableDef<Student>(Student.class);

		public static final Column<Student, String> NAME 		= TABLE.addColumn("name");
		public static final Column<Student, Teacher> TEACHER    = TABLE.addColumn("teacher", Teacher.TABLE);
		
		private String name;
		private Teacher teacher;

		String getName() {
			return name;
		}
		
		public Teacher getTeacher() {
			return teacher;
		}
		
		@Override
		public String toString() {
			return "Student [name=" + name + ", teacher's name=" + (teacher == null ? "''" : teacher.name) + "]";
		}

	}
	
	// building relations
	public static final TableDef<Student> STUDENTS = NeoQL.select(Student.TABLE);
	public static final TableDef<Teacher> TEACHERS = NeoQL.select(Teacher.TABLE);
	
	
	public static final TableDef<Teacher> SELECTED_TEACHERS = NeoQL.select(Teacher.TABLE, Teacher.SELECTED.is(true) );
	public static final TableDef<Student> SELECTED_STUDENTS = NeoQL.left(  NeoQL.innerJoin(Student.TABLE, SELECTED_TEACHERS, Student.TEACHER.joins() ));
	
	
	// instance definition
	Database database;
	
	
	public TutorialModel() {
		super();
		this.database = new Database();
		database.execute(new Script() {{
			createTable(Teacher.TABLE);
			createTable(Student.TABLE);
		}}); 
	}
	
	// operations
	
	/** add a student in the database
	 * 
	 * @param name
	 */
	public void addStudent(final String name) {
		database.execute(new Script() {{
			insertInto(Student.TABLE).set(Student.NAME, name);
		}});
	}
	
	/** add a teacher in the database
	 * 
	 * @param name
	 */
	public void addTeacher(final String name) {
			database.execute(new Script() {{
				insertInto(Teacher.TABLE).set(Teacher.NAME, name);
			}});
	}
	
	/** Connect a Student with its teacher
	 * 
	 * @param student
	 * @param teacher
	 */
	public void link(final Student student, final Teacher teacher) {
		database.execute(new Script() {{
			update(Student.TABLE)
				.where(Student.TABLE.is(student) )
				.set(Student.TEACHER, teacher);
			;
		}});
	}
	
	public void selectTeacher(final Teacher teacher, final boolean selected) {
		database.execute(new Script() {{
			update(Teacher.TABLE)
				.where(Teacher.TABLE.is(teacher) )
				.set(Teacher.SELECTED, selected);
			;
		}});
	}
	
	public Iterable<Teacher> teachers(){
		return database.select(TEACHERS);
	}
	
	public TableList<Student> getStudents() {
		return database.listFor(STUDENTS);
	}
	public TableList<Teacher> getTeachers() {
		return database.listFor(TEACHERS);
	}
	public TableList<Teacher> getSelectedTeachers() {
		return database.listFor(SELECTED_TEACHERS);
	}
	public TableList<Student> getSelectedStudents() {
		return database.listFor(SELECTED_STUDENTS);
	}
	
	
	
	
	
	
}
