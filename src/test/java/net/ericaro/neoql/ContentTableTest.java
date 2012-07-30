package net.ericaro.neoql;

import javax.swing.undo.UndoManager;

import net.ericaro.neoql.patches.Patch;
import net.ericaro.neoql.patches.Patches;
import net.ericaro.neoql.swing.UndoableAdapter;

import org.junit.Test;

public class ContentTableTest {
	public static Column<Tester, String> NAME = NeoQL.column(Tester.class, "name", String.class, false);
	public static Column<Tester, Integer> COUNT= NeoQL.column(Tester.class, "count", Integer.class, false);
	
	
	public static class Tester{
		private String name ;
		private int count = 2;
		@Override
		public String toString() {
			return "Tester [name=" + name + ", count=" + count + "]";
		}
	}
	
		
	public static Column<Marker, Boolean> MARKED= NeoQL.column(Marker.class, "marked", Boolean.class, false);
	public static Column<Marker, Tester> TARGET= NeoQL.column(Marker.class, "target", Tester.class, true);
	public static class Marker {
		// class used to test inner joins, and above all, the foreign key mechanism
		boolean marked = false;
		Tester target;
		@Override
		public String toString() {
			return "Marker [marked=" + marked + ", target=" + target + "]";
		}
		
		
	}
	
	
	@Test
	public void testCreate() {
		Database db = new Database() ;
		ContentTable<Tester> t = db.createTable(Tester.class, NAME, COUNT);
		db.commit();
		ContentTable<Tester>  t2 = db.getTable(Tester.class);
		assert t == t2 : "failed to retrieve the content table" ;
	}
	
	
	
	@Test
	public void testBasic() {
		// we started with a few "usual" changes, and we added step by step some "decoration" 
		Database db = new Database() ;
		db.atomicCreateTable(Tester.class, NAME, COUNT);
		db.commit();
		ContentTable<Tester> t = db.getTable(Tester.class);
		
		
		
		UndoManager m = new UndoManager();
		new UndoableAdapter(this, db).addUndoableEditListener(m); // record the undomanager
		/////////////////////////////////////////////////////////////////////////:
		Tester v = db.insert(t);
		
		assert v !=null && v.count == 2: "wrong default value" ;
		
		assert ! NeoQL.select(t).iterator().hasNext() : "database should be empty before any commit" ;
		db.commit();
		
		Property<Tester> c = NeoQL.track(t, v);
		
		assert len(t)==1 : "Wrong table size" ;
		assert c.get() == v  : "the only value should be the same as the one created during the transaction" ;

		/////////////////////////////////////////////////////////////////////////:
		db.update(t, NeoQL.is(v), NAME.set("tata"));
		
		assert len(t)==1 : "Wrong table size" ;
		assert c.get() == v  : "the only value should be the same as the one created during the previous transaction" ;
		assert !"tata".equals(c.get().name) : "name shouldn't  be tata by now";
		
		db.commit(); // now things changes
		
		assert len(t)==1 : "Wrong table size" ;
		assert "tata".equals(c.get().name) : "name should be tata by now";
		assert c.get() != v  : "the only value should be the same as the one created during the previous transaction" ;
		
		
		/////////////////////////////////////////////////////////////////////////:
		db.delete(t, NeoQL.is(c));
		assert len(t)==1 : "Wrong table size before commit" ;
		db.commit();
		assert len(t)==0 : "the database should be empty by now" ;
		
		/////////////////////////////////////////////////////////////////////////:
		v = db.insert(t, NAME.set("toto"), COUNT.set(3));
		db.commit();
		c = NeoQL.track(t,v);
		/////////////////////////////////////////////////////////////////////////:
		// test that is(cursor) works
		db.update(t, NeoQL.is(c), NAME.set("titi") );
		db.commit();
		
		assert len(t) == 1 : "there is only one entity in there";
		assert "titi".equals(c.get().name): "cursor has not followed the row ";
		
		/////////////////////////////////////////////////////////////////////////:
		// test rollback
		db.update(t, NeoQL.is(c), NAME.set("tutu") );
		db.rollback();
		assert "titi".equals(c.get().name): "cursor has been roll backed";
	}

	@Test public void testForeignKey() {
		Database db = new Database() ;
		ContentTable<Tester> t = db.createTable(Tester.class, NAME, COUNT);
		ContentTable<Marker> m = db.createTable(Marker.class, MARKED, TARGET );
		db.commit();
		UndoManager um = new UndoManager();
		new UndoableAdapter(this, db).addUndoableEditListener(um); // record the undomanager
		
		/////////////////////////////////////////////////////////////////////////
		// insert hello and mark as true
		Tester v = db.insert(t, NAME.set("hello"));
		Marker mv = db.insert(m, MARKED.set(true), TARGET.set(v));
		Property<Tester> cv = NeoQL.track(t, v);
		Property<Marker> c = NeoQL.track(m, mv);
		assert len(m) == 0 : "precommit has changed the overall length";
		Patch firstcs = db.commit();
		
		
		assert len(t) == 1 : "the table was not filled correctly";
		assert len(m) == 1 : "the table was not filled correctly";
		assert c.get().marked : "cursor has not the right value";
		assert c.get().target == cv.get() : "the marker target is no the expected target";
		
		/////////////////////////////////////////////////////////////////////////
		// mark as false
		db.update(m, NeoQL.is(c), MARKED.set(false));
		Patch secondcs = db.commit();
		assert !c.get().marked : "cursor has not the right value";
		assert c.get().target == cv.get() : "the marker target is no the expected target";
		System.out.println("undoing ---------------");
		System.out.println(cv.get());
		System.out.println(c.get());
		
		/////////////////////////////////////////////////////////////////////////
		// undo, hence mark is back to true
		
		Patch thirdcs = Patches.reverse(secondcs);
		db.apply(thirdcs) ;
		
		assert len(t) == 1 : "the table was not filled correctly";
		assert len(m) == 1 : "the table was not filled correctly";
		assert c.get().marked : "cursor has not the right value";
		assert c.get().target == cv.get() : "the marker target is no the expected target";

		/////////////////////////////////////////////////////////////////////////
		// undo hence, delete and delete mark as true
		Patch fourth = Patches.reverse(firstcs);
		db.apply(fourth) ;
		//um.undo();
		
		assert len(t) == 0 : "the table was not filled correctly";
		assert len(m) == 0 : "the table was not filled correctly";
		assert c.get() == null: "cursor has not the right value";
		assert cv.get() == null : "the marker target is no the expected target";
		
		
	}
	
	
	public static final int len(Iterable<?> i) {
		int l= 0;
		for(Object o: i) l++;
		return l;
	}
}
