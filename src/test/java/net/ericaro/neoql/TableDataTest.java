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
		Singleton<Student> t1 = db.track(s1);
		ChangeSet c1 = db.commit();
		clone.add(s1);
		assert clone.areEquals() : "insert was not sucessfully redone";
		assert t1.get() == s1 : "failed to update";
		
		
		db.revert(c1);
		clone.remove(s1);
		assert clone.areEquals() : "insert was not sucessfully undone";
		assert t1.get() == null : "revert should also untrack";
		
		db.commit(c1);
		clone.add(s1);
		assert clone.areEquals() : "insert was not sucessfully redone";
		assert t1.get() == s1 : "redo should also retrack";
		
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
		assert t1.get() == s1 : "redo should also retrack";
		
		
		
		
		
	}
	
	@Test public void testSelfRef() {
		Database db = new Database();
		TableData<Killer> killers = db.createTable(Killer.TABLE);
		
		Singleton<Killer> a = db.track( db.insert(Killer.NAME.set("a")) );
		
		Singleton<Killer> b = db.track(db.insert(Killer.NAME.set("b"), Killer.TARGET.set(a)));
		
		assert b.get().target == a.get() : "wrong target ref";
		// updating a into a' to check that b is updated
		
		db.update(a, Killer.NAME.set("a'"));
		assert b.get().target == a.get() : "wrong target ref";
		
		
		db.update(a, Killer.NAME.set("a''"), Killer.TARGET.set(b));
		
		for (Killer k : NeoQL.select(killers) )
			System.out.println(k);
		
		assert b.get().target == a.get() : "wrong target ref";
		assert a.get().target == b.get() : "wrong target ref";

		
		
		
		
		
	}
	
	
}
