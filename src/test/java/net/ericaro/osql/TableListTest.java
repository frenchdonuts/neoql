package net.ericaro.osql;

import static org.junit.Assert.fail;
import net.ericaro.osql.Model.Student;
import net.ericaro.osql.Model.Teacher;

import org.junit.Test;

 class TableListTest {
	
	@Test
	 void test() {


		Database database = new Database();
		
		Script s;
		
		s = new Script();
		s.createTable(Teacher.class);
		s.createTable(Student.class);
		s.executeOn(database);
		
		
		
		Select<Student> select = new Select<Student>(Student.class, Student.IS_RANK_PAIR );
		TableList<Student> res = new TableList<Student>(database.table(select)); 
		
		
		s = new Script();
		s.insertInto(Student.class)
			.set(Student.RANK, 1)
			.set(Student.NAME, "one");
		
		s.insertInto(Student.class)
		.set(Student.RANK, 2)
		.set(Student.NAME, "two");
		
		s.insertInto(Student.class)
		.set(Student.RANK, 3)
		.set(Student.NAME, "three");
		
		Teacher prof = s.insertInto(Teacher.class)
		.set(Teacher.NAME, "prof").getRow();
		
		s.insertInto(Student.class)
			.set(Student.RANK, 4)
			.set(Student.NAME, "four")
			.set(Student.TEACHER, prof)
			;
		
		s.executeOn(database);
		
		
		for (Student t: res) System.out.println(t);
		
		s = new Script();
		s.update(Student.class)
		.set(Student.RANK, 3)
		.where(nameIs("two"))
		;
		s.update(Student.class)
		.set(Student.RANK, 4)
		.where(nameIs("one"))
		;
		
		s.executeOn(database);
		
		print(database, Student.class);
		System.out.println("res is now");
		for (Student t: res) System.out.println(t);
		
		
		s = new Script();
		s.update(Teacher.class)
		.set(Teacher.NAME, "atchoum")
		.where(DQL.is(Teacher.NAME, "prof") )
		;
		
		s.executeOn(database);
		
		for (Student t: res) System.out.println(t);
		
// TODO add actually smart asserts
		fail("Not yet implemented");
	}

	
	
	 static <T> void print(Database db, Class<T> table) {
		for (Object row : db.select(table) )
			System.out.println(row);
	}
	
	 Predicate<Student> nameIs(final String name){
		return DQL.is(Student.NAME, name);
	}
}
