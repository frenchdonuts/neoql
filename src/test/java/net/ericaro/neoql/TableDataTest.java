package net.ericaro.neoql;

import net.ericaro.neoql.changeset.ChangeSet;

import org.junit.Test;

public class TableDataTest {

	public static class Student {
		public static final Column<Student, String> NAME = NeoQL.column(Student.class, "name", String.class, false);
		String name;
		@Override
		public String toString() {
			return "Student [name=" + name + "]";
		}
		
		
	}
	
	public static class Killer{
		public static final Column<Killer, String> NAME = NeoQL.column(Killer.class, "name", String.class, false);
		public static final Column<Killer, Killer> TARGET= NeoQL.column(Killer.class, "target", Killer.class, true);
		String name;
		Killer target;
		@Override
		public String toString() {
			return "Killer [name=" + name + ", target=" + (target!=null?target.name:"null" )+ "]";
		}
		
		
	}
	
	
	@Test public void testCrud() {
		Database db = new Database();
		ContentTable<Student> students = db.createTable(Student.NAME);
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
		ContentTable<Student> students = db.createTable(Student.NAME);
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
	
	@Test public void testTransaction() {
		Database db = new Database(false);
		ContentTable<Killer> killers = db.createTable(Killer.NAME, Killer.TARGET);
		Killer a = db.insert(Killer.NAME.set("a"));
		Killer b = db.insert(Killer.NAME.set("b"), Killer.TARGET.set(a));
		
		Singleton<Killer> ta = db.track(a);
		Singleton<Killer> tb = db.track(b);
		
		assert tb.get() == null : "b values should not be available until the commit";
		assert ta.get() == null : "a values should not be available until the commit";
		// unfortunately entities are already connected
		assert b.target == a : "wrong target ref"; // by magic it has the right target (I've just set it)
		
		a= db.update(a, Killer.NAME.set("a'")); // when a is updated the target of b is not changed, until next commit 
		
		assert b.target != a : "wrong target ref";
		
		
		db.commit();
		
		assert tb.get() != null : "b values should be available by now";
		assert ta.get() != null : "a values should be available by now";
		
		System.out.println("tb = "+ tb.get());
		System.out.println(" b = "+ b       );
		System.out.println("b.target is "+tb.get().target);
		assert tb.get().target == ta.get() : "wrong target ref";
		
		
	}
	
	@Test public void testSelfRef() {
		Database db = new Database();
		ContentTable<Killer> killers = db.createTable(Killer.NAME, Killer.TARGET);
		
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
