package net.ericaro.neoql;

import org.junit.Test;

public class TableDataTest {

	public static class Student {
		public static final ClassTableDef<Student> TABLE = NeoQL.table(Student.class);
		public static final Column<Student, String> NAME = TABLE.addColumn("name");
		String name;
		
	}
	
	
	@Test public void testCrud() {
		Database db = new Database();
		TableData<Student> students = db.createTable(Student.TABLE);
		CloneTable<Student> clone = new CloneTable<Student>(students);
		Student s1 = db.insert(Student.NAME.set("toto") );
		clone.add(s1);
		assert clone.areEquals();
		
		Student s2 = db.insert(Student.NAME.set("titi") );
		clone.add(s2);
		assert clone.areEquals();
		
		s1 = db.update(s1, Student.NAME.set("tata"));
		clone.set(0, s1);
		assert clone.areEquals();
		
		db.delete(s1);
		clone.remove(0);
		assert clone.areEquals();
	}
	
}
