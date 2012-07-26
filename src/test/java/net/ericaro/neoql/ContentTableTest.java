package net.ericaro.neoql;

import java.util.Iterator;

import javax.swing.undo.UndoManager;

import net.ericaro.neoql.swing.UndoableAdapter;

import org.junit.Test;

public class ContentTableTest {
	public static Column<Tester, String> NAME = NeoQL.column(Tester.class, "name", String.class, false);
	public static Column<Tester, Integer> COUNT= NeoQL.column(Tester.class, "count", Integer.class, false);
	
	public static Column<Marker, Boolean> MARKED= NeoQL.column(Marker.class, "marked", Boolean.class, false);
	public static Column<Marker, Tester> TARGET= NeoQL.column(Marker.class, "target", Tester.class, true);
	
	
	public static class Tester{
		private String name ;
		private int count = 2;
		@Override
		public String toString() {
			return "Tester [name=" + name + ", count=" + count + "]";
		}
	}
	
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
		ContentTable<Tester>  t2 = db.getTable(Tester.class);
		assert t == t2 : "failed to retrieve the content table" ;
	}
	
	
	
	@Test
	public void testBasic() {
		// we started with a few "usual" changes, and we added step by step some "decoration" 
		Database db = new Database() ;
		ContentTable<Tester> t = db.createTable(Tester.class, NAME, COUNT);
		Cursor<Tester> c = db.createCursor(t);
		Property<String> name = NeoQL.track(c, NAME);
		
		UndoManager m = new UndoManager();
		new UndoableAdapter(this, db).addUndoableEditListener(m); // record the undomanager
		/////////////////////////////////////////////////////////////////////////:
		Tester v = db.insert(t);
		db.moveTo(c, v);
		assert v !=null && v.count == 2: "wrong default value" ;
		Iterator<Tester> iterator = NeoQL.select(t).iterator();
		assert ! iterator.hasNext() : "database should be empty before any commit" ;
		assert c.get() == null : "cursor is not yet active";
		assert name.get() == null : "tracking a cursor that has not yet been commited, should be null";
		db.commit();
		
		iterator = NeoQL.select(t).iterator();
		Tester w = iterator.next();
		assert ! iterator.hasNext() : "there should be only one instance" ;
		assert w == v  : "the only value should be the same as the one created during the transaction" ;
		assert c.get() == w : "cursor has not followed the row";
		assert name.get() == null : "commit happened, but the default value is still null";

		/////////////////////////////////////////////////////////////////////////:
		db.update(t, NeoQL.is(v), NAME.set("tata"));
		
		iterator = NeoQL.select(t).iterator();
		w = iterator.next();
		assert ! iterator.hasNext() : "there should be only one instance before commit" ;
		assert w == v  : "the only value should be the same as the one created during the previous transaction" ;
		assert c.get() == w : "cursor has followed the row, pouha";
		assert !"tata".equals(w.name) : "name shouldn't  be tata by now";
		assert name.get() == null : "the name has changed but in the commit only, should not be available";
		
		db.commit(); // now things changes
		
		iterator = NeoQL.select(t).iterator();
		w = iterator.next();
		assert "tata".equals(w.name) : "name should be tata by now";
		assert w != v  : "the only value should be the same as the one created during the previous transaction" ;
		assert c.get() == w : "cursor has not followed the row";
		assert "tata".equals(name.get() ) : "name should be available by now";
		
		
		/////////////////////////////////////////////////////////////////////////:
		db.delete(t, NeoQL.is(w));
		iterator = NeoQL.select(t).iterator();
		assert iterator.hasNext() : "there should be only one instance before commit" ;
		assert c.get() == w : "cursor has followed the row before commit";
		assert "tata".equals(name.get() ) : "name should not have change before commit";
		
		db.commit();
		iterator = NeoQL.select(t).iterator();
		assert ! iterator.hasNext() : "the database should be empty by now" ;
		assert c.get() == null : "cursor has not followed the row ";
		assert name.get() == null : "name should be null as the row has been deleted";
		
		/////////////////////////////////////////////////////////////////////////:
		v = db.insert(t, NAME.set("toto"), COUNT.set(3));
		db.moveTo(c, v);
		db.commit();
		
		assert "toto".equals(c.get().name): "cursor has not followed the row ";
		assert "toto".equals(name.get()): "cursor should track reborn changes";
		
		/////////////////////////////////////////////////////////////////////////:
		// test that is(cursor) works
		db.update(t, NeoQL.is(c), NAME.set("titi") );
		db.commit();
		assert "titi".equals(c.get().name): "cursor has not followed the row ";
		assert "titi".equals(name.get()): "cursor should track reborn changes";
		
		/////////////////////////////////////////////////////////////////////////:
		// test rollback
		db.update(t, NeoQL.is(c), NAME.set("tutu") );
		db.rollback();
		assert "titi".equals(c.get().name): "cursor has been roll backed";
		assert "titi".equals(name.get()): "cursor should track reborn changes";
		
		/////////////////////////////////////////////////////////////////////////:
		// test cursor move to null
		v = c.get();
		db.moveTo(c, null);
		db.commit();
		assert c.get() == null : "cursor didn't move to null";
		assert name.get() == null : "name should be null as the cursor has been deleted";
		
		/////////////////////////////////////////////////////////////////////////:
		// test cursor rollback
		db.moveTo(c, v);
		db.rollback();
		assert c.get() == null : "cursor did take the bait and move to v";
		assert name.get() == null : "name didn't take the bait neither";
		db.moveTo(c, v);
		db.commit();
		assert c.get() == v : "cursor didn't take the bait and move to v";
		assert "titi".equals(name.get()): "name failed run smoothly";
		
		System.out.println(name.get());
		System.out.println(c.get());
		while (m.canUndo()) {
			m.undo();
			System.out.println(name.get());
			System.out.println(c.get());
		}
		
			
		
	}
	@Test
