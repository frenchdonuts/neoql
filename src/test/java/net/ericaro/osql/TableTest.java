package net.ericaro.osql;

import static org.junit.Assert.fail;
import net.ericaro.osql.Model.Student;
import net.ericaro.osql.Model.Teacher;
import net.ericaro.osql.lang.DQL;
import net.ericaro.osql.lang.Predicate;
import net.ericaro.osql.lang.Script;
import net.ericaro.osql.lang.Select;
import net.ericaro.osql.system.Database;
import net.ericaro.osql.system.SelectList;

import org.junit.Test;

public class TableTest {
	
	@Test
	public void test() {

		// TODO a smarter predicate system

		Database database = new Database();
		
		Script s;
		
		s = new Script();
		s.createTable(Teacher.class);
		s.createTable(Student.class);
		s.executeOn(database);
		
		
		
		Select<Student> select = new Select<Student>(Student.class, Student.IS_RANK_PAIR );
		SelectList<Student> res = database.select(select); 
		
		
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
		.where(DQL.columnIs(Teacher.NAME, "prof") )
		;
		
		s.executeOn(database);
		
		for (Student t: res) System.out.println(t);
		
// TODO add actually smart asserts
		fail("Not yet implemented");
	}

	
	
	public static <T> void print(Database db, Class<T> table) {
		Select<T> select = new Select<T>(table, DQL.True);
		for (Object row : db.select(select) )
			System.out.println(row);
	}
	
	public Predicate<Student> nameIs(final String name){
		return DQL.columnIs(Student.NAME, name);
	}
}
