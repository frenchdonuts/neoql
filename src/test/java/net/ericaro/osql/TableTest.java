package net.ericaro.osql;

import static org.junit.Assert.fail;

import java.util.List;

import net.ericaro.osql.Model.Student;
import net.ericaro.osql.predicates.True;
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
		
		database.add(Student.class);
		

		database.beginTransaction();
		
		Where<Student> pair = new Where<Student>() {

			@Override
			public boolean isTrue(Student t) {
				return t.getA()%2 == 0;
			}};
		
			
			
		List<Student> res = database.select(Student.class, pair);
		
		
		database.insertInto(Student.class).set(Student.a, 1).set(Student.b, "one");
		database.insertInto(Student.class).set(Student.a, 2).set(Student.b, "two");
		database.insertInto(Student.class).set(Student.a, 3).set(Student.b, "three");
		database.insertInto(Student.class).set(Student.a, 4).set(Student.b, "four");
		
		database.commit();
		
		for (Student t: res) System.out.println(t);
		
		database.update(Student.class).set(Student.a, 3).where(nameIs("two"));
		database.update(Student.class).set(Student.a, 4).where(nameIs("one"));
		database.commit();
		
		print(database, Student.class);
		System.out.println("res is now");
		for (Student t: res) System.out.println(t);

		fail("Not yet implemented");
	}

	public static void print(Database db, Class table) {
		for (Object row : db.select(table, True))
			System.out.println(row);
	}
	
	public Where<Student> nameIs(final String name){
		return new Where<Student>() {
			@Override
			public boolean isTrue(Student t) {
				return name.equals( t.getB() );
			}};
	}
}