public 	void testUndoBug() {
		// had a bug with thee undo, and the cursor, this test was build to fail
		Database db = new Database() ;
		ContentTable<Tester> t = db.createTable(Tester.class, NAME, COUNT);
		Cursor<Tester> c = db.createCursor(t);
		UndoManager m = new UndoManager();
		new UndoableAdapter(this, db).addUndoableEditListener(m); // record the undomanager
		/////////////////////////////////////////////////////////////////////////:
		Tester v = db.insert(t);
		db.moveTo(c, v);
		db.commit();
		
		assert c.get() != null && c.get().name == null: "oops, the cursor didn't follow";
		
		db.update(t, NeoQL.is(c), NAME.set("1"));
		db.commit();
		assert c.get() != null && "1".equals(c.get().name ): "oops, the cursor didn't follow";
		
		
		/////////////////////////////////////////////////////////////////////////:
		db.delete(t, NeoQL.is(c));
		db.commit();
		assert c.get() == null : "oops, the cursor didn't follow";
		
		m.undo() ;
		assert c.get() != null : "oops, the cursor didn't undo well";
	}
	

	@Test public void testForeignKey() {
		Database db = new Database() ;
		ContentTable<Tester> t = db.createTable(Tester.class, NAME, COUNT);
		ContentTable<Marker> m = db.createTable(Marker.class, MARKED, TARGET );
		Cursor<Marker> c = db.createCursor(m);
		Cursor<Tester> cv = db.createCursor(t);
		
		UndoManager um = new UndoManager();
		new UndoableAdapter(this, db).addUndoableEditListener(um); // record the undomanager
		
		/////////////////////////////////////////////////////////////////////////
		Tester v = db.insert(t, NAME.set("hello"));
		Marker mv = db.insert(m, MARKED.set(true), TARGET.set(v));
		db.moveTo(c, mv);
		db.moveTo(cv, v);
		assert len(m) == 0 : "precommit has changed the overall length";
		db.commit();
		
		
		assert len(t) == 1 : "the table was not filled correctly";
		assert len(m) == 1 : "the table was not filled correctly";
		assert c.get().marked : "cursor has not the right value";
		assert c.get().target == cv.get() : "the marker target is no the expected target";
		
		/////////////////////////////////////////////////////////////////////////
		db.update(m, NeoQL.is(c), MARKED.set(false));
		db.commit();
		assert !c.get().marked : "cursor has not the right value";
		assert c.get().target == cv.get() : "the marker target is no the expected target";
		System.out.println("undoing ---------------");
		System.out.println(cv.get());
		System.out.println(c.get());
		
		/////////////////////////////////////////////////////////////////////////
		um.undo();
		assert len(t) == 1 : "the table was not filled correctly";
		assert len(m) == 1 : "the table was not filled correctly";
		assert c.get().marked : "cursor has not the right value";
		assert c.get().target == cv.get() : "the marker target is no the expected target";

		/////////////////////////////////////////////////////////////////////////
		um.undo();
		
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
