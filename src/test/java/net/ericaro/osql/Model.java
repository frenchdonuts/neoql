package net.ericaro.osql;


 class Model {

	 static class Teacher{
		 static final Column<Teacher, String> NAME = new Column<Teacher,String>("name");
		private String name;
		 String getName() {
			return name;
		}
		@Override
		public  String toString() {
			return "Teacher [name=" + name + "]";
		}
		
		
	}
	
	 static class Student {
		
		 static final Column<Student,Integer> RANK = new Column<Student,Integer>("rank");
		 static final Column<Student,String> NAME = new Column<Student,String>("name");
		 static final Column<Student, Teacher> TEACHER = new Column<Student, Teacher>("teacher", Teacher.class );
		
		 static final Predicate<? super Student> IS_RANK_PAIR = new Predicate<Student>() {
			@Override
			public  boolean eval(Student t) {
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
		public  String toString() {
			return "Student [rank=" + rank + ", name=" + name +", teacher's name=" + (teacher==null?"''":teacher.name)+ "]";
		}
		
		
		
	}
}
