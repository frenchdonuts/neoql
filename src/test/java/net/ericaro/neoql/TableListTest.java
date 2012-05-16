package net.ericaro.neoql;

import static org.junit.Assert.fail;
import net.ericaro.neoql.Model.Student;
import net.ericaro.neoql.Model.Teacher;
import net.ericaro.neoql.lang.NeoQL;
import net.ericaro.neoql.lang.Script;
import net.ericaro.neoql.lang.Select;

import org.junit.Test;

class TableListTest {

	@Test
	void test() {

		Database database = new Database();

		database.execute(new Script() {
			{
				createTable(Teacher.class);
				createTable(Student.class);
			}
		});

		Select<Student> select = new Select<Student>(Student.class,
				Student.IS_RANK_PAIR);
		TableList<Student> res = new TableList<Student>(database.table(select));

		database.execute(new Script() {
			{
				insertInto(Student.class).set(Student.RANK, 1).set(
						Student.NAME, "one");

				insertInto(Student.class).set(Student.RANK, 2).set(
						Student.NAME, "two");

				insertInto(Student.class).set(Student.RANK, 3).set(
						Student.NAME, "three");

				Teacher prof = insertInto(Teacher.class).set(Teacher.NAME,
						"prof").getRow();

				insertInto(Student.class).set(Student.RANK, 4)
						.set(Student.NAME, "four").set(Student.TEACHER, prof);
			}
		});

		for (Student t : res)
			System.out.println(t);

		database.execute(new Script() {
			{
				update(Student.class).set(Student.RANK, 3).where(nameIs("two"));
				update(Student.class).set(Student.RANK, 4).where(nameIs("one"));
			}
		});

		print(database, Student.class);
		System.out.println("res is now");
		for (Student t : res)
			System.out.println(t);

		database.execute(new Script() {
			{
				update(Teacher.class)
					.set(Teacher.NAME, "atchoum").where(
						NeoQL.is(Teacher.NAME, "prof"));
			}
		});

		for (Student t : res)
			System.out.println(t);

		// TODO add actually smart asserts
		fail("Not yet implemented");
	}

	static <T> void print(Database db, Class<T> table) {
		for (Object row : db.select(table))
			System.out.println(row);
	}

	Predicate<Student> nameIs(final String name) {
		return NeoQL.is(Student.NAME, name);
	}
}
