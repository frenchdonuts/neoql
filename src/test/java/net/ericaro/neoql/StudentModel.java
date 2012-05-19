package net.ericaro.neoql;


public class StudentModel {

	public static final ClassTableDef<Teacher> TEACHER= Teacher.TABLE;
	public static final ClassTableDef<Student> STUDENT= Student.TABLE;
	
	
	public static class Teacher {
		public static final ClassTableDef<Teacher> TABLE = new ClassTableDef<Teacher>(Teacher.class);
		
		public static final Column<Teacher, String> NAME = TABLE.addColumn("name");
		private String name;

		String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "Teacher [name=" + name + "]";
		}

	}

	public static class Student {
		public static final ClassTableDef<Student> TABLE = new ClassTableDef<Student>(Student.class);

		public static final Column<Student, Integer> RANK = new Column<Student, Integer>("rank");
		public static final Column<Student, String> NAME 		= TABLE.addColumn("name");
		public static final Column<Student, Teacher> TEACHER    = TABLE.addColumn("teacher", Teacher.TABLE);

		
		
		

		public static final Predicate<? super Student> IS_RANK_PAIR = new Predicate<Student>() {
			@Override
			public boolean eval(Student t) {
				return t.getRank() % 2 == 0;
			}

		};

		private int rank;
		private String name;
		private Teacher teacher;

		int getRank() {
			return rank;
		}

		String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "Student [rank=" + rank + ", name=" + name + ", teacher's name=" + (teacher == null ? "''" : teacher.name) + "]";
		}

	}
	
	
	 public static class Binome {
		 public static final ClassTableDef<Binome> TABLE = new ClassTableDef<StudentModel.Binome>(Binome.class);
			public static Column<Binome, String>	NAME	= TABLE.addColumn("name");
			public static Column<Binome, Binome>	MATE	= TABLE.addColumn("mate", Binome.TABLE); 
			
			
			String							name;
			Binome							mate;
			
			

			public String getName() {
				return name;
			}



			public Binome getMate() {
				return mate;
			}



			@Override
			public  String toString() {
				return "Student [name=" + name + (mate!=null?", mate=" + mate.name:"") + "]";
			}

		}
	
}
