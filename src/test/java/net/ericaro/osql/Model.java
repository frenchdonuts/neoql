package net.ericaro.osql;

import net.ericaro.osql.system.Column;
import net.ericaro.osql.system.Where;

public class Model {

	public static class Teacher{
		public static final Column<Teacher, String> NAME = new Column<Teacher,String>("name");
		private String name;
		public String getName() {
			return name;
		}
		@Override
		public String toString() {
			return "Teacher [name=" + name + "]";
		}
		
		
	}
	
	public static class Student {
		
		public static final Column<Student,Integer> RANK = new Column<Student,Integer>("rank");
		public static final Column<Student,String> NAME = new Column<Student,String>("name");
		public static final Column<Student, Teacher> TEACHER = new Column<Student, Teacher>("teacher", Teacher.class );
		
		public static final Where<? super Student> IS_RANK_PAIR = new Where<Student>() {

			@Override
			public boolean isTrue(Student t) {
				return t.getRank()%2 == 0;
			}
			
		};
		
		private int rank ;
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
			return "Student [rank=" + rank + ", name=" + name +", teacher's name=" + (teacher==null?"''":teacher.name)+ "]";
		}
		
		
		
	}
}
