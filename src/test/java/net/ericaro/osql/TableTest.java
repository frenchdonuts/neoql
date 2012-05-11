package net.ericaro.osql;

import static org.junit.Assert.fail;

import java.util.Arrays;

import net.ericaro.osql.Model.Student;
import net.ericaro.osql.predicates.True;
import net.ericaro.osql.projectors.All;
import net.ericaro.osql.system.Database;

import org.junit.Test;

public class TableTest {
	static True True = new True();
	static All all = new All();

	@Test
	public void test() {

		// TODO a smarter predicate system
		// TODO handle the create table too (so that will define the edsl)

		Database database = new Database();
		database.add(Student.class);

		database.beginTransaction();
		database.insertInto(Student.class).set(Student.a, 1).set(Student.b, "toto");
		
		System.out.println("print before");
		print(database, Student.class);
		System.out.println("commit");
		database.commit();
		print(database, Student.class);

		database.update(Student.class).set(Student.a, 3).where(True);
		print(database, Student.class);
		database.commit();
		print(database, Student.class);

		fail("Not yet implemented");
	}

	public static void print(Database db, Class table) {
		for (Object row : db.select(table, True))
			System.out.println(row);
	}
}
