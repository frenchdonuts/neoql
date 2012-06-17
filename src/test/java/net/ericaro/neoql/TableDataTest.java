package net.ericaro.neoql;

import javax.swing.undo.UndoManager;

import org.junit.Test;

public class TableDataTest {

	public static class Student {
		public static final ClassTableDef<Student> TABLE = NeoQL.table(Student.class);
		public static final Column<Student, String> NAME = TABLE.addColumn("name");
		String name;
		@Override
		public String toString() {
			return "Student [name=" + name + "]";
		}
		
		
	}
	
	public static class Killer{
		public static final ClassTableDef<Killer> TABLE = NeoQL.table(Killer.class);
		public static final Column<Killer, String> NAME = TABLE.addColumn("name");
		public static final Column<Killer, Killer> TARGET= TABLE.addColumn("target", TABLE);
		String name;
		Killer target;
		@Override
		public String toString() {
			return "Killer [name=" + name + ", target=" + target.name + "]";
		}
		
		
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
		for(Student s: students)
			System.out.println(s);
		assert clone.areEquals();
		
		db.delete(s1);
		clone.remove(0);
		assert clone.areEquals();
	}
	
	
	@Test public void testUndo() {
		Database db = new Database(false);
		TableData<Student> students = db.createTable(Student.TABLE);
		CloneTable<Student> clone = new CloneTable<Student>(students);
		
		Student s1 = db.insert(Student.NAME.set("toto") );
		ChangeSet c1 = db.commit();
		clone.add(s1);
		assert clone.areEquals() : "insert was not sucessfully redone";
		
		db.revert(c1);
		clone.remove(s1);
		assert clone.areEquals() : "insert was not sucessfully undone";
		
		db.commit(c1);
		clone.add(s1);
		assert clone.areEquals() : "insert was not sucessfully redone";
		
		db.revert(c1);
		clone.remove(s1);
		assert clone.areEquals() : "insert was not sucessfully reundone";
		
		Student s2 = db.insert(Student.NAME.set("titi") );
		ChangeSet c2 = db.commit();
		clone.add(s2);
		assert clone.areEquals() : "insert was not sucessfully redone";
		
		
		db.revert(c2);
		db.commit(c1);
		clone.remove(s2);
		clone.add(s1);
		assert clone.areEquals() : "insert was not sucessfully redone";
		
		
		
		
		
	}
	
	@Test public void testSelfRef() {
		Database db = new Database();
		TableData<Killer> killers = db.createTable(Killer.TABLE);
		Killer a = db.insert(Killer.NAME.set("a"));
		Killer b = db.insert(Killer.NAME.set("b"), Killer.TARGET.set(a));
		assert b.target == a : "wrong target ref";
		// updating a into a' to check that b is updated
		a = db.update(a, Killer.NAME.set("a'"));
		b = NeoQL.select(killers, Killer.TARGET.is(a) ).iterator().next();
		assert b.target == a : "wrong target ref";
		
		a = db.update(a, Killer.NAME.set("a''"), Killer.TARGET.set(b));
		b = NeoQL.select(killers, Killer.TARGET.is(a) ).iterator().next();
		for (Killer k : NeoQL.select(killers) )
			System.out.println(k);
		assert b.target == a : "wrong target ref";
		assert a.target == b : "wrong target ref";

		
		
		
		
		
	}
	
	
}
