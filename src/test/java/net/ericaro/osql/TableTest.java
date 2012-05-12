package net.ericaro.osql;

import static org.junit.Assert.fail;

import java.util.List;

import net.ericaro.osql.Model.Student;
import net.ericaro.osql.Model.Teacher;
import net.ericaro.osql.predicates.True;
import net.ericaro.osql.system.DQL;
import net.ericaro.osql.system.Database;
import net.ericaro.osql.system.SelectList;
import net.ericaro.osql.system.Where;

import org.junit.Test;

public class TableTest {
	static True True = new True();
	
	@Test
	public void test() {

		// TODO a smarter predicate system
		// TODO handle the create table too (so that will define the edsl)

		Database database = new Database();
		
		database.add(Student.class, Teacher.class);
		

		database.beginTransaction();
			
		
		List<Student> res = database.select(Student.class, Student.IS_RANK_PAIR );
		
		
		database.insertInto(Student.class).set(Student.RANK, 1).set(Student.NAME, "one");
		database.insertInto(Student.class).set(Student.RANK, 2).set(Student.NAME, "two");
		database.insertInto(Student.class).set(Student.RANK, 3).set(Student.NAME, "three");
		
		Teacher prof = database.insertInto(Teacher.class).set(Teacher.NAME, "prof").build();
		
		database.insertInto(Student.class).set(Student.RANK, 4).set(Student.NAME, "four").set(Student.TEACHER, prof);
		
		
		database.commit();
		
		for (Student t: res) System.out.println(t);
		
		database.update(Student.class).set(Student.RANK, 3).where(nameIs("two"));
		database.update(Student.class).set(Student.RANK, 4).where(nameIs("one"));
		database.commit();
		
		print(database, Student.class);
		System.out.println("res is now");
		for (Student t: res) System.out.println(t);
		
		database.update(Teacher.class).set(Teacher.NAME, "atchoum").where(DQL.columnIs(Teacher.NAME, "prof") );
		database.commit();
		
		for (Student t: res) System.out.println(t);
		

		fail("Not yet implemented");
	}

	public static void print(Database db, Class table) {
		for (Object row : db.select(table, True))
			System.out.println(row);
	}
	
	public Where<Student> nameIs(final String name){
		return DQL.columnIs(Student.NAME, name);
	}
}
